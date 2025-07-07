package com.tuum.csaccountseventsconsumer.consumer;

import com.tuum.common.adapter.AmqpMessageAdapter;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.csaccountseventsconsumer.service.AccountEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AccountsEventConsumer {

    private final AccountEventService accountEventService;
    private final AmqpMessageAdapter amqpMessageAdapter;

    @RabbitListener(queues = "#{T(com.tuum.common.types.RabbitMQConfig).ACCOUNTS_EVENTS_QUEUE.getValue()}")
    public void handleAccountCreatedEvent(Message message) {
        try {
            MQMessageData messageData = amqpMessageAdapter.adapt(message);
            accountEventService.processAccountCreatedEvent(messageData);
        } catch (Exception e) {
            log.error("Error processing account event", e);
        }
    }
} 