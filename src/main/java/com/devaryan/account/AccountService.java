package com.devaryan.account;

import java.util.concurrent.ThreadLocalRandom;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountEntity createAccount(String accountHolder) {
        AccountEntity account = new AccountEntity(accountHolder, generateAccountNumber());
        return accountRepository.save(account);
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "MB" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional
    public void deposit(String accountNumber, BigDecimal amount) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.deposit(amount);
    }

    @Transactional
    public void withdraw(String accountNumber, BigDecimal amount) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
        account.withdraw(amount);
    }

    @Transactional(readOnly = true)
    public AccountEntity getAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        AccountEntity fromAccount = accountRepository.findByAccountNumber(fromAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("From account not found"));
        AccountEntity toAccount = accountRepository.findByAccountNumber(toAccountNumber)
                .orElseThrow(() -> new IllegalArgumentException("To account not found"));
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
    }
}
