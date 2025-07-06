package com.tuum.fsaccountsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.fsaccountsservice.service.AccountService;
import com.tuum.common.types.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .build();
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @CsvSource({
        "CUST001, EE, EUR,USD",
        "CUST002, SE, SEK,GBP",
        "CUST003, GB, GBP,EUR"
    })
    void testCreateAccount_Success(String customerId, String country, String currency1, String currency2) throws Exception {
        // Arrange
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(customerId);
        request.setCountry(country);
        request.setCurrencies(Arrays.asList(Currency.valueOf(currency1), Currency.valueOf(currency2)));

        AccountResponse mockResponse = createMockAccountResponse(customerId, country);

        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Idempotency-Key", "test-key-123")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value("ACC123"))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.country").value(country))
                .andExpect(jsonPath("$.balances").isArray())
                .andExpect(jsonPath("$.balances.length()").value(2));

        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), eq("test-key-123"));
    }

    // @ParameterizedTest
    // @ValueSource(strings = {"ACC123", "ACC456", "ACC789"})
    // void testGetAccount_Success(String accountId) throws Exception {
    //     // Arrange
    //     Account mockAccount = createMockAccount(accountId, "CUST001", "EE");
    //     AccountResponse mockResponse = createMockAccountResponse("CUST001", "EE");
        
    //     when(accountService.getAccount(accountId)).thenReturn(mockAccount);

    //     // Act & Assert
    //     mockMvc.perform(get("/accounts/" + accountId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$.accountId").value(accountId))
    //             .andExpect(jsonPath("$.customerId").value("CUST001"))
    //             .andExpect(jsonPath("$.country").value("EE"));

    //     verify(accountService, times(1)).getAccount(accountId);
    // }

    // @ParameterizedTest
    // @CsvSource({
    //     "CUST001, 2",
    //     "CUST002, 1",
    //     "CUST003, 3"
    // })
    // void testGetAccountsByCustomerId_Success(String customerId, int expectedCount) throws Exception {
    //     // Arrange
    //     List<Account> mockAccounts = Arrays.asList(
    //         createMockAccount("ACC1", customerId, "EE"),
    //         createMockAccount("ACC2", customerId, "SE")
    //     );
        
    //     when(accountService.getAccountsByCustomerId(customerId)).thenReturn(mockAccounts);

    //     // Act & Assert
    //     mockMvc.perform(get("/accounts/customer/" + customerId))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$").isArray())
    //             .andExpect(jsonPath("$.length()").value(expectedCount));

    //     verify(accountService, times(1)).getAccountsByCustomerId(customerId);
    // }

    // @Test
    // void testGetAllAccounts_Success() throws Exception {
    //     // Arrange
    //     List<Account> mockAccounts = Arrays.asList(
    //         createMockAccount("ACC1", "CUST001", "EE"),
    //         createMockAccount("ACC2", "CUST002", "SE"),
    //         createMockAccount("ACC3", "CUST003", "GB")
    //     );
        
    //     when(accountService.getAllAccounts()).thenReturn(mockAccounts);

    //     // Act & Assert
    //     mockMvc.perform(get("/accounts"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$").isArray())
    //             .andExpect(jsonPath("$.length()").value(3));

    //     verify(accountService, times(1)).getAllAccounts();
    // }

    // @ParameterizedTest
    // @ValueSource(strings = {"ACC123", "ACC456", "ACC789"})
    // void testGetAccountBalances_Success(String accountId) throws Exception {
    //     // Arrange
    //     List<Balance> mockBalances = Arrays.asList(
    //         createMockBalance(accountId, Currency.EUR, new BigDecimal("100.00")),
    //         createMockBalance(accountId, Currency.USD, new BigDecimal("200.00"))
    //     );
        
    //     when(accountService.getAccountBalances(accountId)).thenReturn(mockBalances);

    //     // Act & Assert
    //     mockMvc.perform(get("/accounts/" + accountId + "/balances"))
    //             .andExpect(status().isOk())
    //             .andExpect(jsonPath("$").isArray())
    //             .andExpect(jsonPath("$.length()").value(2));

    //     verify(accountService, times(1)).getAccountBalances(accountId);
    // }

    // Helper methods
    private Account createMockAccount(String accountId, String customerId, String country) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setCustomerId(customerId);
        account.setCountry(country);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private Balance createMockBalance(String accountId, Currency currency, BigDecimal amount) {
        Balance balance = new Balance();
        balance.setBalanceId("BAL123");
        balance.setAccountId(accountId);
        balance.setCurrency(currency);
        balance.setAvailableAmount(amount);
        balance.setVersionNumber(1);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());
        return balance;
    }

    private AccountResponse createMockAccountResponse(String customerId, String country) {
        AccountResponse response = new AccountResponse();
        response.setAccountId("ACC123");
        response.setCustomerId(customerId);
        response.setCountry(country);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());

        com.tuum.fsaccountsservice.dto.resonse.BalanceResponse balance1 = new com.tuum.fsaccountsservice.dto.resonse.BalanceResponse();
        balance1.setBalanceId("BAL123");
        balance1.setAccountId("ACC123");
        balance1.setCurrency(Currency.EUR);
        balance1.setAvailableAmount(BigDecimal.valueOf(100.00));
        balance1.setCreatedAt(LocalDateTime.now());
        balance1.setUpdatedAt(LocalDateTime.now());

        com.tuum.fsaccountsservice.dto.resonse.BalanceResponse balance2 = new com.tuum.fsaccountsservice.dto.resonse.BalanceResponse();
        balance2.setBalanceId("BAL124");
        balance2.setAccountId("ACC123");
        balance2.setCurrency(Currency.USD);
        balance2.setAvailableAmount(BigDecimal.valueOf(50.00));
        balance2.setCreatedAt(LocalDateTime.now());
        balance2.setUpdatedAt(LocalDateTime.now());

        response.setBalances(Arrays.asList(balance1, balance2));
        return response;
    }
}