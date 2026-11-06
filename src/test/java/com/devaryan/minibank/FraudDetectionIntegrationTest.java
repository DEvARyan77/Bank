package com.devaryan.minibank;

import com.devaryan.minibank.client.FraudServiceClient;
import com.devaryan.minibank.dto.FraudAssessment;
import com.devaryan.minibank.entity.RiskLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.closeTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The Python service is replaced with a mock that says "HIGH risk", to test the blocking flow end to end
@SpringBootTest
class FraudDetectionIntegrationTest {

    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("\"accountNumber\"\\s*:\\s*\"(MB\\d{10})\"");

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FraudServiceClient fraudClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void highRiskTransferIsBlockedAndBalanceUnchanged() throws Exception {
        when(fraudClient.assess(any())).thenReturn(
                new FraudAssessment(0.97, RiskLevel.HIGH, List.of("Very large amount"), "test"));
        String a = createAccount("Sender");
        String b = createAccount("Receiver");
        mockMvc.perform(post("/api/accounts/" + a + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 300000}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccount\":\"" + a + "\",\"toAccount\":\"" + b + "\",\"amount\":250000}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.reasons[0]").value("Very large amount"));

        mockMvc.perform(get("/api/accounts/" + a))
                .andExpect(jsonPath("$.balance", closeTo(300000.0, 0.001), Double.class));

        mockMvc.perform(get("/api/fraud-alerts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].fromAccountNumber").value(a))
                .andExpect(jsonPath("$.content[0].decision").value("BLOCKED"));
    }

    private String createAccount(String holder) throws Exception {
        String body = mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountHolder\":\"" + holder + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = ACCOUNT_NUMBER.matcher(body);
        if (!matcher.find()) {
            throw new AssertionError("No account number in response: " + body);
        }
        return matcher.group(1);
    }
}
