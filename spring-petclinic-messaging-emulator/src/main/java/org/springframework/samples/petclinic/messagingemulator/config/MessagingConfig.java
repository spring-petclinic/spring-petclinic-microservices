package org.springframework.samples.petclinic.messagingemulator.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.samples.petclinic.messagingemulator.entity.PetClinicMessageRequest;
import org.springframework.samples.petclinic.messagingemulator.entity.PetClinicMessageResponse;

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
        typeMappings.put("visitRequest", PetClinicMessageRequest.class);
        typeMappings.put("visitResponse", PetClinicMessageResponse.class);
        converter.setTypeIdMappings(typeMappings);
        converter.setTypeIdPropertyName("messageType");
        return converter;
    }

    // @Bean
    // public DefaultMessageHandlerMethodFactory myHandlerMethodFactory() {
    // DefaultMessageHandlerMethodFactory factory = new
    // DefaultMessageHandlerMethodFactory();
    // factory.setMessageConverter(jackson2Converter());
    // return factory;
    // }
}
