package com.tuum.csaccountseventsconsumer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.dto.mq.ErrorNotification;
import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.types.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final AmqpTemplate amqpTemplate;
    private final ObjectMapper objectMapper;

    public  <T> void publishSuccessNotification(
            String exchangeName,
            String routingKey,
            String type,
            String status,
            String requestId,
            T messageBodyObject,
            String idempotencyKey,
            Map<String, Object> extraHeaders
    ) {
        try {

            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("request-id", requestId);
            messageProperties.setHeader("request-type", type);
            messageProperties.setHeader("status", status);
            messageProperties.setHeader("idempotency-key", idempotencyKey);
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            messageProperties.setTimestamp(new Date());
            String messageBody = objectMapper.writeValueAsString(messageBodyObject);
            Message message = new Message(messageBody.getBytes(StandardCharsets.UTF_8), messageProperties);
            log.info("Published success message {}:",message);

            if (extraHeaders != null) {
                extraHeaders.forEach(messageProperties::setHeader);
            }
            amqpTemplate.convertAndSend(exchangeName, routingKey, message);

            log.info("Published message of type {} with requestId: {}, message: {}", status, requestId,message);
        } catch (Exception e) {
            log.error("Failed to publish success notification of type {} with requestId: {}", type, requestId, e);
        }
    }


    public void publishErrorResponse(String exchangeName, String routingKey, MQMessageData data, ErrorCode errorCode, String errMsg) {

        ErrorNotification errorNotification = new ErrorNotification(
                errorCode,
                errMsg,
                "Error occurred during processing",
                LocalDateTime.now(),
                data.getRequestId()
        );

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        try {
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setHeader("request-id", data.getRequestId());
            messageProperties.setHeader("request-type", data.getRequestType());
            messageProperties.setHeader("idempotency-key", data.getIdempotencyKey());
            messageProperties.setHeader("status", "ERROR");
            messageProperties.setTimestamp(new Date());
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            String jsonString = objectMapper.writeValueAsString(errorNotification);
            Message message = new Message(jsonString.getBytes(StandardCharsets.UTF_8), messageProperties);

            amqpTemplate.convertAndSend(exchangeName, routingKey, message);

            log.info("Published error message of  {} for with requestId: {}, origin message: {}", jsonString, data.getRequestId() ,message);
        } catch (Exception e) {
            log.error("Failed to publish error message for request: {}", data.getRequestId(), e);
        }
    }
}
