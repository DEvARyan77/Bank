package com.devaryan.account;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import java.math.BigDecimal;
import java.util.List;


@RestController 
@RequestMapping("/api/accounts")
public class AccountController{
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("") 
    public ResponseEntity<List<AccountEntity>> getAllAccounts() {
        List<AccountEntity> accounts = service.getAllAccounts();
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AccountEntity> createAccountEntity(@RequestBody CreateAccountRequest request) {
        
        AccountEntity account = service.createAccount(request.accountHolder());
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("{accountNumber}")
    public ResponseEntity<AccountEntity> getAccounts(@PathVariable String accountNumber) {
        AccountEntity accounts = service.getAccount(accountNumber);
        return ResponseEntity.ok(accounts);
    }

    @PutMapping("{id}/deposit")
    public ResponseEntity<String> deposit(@PathVariable String id, @RequestBody BigDecimal amount) {
        service.deposit(id, amount);
        return ResponseEntity.ok("Amount deposited successfully");
    }

    @PutMapping("{id}/withdraw")
    public ResponseEntity<String> withdrawal(@PathVariable String id, @RequestBody BigDecimal amount) {
        service.withdraw(id, amount);
        return ResponseEntity.ok("Amount withdrawn successfully");
    }

    @PutMapping("{id}/transfer")
    public ResponseEntity<String> transfer(@PathVariable String id, @RequestBody TransferRequest request) {
        service.transfer(id, request.toAccount(), request.amount());
        return ResponseEntity.ok("Amount transferred successfully");
    }
}