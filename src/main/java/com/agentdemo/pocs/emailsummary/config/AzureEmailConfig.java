package com.agentdemo.pocs.emailsummary.config;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@EnableConfigurationProperties(AzureEmailProperties.class)
public class AzureEmailConfig {

    @Bean
    @ConditionalOnProperty(prefix = "pocs.email-summary.azure.communication.email", name = "enabled", havingValue = "true")
    EmailClient emailClient(AzureEmailProperties properties) {
        if (!StringUtils.hasText(properties.connectionString())) {
            throw new IllegalStateException(
                    "pocs.email-summary.azure.communication.email.connection-string is required when email is enabled");
        }
        return new EmailClientBuilder()
                .connectionString(properties.connectionString())
                .buildClient();
    }
}
