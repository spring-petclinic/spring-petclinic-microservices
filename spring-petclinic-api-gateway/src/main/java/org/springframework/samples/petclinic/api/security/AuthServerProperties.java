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

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Coordinates of the PetClinic authorization server, as seen by the API gateway.
 *
 * @param issuerUri the public URL of the authorization server. It is the {@code iss}
 * claim of the issued tokens and the host the end user browser is redirected to, so it
 * has to be resolvable from the browser.
 * @param internalUri the URL used by the gateway itself for the back channel calls (token
 * exchange, JWK set and user info). It defaults to the issuer URI and only needs to be
 * customized when the gateway does not reach the authorization server through its public
 * URL, which is typically the case inside a container network.
 * @param clientId the identifier of the gateway client registration
 * @param clientSecret the secret of the gateway client registration
 */
@ConfigurationProperties("petclinic.auth-server")
public record AuthServerProperties(String issuerUri, String internalUri, String clientId, String clientSecret) {

    public AuthServerProperties {
        issuerUri = withoutTrailingSlash(issuerUri);
        internalUri = (internalUri == null || internalUri.isBlank()) ? issuerUri : withoutTrailingSlash(internalUri);
    }

    private static String withoutTrailingSlash(String url) {
        if (url == null) {
            return null;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
