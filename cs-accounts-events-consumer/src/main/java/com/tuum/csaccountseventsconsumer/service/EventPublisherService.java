package com.tuum.csaccountseventsconsumer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final AmqpTemplate amqpTemplate;

    // Event Topics
    private static final String ACCOUNT_EVENTS_TOPIC = "account-events-topic";
    private static final String TRANSACTIONS_EVENTS_TOPIC = "transactions-events-topic";
    
    // Error Topics
    private static final String ACCOUNT_ERROR_TOPIC = "account-error-topic";
    private static final String TRANSACTIONS_ERROR_TOPIC = "transactions-error-topic";
    
    // Notification Topics
    private static final String ACCOUNT_NOTIFICATION_TOPIC = "account-notification-topic";
    private static final String TRANSACTIONS_NOTIFICATION_TOPIC = "transactions-notification-topic";

    /**
     * Publish account event to account-events-topic
     */
    public void publishAccountEvent(String routingKey, Object event) {
        try {
            amqpTemplate.convertAndSend(ACCOUNT_EVENTS_TOPIC, routingKey, event);
            log.info("Published account event to routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish account event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Publish transaction event to transactions-events-topic
     */
    public void publishTransactionEvent(String routingKey, Object event) {
        try {
            amqpTemplate.convertAndSend(TRANSACTIONS_EVENTS_TOPIC, routingKey, event);
            log.info("Published transaction event to routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish transaction event: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Publish account error to account-error-topic
     */
    public void publishAccountError(String routingKey, Object error) {
        try {
            amqpTemplate.convertAndSend(ACCOUNT_ERROR_TOPIC, routingKey, error);
            log.info("Published account error to routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish account error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Publish transaction error to transactions-error-topic
     */
    public void publishTransactionError(String routingKey, Object error) {
        try {
            amqpTemplate.convertAndSend(TRANSACTIONS_ERROR_TOPIC, routingKey, error);
            log.info("Published transaction error to routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish transaction error: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Publish account notification to account-notification-topic
     */
    public void publishAccountNotification(String routingKey, Object notification) {
        try {
            amqpTemplate.convertAndSend(ACCOUNT_NOTIFICATION_TOPIC, routingKey, notification);
            log.info("Published account notification to routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish account notification: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Publish transaction notification to transactions-notification-topic
     */
    public void publishTransactionNotification(String routingKey, Object notification) {
        try {
            amqpTemplate.convertAndSend(TRANSACTIONS_NOTIFICATION_TOPIC, routingKey, notification);
            log.info("Published transaction notification to routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Failed to publish transaction notification: {}", e.getMessage());
            throw e;
        }
    }
} 