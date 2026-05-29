package com.agentdemo.pocs.crewaiemailsummary;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pocs.crewai-email-summary")
public record CrewAiEmailSummaryProperties(
        String pythonExecutable,
        String script,
        Ollama ollama,
        AzureEmail azure
) {
    public record Ollama(String baseUrl, String model) {
    }

    public record AzureEmail(AzureCommunication communication) {
    }

    public record AzureCommunication(Email email) {
    }

    public record Email(
            boolean enabled,
            String connectionString,
            String sender,
            String recipient,
            String subject
    ) {
    }
}
