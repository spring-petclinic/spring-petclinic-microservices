package lastTesting;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;

/**
 * This Load Test checks the performance of getting all owners..
 */
public class CustomersLoadTest extends Simulation {

    int users = 100;
    int time = 15;
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder owners = scenario("Owner Usage Scenario")
        .exec(http("owners").get("/api/customer/owners"));

    {
        setUp(
            owners.injectOpen(constantUsersPerSec(users).during(time))
        ).protocols(httpProtocol);
    }

}
