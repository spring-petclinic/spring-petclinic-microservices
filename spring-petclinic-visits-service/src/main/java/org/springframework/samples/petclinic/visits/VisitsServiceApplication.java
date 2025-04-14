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
package org.springframework.samples.petclinic.visits;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Maciej Szarlinski
 */
@EnableDiscoveryClient
@SpringBootApplication
public class VisitsServiceApplication {

    private static final Logger log = LoggerFactory.getLogger(VisitsServiceApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(VisitsServiceApplication.class, args);
        printAllProperties(context.getEnvironment());
    }

    private static void printAllProperties(Environment environment) {
        MutablePropertySources propertySources = ((org.springframework.core.env.AbstractEnvironment) environment).getPropertySources();
        
        log.info("Configuration Properties:");

        Iterator<org.springframework.core.env.PropertySource<?>> iterator = propertySources.iterator();
        while (iterator.hasNext()) {
            org.springframework.core.env.PropertySource<?> propertySource = iterator.next();
            if (propertySource instanceof MapPropertySource) {
                Map<String, Object> properties = ((MapPropertySource) propertySource).getSource();
                for (String key : properties.keySet()) {
                    log.info("    " + key + "=" + properties.get(key));
                }
            }
        }
    }


}
