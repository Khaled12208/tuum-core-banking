package com.tuum.fsaccountsservice.mapper;

import com.tuum.fsaccountsservice.model.Balance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BalanceMapper {
    
    Balance findBalanceById(@Param("balanceId") String balanceId);
    
    List<Balance> findBalancesByAccountId(@Param("accountId") String accountId);
    
    Balance findBalanceByAccountIdAndCurrency(@Param("accountId") String accountId, @Param("currency") String currency);
} 