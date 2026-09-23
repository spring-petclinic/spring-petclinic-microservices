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
package org.springframework.samples.petclinic.api.boundary.web;

import org.springframework.samples.petclinic.api.security.SecurityConfig;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Exposes the identity of the currently logged in user to the single page application so
 * that it can display the user name and the login or logout links.
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * @param authenticated whether an end user is logged in
     * @param username the preferred user name, {@code null} when nobody is logged in
     * @param roles the roles granted by the authorization server
     */
    public record CurrentUser(boolean authenticated, String username, List<String> roles) {
    }

    private static final CurrentUser ANONYMOUS = new CurrentUser(false, null, List.of());

    @GetMapping("/me")
    public Mono<CurrentUser> currentUser(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return Mono.just(ANONYMOUS);
        }
        String username = user.getPreferredUsername() != null ? user.getPreferredUsername() : user.getSubject();
        return Mono.just(new CurrentUser(true, username, SecurityConfig.rolesOf(user)));
    }
}
