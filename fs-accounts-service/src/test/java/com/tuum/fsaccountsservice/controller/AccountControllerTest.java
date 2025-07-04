package com.tuum.fsaccountsservice.controller;

import com.tuum.fsaccountsservice.model.Account;
import com.tuum.fsaccountsservice.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@TestPropertySource(properties = {
    "server.servlet.context-path=/api/v1"
})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void testGetAccount_returnsAccount() throws Exception {
        // Arrange
        String accountId = "ACC123";
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        mockAccount.setCustomerId("CUST123");
        mockAccount.setCountry("EE");
        when(accountService.getAccount(accountId)).thenReturn(mockAccount);

        // Act & Assert
        mockMvc.perform(get("/accounts/" + accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.customerId").value("CUST123"))
                .andExpect(jsonPath("$.country").value("EE"));
    }
} 