package com.devaryan.account;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(String accountNumber, String accountHolder,
                              BigDecimal balance, Instant createdAt) {

    static AccountResponse from(AccountEntity a) {
        return new AccountResponse(a.getAccountNumber(), a.getAccountHolder(),
                a.getBalance(), a.getCreatedAt());
    }
}