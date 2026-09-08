package backend.dto.response;

import backend.enums.BidStatus;
import backend.model.TenderBid;

import java.time.LocalDateTime;
import java.util.UUID;

public class TenderBidResponse {

    private final UUID id;
    private final UUID tenderId;
    private final UUID bidderId;
    private final BidStatus status;
    private final LocalDateTime createdAt;
    private final LocalDateTime submittedAt;


    public TenderBidResponse(TenderBid tenderBid) {

        this.id = tenderBid.getId();
        this.tenderId = tenderBid.getTenderId();
        this.bidderId = tenderBid.getBidderId();
        this.status = tenderBid.getStatus();
        this.createdAt = tenderBid.getCreatedAt();
        this.submittedAt = tenderBid.getSubmittedAt();
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
}