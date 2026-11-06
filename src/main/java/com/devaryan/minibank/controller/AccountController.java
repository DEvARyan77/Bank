package com.devaryan.minibank.controller;

import com.devaryan.minibank.dto.AccountResponse;
import com.devaryan.minibank.dto.AmountRequest;
import com.devaryan.minibank.dto.CreateAccountRequest;
import com.devaryan.minibank.dto.PageResponse;
import com.devaryan.minibank.dto.TransactionResponse;
import com.devaryan.minibank.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@Valid @RequestBody CreateAccountRequest request) {
        return AccountResponse.from(service.createAccount(request.accountHolder()));
    }

    @GetMapping("/accounts")
    public List<AccountResponse> getAll() {
        return service.getAllAccounts().stream().map(AccountResponse::from).toList();
    }

    @GetMapping("/accounts/{accountNumber}")
    public AccountResponse get(@PathVariable String accountNumber) {
        return AccountResponse.from(service.getAccount(accountNumber));
    }

    @GetMapping("/accounts/{accountNumber}/transactions")
    public PageResponse<TransactionResponse> transactions(@PathVariable String accountNumber,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(service.getTransactions(accountNumber, page, size), TransactionResponse::from);
    }

    @PostMapping("/accounts/{accountNumber}/deposit")
    public AccountResponse deposit(@PathVariable String accountNumber,
                                   @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(service.deposit(accountNumber, request.amount()));
    }

    @PostMapping("/accounts/{accountNumber}/withdraw")
    public AccountResponse withdraw(@PathVariable String accountNumber,
                                    @Valid @RequestBody AmountRequest request) {
        return AccountResponse.from(service.withdraw(accountNumber, request.amount()));
    }
}
