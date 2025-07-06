package com.tuum.csaccountseventsconsumer.config;

import com.tuum.common.types.RabbitMQConfig;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfiguration {

    @Bean
    public TopicExchange tuumBankingExchange() {
        return new TopicExchange(RabbitMQConfig.TUUM_BANKING_EXCHANGE.getValue(), true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new org.springframework.amqp.support.converter.SimpleMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
} 