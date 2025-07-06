package com.tuum.fsaccountsservice.service;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.fsaccountsservice.dto.requests.CreateAccountRequest;
import com.tuum.fsaccountsservice.dto.resonse.AccountResponse;
import com.tuum.common.types.Currency;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.mapper.BalanceMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private AccountService accountService;

    // @ParameterizedTest
    // @CsvSource({
    //     "ACC123, 12345, EE, EUR, 100.00",
    //     "ACC456, 67890, SE, SEK, 200.00",
    //     "ACC789, 11111, GB, GBP, 300.00"
    // })
    // void getAccount_Success(String accountId, String customerId, String country, Currency currency, BigDecimal amount) {
    //     // Given
    //     Account mockAccount = createMockAccount(accountId, customerId, country);
    //     Balance mockBalance = createMockBalance(accountId, currency, amount);
    //     mockAccount.setBalances(Arrays.asList(mockBalance));
        
    //     when(accountMapper.findAccountById(accountId)).thenReturn(mockAccount);
    //     when(balanceMapper.findBalancesByAccountId(accountId)).thenReturn(Arrays.asList(mockBalance));

    //     // When
    //     Account result = accountService.getAccount(accountId);

    //     // Then
    //     assertNotNull(result);
    //     assertEquals(accountId, result.getAccountId());
    //     assertEquals(customerId, result.getCustomerId());
    //     assertEquals(country, result.getCountry());
    //     assertEquals(1, result.getBalances().size());
    //     assertEquals(currency, result.getBalances().get(0).getCurrency());
    //     assertEquals(amount, result.getBalances().get(0).getAvailableAmount());

    //     verify(accountMapper, times(1)).findAccountById(accountId);
    //     verify(balanceMapper, times(1)).findBalancesByAccountId(accountId);
    // }

    @ParameterizedTest
    @ValueSource(strings = {"ACC123", "ACC456", "ACC789"})
    void getAccount_NotFound_ThrowsException(String accountId) {
        // Given
        when(accountMapper.findAccountById(accountId)).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            accountService.getAccount(accountId);
        });

        verify(accountMapper, times(1)).findAccountById(accountId);
        verify(balanceMapper, never()).findBalancesByAccountId(anyString());
    }

    @ParameterizedTest
    @CsvSource({
        "12345, 2",
        "67890, 2",
        "11111, 2"
    })
    void getAccountsByCustomerId_Success(String customerId, int expectedCount) {
        // Given
        List<Account> mockAccounts = Arrays.asList(
            createMockAccount("ACC1", customerId, "EE"),
            createMockAccount("ACC2", customerId, "SE")
        );
        when(accountMapper.findAccountsByCustomerId(customerId)).thenReturn(mockAccounts);

        // When
        List<Account> result = accountService.getAccountsByCustomerId(customerId);

        // Then
        assertNotNull(result);
        assertEquals(expectedCount, result.size());
        result.forEach(account -> assertEquals(customerId, account.getCustomerId()));

        verify(accountMapper, times(1)).findAccountsByCustomerId(customerId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "67890", "11111"})
    void getAccountsByCustomerId_EmptyList(String customerId) {
        // Given
        when(accountMapper.findAccountsByCustomerId(customerId)).thenReturn(Arrays.asList());

        // When
        List<Account> result = accountService.getAccountsByCustomerId(customerId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(accountMapper, times(1)).findAccountsByCustomerId(customerId);
    }

    @Test
    void getAllAccounts_Success() {
        // Given
        List<Account> mockAccounts = Arrays.asList(
            createMockAccount("ACC1", "12345", "EE"),
            createMockAccount("ACC2", "67890", "SE"),
            createMockAccount("ACC3", "11111", "GB")
        );
        when(accountMapper.findAllAccounts()).thenReturn(mockAccounts);

        // When
        List<Account> result = accountService.getAllAccounts();

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());

        verify(accountMapper, times(1)).findAllAccounts();
    }

    @ParameterizedTest
    @CsvSource({
        "ACC123, EUR, 100.00",
        "ACC456, SEK, 200.00",
        "ACC789, GBP, 300.00"
    })
    void getAccountBalances_Success(String accountId, Currency currency, BigDecimal amount) {
        // Given
        Balance mockBalance = createMockBalance(accountId, currency, amount);
        when(balanceMapper.findBalancesByAccountId(accountId)).thenReturn(Arrays.asList(mockBalance));

        // When
        List<Balance> result = accountService.getAccountBalances(accountId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(currency, result.get(0).getCurrency());
        assertEquals(amount, result.get(0).getAvailableAmount());

        verify(balanceMapper, times(1)).findBalancesByAccountId(accountId);
    }

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
        balance.setBalanceId(UUID.randomUUID().toString());
        balance.setAccountId(accountId);
        balance.setCurrency(currency);
        balance.setAvailableAmount(amount);
        balance.setVersionNumber(1);
        balance.setCreatedAt(LocalDateTime.now());
        balance.setUpdatedAt(LocalDateTime.now());
        return balance;
    }
}