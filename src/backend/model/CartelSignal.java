package backend.model;

import backend.enums.CollusionRecommendation;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cartel_signals")
public class CartelSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cluster_id", nullable = false)
    private String clusterId;

    @Column(name = "tender_id", nullable = false)
    private UUID tenderId;

    @Column(name = "bidder_ids", columnDefinition = "TEXT", nullable = false)
    private String bidderIds;

    @Column(name = "connection_strength", precision = 5, scale = 2)
    private BigDecimal connectionStrength;

    @Column(name = "shared_signals", columnDefinition = "TEXT")
    private String sharedSignals;

    @Column(name = "pattern_flags", columnDefinition = "TEXT")
    private String patternFlags;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CollusionRecommendation recommendation;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected CartelSignal() {}

    public CartelSignal(
            String clusterId,
            UUID tenderId,
            String bidderIds,
            BigDecimal connectionStrength,
            String sharedSignals,
            String patternFlags,
            String explanation,
            CollusionRecommendation recommendation
    ) {
        this.clusterId = clusterId;
        this.tenderId = tenderId;
        this.bidderIds = bidderIds;
        this.connectionStrength = connectionStrength;
        this.sharedSignals = sharedSignals;
        this.patternFlags = patternFlags;
        this.explanation = explanation;
        this.recommendation = recommendation;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public String getClusterId() { return clusterId; }
    public UUID getTenderId() { return tenderId; }
    public String getBidderIds() { return bidderIds; }
    public BigDecimal getConnectionStrength() { return connectionStrength; }
    public String getSharedSignals() { return sharedSignals; }
    public String getPatternFlags() { return patternFlags; }
    public String getExplanation() { return explanation; }
    public CollusionRecommendation getRecommendation() { return recommendation; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
