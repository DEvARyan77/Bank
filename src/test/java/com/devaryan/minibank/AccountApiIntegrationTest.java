package com.devaryan.minibank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Full flow through HTTP -> controller -> service -> H2 database
@SpringBootTest
class AccountApiIntegrationTest {

    private static final Pattern ACCOUNT_NUMBER = Pattern.compile("\"accountNumber\"\\s*:\\s*\"(MB\\d{10})\"");

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void depositTransferAndHistory() throws Exception {
        String a = createAccount("Dev Aryan");
        String b = createAccount("Test User");

        mockMvc.perform(post("/api/accounts/" + a + "/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 5000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", closeTo(5000.0, 0.001), Double.class));

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromAccount\":\"" + a + "\",\"toAccount\":\"" + b + "\",\"amount\":1500}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/accounts/" + a))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", closeTo(3500.0, 0.001), Double.class));

        mockMvc.perform(get("/api/accounts/" + a + "/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].type").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$.content[1].type").value("DEPOSIT"));
    }

    @Test
    void overdraftReturns400() throws Exception {
        String a = createAccount("Poor User");

        mockMvc.perform(post("/api/accounts/" + a + "/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidInputReturns400() throws Exception {
        mockMvc.perform(post("/api/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accountHolder\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownAccountReturns404() throws Exception {
        mockMvc.perform(get("/api/accounts/MB0000000000"))
                .andExpect(status().isNotFound());
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
