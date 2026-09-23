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
package org.springframework.samples.petclinic.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Configuration of the OAuth2 Authorization Server.
 * <p>
 * Everything is kept in memory on purpose: this is a demonstration application and we
 * want it to start without any external dependency. A production grade authorization
 * server would store its clients, its users and its keys in a database or a vault.
 */
@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfig {

    /**
     * Identifier of the client registration used by the API gateway.
     */
    public static final String GATEWAY_CLIENT_ID = "petclinic-gateway";

    /**
     * Scopes granted to the gateway on top of the OpenID Connect ones.
     */
    public static final String READ_SCOPE = "petclinic.read";

    public static final String WRITE_SCOPE = "petclinic.write";

    @Bean
    RegisteredClientRepository registeredClientRepository(
        @Value("${petclinic.auth-server.gateway-base-url}") String gatewayBaseUrl,
        @Value("${petclinic.auth-server.gateway-client-secret}") String gatewayClientSecret) {

        String baseUrl = withoutTrailingSlash(gatewayBaseUrl);
        RegisteredClient gateway = RegisteredClient.withId(UUID.randomUUID().toString())
            .clientId(GATEWAY_CLIENT_ID)
            .clientName("Spring PetClinic API Gateway")
            .clientSecret("{noop}" + gatewayClientSecret)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri(baseUrl + "/login/oauth2/code/petclinic")
            .postLogoutRedirectUri(baseUrl + "/")
            .scope(OidcScopes.OPENID)
            .scope(OidcScopes.PROFILE)
            .scope(READ_SCOPE)
            .scope(WRITE_SCOPE)
            // The gateway is a first party application: asking the user for consent
            // would only add noise to the demonstration. PKCE is required, even though
            // the gateway is a confidential client.
            .clientSettings(ClientSettings.builder()
                .requireAuthorizationConsent(false)
                .requireProofKey(true)
                .build())
            .tokenSettings(TokenSettings.builder()
                .accessTokenTimeToLive(Duration.ofMinutes(30))
                .refreshTokenTimeToLive(Duration.ofHours(8))
                .build())
            .build();
        return new InMemoryRegisteredClientRepository(gateway);
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN", "USER")
            .build();
        UserDetails user = User.withUsername("user")
            .password(passwordEncoder.encode("user"))
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(admin, user);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Adds the roles of the authenticated user to the ID token and to the access token so
     * that the resource servers, and the gateway, can take authorization decisions.
     */
    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return context -> {
            String tokenType = context.getTokenType().getValue();
            boolean idOrAccessToken = OidcParameterNames.ID_TOKEN.equals(tokenType)
                || OAuth2TokenType.ACCESS_TOKEN.getValue().equals(tokenType);
            if (!idOrAccessToken || context.getPrincipal() == null) {
                return;
            }
            List<String> roles = AuthorityUtils.authorityListToSet(context.getPrincipal().getAuthorities())
                .stream()
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .sorted()
                .toList();
            context.getClaims().claim("roles", roles);
        };
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        return new ImmutableJWKSet<>(new JWKSet(generateRsaKey()));
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings(
        @Value("${petclinic.auth-server.issuer-uri}") String issuerUri) {
        return AuthorizationServerSettings.builder()
            .issuer(withoutTrailingSlash(issuerUri))
            .build();
    }

    private static RSAKey generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unable to generate the RSA key pair", ex);
        }
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyID(UUID.randomUUID().toString())
            .build();
    }

    private static String withoutTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
