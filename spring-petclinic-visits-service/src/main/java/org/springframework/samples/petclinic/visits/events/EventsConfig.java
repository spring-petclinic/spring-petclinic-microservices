package org.springframework.samples.petclinic.visits.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class EventsConfig {

    public static final String VISIT_EVENT_EXECUTOR = "visitEventExecutor";

    private static final Logger log = LoggerFactory.getLogger(EventsConfig.class);

    /**
     * One thread, so events are sent in the order the visits were committed.
     * When Kafka is down the queue fills up; further events are dropped and logged
     * instead of blocking or failing the request that produced them.
     */
    @Bean(VISIT_EVENT_EXECUTOR)
    ThreadPoolTaskExecutor visitEventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("visit-events-");
        executor.setRejectedExecutionHandler((task, pool) ->
            log.error("Visit event queue is full; dropping an event. Is Kafka reachable?"));
        return executor;
    }
}
