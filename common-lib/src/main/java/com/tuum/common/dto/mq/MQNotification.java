package com.tuum.common.dto.mq;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MQNotification <T> {
        private Map<String, Object> headers;
        private T body;

    public MQNotification(Map<String, Object> headers, T body) {
            this.headers = headers;
            this.body = body;
        }
}
