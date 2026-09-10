package backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "verification_results")
public class VerificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID tenderBidId;

    @Column(nullable = false)
    private String verificationType;

    @Column(nullable = false)
    private String status;

    private String identifier;
    private String source;
    private String errorState;
    private Double confidence;

    @Lob
    private String evidence;

    @Column(nullable = false)
    private LocalDateTime verifiedAt;

    public VerificationResult() {
    }

    public VerificationResult(
            UUID tenderBidId,
            String verificationType,
            String status,
            String identifier,
            String source,
            String errorState,
            Double confidence,
            String evidence
    ) {
        this.tenderBidId = tenderBidId;
        this.verificationType = verificationType;
        this.status = status;
        this.identifier = identifier;
        this.source = source;
        this.errorState = errorState;
        this.confidence = confidence;
        this.evidence = evidence;
        this.verifiedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenderBidId() {
        return tenderBidId;
    }

    public String getVerificationType() {
        return verificationType;
    }

    public String getStatus() {
        return status;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getSource() {
        return source;
    }

    public String getErrorState() {
        return errorState;
    }

    public Double getConfidence() {
        return confidence;
    }

    public String getEvidence() {
        return evidence;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
}
