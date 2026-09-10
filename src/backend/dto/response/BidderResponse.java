package backend.dto.response;

import backend.model.Bidder;

import java.time.LocalDateTime;
import java.util.UUID;

public class BidderResponse {

    private final UUID id;
    private final String companyName;
    private final String email;
    private final String phone;
    private final String pan;
    private final String gstin;
    private final LocalDateTime createdAt;


    public BidderResponse(Bidder bidder) {

        this.id = bidder.getId();
        this.companyName = bidder.getCompanyName();
        this.email = bidder.getEmail();
        this.phone = bidder.getPhone();
        this.pan = bidder.getPan();
        this.gstin = bidder.getGstin();
        this.createdAt = bidder.getCreatedAt();
    }


    public UUID getId() {

        return id;
    }


    public String getCompanyName() {

        return companyName;
    }


    public String getEmail() {

        return email;
    }


    public String getPhone() {

        return phone;
    }


    public String getPan() {
        return pan;
    }


    public String getGstin() {
        return gstin;
    }


    public LocalDateTime getCreatedAt() {

        return createdAt;
    }
}