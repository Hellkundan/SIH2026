package backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TenderRequirementRequest {

    @NotNull
    private UUID tenderId;

    @NotBlank
    private String requirement;
    private boolean mandatory;


    public TenderRequirementRequest() {
    }


    public TenderRequirementRequest(
            UUID tenderId,
            String requirement,
            boolean mandatory
    ) {

        this.tenderId = tenderId;
        this.requirement = requirement;
        this.mandatory = mandatory;
    }


    public UUID getTenderId() {

        return tenderId;
    }


    public String getRequirement() {

        return requirement;
    }


    public boolean isMandatory() {

        return mandatory;
    }


    public void setTenderId(UUID tenderId) {

        this.tenderId = tenderId;
    }


    public void setRequirement(String requirement) {

        this.requirement = requirement;
    }


    public void setMandatory(boolean mandatory) {

        this.mandatory = mandatory;
    }
}