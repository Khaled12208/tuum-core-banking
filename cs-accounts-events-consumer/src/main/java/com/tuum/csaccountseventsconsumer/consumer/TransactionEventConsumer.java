package com.tuum.csaccountseventsconsumer.consumer;

import com.tuum.common.adapter.AmqpMessageAdapter;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.csaccountseventsconsumer.service.TransactionEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final TransactionEventService transactionEventService;
    private final AmqpMessageAdapter amqpMessageAdapter;

    @RabbitListener(queues = "#{T(com.tuum.common.types.RabbitMQConfig).TRANSACTIONS_EVENTS_QUEUE.getValue()}")
    public void handleTransactionEvent(Message message) {
        log.info("Consumer received message: {}", message);
        log.info("Message length: {}", message.getBody().length);
        
        try {
            MQMessageData messageData = amqpMessageAdapter.adapt(message);
            transactionEventService.processTransactionCreatedEvent(messageData);
        } catch (Exception e) {
            log.error("Error processing transaction event", e);
        }
    }
} 