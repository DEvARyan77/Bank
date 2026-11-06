package com.devaryan.minibank.service;

import com.devaryan.minibank.client.FraudServiceClient;
import com.devaryan.minibank.dto.FraudAssessment;
import com.devaryan.minibank.dto.FraudFeatures;
import com.devaryan.minibank.dto.TransferResponse;
import com.devaryan.minibank.entity.FraudAlertEntity;
import com.devaryan.minibank.entity.FraudDecision;
import com.devaryan.minibank.entity.RiskLevel;
import com.devaryan.minibank.exception.InvalidAmountException;
import com.devaryan.minibank.exception.TransferBlockedException;
import com.devaryan.minibank.repository.FraudAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    private static final String FROM = "MB0000000001";
    private static final String TO = "MB0000000002";
    private static final BigDecimal AMOUNT = new BigDecimal("1000");

    @Mock
    private AccountService accountService;

    @Mock
    private FraudFeatureService featureService;

    @Mock
    private FraudServiceClient fraudClient;

    @Mock
    private FraudAlertRepository fraudAlertRepository;

    @InjectMocks
    private TransferService transferService;

    private final FraudFeatures features = new FraudFeatures(AMOUNT, 14, 100, 1, 900, false);

    @BeforeEach
    void setUp() {
        when(featureService.build(FROM, TO, AMOUNT)).thenReturn(features);
    }

    @Test
    void highRiskTransferIsBlockedAndAlertSaved() {
        when(fraudClient.assess(features)).thenReturn(
                new FraudAssessment(0.95, RiskLevel.HIGH, List.of("Very large amount"), "v1"));

        TransferBlockedException ex = assertThrows(TransferBlockedException.class,
                () -> transferService.transfer(FROM, TO, AMOUNT));

        assertEquals(List.of("Very large amount"), ex.getReasons());
        verify(accountService, never()).transfer(anyString(), anyString(), any());
        ArgumentCaptor<FraudAlertEntity> alert = ArgumentCaptor.forClass(FraudAlertEntity.class);
        verify(fraudAlertRepository).save(alert.capture());
        assertEquals(FraudDecision.BLOCKED, alert.getValue().getDecision());
    }

    @Test
    void mediumRiskTransferGoesThroughAndIsFlagged() {
        when(fraudClient.assess(features)).thenReturn(
                new FraudAssessment(0.6, RiskLevel.MEDIUM, List.of("Large amount to a new recipient"), "v1"));

        TransferResponse response = transferService.transfer(FROM, TO, AMOUNT);

        assertEquals("COMPLETED", response.status());
        assertEquals(RiskLevel.MEDIUM, response.riskLevel());
        verify(accountService).transfer(FROM, TO, AMOUNT);
        ArgumentCaptor<FraudAlertEntity> alert = ArgumentCaptor.forClass(FraudAlertEntity.class);
        verify(fraudAlertRepository).save(alert.capture());
        assertEquals(FraudDecision.FLAGGED, alert.getValue().getDecision());
    }

    @Test
    void lowRiskTransferGoesThroughWithoutAlert() {
        when(fraudClient.assess(features)).thenReturn(new FraudAssessment(0.1, RiskLevel.LOW, List.of(), "v1"));

        TransferResponse response = transferService.transfer(FROM, TO, AMOUNT);

        assertEquals(RiskLevel.LOW, response.riskLevel());
        verify(accountService).transfer(FROM, TO, AMOUNT);
        verifyNoInteractions(fraudAlertRepository);
    }

    @Test
    void uncheckedTransferStillGoesThrough() {
        when(fraudClient.assess(features)).thenReturn(FraudAssessment.unchecked("Fraud service unavailable"));

        TransferResponse response = transferService.transfer(FROM, TO, AMOUNT);

        assertEquals(RiskLevel.UNCHECKED, response.riskLevel());
        verify(accountService).transfer(FROM, TO, AMOUNT);
        verifyNoInteractions(fraudAlertRepository);
    }
}
