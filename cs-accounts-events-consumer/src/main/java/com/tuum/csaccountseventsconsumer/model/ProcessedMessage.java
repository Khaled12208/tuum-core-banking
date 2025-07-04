package com.tuum.csaccountseventsconsumer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMessage {
    
    private String messageId;
    private String messageType;
    private LocalDateTime processedAt;
    private String resultData;
} 