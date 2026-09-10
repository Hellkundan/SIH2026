package backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "extracted_entities")
public class ExtractedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "document_id", nullable = false)
    private UUID documentId;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(name = "field_value", columnDefinition = "TEXT")
    private String fieldValue;

    private Double confidence;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected ExtractedEntity() {}

    public ExtractedEntity(UUID documentId, String fieldName, String fieldValue, Double confidence) {
        this.documentId = documentId;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
        this.confidence = confidence;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getDocumentId() { return documentId; }
    public String getFieldName() { return fieldName; }
    public String getFieldValue() { return fieldValue; }
    public Double getConfidence() { return confidence; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}