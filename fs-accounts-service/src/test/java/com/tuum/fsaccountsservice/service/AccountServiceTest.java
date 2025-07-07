package com.tuum.fsaccountsservice.service;

import com.tuum.common.domain.entities.Account;
import com.tuum.common.domain.entities.Balance;
import com.tuum.fsaccountsservice.mapper.AccountMapper;
import com.tuum.fsaccountsservice.mapper.BalanceMapper;
import com.tuum.fsaccountsservice.utils.TestConstants;
import com.tuum.fsaccountsservice.utils.TestDataBuilder;
import com.tuum.common.types.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private BalanceMapper balanceMapper;

    @InjectMocks
    private AccountService accountService;

    @Test
    void testGetAllAccounts_Success() {
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
        when(accountMapper.findAllAccounts()).thenReturn(mockAccounts);
        List<Account> result = accountService.getAllAccounts();
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(accountMapper, times(1)).findAllAccounts();
    }

}