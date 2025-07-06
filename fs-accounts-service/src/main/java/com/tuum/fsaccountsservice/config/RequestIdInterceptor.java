package com.tuum.fsaccountsservice.config;

import com.tuum.common.util.TraceIdGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class RequestIdInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestId = TraceIdGenerator.generateTraceId();
        log.debug("Generated request ID for tracing: {}", requestId);
        
        String idempotencyKey = request.getHeader("Idempotency-Key");
        if (idempotencyKey != null && !idempotencyKey.trim().isEmpty()) {
            log.debug("Request has Idempotency-Key: {}", idempotencyKey);
        }
        MDC.put(REQUEST_ID_HEADER, requestId);
        response.setHeader(REQUEST_ID_HEADER, requestId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TraceIdGenerator.clear();
        MDC.remove(REQUEST_ID_HEADER);
        log.debug("Cleared request ID from ThreadLocal");
    }
} 