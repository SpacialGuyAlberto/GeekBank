package com.geekbank.bank.config;

import com.geekbank.bank.support.email.service.SendGridEmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public SendGridEmailService mockSendGridEmailService() {
        return mock(SendGridEmailService.class);
    }
}
