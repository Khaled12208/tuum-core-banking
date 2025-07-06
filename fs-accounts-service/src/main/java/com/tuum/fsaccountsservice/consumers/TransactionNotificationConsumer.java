package com.tuum.fsaccountsservice.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.adapter.AmqpMessageAdapter;
import com.tuum.common.dto.mq.CreateTransactionEvent;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.fsaccountsservice.service.TransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Slf4j
public class TransactionNotificationConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionService transactionService;
    private final ConcurrentMap<String, ErrorNotification> errorCache = new ConcurrentHashMap<>();

    public TransactionNotificationConsumer(SimpMessagingTemplate messagingTemplate,
                                            ObjectMapper objectMapper,
                                            @Lazy TransactionService transactionService) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.transactionService = transactionService;
    }

    @RabbitListener(queues = "#{T(com.tuum.common.types.RabbitMQConfig).TRANSACTIONS_NOTIFICATIONS_QUEUE.getValue()}")
    public void handleTransactionNotification(Message message) {
        try {
            MQMessageData messageData = new AmqpMessageAdapter().adapt(message);
            CreateTransactionEvent event = objectMapper.readValue(messageData.getMessageBody(), CreateTransactionEvent.class);
            log.info("Raw message body: {}", messageData.getMessageBody());
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            log.info("Parsed TransactionProcessedEvent: requestId={}, idempotencyKey={}, status={}", messageData.getRequestId(), messageData.getIdempotencyKey(), messageData.getStatus());
            Map<String, Object> notification = new HashMap<>();
            notification.put("transactionId", event.getTransactionId());
            notification.put("accountId", event.getAccountId());
            notification.put("amount", event.getAmount());
            notification.put("currency", event.getCurrency());
            notification.put("direction", event.getDirection());
            notification.put("description", event.getDescription());
            notification.put("status", messageData.getStatus());
            notification.put("processedAt", event.getCreatedAt());
            notification.put("timestamp", System.currentTimeMillis());
            transactionService.completeTransaction(event, messageData);
            log.info("Successfully completed pending transaction with idempotencyKey: {}", messageData.getIdempotencyKey());
            messagingTemplate.convertAndSend("/topic/transactions", notification);
            log.info("Sent detailed transaction success notification: {}", event.getTransactionId());
        } catch (Exception e) {
            log.error("Error processing transaction notification: {}", e.getMessage(), e);
            log.error("Failed to deserialize message: {}", new String(message.getBody()));
        }
    }

    @RabbitListener(queues = "#{T(com.tuum.common.types.RabbitMQConfig).TRANSACTIONS_ERRORS_QUEUE.getValue()}")
    public void handleTransactionError(Message message) {
        try {
            MQMessageData messageData = new AmqpMessageAdapter().adapt(message);
            ErrorNotification errorNotification = objectMapper.readValue(messageData.getMessageBody(), ErrorNotification.class);
            String idempotencyKey = messageData.getIdempotencyKey();
            
            // Store error in cache for EventPublisherService
            errorCache.put(idempotencyKey, errorNotification);
            
            log.info("Raw message body: {}", messageData.getMessageBody());
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            log.info("Parsed TransactionErrorEvent: requestId={}, idempotencyKey={}, status={}", messageData.getRequestId(), messageData.getIdempotencyKey(), messageData.getStatus());
            Map<String, Object> notification = new HashMap<>();
            notification.put("requestId", errorNotification.getRequestId());
            notification.put("error-message", errorNotification.getErrorMessage());
            notification.put("error-code", errorNotification.getErrorCode());
            notification.put("timestamp", System.currentTimeMillis());
            transactionService.completeTransaction(errorNotification, messageData);
            messagingTemplate.convertAndSend("/topic/transactions", notification);
            log.error("Transaction processing error for idempotency key {}: {} - {}", idempotencyKey, errorNotification.getErrorCode(), errorNotification.getErrorMessage());
        } catch (Exception e) {
            log.error("Error processing transaction error message: {}", e.getMessage(), e);
            log.error("Failed to deserialize message: {}", new String(message.getBody()));
        }
    }

    public ErrorNotification getErrorForIdempotencyKey(String idempotencyKey) {
        return errorCache.get(idempotencyKey);
    }

    public void removeErrorFromCache(String idempotencyKey) {
        errorCache.remove(idempotencyKey);
    }
}

