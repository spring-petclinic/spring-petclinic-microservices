/*
 * Copyright 2002-2021 the original author or authors.
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
package org.springframework.samples.petclinic.vets.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Dynamic configuration bean demonstrating Spring Cloud {@link RefreshScope}.
 * Reloads dynamically when {@code /actuator/refresh} is invoked without restarting the service.
 *
 * @author Maciej Szarlinski
 */
@Component
@RefreshScope
public class VetsConfiguration {

    private final String welcomeMessage;

    public VetsConfiguration() {
        this("Welcome to the PetClinic Veterinarians");
    }

    public VetsConfiguration(
            @Value("${vets.welcome:Welcome to the PetClinic Veterinarians}") String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }
}
