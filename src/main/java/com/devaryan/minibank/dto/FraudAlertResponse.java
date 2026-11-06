package com.devaryan.minibank.dto;

import com.devaryan.minibank.entity.FraudAlertEntity;
import com.devaryan.minibank.entity.FraudDecision;
import com.devaryan.minibank.entity.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record FraudAlertResponse(Long id, String fromAccountNumber, String toAccountNumber, BigDecimal amount,
                                 double riskScore, RiskLevel riskLevel, FraudDecision decision,
                                 List<String> reasons, Instant createdAt) {

    public static FraudAlertResponse from(FraudAlertEntity alert) {
        return new FraudAlertResponse(alert.getId(), alert.getFromAccountNumber(), alert.getToAccountNumber(),
                alert.getAmount(), alert.getRiskScore(), alert.getRiskLevel(), alert.getDecision(),
                alert.getReasons(), alert.getCreatedAt());
    }
}
