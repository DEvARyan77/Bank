package com.devaryan.minibank.dto;

import com.devaryan.minibank.entity.RiskLevel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

// What the Python fraud service returns
@JsonIgnoreProperties(ignoreUnknown = true)
public record FraudAssessment(double riskScore, RiskLevel riskLevel, List<String> reasons, String modelVersion) {

    public FraudAssessment {
        reasons = reasons == null ? List.of() : List.copyOf(reasons);
    }

    public static FraudAssessment unchecked(String reason) {
        return new FraudAssessment(0, RiskLevel.UNCHECKED, List.of(reason), null);
    }
}
