package com.tuum.csaccountseventsconsumer.mapper;

import com.tuum.common.domain.entities.ProcessedMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProcessedMessageMapper {
    
    void insertProcessedMessage(ProcessedMessage processedMessage);
    
    boolean existsProcessedMessage(@Param("messageId") String messageId);
    
    boolean existsProcessedMessageByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
} 