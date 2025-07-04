package com.tuum.fsaccountsservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Single exchange for all banking operations
    public static final String TUUM_BANKING_EXCHANGE = "tuum.banking";
    
    // Queue names (plural case preserved)
    public static final String ACCOUNTS_EVENTS_QUEUE = "accounts-events-queue";
    public static final String ACCOUNTS_ERRORS_QUEUE = "accounts-errors-queue";
    public static final String ACCOUNTS_NOTIFICATIONS_QUEUE = "accounts-notifications-queue";
    public static final String TRANSACTIONS_EVENTS_QUEUE = "transactions-events-queue";
    public static final String TRANSACTIONS_ERRORS_QUEUE = "transactions-errors-queue";
    public static final String TRANSACTIONS_NOTIFICATIONS_QUEUE = "transactions-notifications-queue";
    
    // Routing keys for topic routing
    public static final String ACCOUNTS_EVENTS_ROUTING_KEY = "accounts.events.*";
    public static final String ACCOUNTS_ERRORS_ROUTING_KEY = "accounts.errors.*";
    public static final String ACCOUNTS_NOTIFICATIONS_ROUTING_KEY = "accounts.notifications.*";
    public static final String TRANSACTIONS_EVENTS_ROUTING_KEY = "transactions.events.*";
    public static final String TRANSACTIONS_ERRORS_ROUTING_KEY = "transactions.errors.*";
    public static final String TRANSACTIONS_NOTIFICATIONS_ROUTING_KEY = "transactions.notifications.*";
    
    // Specific routing keys for publishing
    public static final String ACCOUNT_CREATED_ROUTING_KEY = "accounts.events.created";
    public static final String ACCOUNT_PROCESSED_ROUTING_KEY = "accounts.notifications.processed";
    public static final String ACCOUNT_ERROR_ROUTING_KEY = "accounts.errors.processing";
    public static final String TRANSACTION_CREATED_ROUTING_KEY = "transactions.events.created";
    public static final String TRANSACTION_PROCESSED_ROUTING_KEY = "transactions.notifications.processed";
    public static final String TRANSACTION_ERROR_ROUTING_KEY = "transactions.errors.processing";

    @Bean
    public TopicExchange tuumBankingExchange() {
        return new TopicExchange(TUUM_BANKING_EXCHANGE, true, false);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return new Jackson2JsonMessageConverter(objectMapper);
    }
} 