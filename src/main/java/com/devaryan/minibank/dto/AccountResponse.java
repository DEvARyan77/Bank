package com.devaryan.minibank.dto;

import com.devaryan.minibank.entity.AccountEntity;

import java.math.BigDecimal;
import java.time.Instant;

// What the API returns: internal fields like id and version are never exposed
public record AccountResponse(String accountNumber, String accountHolder,
                              BigDecimal balance, Instant createdAt) {

    public static AccountResponse from(AccountEntity account) {
        return new AccountResponse(account.getAccountNumber(), account.getAccountHolder(),
                account.getBalance(), account.getCreatedAt());
    }
}
