package org.springframework.samples.petclinic.api;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class EnvController {

    private static final Logger logger = LoggerFactory.getLogger(EnvController.class);

    @Autowired
    private Environment env;

    private String realm;
    private String rumToken;
    private String instance;
    private String rumEnv;

    // Initialize values from the environment in the constructor or another initialization block
    @PostConstruct
    public void init() {
        // Fetching environment variables and logging them
        realm = env.getProperty("RUM_REALM", "");
        rumToken = env.getProperty("RUM_AUTH", "");
        instance = env.getProperty("RUM_APP_NAME", "default-instance");
        rumEnv = env.getProperty("RUM_ENVIRONMENT", "default-env");

        // Log the fetched values
        logger.info("Initialized environment values:");
        logger.info("REALM: {}", realm);
        logger.info("RUM_TOKEN: {}", rumToken);
        logger.info("INSTANCE: {}", instance);
        logger.info("ENV: {}", rumEnv);
    }

    @GetMapping(value = "/env.js", produces = "application/javascript")
    public String getEnvScript() {
        // Log the request for the env.js file
        logger.info("Serving env.js with current environment values");
        return "var env = {" +
                "RUM_REALM: '" + realm + "'," +
                "RUM_AUTH: '" + rumToken + "'," +
                "RUM_APP_NAME: '" + instance + "-store'," +
                "RUM_ENVIRONMENT: '" + instance + "-workshop'" +
                "};";
    }
}