package backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Bidder {

    private final UUID id = UUID.randomUUID();

    private String companyName;
    private String email;
    private String phone;

    private LocalDateTime createdAt;


    public Bidder(
            String companyName,
            String email,
            String phone
    ) {
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.createdAt = LocalDateTime.now();
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


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void updateBidder(
            String companyName,
            String email,
            String phone
    ) {
        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
    }
}