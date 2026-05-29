package com.agentdemo.pocs.emailsummary.agent;

import com.agentdemo.pocs.emailsummary.config.AzureEmailProperties;
import com.azure.communication.email.EmailClient;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "pocs.email-summary.azure.communication.email", name = "enabled", havingValue = "true")
public class EmailAgent {

    private final EmailClient emailClient;
    private final AzureEmailProperties emailProperties;

    public EmailAgent(EmailClient emailClient, AzureEmailProperties emailProperties) {
        this.emailClient = emailClient;
        this.emailProperties = emailProperties;
    }

    public String run(String summary) {
        validateConfiguration();

        EmailMessage message = new EmailMessage()
                .setSenderAddress(emailProperties.sender())
                .setToRecipients(emailProperties.recipient())
                .setSubject(emailProperties.subject())
                .setBodyPlainText(summary);

        SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message);
        PollResponse<EmailSendResult> response = poller.waitForCompletion();

        EmailSendResult result = response.getValue();
        if (result == null || result.getId() == null) {
            throw new IllegalStateException("Azure Email send completed without an operation id");
        }

        return result.getId();
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(emailProperties.sender())) {
            throw new IllegalStateException("azure.communication.email.sender is required when email is enabled");
        }
        if (!StringUtils.hasText(emailProperties.recipient())) {
            throw new IllegalStateException("azure.communication.email.recipient is required when email is enabled");
        }
        if (!StringUtils.hasText(emailProperties.subject())) {
            throw new IllegalStateException("azure.communication.email.subject is required when email is enabled");
        }
    }
}
