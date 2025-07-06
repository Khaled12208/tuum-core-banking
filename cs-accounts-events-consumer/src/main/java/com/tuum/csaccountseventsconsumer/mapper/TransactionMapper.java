package com.tuum.csaccountseventsconsumer.mapper;

import com.tuum.common.domain.entities.Transaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionMapper {
    
    void insertTransaction(Transaction transaction);
    
    Transaction findTransactionById(@Param("transactionId") String transactionId);
    
    boolean existsTransactionById(@Param("transactionId") String transactionId);
    
    boolean existsTransactionByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
    
    Transaction findTransactionByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
} 