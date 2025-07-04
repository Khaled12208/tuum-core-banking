package com.tuum.csaccountseventsconsumer.consumer;

import com.tuum.csaccountseventsconsumer.service.TransactionEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final TransactionEventService transactionEventService;

    @RabbitListener(queues = "transactions-events-queue")
    public void handleTransactionCreatedEvent(String message) {
        log.info("Received transaction creation request: {}", message);
        
        try {
            transactionEventService.processTransactionCreatedEvent(message);
        } catch (Exception e) {
            log.error("Error processing transaction creation request", e);
        }
    }
} 