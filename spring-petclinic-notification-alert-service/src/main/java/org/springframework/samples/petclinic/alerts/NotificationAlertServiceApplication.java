package org.springframework.samples.petclinic.alerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class NotificationAlertServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationAlertServiceApplication.class, args);
    }
}
