package com.tuum.csaccountseventsconsumer.mapper;

import com.tuum.csaccountseventsconsumer.model.Balance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BalanceMapper {
    
    void insertBalance(Balance balance);
    
    List<Balance> findBalancesByAccountId(@Param("accountId") String accountId);
    
    boolean existsBalance(@Param("accountId") String accountId, @Param("currency") String currency);
    
    Balance findBalanceByAccountIdAndCurrency(@Param("accountId") String accountId, @Param("currency") String currency);
    
    void updateBalance(Balance balance);
} 