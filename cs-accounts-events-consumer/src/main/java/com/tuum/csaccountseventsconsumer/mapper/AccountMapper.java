package com.tuum.csaccountseventsconsumer.mapper;

import com.tuum.common.domain.entities.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {
    
    void insertAccount(Account account);
    
    Account findAccountById(@Param("accountId") String accountId);
    
    boolean existsAccountById(@Param("accountId") String accountId);
    
    boolean existsAccountByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
} 