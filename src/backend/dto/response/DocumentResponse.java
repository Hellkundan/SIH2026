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
    private final String filePath;
    private final String extractedText;
    private final String documentHash;
    private final Double classificationConfidence;
    private final String ocrExtractedFields;


    public DocumentResponse(Document document) {

        this.id = document.getId();
        this.tenderBidId = document.getTenderBidId();
        this.documentType = document.getDocumentType();
        this.fileName = document.getFileName();
        this.status = document.getStatus();
        this.uploadedAt = document.getUploadedAt();
        this.filePath = document.getFilePath();
        this.extractedText = document.getExtractedText();
        this.documentHash = document.getDocumentHash();
        this.classificationConfidence = document.getClassificationConfidence();
        this.ocrExtractedFields = document.getOcrExtractedFields();
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


    public String getFilePath() {
        return filePath;
    }


    public String getExtractedText() {
        return extractedText;
    }


    public String getDocumentHash() {
        return documentHash;
    }


    public Double getClassificationConfidence() {
        return classificationConfidence;
    }


    public String getOcrExtractedFields() {
        return ocrExtractedFields;
    }
}