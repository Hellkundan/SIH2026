package backend.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tender_bid_id", nullable = false)
    private UUID tenderBidId;

    @Column(name = "compliance_result_id")
    private UUID complianceResultId;

    @Column(name = "ai_recommendation", length = 40)
    private String aiRecommendation;

    @Column(name = "ai_confidence", precision = 5, scale = 2)
    private BigDecimal aiConfidence;

    @Column(name = "officer_decision", nullable = false, length = 30)
    private String officerDecision;

    @Column(name = "officer_id")
    private UUID officerId;

    @Column(name = "officer_notes", columnDefinition = "TEXT")
    private String officerNotes;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Recommendation() {}

    public Recommendation(UUID tenderBidId, UUID complianceResultId,
                           String aiRecommendation, BigDecimal aiConfidence) {
        this.tenderBidId = tenderBidId;
        this.complianceResultId = complianceResultId;
        this.aiRecommendation = aiRecommendation;
        this.aiConfidence = aiConfidence;
        this.officerDecision = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getTenderBidId() { return tenderBidId; }
    public UUID getComplianceResultId() { return complianceResultId; }
    public String getAiRecommendation() { return aiRecommendation; }
    public BigDecimal getAiConfidence() { return aiConfidence; }
    public String getOfficerDecision() { return officerDecision; }
    public UUID getOfficerId() { return officerId; }
    public String getOfficerNotes() { return officerNotes; }
    public LocalDateTime getDecidedAt() { return decidedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void recordOfficerDecision(String decision, UUID officerId, String notes) {
        if (!decision.equals("QUALIFIED") && !decision.equals("DISQUALIFIED")
                && !decision.equals("NEEDS_MORE_INFO")) {
            throw new IllegalArgumentException("Unsupported officer decision: " + decision);
        }

        this.officerDecision = decision;
        this.officerId = officerId;
        this.officerNotes = notes;
        this.decidedAt = LocalDateTime.now();
    }
}