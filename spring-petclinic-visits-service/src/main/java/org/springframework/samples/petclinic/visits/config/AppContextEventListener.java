package org.springframework.samples.petclinic.visits.config;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class AppContextEventListener {

    @EventListener
    public void printProperties(ContextRefreshedEvent contextRefreshedEvent) {
        System.out.println("************************* ACTIVE PROPERTIES *************************");

        ((ConfigurableEnvironment) contextRefreshedEvent.getApplicationContext().getEnvironment())
            .getPropertySources()
            .stream()
            .filter(ps -> ps instanceof OriginTrackedMapPropertySource)
            // Convert each PropertySource to its properties Set
            .map(ps -> ((OriginTrackedMapPropertySource) ps).getSource().entrySet())
            .flatMap(Collection::stream)
            // Print properties within each Set
            .forEach(property -> System.out.println(property.getKey() + "=" + property.getValue()));

        System.out.println("*********************************************************************");
    }

}
