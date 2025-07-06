package com.tuum.common.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedMessage {

    private String messageId;
    private String messageType;
    private String idempotencyKey;
    private LocalDateTime processedAt;
    private String resultData;
}