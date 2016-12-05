package org.springframework.samples.petclinic.monitoring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author Maciej Szarlinski
 */
@Configuration
@EnableAspectJAutoProxy
public class MonitoringConfig {

    @Bean
    CallMonitoringAspect callMonitoringAspect() {
        return new CallMonitoringAspect();
    }
}
