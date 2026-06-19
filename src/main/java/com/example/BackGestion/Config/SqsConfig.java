package com.example.BackGestion.Config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@ConditionalOnProperty(name = "app.environment", havingValue = "prod")
public class SqsConfig {

    @Bean
    public SqsClient sqsClient() {
        return SqsClient.create();
    }
}
