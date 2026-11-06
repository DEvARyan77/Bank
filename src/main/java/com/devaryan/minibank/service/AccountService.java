package com.devaryan.minibank.service;

import com.devaryan.minibank.entity.AccountEntity;
import com.devaryan.minibank.exception.AccountNotFoundException;
import com.devaryan.minibank.exception.InvalidAmountException;
import com.devaryan.minibank.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountEntity createAccount(String accountHolder) {
        return accountRepository.save(new AccountEntity(accountHolder, generateAccountNumber()));
    }

    @Transactional(readOnly = true)
    public List<AccountEntity> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AccountEntity getAccount(String accountNumber) {
        return findAccount(accountNumber);
    }

    // No save() needed: inside a transaction, JPA saves changes to loaded entities automatically (dirty checking)
    @Transactional
    public AccountEntity deposit(String accountNumber, BigDecimal amount) {
        AccountEntity account = findAccount(accountNumber);
        account.deposit(amount);
        return account;
    }

    @Transactional
    public AccountEntity withdraw(String accountNumber, BigDecimal amount) {
        AccountEntity account = findAccount(accountNumber);
        account.withdraw(amount);
        return account;
    }

    // All or nothing: if the deposit fails, the withdrawal is rolled back
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account");
        }
        AccountEntity source = findAccount(fromAccountNumber);
        AccountEntity target = findAccount(toAccountNumber);
        source.withdraw(amount);
        target.deposit(amount);
    }

    private AccountEntity findAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "MB" + ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
