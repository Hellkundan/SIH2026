package backend.model;

import backend.enums.DocumentStatus;
import backend.enums.DocumentType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenderBidId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String extractedText;

    private String documentHash;

    private Double classificationConfidence;

    @Column(columnDefinition = "TEXT")
    private String ocrExtractedFields;


    public Document() {}

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


    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


    public void setOcrResults(
            String extractedText,
            String documentHash,
            Double classificationConfidence,
            String ocrExtractedFields
    ) {
        this.extractedText = extractedText;
        this.documentHash = documentHash;
        this.classificationConfidence = classificationConfidence;
        this.ocrExtractedFields = ocrExtractedFields;
    }


    public void startProcessing() {
        this.status = DocumentStatus.PROCESSING;
    }


    public void markAsProcessed() {
        this.status = DocumentStatus.PROCESSED;
    }


    public void markAsFailed() {
        this.status = DocumentStatus.FAILED;
    }
}