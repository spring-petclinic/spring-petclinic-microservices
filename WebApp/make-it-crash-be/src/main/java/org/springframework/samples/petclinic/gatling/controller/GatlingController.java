package org.springframework.samples.petclinic.gatling.controller;

import org.springframework.web.bind.annotation.*;
import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import io.gatling.core.config.GatlingFiles;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController("API")
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class GatlingController {

    @GetMapping()
    public String index() {
        return "Gatling test controller.";
    }

    @GetMapping("/loadtest/vets")
    public String runCustomGatlingTest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.VetsCustomers";

        return GatlingTests(simulationClass, users, duration);
    }
    @GetMapping("/loadtest/owners")
    public String runCustomGatlingTest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.OwnerInformationLoadTest";

        return GatlingTests(simulationClass, users, duration);
    }

    @GetMapping("/loadtest/customers")
    public String runCustomGatlingTest(
        @RequestParam(value = "users", defaultValue = "60") int users,
        @RequestParam(value = "duration", defaultValue = "30") int duration) {

        String simulationClass = "org.springframework.samples.petclinic.gatling.simulations.CustomersLoadTest";

        return GatlingTests(simulationClass, users, duration);
    }


    public static String GatlingTests(String simulationClass, int users, int duration) {
        GatlingPropertiesBuilder props = new GatlingPropertiesBuilder();
        props.simulationClass(simulationClass);

        System.setProperty("gatling.users", String.valueOf(users));
        System.setProperty("gatling.duration", String.valueOf(duration));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        PrintStream old = System.out;
        System.setOut(ps);
        String resultsPath = "";

        try {
            Gatling.fromMap(props.build());

            System.out.flush();
            System.setOut(old);

            String gatlingOutput = baos.toString();

            Pattern pattern = Pattern.compile("file://(.*?)/index.html");
            Matcher matcher = pattern.matcher(gatlingOutput);
            resultsPath = "not found";
            String path = "";
            if (matcher.find()) {
                resultsPath = matcher.group(1).replace("%20", " ");
                if (System.getProperty("os.name").startsWith("Windows")) {
                    resultsPath = resultsPath.substring(1);
                }else{
                    path = resultsPath;
                }

                path += "/js/stats.json";
            }
            System.out.println("Results path: " + path);
            //System.out.println("Gatling output: " + gatlingOutput);

            File file = new File(path);
            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()));
            } else {
                return "HTML results file not found.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing Gatling test.";
        } finally {
            System.setOut(old);
            File file = new File(resultsPath);
            deleteFolder(file);
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }
}
