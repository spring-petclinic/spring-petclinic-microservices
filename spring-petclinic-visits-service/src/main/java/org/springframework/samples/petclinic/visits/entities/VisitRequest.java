package org.springframework.samples.petclinic.visits.entities;

   import java.io.Serializable;
   import java.util.Date;

   public class VisitRequest implements Serializable {
       private static final long serialVersionUID = -249974321255677286L;

       private Integer requestId;
       private Integer petId;
       private String message;

       public VisitRequest() {
       }

       public Integer getRequestId() {
           return requestId;
       }

       public void setRequestId(Integer id) {
           this.requestId = id;
       }

       public Integer getPetId() {
           return petId;
       }

       public void setPetId(Integer petId) {
           this.petId = petId;
       }

       public String getMessage() {
           return message;
       }

       public void setMessage(String message) {
           this.message = message;
       }
   }