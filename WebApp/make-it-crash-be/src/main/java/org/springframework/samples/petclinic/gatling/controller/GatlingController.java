package org.springframework.samples.petclinic.gatling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.stream.Stream;
import java.io.IOException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController("API")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GatlingController {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @GetMapping()
    public String index() {
        return "Gatling test controller.";
    }

    @GetMapping("/url")
    public String url() {
        return gatewayUrl;
    }

    @GetMapping(
        value = "/loadtest/vets",
        produces = "application/json"
    )
    public String runVetsLoadtest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.VetsLoadTest";
        System.out.println("Running Vets Loadtest with users: " + users + " and duration: " + duration);
        return GatlingTests(simulationClass, users, duration);
    }

    @GetMapping(
        value = "/loadtest/vets-customers",
        produces = "application/json"
    )
    public String runVetsCustomersLoadtest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.VetsCustomers";
        System.out.println("Running Vets Loadtest with users: " + users + " and duration: " + duration);
        return GatlingTests(simulationClass, users, duration);
    }

    @GetMapping(
        value = "/loadtest/owners",
        produces = "application/json"
    )
    public String runOwnersLoadtest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.OwnerInformationLoadTest";
        System.out.println("Running Owners Loadtest with users: " + users + " and duration: " + duration);
        return GatlingTests(simulationClass, users, duration);
    }

    @GetMapping(
        value = "/loadtest/customers",
        produces = "application/json"
    )
    public String runCustomersLoadtest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.CustomersLoadTest";
        System.out.println("Running Customers Loadtest with users: " + users + " and duration: " + duration);
        return GatlingTests(simulationClass, users, duration);
    }

    public static String GatlingTests(String simulationClass, int users, int duration) {
        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder();
        props.simulationClass(simulationClass);

        System.setProperty("gatling.users", String.valueOf(users));
        System.setProperty("gatling.duration", String.valueOf(duration));

        String resultsDir = "";
        String resultsPath = "";

        try {
            Gatling.fromMap(props.build());

            Path directory = Paths.get("/app/results/");

            try (Stream<Path> paths = Files.list(directory)) {
                Optional<Path> firstPath = paths.findFirst();
                if (firstPath.isPresent()) {
                    resultsDir = firstPath.get().toString();
                    resultsPath = resultsDir + "/js/stats.json";
                } else {
                    resultsPath = "not found";
                }
            } catch (IOException e) {
                e.printStackTrace();
                resultsPath = "not found";
            }

            if (!resultsPath.equals("not found")) {
                File file = new File(resultsPath);
                if (file.exists()) {
                    byte[] fileContent = Files.readAllBytes(file.toPath());
                    return new String(fileContent);
                } else {
                    return "Results file not found.";
                }
            } else {
                return "Results directory not found.";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing Gatling test.";
        } finally {
            if (!resultsPath.equals("not found")) {
                File file = new File(resultsDir);
                deleteFolder(file);
            }
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) { // some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
