package com.tuum.csaccountseventsconsumer.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Single exchange for all banking operations
    public static final String TUUM_BANKING_EXCHANGE = "tuum.banking";
    
    // Queue names (plural case preserved)
    public static final String ACCOUNTS_EVENTS_QUEUE = "accounts-events-queue";
    public static final String TRANSACTIONS_EVENTS_QUEUE = "transactions-events-queue";
    
    // Routing keys for consuming events
    public static final String ACCOUNTS_EVENTS_ROUTING_KEY = "accounts.events.*";
    public static final String TRANSACTIONS_EVENTS_ROUTING_KEY = "transactions.events.*";
    
    // Routing keys for publishing notifications
    public static final String ACCOUNTS_SUCCESS_ROUTING_KEY = "accounts.notifications.success";
    public static final String ACCOUNTS_ERROR_ROUTING_KEY = "accounts.errors.processing";
    public static final String TRANSACTIONS_SUCCESS_ROUTING_KEY = "transactions.notifications.success";
    public static final String TRANSACTIONS_ERROR_ROUTING_KEY = "transactions.errors.processing";

    // Single exchange
    @Bean
    public TopicExchange tuumBankingExchange() {
        return new TopicExchange(TUUM_BANKING_EXCHANGE, true, false);
    }

    // Message converter - use SimpleMessageConverter to get raw JSON as String
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new org.springframework.amqp.support.converter.SimpleMessageConverter();
    }

    // Rabbit template
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Container factory for listeners to use SimpleMessageConverter
    @Bean
    public org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory simpleMessageListenerContainerFactory(
            org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory factory = 
            new org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(new org.springframework.amqp.support.converter.SimpleMessageConverter());
        return factory;
    }
} 