package com.devaryan.minibank.dto;

import com.devaryan.minibank.entity.RiskLevel;

import java.util.List;

public record TransferResponse(String status, RiskLevel riskLevel, double riskScore, List<String> reasons) {
}
