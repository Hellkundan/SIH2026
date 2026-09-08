package backend.model;

import backend.enums.DocumentStatus;
import backend.enums.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

public class Document {

    private final UUID id = UUID.randomUUID();

    private final UUID tenderBidId;

    private final DocumentType documentType;

    private final String fileName;

    private DocumentStatus status;

    private final LocalDateTime uploadedAt;


    public Document(
            UUID tenderBidId,
            DocumentType documentType,
            String fileName
    ) {

        this.tenderBidId = tenderBidId;
        this.documentType = documentType;
        this.fileName = fileName;

        this.status = DocumentStatus.UPLOADED;

        this.uploadedAt = LocalDateTime.now();
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


    public void startProcessing() {

        if (this.status != DocumentStatus.UPLOADED) {

            throw new IllegalStateException(
                    "Only an UPLOADED document can start processing"
            );
        }

        this.status = DocumentStatus.PROCESSING;
    }


    public void markAsProcessed() {

        if (this.status != DocumentStatus.PROCESSING) {

            throw new IllegalStateException(
                    "Only a PROCESSING document can be marked as processed"
            );
        }

        this.status = DocumentStatus.PROCESSED;
    }


    public void markAsFailed() {

        if (this.status != DocumentStatus.PROCESSING) {

            throw new IllegalStateException(
                    "Only a PROCESSING document can be marked as failed"
            );
        }

        this.status = DocumentStatus.FAILED;
    }
}