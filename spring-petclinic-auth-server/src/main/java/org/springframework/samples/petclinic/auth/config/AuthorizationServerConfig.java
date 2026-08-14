package org.springframework.samples.petclinic.auth.config;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;

import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;

import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class AuthorizationServerConfig {

    /**
     * Security configuration for OAuth2 Authorization Server endpoints.
     *
     * This filter chain handles endpoints such as:
     *
     * /oauth2/authorize
     * /oauth2/token
     * /oauth2/jwks
     * /.well-known/oauth-authorization-server
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(
        HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
            new OAuth2AuthorizationServerConfigurer();

        http
            .securityMatcher(
                authorizationServerConfigurer.getEndpointsMatcher()
            )
            .with(
                authorizationServerConfigurer,
                authorizationServer ->
                    authorizationServer
                        .oidc(Customizer.withDefaults())
            )
            .authorizeHttpRequests(authorize ->
                authorize
                    .anyRequest()
                    .authenticated()
            );

        return http.build();
    }


    /**
     * Default security configuration for the Authorization Server application.
     *
     * This protects normal application endpoints and provides
     * the login page used during the Authorization Code flow.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(
        HttpSecurity http) throws Exception {

        return http
            .authorizeHttpRequests(authorize ->
                authorize
                    .anyRequest()
                    .authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .build();
    }


    /**
     * Development user used to authenticate against the
     * Authorization Server.
     *
     * This is intentionally in-memory for the first implementation.
     *
     * TODO:
     * Replace with a persistent user store before considering
     * this production-ready.
     */
    @Bean
    public UserDetailsService userDetailsService(
        @Value("${AUTH_SERVER_USERNAME}") String username,
        @Value("${AUTH_SERVER_PASSWORD}") String password) {

        UserDetails user = User
            .withUsername(username)
            .password("{noop}" + password)
            .roles("USER")
            .build();

        return new InMemoryUserDetailsManager(user);
    }


    /**
     * OAuth2 clients that are allowed to request tokens
     * from this Authorization Server.
     *
     * This is an in-memory implementation for development.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository(
        @Value("${AUTH_SERVER_CLIENT_SECRET}") String clientSecret) {

        RegisteredClient petclinicClient =
            RegisteredClient.withId(
                    UUID.randomUUID().toString()
                )
                .clientId("petclinic-client")

                .clientSecret("{noop}" + clientSecret)

                .clientAuthenticationMethod(
                    ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )

                /*
                 * Authorization Code flow.
                 *
                 * This will eventually be useful when
                 * integrating the PetClinic UI/Gateway.
                 */
                .authorizationGrantType(
                    AuthorizationGrantType.AUTHORIZATION_CODE
                )

                /*
                 * Refresh Token flow.
                 */
                .authorizationGrantType(
                    AuthorizationGrantType.REFRESH_TOKEN
                )

                /*
                 * Client Credentials flow.
                 *
                 * Useful for testing service-to-service
                 * authentication.
                 */
                .authorizationGrantType(
                    AuthorizationGrantType.CLIENT_CREDENTIALS
                )

                /*
                 * Temporary redirect URI for the future
                 * Gateway OAuth2 client integration.
                 */
                .redirectUri(
                    "http://127.0.0.1:8080/login/oauth2/code/petclinic-client"
                )

                /*
                 * OpenID Connect scopes.
                 */
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)

                /*
                 * Application-specific scope.
                 */
                .scope("gateway.read")

                .build();

        return new InMemoryRegisteredClientRepository(
            petclinicClient
        );
    }


    /**
     * Creates the RSA key pair used to sign JWT access tokens.
     *
     * Private key:
     *     Used by Authorization Server to sign JWTs.
     *
     * Public key:
     *     Exposed through the JWK endpoint so that
     *     Resource Servers can verify JWT signatures.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {

        KeyPair keyPair = generateRsaKey();

        RSAPublicKey publicKey =
            (RSAPublicKey) keyPair.getPublic();

        RSAPrivateKey privateKey =
            (RSAPrivateKey) keyPair.getPrivate();

        RSAKey rsaKey =
            new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);

        return new ImmutableJWKSet<>(jwkSet);
    }


    /**
     * Generates a 2048-bit RSA key pair.
     */
    private static KeyPair generateRsaKey() {

        try {

            KeyPairGenerator keyPairGenerator =
                KeyPairGenerator.getInstance("RSA");

            keyPairGenerator.initialize(2048);

            return keyPairGenerator.generateKeyPair();

        } catch (Exception exception) {

            throw new IllegalStateException(
                "Failed to generate RSA key pair",
                exception
            );
        }
    }


    /**
     * JwtDecoder used by the Authorization Server.
     *
     * It uses the same JWK source that contains the RSA
     * public/private key pair.
     */
    @Bean
    public JwtDecoder jwtDecoder(
        JWKSource<SecurityContext> jwkSource) {

        return OAuth2AuthorizationServerConfiguration
            .jwtDecoder(jwkSource);
    }


    /**
     * Defines the issuer URL of this Authorization Server.
     *
     * This value will eventually appear in JWTs as:
     *
     * "iss": "http://localhost:9000"
     *
     * The API Gateway will later validate this issuer.
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {

        return AuthorizationServerSettings
            .builder()
            .issuer("http://localhost:9000")
            .build();
    }
}
