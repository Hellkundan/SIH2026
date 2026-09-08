package backend.model;

import java.util.UUID;

public class TenderRequirement {

    private final UUID id = UUID.randomUUID();

    private final UUID tenderId;

    private String requirement;

    private boolean mandatory;


    public TenderRequirement(
            UUID tenderId,
            String requirement,
            boolean mandatory
    ) {
        this.tenderId = tenderId;
        this.requirement = requirement;
        this.mandatory = mandatory;
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


    public void updateRequirement(
            String requirement,
            boolean mandatory
    ) {
        this.requirement = requirement;
        this.mandatory = mandatory;
    }
}