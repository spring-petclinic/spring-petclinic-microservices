package lastTesting;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * This Load Test checks the performance of getting all vets.
 */
public class VetsLoadTest extends Simulation {

    int users = 100;
    int time = 15;
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder vets = scenario("Vets Usage Scenario")
        .exec(http("vets").get("/api/vet/vets"));

    {
        setUp(
            vets.injectOpen(constantUsersPerSec(users).during(time))
        ).protocols(httpProtocol);
    }

}
