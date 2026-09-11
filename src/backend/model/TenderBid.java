package backend.model;

import backend.enums.BidStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tender_bids")
public class TenderBid {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenderId;

    @Column(nullable = false)
    private UUID bidderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BidStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;


    public TenderBid() {}

    public TenderBid(
            UUID tenderId,
            UUID bidderId
    ) {

        this.tenderId = tenderId;
        this.bidderId = bidderId;

        this.status = BidStatus.DRAFT;

        this.createdAt = LocalDateTime.now();
    }


    public UUID getId() {
        return id;
    }


    public UUID getTenderId() {
        return tenderId;
    }


    public UUID getBidderId() {
        return bidderId;
    }


    public BidStatus getStatus() {
        return status;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }


    public void submitBid() {

        if (this.status != BidStatus.DRAFT) {

            throw new IllegalStateException(
                    "Only a DRAFT bid can be submitted"
            );
        }

        this.status = BidStatus.SUBMITTED;

        this.submittedAt = LocalDateTime.now();
    }


    public void startReview() {
        if (this.status == BidStatus.DRAFT) {
            throw new IllegalStateException(
                    "A DRAFT bid cannot be moved to review"
            );
        }

        this.status = BidStatus.UNDER_REVIEW;
    }


    public void qualifyBid() {
        if (this.status == BidStatus.DRAFT) {
            throw new IllegalStateException(
                    "A DRAFT bid cannot be qualified"
            );
        }

        this.status = BidStatus.QUALIFIED;
    }


    public void disqualifyBid() {
        if (this.status == BidStatus.DRAFT) {
            throw new IllegalStateException(
                    "A DRAFT bid cannot be disqualified"
            );
        }

        this.status = BidStatus.DISQUALIFIED;
    }


    public void markComplianceResult(BidStatus complianceStatus) {
        if (complianceStatus != BidStatus.NEEDS_REVIEW
                && complianceStatus != BidStatus.PASSED_AUTOMATED_CHECKS) {
            throw new IllegalArgumentException("Invalid compliance status");
        }

        this.status = complianceStatus;
    }
}