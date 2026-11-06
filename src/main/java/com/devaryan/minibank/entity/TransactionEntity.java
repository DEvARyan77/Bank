package com.devaryan.minibank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

// One row per money movement. Rows are never updated or deleted, so this is a full audit trail.
@Entity
@Table(name = "transactions",
        indexes = @Index(name = "idx_transaction_account_created", columnList = "accountNumber, createdAt"))
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    // The other account in a transfer; null for deposits and withdrawals
    @Column(updatable = false)
    private String counterpartyAccountNumber;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected TransactionEntity() {
        // required by JPA
    }

    public TransactionEntity(String accountNumber, TransactionType type, BigDecimal amount,
                             BigDecimal balanceAfter, String counterpartyAccountNumber) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartyAccountNumber = counterpartyAccountNumber;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getCounterpartyAccountNumber() {
        return counterpartyAccountNumber;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
