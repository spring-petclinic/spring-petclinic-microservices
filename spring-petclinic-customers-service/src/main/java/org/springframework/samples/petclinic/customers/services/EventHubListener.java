 package org.springframework.samples.petclinic.customers.services;

   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.kafka.annotation.KafkaListener;
   import org.springframework.stereotype.Service;

   @Service
   public class EventHubListener {

      private static final Logger log = LoggerFactory.getLogger(EventHubListener.class);

      @KafkaListener(topics = "telemetry", groupId = "$Default")
        public void receive(String in) {
           log.info("Received message from kafka queue: {}",in);
           System.out.println(in);
       }
   } 