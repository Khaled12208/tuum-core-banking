package com.tuum.csaccountseventsconsumer.mapper;

import com.tuum.common.domain.entities.Balance;
import com.tuum.common.types.Currency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface BalanceMapper {
    
    void insertBalance(Balance balance);
    
    List<Balance> findBalancesByAccountId(@Param("accountId") String accountId);
    
    boolean existsBalance(@Param("accountId") String accountId, @Param("currency") Currency currency);
    
    Balance findBalanceByAccountIdAndCurrency(@Param("accountId") String accountId, @Param("currency") Currency currency);
    
    int updateBalance(@Param("balance") Balance balance, @Param("oldVersionNumber") int oldVersionNumber);
} 