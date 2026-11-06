package com.devaryan.minibank.service;

import com.devaryan.minibank.entity.AccountEntity;
import com.devaryan.minibank.entity.TransactionEntity;
import com.devaryan.minibank.entity.TransactionType;
import com.devaryan.minibank.exception.AccountNotFoundException;
import com.devaryan.minibank.exception.InvalidAmountException;
import com.devaryan.minibank.repository.AccountRepository;
import com.devaryan.minibank.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AccountService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
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

    @Transactional(readOnly = true)
    public Page<TransactionEntity> getTransactions(String accountNumber, int page, int size) {
        findAccount(accountNumber); // 404 if the account doesn't exist
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
        return transactionRepository.findByAccountNumberOrderByCreatedAtDescIdDesc(accountNumber, pageRequest);
    }

    // No save() needed for the account: inside a transaction, JPA saves changes to loaded entities (dirty checking)
    @Transactional
    public AccountEntity deposit(String accountNumber, BigDecimal amount) {
        AccountEntity account = findAccount(accountNumber);
        account.deposit(amount);
        record(account, TransactionType.DEPOSIT, amount, null);
        return account;
    }

    @Transactional
    public AccountEntity withdraw(String accountNumber, BigDecimal amount) {
        AccountEntity account = findAccount(accountNumber);
        account.withdraw(amount);
        record(account, TransactionType.WITHDRAWAL, amount, null);
        return account;
    }

    // All or nothing: if any step fails, the whole transfer (and its history rows) is rolled back
    @Transactional
    public void transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account");
        }
        AccountEntity source = findAccount(fromAccountNumber);
        AccountEntity target = findAccount(toAccountNumber);
        source.withdraw(amount);
        target.deposit(amount);
        record(source, TransactionType.TRANSFER_OUT, amount, toAccountNumber);
        record(target, TransactionType.TRANSFER_IN, amount, fromAccountNumber);
    }

    private void record(AccountEntity account, TransactionType type, BigDecimal amount, String counterparty) {
        transactionRepository.save(new TransactionEntity(
                account.getAccountNumber(), type, amount, account.getBalance(), counterparty));
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
