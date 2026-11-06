package com.devaryan.minibank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlertEntity {

    private static final String REASON_SEPARATOR = "; ";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String fromAccountNumber;

    @Column(nullable = false, updatable = false)
    private String toAccountNumber;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false)
    private double riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private FraudDecision decision;

    @Column(nullable = false, updatable = false, length = 2000)
    private String reasons;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected FraudAlertEntity() {
        // required by JPA
    }

    public FraudAlertEntity(String fromAccountNumber, String toAccountNumber, BigDecimal amount,
                            double riskScore, RiskLevel riskLevel, FraudDecision decision, List<String> reasons) {
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.riskScore = riskScore;
        this.riskLevel = riskLevel;
        this.decision = decision;
        this.reasons = String.join(REASON_SEPARATOR, reasons);
    }

    public Long getId() {
        return id;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public double getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public FraudDecision getDecision() {
        return decision;
    }

    public List<String> getReasons() {
        return reasons.isEmpty() ? List.of() : Arrays.asList(reasons.split(REASON_SEPARATOR));
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
