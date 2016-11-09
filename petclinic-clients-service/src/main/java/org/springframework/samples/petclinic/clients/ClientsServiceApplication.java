package org.springframework.samples.petclinic.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.monitoring.MonitoringConfig;

@EnableDiscoveryClient
@SpringBootApplication
@Import(MonitoringConfig.class)
public class ClientsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClientsServiceApplication.class, args);
	}
}
