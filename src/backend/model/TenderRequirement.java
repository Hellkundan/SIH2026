package backend.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "tender_requirements")
public class TenderRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenderId;

    @Column(nullable = false)
    private String requirement;

    @Column(nullable = false)
    private boolean mandatory;


    public TenderRequirement() {}

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