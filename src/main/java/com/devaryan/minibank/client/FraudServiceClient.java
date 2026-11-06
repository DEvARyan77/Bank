package com.devaryan.minibank.client;

import com.devaryan.minibank.dto.FraudAssessment;
import com.devaryan.minibank.dto.FraudFeatures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

// Calls the Python AI fraud service. If it is down, transfers still work ("fail open") but are marked UNCHECKED.
@Component
public class FraudServiceClient {

    private static final Logger log = LoggerFactory.getLogger(FraudServiceClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public FraudServiceClient(@Value("${fraud.service.url:http://localhost:8000}") String baseUrl,
                              @Value("${fraud.enabled:true}") boolean enabled) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(1)).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(2));
        this.restClient = RestClient.builder().baseUrl(baseUrl).requestFactory(requestFactory).build();
        this.enabled = enabled;
    }

    public FraudAssessment assess(FraudFeatures features) {
        if (!enabled) {
            return FraudAssessment.unchecked("Fraud detection is turned off");
        }
        try {
            FraudAssessment assessment = restClient.post()
                    .uri("/score")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(features)
                    .retrieve()
                    .body(FraudAssessment.class);
            return assessment != null ? assessment : FraudAssessment.unchecked("Empty response from fraud service");
        } catch (RestClientException e) {
            log.warn("Fraud service unavailable, transfer not scored: {}", e.getMessage());
            return FraudAssessment.unchecked("Fraud service unavailable");
        }
    }
}
