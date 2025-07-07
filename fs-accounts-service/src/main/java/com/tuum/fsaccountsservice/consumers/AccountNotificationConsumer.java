package com.tuum.fsaccountsservice.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.adapter.AmqpMessageAdapter;
import com.tuum.common.dto.ErrorResponse;
import com.tuum.common.dto.mq.CreateAccountEvent;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.fsaccountsservice.service.AccountService;
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
public class AccountNotificationConsumer {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final ConcurrentMap<String, ErrorNotification> errorCache = new ConcurrentHashMap<>();

    public AccountNotificationConsumer(SimpMessagingTemplate messagingTemplate,
                                        ObjectMapper objectMapper,
                                        @Lazy AccountService accountService) {
        this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
        this.accountService = accountService;
    }

    @RabbitListener(queues = "#{T(com.tuum.common.types.RabbitMQConfig).ACCOUNTS_NOTIFICATIONS_QUEUE.getValue()}")
    public void handleAccountNotification(Message message) {
        try {
            MQMessageData messageData = new AmqpMessageAdapter().adapt(message);
            CreateAccountEvent event = objectMapper.readValue(messageData.getMessageBody(), CreateAccountEvent.class);
            log.info("Raw message body: {}", messageData.getMessageBody());
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            log.info("Parsed AccountProcessedEvent: requestId={}, idempotencyKey={}, status={}", messageData.getRequestId(), messageData.getIdempotencyKey(), messageData.getStatus());
            Map<String, Object> notification = new HashMap<>();
            notification.put("accountId", event.getAccountId());
            notification.put("customerId", event.getCustomerId());
            notification.put("country", event.getCountry());
            notification.put("balances", event.getBalances());
            notification.put("status", messageData.getStatus());
            notification.put("processedAt", event.getCreatedAt());
            notification.put("timestamp", System.currentTimeMillis());
            accountService.completeAccount(event, messageData);
            log.info("Successfully completed pending account with idempotencyKey: {}", messageData.getIdempotencyKey());
            messagingTemplate.convertAndSend("/topic/accounts", notification);
            log.info("Sent detailed account success notification: {}", event.getAccountId());
        } catch (Exception e) {
            log.error("Error processing account notification: {}", e.getMessage(), e);
            log.error("Failed to deserialize message: {}", new String(message.getBody()));
        }
    }

    @RabbitListener(queues = "#{T(com.tuum.common.types.RabbitMQConfig).ACCOUNTS_ERRORS_QUEUE.getValue()}")
    public void handleAccountError(Message message) {
        try {
            MQMessageData messageData = new AmqpMessageAdapter().adapt(message);
            ErrorNotification errorNotification = objectMapper.readValue(messageData.getMessageBody(), ErrorNotification.class);
            String idempotencyKey = messageData.getIdempotencyKey();
            
            errorCache.put(idempotencyKey, errorNotification);
            log.info("Raw message body: {}", messageData.getMessageBody());
            log.info("Message headers: {}", message.getMessageProperties().getHeaders());
            log.info("Parsed AccountErrorEvent: requestId={}, idempotencyKey={}, status={}", messageData.getRequestId(), messageData.getIdempotencyKey(), messageData.getStatus());
            Map<String, Object> notification = new HashMap<>();
            notification.put("requestId", errorNotification.getRequestId());
            notification.put("error-message", errorNotification.getErrorMessage());
            notification.put("error-code", errorNotification.getErrorCode());
            notification.put("timestamp", System.currentTimeMillis());
            accountService.completeAccount(errorNotification, messageData);
            messagingTemplate.convertAndSend("/topic/accounts", notification);
            log.error("Account processing error for idempotency key {}: {} - {}", idempotencyKey, errorNotification.getErrorCode(), errorNotification.getErrorMessage());
        } catch (Exception e) {
            log.error("Error processing account error message: {}", e.getMessage(), e);
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


