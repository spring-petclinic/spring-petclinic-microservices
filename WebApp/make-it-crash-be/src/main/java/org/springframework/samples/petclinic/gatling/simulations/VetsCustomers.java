package org.springframework.samples.petclinic.gatling.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;



public class VetsCustomers extends Simulation {
    int users = Integer.parseInt(System.getProperty("gatling.users", "60"));
    int duration = Integer.parseInt(System.getProperty("gatling.duration", "30"));
    //int duration = 30;
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder owners = scenario("Owner Usage Scenario")
        .exec(http("owners").get("/api/customer/owners"));
    ScenarioBuilder vets = scenario("Vets Usage Scenario")
        .exec(http("vets").get("/api/vet/vets"));

    {
        setUp(
            vets.injectOpen(constantUsersPerSec(users).during(duration)),
            owners.injectOpen(constantUsersPerSec(users).during(duration))
        ).protocols(httpProtocol);
    }
}
