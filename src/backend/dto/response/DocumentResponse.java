package backend.dto.response;

import backend.enums.DocumentStatus;
import backend.enums.DocumentType;
import backend.model.Document;

import java.time.LocalDateTime;
import java.util.UUID;

public class DocumentResponse {

    private final UUID id;
    private final UUID tenderBidId;
    private final DocumentType documentType;
    private final String fileName;
    private final DocumentStatus status;
    private final LocalDateTime uploadedAt;


    public DocumentResponse(Document document) {

        this.id = document.getId();
        this.tenderBidId = document.getTenderBidId();
        this.documentType = document.getDocumentType();
        this.fileName = document.getFileName();
        this.status = document.getStatus();
        this.uploadedAt = document.getUploadedAt();
    }


    public UUID getId() {

        return id;
    }


    public UUID getTenderBidId() {

        return tenderBidId;
    }


    public DocumentType getDocumentType() {

        return documentType;
    }


    public String getFileName() {

        return fileName;
    }


    public DocumentStatus getStatus() {

        return status;
    }


    public LocalDateTime getUploadedAt() {

        return uploadedAt;
    }
}