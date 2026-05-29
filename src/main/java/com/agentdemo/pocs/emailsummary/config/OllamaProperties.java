package com.agentdemo.pocs.emailsummary.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pocs.email-summary.ollama")
public record OllamaProperties(String baseUrl, String model) {
}
