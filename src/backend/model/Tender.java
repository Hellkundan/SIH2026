package backend.model;

import backend.enums.TenderStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenders")
public class Tender {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenderStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;


    public Tender() {}

    public Tender(String title, String description) {
        this.title = title;
        this.description = description;
        this.status = TenderStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
    }


    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TenderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void openTender() {

        if (this.status == TenderStatus.DRAFT) {
            this.status = TenderStatus.OPEN;
        } else {
            throw new IllegalStateException(
                    "Tender can only be opened when status is DRAFT"
            );
        }
    }


    public void closeTender() {

        if (this.status == TenderStatus.OPEN) {
            this.status = TenderStatus.CLOSED;
        } else {
            throw new IllegalStateException(
                    "Tender can only be closed when status is OPEN"
            );
        }
    }


    public void updateTender(String title, String description) {

        if (this.status == TenderStatus.DRAFT) {
            this.title = title;
            this.description = description;
        } else {
            throw new IllegalStateException(
                    "Tender can only be edited when status is DRAFT"
            );
        }
    }
}