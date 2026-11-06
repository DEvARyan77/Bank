package com.devaryan.minibank.entity;

public enum FraudDecision {
    // High risk: the transfer was stopped
    BLOCKED,
    // Medium risk: the transfer went through but was flagged for review
    FLAGGED
}
