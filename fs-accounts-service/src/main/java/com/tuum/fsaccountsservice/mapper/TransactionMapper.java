package com.tuum.fsaccountsservice.mapper;

import com.tuum.common.domain.entities.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TransactionMapper {
    
    Transaction findTransactionById(@Param("transactionId") String transactionId);
    
    List<Transaction> findTransactionsByAccountId(@Param("accountId") String accountId);
    
    List<Transaction> findAllTransactions();
    
    boolean existsTransactionByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    
    Transaction findTransactionByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
} 