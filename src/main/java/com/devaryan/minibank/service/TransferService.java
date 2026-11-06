package com.devaryan.minibank.service;

import com.devaryan.minibank.client.FraudServiceClient;
import com.devaryan.minibank.dto.FraudAssessment;
import com.devaryan.minibank.dto.TransferResponse;
import com.devaryan.minibank.entity.FraudAlertEntity;
import com.devaryan.minibank.entity.FraudDecision;
import com.devaryan.minibank.entity.RiskLevel;
import com.devaryan.minibank.exception.InvalidAmountException;
import com.devaryan.minibank.exception.TransferBlockedException;
import com.devaryan.minibank.repository.FraudAlertRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/*
 * Transfer flow:
 *   1. Build features from the sender's history
 *   2. Ask the AI fraud service for a risk score
 *   3. HIGH   -> save a BLOCKED alert and stop
 *      MEDIUM -> move the money, save a FLAGGED alert for review
 *      LOW / UNCHECKED -> move the money
 *
 * The fraud call happens outside the money transaction on purpose: a slow HTTP call
 * shouldn't hold database locks, and a BLOCKED alert must be saved even though we throw.
 */
@Service
public class TransferService {

    private final AccountService accountService;
    private final FraudFeatureService featureService;
    private final FraudServiceClient fraudClient;
    private final FraudAlertRepository fraudAlertRepository;

    public TransferService(AccountService accountService, FraudFeatureService featureService,
                           FraudServiceClient fraudClient, FraudAlertRepository fraudAlertRepository) {
        this.accountService = accountService;
        this.featureService = featureService;
        this.fraudClient = fraudClient;
        this.fraudAlertRepository = fraudAlertRepository;
    }

    public TransferResponse transfer(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        if (fromAccountNumber.equals(toAccountNumber)) {
            throw new InvalidAmountException("Cannot transfer to the same account");
        }

        FraudAssessment assessment = fraudClient.assess(
                featureService.build(fromAccountNumber, toAccountNumber, amount));

        if (assessment.riskLevel() == RiskLevel.HIGH) {
            saveAlert(fromAccountNumber, toAccountNumber, amount, assessment, FraudDecision.BLOCKED);
            throw new TransferBlockedException(assessment.reasons());
        }

        accountService.transfer(fromAccountNumber, toAccountNumber, amount);

        if (assessment.riskLevel() == RiskLevel.MEDIUM) {
            saveAlert(fromAccountNumber, toAccountNumber, amount, assessment, FraudDecision.FLAGGED);
        }
        return new TransferResponse("COMPLETED", assessment.riskLevel(), assessment.riskScore(),
                assessment.reasons());
    }

    private void saveAlert(String from, String to, BigDecimal amount, FraudAssessment assessment,
                           FraudDecision decision) {
        fraudAlertRepository.save(new FraudAlertEntity(from, to, amount, assessment.riskScore(),
                assessment.riskLevel(), decision, assessment.reasons()));
    }
}
