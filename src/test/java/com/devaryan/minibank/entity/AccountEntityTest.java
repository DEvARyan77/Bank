package com.devaryan.minibank.entity;

import com.devaryan.minibank.exception.InsufficientFundsException;
import com.devaryan.minibank.exception.InvalidAmountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountEntityTest {

    private AccountEntity account;

    @BeforeEach
    void setUp() {
        account = new AccountEntity("Dev Aryan", "MB1234567890");
    }

    @Test
    void depositIncreasesBalance() {
        account.deposit(new BigDecimal("500.50"));

        assertEquals(0, new BigDecimal("500.50").compareTo(account.getBalance()));
    }

    @Test
    void withdrawDecreasesBalance() {
        account.deposit(new BigDecimal("1000"));

        account.withdraw(new BigDecimal("250.25"));

        assertEquals(0, new BigDecimal("749.75").compareTo(account.getBalance()));
    }

    @Test
    void withdrawMoreThanBalanceThrows() {
        account.deposit(new BigDecimal("100"));

        assertThrows(InsufficientFundsException.class, () -> account.withdraw(new BigDecimal("100.01")));
        assertEquals(0, new BigDecimal("100").compareTo(account.getBalance()));
    }

    @Test
    void zeroOrNegativeAmountThrows() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(BigDecimal.ZERO));
        assertThrows(InvalidAmountException.class, () -> account.deposit(new BigDecimal("-10")));
        assertThrows(InvalidAmountException.class, () -> account.deposit(null));
    }

    @Test
    void moreThanTwoDecimalPlacesThrows() {
        assertThrows(InvalidAmountException.class, () -> account.deposit(new BigDecimal("10.555")));
    }
}
