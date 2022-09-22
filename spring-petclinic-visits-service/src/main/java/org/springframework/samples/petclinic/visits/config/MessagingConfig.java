package org.springframework.samples.petclinic.visits.config;
    import java.util.HashMap;
    import java.util.Map;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
    import org.springframework.jms.support.converter.MessageConverter;
    import org.springframework.samples.petclinic.visits.entities.VisitRequest;
    import org.springframework.samples.petclinic.visits.entities.VisitResponse;
    @Configuration
    public class MessagingConfig {
        @Bean("QueueConfig")
        public QueueConfig queueConfig() {
            return new QueueConfig();
        }
        @Bean
        public MessageConverter jackson2Converter() {
            MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
            Map<String, Class<?>> typeMappings = new HashMap<String, Class<?>>();
            typeMappings.put("visitRequest", VisitRequest.class);
            typeMappings.put("visitResponse", VisitResponse.class);
            converter.setTypeIdMappings(typeMappings);
            converter.setTypeIdPropertyName("messageType");
            return converter;
        }
    }