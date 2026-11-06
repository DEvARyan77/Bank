package com.devaryan.minibank.service;

import com.devaryan.minibank.dto.FraudFeatures;
import com.devaryan.minibank.entity.AccountEntity;
import com.devaryan.minibank.entity.TransactionType;
import com.devaryan.minibank.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

// Turns a transfer plus the sender's history into the numbers the AI model scores
@Service
public class FraudFeatureService {

    private static final double SECONDS_PER_DAY = 86_400.0;

    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final ZoneId zone;

    public FraudFeatureService(AccountService accountService, TransactionRepository transactionRepository,
                               @Value("${fraud.timezone:Asia/Kolkata}") String zone) {
        this.accountService = accountService;
        this.transactionRepository = transactionRepository;
        this.zone = ZoneId.of(zone);
    }

    @Transactional(readOnly = true)
    public FraudFeatures build(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        AccountEntity sender = accountService.getAccount(fromAccountNumber);
        accountService.getAccount(toAccountNumber); // 404 if the recipient doesn't exist

        Instant now = Instant.now();
        double accountAgeDays = Duration.between(sender.getCreatedAt(), now).toSeconds() / SECONDS_PER_DAY;
        long transfersLastDay = transactionRepository.countByAccountNumberAndTypeAndCreatedAtAfter(
                fromAccountNumber, TransactionType.TRANSFER_OUT, now.minus(Duration.ofDays(1)));
        Double average = transactionRepository.averageAmount(fromAccountNumber, TransactionType.TRANSFER_OUT);
        boolean newRecipient = !transactionRepository.existsByAccountNumberAndTypeAndCounterpartyAccountNumber(
                fromAccountNumber, TransactionType.TRANSFER_OUT, toAccountNumber);

        return new FraudFeatures(amount, ZonedDateTime.now(zone).getHour(), Math.max(accountAgeDays, 0),
                transfersLastDay, average == null ? 0 : average, newRecipient);
    }
}
