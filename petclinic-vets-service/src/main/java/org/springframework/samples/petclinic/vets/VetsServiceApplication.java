package org.springframework.samples.petclinic.vets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class VetsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(VetsServiceApplication.class, args);
	}
}
