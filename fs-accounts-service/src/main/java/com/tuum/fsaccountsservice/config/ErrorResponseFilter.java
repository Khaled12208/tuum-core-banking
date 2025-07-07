package com.tuum.fsaccountsservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuum.common.dto.ErrorResponse;
import com.tuum.common.util.TraceIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@Order(1)
@Slf4j
public class ErrorResponseFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    public ErrorResponseFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            String traceId = TraceIdGenerator.generateTraceId();
            log.error("Unhandled exception in filter [{}]: {}", traceId, ex.getMessage(), ex);
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .traceId(traceId)
                    .error("INTERNAL_SERVER_ERROR")
                    .message("An unexpected error occurred. Please try again later.")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .timestamp(LocalDateTime.now())
                    .path(request.getRequestURI())
                    .build();
            
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }
} 