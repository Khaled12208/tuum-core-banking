package com.tuum.fsaccountsservice;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.tuum.fsaccountsservice", "com.tuum.common"})
@EnableRabbit
public class FsAccountsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FsAccountsServiceApplication.class, args);
    }
} 