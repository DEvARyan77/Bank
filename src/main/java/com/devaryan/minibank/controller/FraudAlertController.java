package com.devaryan.minibank.controller;

import com.devaryan.minibank.dto.FraudAlertResponse;
import com.devaryan.minibank.dto.PageResponse;
import com.devaryan.minibank.repository.FraudAlertRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fraud-alerts")
public class FraudAlertController {

    private final FraudAlertRepository fraudAlertRepository;

    public FraudAlertController(FraudAlertRepository fraudAlertRepository) {
        this.fraudAlertRepository = fraudAlertRepository;
    }

    @GetMapping
    public PageResponse<FraudAlertResponse> list(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, 100));
        return PageResponse.from(fraudAlertRepository.findAllByOrderByCreatedAtDescIdDesc(pageRequest),
                FraudAlertResponse::from);
    }
}
