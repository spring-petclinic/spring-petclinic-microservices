package org.springframework.samples.petclinic.visits.entities;

   public class VisitResponse {
       Integer requestId;
       Boolean confirmed;
       String reason;

       public VisitResponse() {
       }

       public VisitResponse(Integer requestId, Boolean confirmed, String reason) {
           this.requestId = requestId;
           this.confirmed = confirmed;
           this.reason = reason;
       }    

       public Boolean getConfirmed() {
           return confirmed;
       }

       public void setConfirmed(Boolean confirmed) {
           this.confirmed = confirmed;
       }

       public String getReason() {
           return reason;
       }

       public void setReason(String reason) {
           this.reason = reason;
       }

       public Integer getRequestId() {
           return requestId;
       }

       public void setRequestId(Integer requestId) {
           this.requestId = requestId;
       }
   }