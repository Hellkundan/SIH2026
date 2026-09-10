package backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bidders")
public class Bidder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    public Bidder() {}

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