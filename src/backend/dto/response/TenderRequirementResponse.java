package backend.dto.response;

import backend.model.TenderRequirement;

import java.util.UUID;

public class TenderRequirementResponse {

    private final UUID id;
    private final UUID tenderId;
    private final String requirement;
    private final boolean mandatory;


    public TenderRequirementResponse(TenderRequirement requirement) {

        this.id = requirement.getId();
        this.tenderId = requirement.getTenderId();
        this.requirement = requirement.getRequirement();
        this.mandatory = requirement.isMandatory();
    }


    public UUID getId() {

        return id;
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
}