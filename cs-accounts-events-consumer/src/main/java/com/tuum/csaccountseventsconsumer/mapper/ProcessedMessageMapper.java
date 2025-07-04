package com.tuum.csaccountseventsconsumer.mapper;

import com.tuum.csaccountseventsconsumer.model.ProcessedMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProcessedMessageMapper {
    
    void insertProcessedMessage(ProcessedMessage processedMessage);
    
    boolean existsProcessedMessage(@Param("messageId") String messageId);
} 