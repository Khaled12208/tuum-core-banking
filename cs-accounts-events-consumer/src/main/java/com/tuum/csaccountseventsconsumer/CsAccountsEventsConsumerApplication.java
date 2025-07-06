package com.tuum.csaccountseventsconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.tuum.csaccountseventsconsumer", "com.tuum.common"})
public class CsAccountsEventsConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(CsAccountsEventsConsumerApplication.class, args);
    }

} 