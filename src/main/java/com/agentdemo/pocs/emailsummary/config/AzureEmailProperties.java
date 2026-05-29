package com.agentdemo.pocs.emailsummary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pocs.email-summary.azure.communication.email")
public record AzureEmailProperties(
        boolean enabled,
        String connectionString,
        String sender,
        String recipient,
        String subject
) {
}
