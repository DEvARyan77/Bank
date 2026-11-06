package com.devaryan.account;

public class InvalidAmountException extends RuntimeException {
    public InvalidAmountException(String accountNumber) {
        super("Invalid amount for account: " + accountNumber);
    }
}
