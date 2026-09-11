package backend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public class TenderRequirementRequest {

    @NotNull
    private UUID tenderId;

    private String requirement;
    private boolean mandatory;
    private List<Item> requirements;

    public static class Item {
        private UUID tenderId;
        private String requirement;
        private boolean mandatory;

        public Item() {
        }

        public Item(UUID tenderId, String requirement, boolean mandatory) {
            this.tenderId = tenderId;
            this.requirement = requirement;
            this.mandatory = mandatory;
        }

        public UUID getTenderId() {
            return tenderId;
        }

        public void setTenderId(UUID tenderId) {
            this.tenderId = tenderId;
        }

        public String getRequirement() {
            return requirement;
        }

        public void setRequirement(String requirement) {
            this.requirement = requirement;
        }

        public boolean isMandatory() {
            return mandatory;
        }

        public void setMandatory(boolean mandatory) {
            this.mandatory = mandatory;
        }
    }

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

    public List<Item> getRequirements() {
        return requirements;
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

    public void setRequirements(List<Item> requirements) {
        this.requirements = requirements;
    }
}