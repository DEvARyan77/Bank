package com.devaryan.minibank.dto;

import com.devaryan.minibank.entity.TransactionEntity;
import com.devaryan.minibank.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(Long id, TransactionType type, BigDecimal amount,
                                  BigDecimal balanceAfter, String counterpartyAccountNumber,
                                  Instant createdAt) {

    public static TransactionResponse from(TransactionEntity transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getType(), transaction.getAmount(),
                transaction.getBalanceAfter(), transaction.getCounterpartyAccountNumber(),
                transaction.getCreatedAt());
    }
}
