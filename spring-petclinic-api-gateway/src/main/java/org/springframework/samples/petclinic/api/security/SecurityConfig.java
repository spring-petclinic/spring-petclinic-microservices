/*
 * Copyright 2002-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.api.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.AuthenticationMethod;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.DelegatingServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationEntryPoint;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.ServerCsrfTokenRequestAttributeHandler;
import org.springframework.security.web.server.util.matcher.MediaTypeServerWebExchangeMatcher;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Secures the API gateway, the only PetClinic application exposed to the outside world.
 * <p>
 * End users authenticate against the PetClinic authorization server with the OpenID
 * Connect authorization code flow. The resulting access token is then relayed to the
 * downstream microservices by the {@code TokenRelay} gateway filter.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebFluxSecurity
@EnableConfigurationProperties(AuthServerProperties.class)
public class SecurityConfig {

    /**
     * Identifier of the client registration, it drives the login
     * ({@code /oauth2/authorization/petclinic}) and the redirection
     * ({@code /login/oauth2/code/petclinic}) URLs.
     */
    public static final String REGISTRATION_ID = "petclinic";

    private static final String LOGIN_URL = "/oauth2/authorization/" + REGISTRATION_ID;

    /**
     * The client registration is built programmatically rather than with the
     * {@code spring.security.oauth2.client} properties for two reasons: the gateway must
     * start even when the authorization server is not up yet (setting an
     * {@code issuer-uri} property would trigger a blocking discovery call at startup),
     * and the browser facing endpoints may use a different host than the back channel
     * ones when everything runs inside a container network.
     */
    @Bean
    ReactiveClientRegistrationRepository clientRegistrationRepository(AuthServerProperties properties) {
        ClientRegistration registration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
            .clientName("Spring PetClinic")
            .clientId(properties.clientId())
            .clientSecret(properties.clientSecret())
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid", "profile", "petclinic.read", "petclinic.write")
            // Browser facing endpoint
            .authorizationUri(properties.issuerUri() + "/oauth2/authorize")
            // Back channel endpoints
            .tokenUri(properties.internalUri() + "/oauth2/token")
            .jwkSetUri(properties.internalUri() + "/oauth2/jwks")
            .userInfoUri(properties.internalUri() + "/userinfo")
            .userInfoAuthenticationMethod(AuthenticationMethod.HEADER)
            .userNameAttributeName(IdTokenClaimNames.SUB)
            .issuerUri(properties.issuerUri())
            // Advertise the RP initiated logout endpoint, which would otherwise only be
            // known through the OpenID provider configuration.
            .providerConfigurationMetadata(
                Map.of("end_session_endpoint", properties.issuerUri() + "/connect/logout"))
            .clientSettings(ClientRegistration.ClientSettings.builder().requireProofKey(true).build())
            .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                    ReactiveClientRegistrationRepository clientRegistrations) {
        return http
            .authorizeExchange(exchanges -> exchanges
                // Infrastructure endpoints, scraped by Prometheus and the admin server
                .pathMatchers("/actuator/**", "/fallback").permitAll()
                // Lets the single page application know whether somebody is logged in
                .pathMatchers(HttpMethod.GET, "/api/user/me").permitAll()
                // Everything the gateway proxies or aggregates requires an end user
                .pathMatchers("/api/**").authenticated()
                // The static resources of the single page application
                .anyExchange().permitAll())
            .oauth2Login(Customizer.withDefaults())
            .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler(clientRegistrations)))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()))
            // The AngularJS front end reads the CSRF token from the XSRF-TOKEN cookie and
            // sends it back in the X-XSRF-TOKEN header, hence a cookie readable by scripts
            // and a handler that expects the raw token instead of an encoded one.
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new ServerCsrfTokenRequestAttributeHandler()))
            .addFilterAfter(csrfCookieWebFilter(), SecurityWebFiltersOrder.CSRF)
            .build();
    }

    /**
     * Maps the {@code roles} claim issued by the authorization server to Spring Security
     * authorities, so that the gateway can take authorization decisions on top of them.
     */
    @Bean
    ReactiveOAuth2UserService<OidcUserRequest, OidcUser> oidcUserService() {
        OidcReactiveOAuth2UserService delegate = new OidcReactiveOAuth2UserService();
        return userRequest -> delegate.loadUser(userRequest).map(user -> {
            Set<GrantedAuthority> authorities = new LinkedHashSet<>(user.getAuthorities());
            rolesOf(user).forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
            return new DefaultOidcUser(authorities, user.getIdToken(), user.getUserInfo(), IdTokenClaimNames.SUB);
        });
    }

    public static List<String> rolesOf(OidcUser user) {
        Object roles = user.getClaims().get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    /**
     * A browser navigating to a protected page is redirected to the authorization server,
     * while an XHR call made by the single page application gets a 401 it can react to.
     */
    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        MediaTypeServerWebExchangeMatcher htmlMatcher = new MediaTypeServerWebExchangeMatcher(MediaType.TEXT_HTML);
        htmlMatcher.setIgnoredMediaTypes(Set.of(MediaType.ALL));
        DelegatingServerAuthenticationEntryPoint entryPoint = new DelegatingServerAuthenticationEntryPoint(
            List.of(new DelegatingServerAuthenticationEntryPoint.DelegateEntry(htmlMatcher,
                new RedirectServerAuthenticationEntryPoint(LOGIN_URL))));
        entryPoint.setDefaultEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED));
        return entryPoint;
    }

    /**
     * RP initiated logout: the session is closed on the gateway and on the authorization
     * server, then the browser comes back to the PetClinic home page.
     */
    private OidcClientInitiatedServerLogoutSuccessHandler logoutSuccessHandler(
        ReactiveClientRegistrationRepository clientRegistrations) {
        OidcClientInitiatedServerLogoutSuccessHandler handler =
            new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrations);
        handler.setPostLogoutRedirectUri("{baseUrl}/");
        return handler;
    }

    /**
     * The CSRF token is lazily computed in WebFlux, subscribing to it makes sure the
     * XSRF-TOKEN cookie is actually written on the response.
     */
    private WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
            return csrfToken == null ? chain.filter(exchange) : csrfToken.then(chain.filter(exchange));
        };
    }
}
