package com.devaryan.minibank.service;

import com.devaryan.minibank.entity.AccountEntity;
import com.devaryan.minibank.exception.AccountNotFoundException;
import com.devaryan.minibank.exception.InsufficientFundsException;
import com.devaryan.minibank.exception.InvalidAmountException;
import com.devaryan.minibank.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountGeneratesAccountNumber() {
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(AccountEntity.class))).thenAnswer(call -> call.getArgument(0));

        AccountEntity account = accountService.createAccount("Dev Aryan");

        assertEquals("Dev Aryan", account.getAccountHolder());
        assertTrue(account.getAccountNumber().matches("MB\\d{10}"));
    }

    @Test
    void transferMovesMoneyBetweenAccounts() {
        AccountEntity from = new AccountEntity("A", "MB0000000001");
        AccountEntity to = new AccountEntity("B", "MB0000000002");
        from.deposit(new BigDecimal("5000"));
        when(accountRepository.findByAccountNumber("MB0000000001")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("MB0000000002")).thenReturn(Optional.of(to));

        accountService.transfer("MB0000000001", "MB0000000002", new BigDecimal("1500"));

        assertEquals(0, new BigDecimal("3500").compareTo(from.getBalance()));
        assertEquals(0, new BigDecimal("1500").compareTo(to.getBalance()));
    }

    @Test
    void transferWithInsufficientFundsThrows() {
        AccountEntity from = new AccountEntity("A", "MB0000000001");
        AccountEntity to = new AccountEntity("B", "MB0000000002");
        when(accountRepository.findByAccountNumber("MB0000000001")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("MB0000000002")).thenReturn(Optional.of(to));

        assertThrows(InsufficientFundsException.class,
                () -> accountService.transfer("MB0000000001", "MB0000000002", new BigDecimal("1")));
    }

    @Test
    void transferToSameAccountThrows() {
        assertThrows(InvalidAmountException.class,
                () -> accountService.transfer("MB0000000001", "MB0000000001", new BigDecimal("10")));
    }

    @Test
    void unknownAccountThrows() {
        when(accountRepository.findByAccountNumber("MB9999999999")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount("MB9999999999"));
    }
}
