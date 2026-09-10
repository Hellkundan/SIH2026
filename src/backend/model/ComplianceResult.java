package backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "compliance_results")
public class ComplianceResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tender_bid_id", nullable = false)
    private UUID tenderBidId;

    @Column(name = "entity_match_score", precision = 5, scale = 2)
    private BigDecimal entityMatchScore;

    @Column(length = 20)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String discrepancies;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "evaluated_at", nullable = false)
    private LocalDateTime evaluatedAt;

    protected ComplianceResult() {}

    public ComplianceResult(UUID tenderBidId, BigDecimal entityMatchScore,
                            String severity, String discrepancies, String explanation) {
        this.tenderBidId = tenderBidId;
        this.entityMatchScore = entityMatchScore;
        this.severity = severity;
        this.discrepancies = discrepancies;
        this.explanation = explanation;
        this.evaluatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTenderBidId() { return tenderBidId; }
    public BigDecimal getEntityMatchScore() { return entityMatchScore; }
    public String getSeverity() { return severity; }
    public String getDiscrepancies() { return discrepancies; }
    public String getExplanation() { return explanation; }
    public LocalDateTime getEvaluatedAt() { return evaluatedAt; }
}