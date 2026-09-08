package backend.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TenderBidRequest {

    @NotNull
    private UUID tenderId;

    @NotNull
    private UUID bidderId;


    public TenderBidRequest() {
    }


    public TenderBidRequest(UUID tenderId, UUID bidderId) {

        this.tenderId = tenderId;
        this.bidderId = bidderId;
    }


    public UUID getTenderId() {

        return tenderId;
    }


    public UUID getBidderId() {

        return bidderId;
    }


    public void setTenderId(UUID tenderId) {

        this.tenderId = tenderId;
    }


    public void setBidderId(UUID bidderId) {

        this.bidderId = bidderId;
    }
}