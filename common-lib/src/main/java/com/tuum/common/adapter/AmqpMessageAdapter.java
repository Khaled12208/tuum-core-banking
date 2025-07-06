package com.tuum.common.adapter;

import com.tuum.common.dto.mq.MQMessageData;
import com.tuum.common.types.RequestType;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class AmqpMessageAdapter implements MessageAdapter {

     private Map<String, Object> headers;
    @Override
    public MQMessageData adapt(Object rawMessage) throws Exception {
        if (!(rawMessage instanceof Message)) {
            throw new IllegalArgumentException("Expected AMQP Message");
        }

        Message message = (Message) rawMessage;
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        MessageProperties props = message.getMessageProperties();
        headers = props.getHeaders();
        String requestTypeCode  = getSafeHeader( "request-type");
        String idmKey           = getSafeHeader("idempotency-key");
        String requestID        = getSafeHeader("request-id");
        String status           = getSafeHeader("status");
        RequestType requestType = RequestType.fromCode(requestTypeCode);
        return new MQMessageData(requestType, idmKey, requestID,status, messageBody);
    }

    private String getSafeHeader(String key) {
        if (headers == null || !headers.containsKey(key) || headers.get(key) == null) {
            return "UNKNOWN";
        }
        return headers.get(key).toString();
    }
}
