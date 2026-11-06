package com.devaryan.minibank.controller;

import com.devaryan.minibank.dto.TransferRequest;
import com.devaryan.minibank.dto.TransferResponse;
import com.devaryan.minibank.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return transferService.transfer(request.fromAccount(), request.toAccount(), request.amount());
    }
}
