package com.devaryan.minibank.entity;

import com.devaryan.minibank.exception.InsufficientFundsException;
import com.devaryan.minibank.exception.InvalidAmountException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountHolder;

    @Column(nullable = false, unique = true)
    private String accountNumber;

    // BigDecimal (never double) so money has no rounding errors: up to 17 digits + 2 decimals
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Optimistic locking: stops two requests from overwriting the same balance at the same time
    @Version
    private Long version;

    protected AccountEntity() {
        // required by JPA
    }

    public AccountEntity(String accountHolder, String accountNumber) {
        this.accountHolder = accountHolder;
        this.accountNumber = accountNumber;
    }

    public void deposit(BigDecimal amount) {
        requireValidAmount(amount);
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requireValidAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountNumber);
        }
        this.balance = this.balance.subtract(amount);
    }

    private static void requireValidAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidAmountException("Amount can have at most 2 decimal places");
        }
    }

    public Long getId() {
        return id;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Long getVersion() {
        return version;
    }
}
