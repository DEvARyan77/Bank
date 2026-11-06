package com.devaryan.minibank.entity;

public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    // The fraud service was disabled or unreachable, so the transfer was not scored
    UNCHECKED
}
