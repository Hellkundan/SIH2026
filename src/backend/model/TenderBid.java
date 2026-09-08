package backend.model;

import backend.enums.BidStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TenderBid {

    private final UUID id = UUID.randomUUID();

    private final UUID tenderId;

    private final UUID bidderId;

    private BidStatus status;

    private final LocalDateTime createdAt;

    private LocalDateTime submittedAt;


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

        if (this.status != BidStatus.SUBMITTED) {

            throw new IllegalStateException(
                    "Only a SUBMITTED bid can be moved to review"
            );
        }

        this.status = BidStatus.UNDER_REVIEW;
    }


    public void qualifyBid() {

        if (this.status != BidStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "Only a bid UNDER_REVIEW can be qualified"
            );
        }

        this.status = BidStatus.QUALIFIED;
    }


    public void disqualifyBid() {

        if (this.status != BidStatus.UNDER_REVIEW) {

            throw new IllegalStateException(
                    "Only a bid UNDER_REVIEW can be disqualified"
            );
        }

        this.status = BidStatus.DISQUALIFIED;
    }
}