  package org.springframework.samples.petclinic.visits.config;

   import org.springframework.beans.factory.annotation.Value;

   public class QueueConfig {
       @Value("${spring.jms.queue.visits-requests:visits-requests}")
       private String visitsRequestsQueue;

       public String getVisitsRequestsQueue() {
           return visitsRequestsQueue;
       }   
   }