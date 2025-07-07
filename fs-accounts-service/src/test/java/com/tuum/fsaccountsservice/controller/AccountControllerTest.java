package com.tuum.fsaccountsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.common.exception.BusinessException;
import com.tuum.common.exception.ResourceNotFoundException;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.fsaccountsservice.service.AccountService;
import com.tuum.fsaccountsservice.utils.TestConstants;
import com.tuum.fsaccountsservice.utils.TestDataBuilder;
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
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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
                .setControllerAdvice(new TestExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @ParameterizedTest
    @CsvSource({
        "CUST001, EE, EUR,USD",
        "CUST002, SE, SEK,GBP",
        "CUST003, GB, GBP,EUR",
        "CUST004, FI, EUR,USD",
        "CUST005, NO, EUR,GBP"
    })
    void testCreateAccount_Success(String customerId, String country, String currency1, String currency2) throws Exception {
        CreateAccountRequest request = TestDataBuilder.createAccountRequest()
                .customerId(customerId)
                .country(country)
                .currencies(Arrays.asList(Currency.valueOf(currency1), Currency.valueOf(currency2)))
                .build();

        AccountResponse mockResponse = TestDataBuilder.accountResponse()
                .accountId(TestConstants.DEFAULT_ACCOUNT_ID)
                .customerId(customerId)
                .country(country)
                .balances(createMockBalanceResponses(currency1, currency2))
                .build();

        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestConstants.IDEMPOTENCY_KEY_HEADER, TestConstants.TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(TestConstants.DEFAULT_ACCOUNT_ID))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.country").value(country))
                .andExpect(jsonPath("$.balances").isArray())
                .andExpect(jsonPath("$.balances.length()").value(2));

        verify(accountService, times(1)).createAccount(any(CreateAccountRequest.class), eq(TestConstants.TEST_IDEMPOTENCY_KEY));
    }

    @Test
    void testCreateAccount_MissingIdempotencyKey_ThrowsException() throws Exception {
        CreateAccountRequest request = TestDataBuilder.createAccountRequest().build();
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACC123", "ACC456", "ACC789", "ACC101", "ACC202"})
    void testGetAccount_Success(String accountId) throws Exception {
        Account mockAccount = TestDataBuilder.account()
                .accountId(accountId)
                .customerId(TestConstants.DEFAULT_CUSTOMER_ID)
                .country(TestConstants.DEFAULT_COUNTRY)
                .balances(createMockBalances(accountId))
                .build();

        when(accountService.getAccount(accountId)).thenReturn(mockAccount);

        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.customerId").value(TestConstants.DEFAULT_CUSTOMER_ID))
                .andExpect(jsonPath("$.country").value(TestConstants.DEFAULT_COUNTRY))
                .andExpect(jsonPath("$.balances").isArray());

        verify(accountService, times(1)).getAccount(accountId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ACC123", "ACC456", "ACC789", "ACC101", "ACC202"})
    void testGetAccount_NotFound_ThrowsException(String accountId) throws Exception {
        when(accountService.getAccount(accountId))
                .thenThrow(new ResourceNotFoundException(TestConstants.ACCOUNT_NOT_FOUND_MESSAGE + accountId));
        mockMvc.perform(get("/accounts/{accountId}", accountId))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).getAccount(accountId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"CUST001", "CUST002", "CUST003", "CUST004", "CUST005"})
    void testGetAccountsByCustomerId_Success(String customerId) throws Exception {
        List<Account> mockAccounts = Arrays.asList(
                TestDataBuilder.account()
                        .accountId("ACC1")
                        .customerId(customerId)
                        .country("EE")
                        .build(),
                TestDataBuilder.account()
                        .accountId("ACC2")
                        .customerId(customerId)
                        .country("SE")
                        .build()
        );

        when(accountService.getAccountsByCustomerId(customerId)).thenReturn(mockAccounts);

        mockMvc.perform(get("/accounts/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value(customerId))
                .andExpect(jsonPath("$[1].customerId").value(customerId));

        verify(accountService, times(1)).getAccountsByCustomerId(customerId);
    }

    @Test
    void testGetAllAccounts_Success() throws Exception {
        List<Account> mockAccounts = Arrays.asList(
                TestDataBuilder.account()
                        .accountId("ACC1")
                        .customerId("CUST001")
                        .country("EE")
                        .build(),
                TestDataBuilder.account()
                        .accountId("ACC2")
                        .customerId("CUST002")
                        .country("SE")
                        .build(),
                TestDataBuilder.account()
                        .accountId("ACC3")
                        .customerId("CUST003")
                        .country("GB")
                        .build()
        );

        when(accountService.getAllAccounts()).thenReturn(mockAccounts);
        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(accountService, times(1)).getAllAccounts();
    }

    @Test
    void testCreateAccount_ServiceThrowsBusinessException_ReturnsError() throws Exception {
        CreateAccountRequest request = TestDataBuilder.createAccountRequest().build();

        when(accountService.createAccount(any(CreateAccountRequest.class), anyString()))
                .thenThrow(new BusinessException("Account creation failed"));
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(TestConstants.IDEMPOTENCY_KEY_HEADER, TestConstants.TEST_IDEMPOTENCY_KEY)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    private List<Balance> createMockBalances(String accountId) {
        return Arrays.asList(
                TestDataBuilder.balance()
                        .accountId(accountId)
                        .currency(Currency.EUR)
                        .availableAmount(BigDecimal.valueOf(1000.00))
                        .build(),
                TestDataBuilder.balance()
                        .accountId(accountId)
                        .currency(Currency.USD)
                        .availableAmount(BigDecimal.valueOf(500.00))
                        .build()
        );
    }

    private List<com.tuum.fsaccountsservice.dto.resonse.BalanceResponse> createMockBalanceResponses(String currency1, String currency2) {
        return Arrays.asList(
                TestDataBuilder.balanceResponse()
                        .currency(Currency.valueOf(currency1))
                        .availableAmount(BigDecimal.valueOf(1000.00))
                        .build(),
                TestDataBuilder.balanceResponse()
                        .currency(Currency.valueOf(currency2))
                        .availableAmount(BigDecimal.valueOf(500.00))
                        .build()
        );
    }

    @ControllerAdvice
    static class TestExceptionHandler {
        
        @ExceptionHandler(BusinessException.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ResponseBody
        public String handleBusinessException(BusinessException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
        
        @ExceptionHandler(ResourceNotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        @ResponseBody
        public String handleResourceNotFoundException(ResourceNotFoundException e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }
}