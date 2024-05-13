package lastTesting;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

public class FrontendUsageSimulation extends Simulation {
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder frontendUsageScenario = scenario("Frontend Usage")
        .exec(http("Request 1").get("/"))
        .pause(1, 3); // Pause von 1 bis 3 Sekunden nach jeder Anfrage

    {
        setUp(
            frontendUsageScenario.injectOpen(constantUsersPerSec(200).during(30))
        ).protocols(httpProtocol);
    }
}
