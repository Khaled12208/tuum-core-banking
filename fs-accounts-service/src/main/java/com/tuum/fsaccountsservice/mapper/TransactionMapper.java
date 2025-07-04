package com.tuum.fsaccountsservice.mapper;

import com.tuum.fsaccountsservice.model.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {
    
    Transaction findTransactionById(@Param("transactionId") String transactionId);
    
    List<Transaction> findTransactionsByAccountId(@Param("accountId") String accountId);
    
    List<Transaction> findAllTransactions();
} 