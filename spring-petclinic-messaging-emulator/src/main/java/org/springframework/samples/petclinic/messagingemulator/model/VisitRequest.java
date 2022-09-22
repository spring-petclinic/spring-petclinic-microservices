package org.springframework.samples.petclinic.messagingemulator.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visitrequests")
@NoArgsConstructor
@AllArgsConstructor
public class VisitRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pet_id")
    private Integer petId;

    @Column(name="message")
    @Size(max = 8192)
    private String message;

    @Column(name = "response")
    @Size(max = 8192)
    private String response;

    @Column(name="accepted")
    private Boolean accepted;

    public VisitRequest( Integer petId, String message) {
        this.petId = petId;
        this.message = message;
        accepted = false;
	}

    public Integer getId(){
        return id;
    }

    public Integer getPetId(){
        return petId;
    }

    public String getMessage(){
        return message;
    }

    public String getResponse(){
        return response;
    }

    public void setResponse(String response){
        this.response = response;
    }

    public Boolean getAccepted(){
        return accepted;
    }

    public void setAccepted(Boolean accepted){
        this.accepted = accepted;
    }

	
}
