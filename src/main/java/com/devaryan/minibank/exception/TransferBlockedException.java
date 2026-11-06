package com.devaryan.minibank.exception;

import java.util.List;

public class TransferBlockedException extends RuntimeException {

    private final List<String> reasons;

    public TransferBlockedException(List<String> reasons) {
        super("Transfer blocked: it looks like possible fraud");
        this.reasons = List.copyOf(reasons);
    }

    public List<String> getReasons() {
        return reasons;
    }
}
