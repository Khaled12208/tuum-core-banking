package com.tuum.fsaccountsservice.service;

import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAccount() {
        // Arrange
        String accountId = "ACC123";
        Account mockAccount = new Account();
        mockAccount.setAccountId(accountId);
        when(accountMapper.findAccountById(accountId)).thenReturn(mockAccount);

        // Act
        Account result = accountService.getAccount(accountId);

        // Assert
        assertNotNull(result);
        assertEquals(accountId, result.getAccountId());
        verify(accountMapper, times(1)).findAccountById(accountId);
    }
} 