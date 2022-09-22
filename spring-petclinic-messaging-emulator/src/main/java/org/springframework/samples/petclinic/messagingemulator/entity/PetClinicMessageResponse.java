package org.springframework.samples.petclinic.messagingemulator.entity;

public class PetClinicMessageResponse {
    int requestId;
    Boolean confirmed;
    String reason;

    public PetClinicMessageResponse() {
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public Boolean getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(Boolean confirmed) {
        this.confirmed = confirmed;
    }
}
