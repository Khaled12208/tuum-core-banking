package com.tuum.fsaccountsservice.mapper;

import com.tuum.common.domain.entities.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AccountMapper {
    
    Account findAccountById(@Param("accountId") String accountId);
        
    List<Account> findAllAccounts();
    
    List<Account> findAccountsByCurrencyAndAccountId(@Param("currency") String currency, @Param("accountId") String accountId);

    List<Account> findAccountsByCustomerId(String customerId);

    boolean existsAccountById(@Param("accountId") String accountId);
    boolean existsAccountByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    Account findAccountByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
} 