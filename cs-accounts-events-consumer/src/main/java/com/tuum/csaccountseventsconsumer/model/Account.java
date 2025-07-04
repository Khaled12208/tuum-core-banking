package com.tuum.csaccountseventsconsumer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    
    private String accountId;
    private String customerId;
    private String country;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 