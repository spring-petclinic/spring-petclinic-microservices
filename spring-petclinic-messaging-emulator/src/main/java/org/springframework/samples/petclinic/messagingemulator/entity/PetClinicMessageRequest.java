package org.springframework.samples.petclinic.messagingemulator.entity;

import java.io.Serializable;

public class PetClinicMessageRequest implements Serializable {

    private static final long serialVersionUID = -249322721255887286L;


    private Integer requestId;
    private Integer petId;

    private String message;

    public PetClinicMessageRequest() {
    }

    public PetClinicMessageRequest(Integer ownerId, String message) {
        this.petId = ownerId;
        this.message = message;
    }

    public Integer getPetId(){
        return petId;
    }

    public void setPetId(Integer petId){
        this.petId = petId;
    }

    public String getMessage(){
        return message;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
}
