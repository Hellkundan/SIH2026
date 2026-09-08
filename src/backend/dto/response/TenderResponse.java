package backend.dto.response;

import backend.model.Tender;
import backend.enums.TenderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TenderResponse {

    private final UUID id;
    private final String title;
    private final String description;
    private final TenderStatus status;
    private final LocalDateTime createdAt;


    public TenderResponse(Tender tender) {

        this.id = tender.getId();
        this.title = tender.getTitle();
        this.description = tender.getDescription();
        this.status = tender.getStatus();
        this.createdAt = tender.getCreatedAt();
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
}