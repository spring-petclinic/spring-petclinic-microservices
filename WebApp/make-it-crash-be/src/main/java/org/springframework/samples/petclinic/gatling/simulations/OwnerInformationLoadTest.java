package lastTesting;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;


/**
 * This Load Test checks the performance of getting owner information (owner infos, pet infos and visits),
 * sampling the Information of owner 5/"Jean Coleman".
 */
public class OwnerInformationLoadTest extends Simulation {

    int users = Integer.parseInt(System.getProperty("gatling.users", "60"));
    int duration = Integer.parseInt(System.getProperty("gatling.duration", "30"));
    HttpProtocolBuilder httpProtocol = http.baseUrl("http://localhost:8080")
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder owners = scenario("owner information Scenario")
        .exec(http("owner information").get("/api/gateway/owners/5"));

    {
        setUp(
            owners.injectOpen(constantUsersPerSec(users).during(duration))
        ).protocols(httpProtocol);
    }
}
