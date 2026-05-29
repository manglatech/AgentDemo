package com.agentdemo.pocs.emailsummary.agent;

import com.agentdemo.pocs.emailsummary.config.OllamaProperties;
import com.agentdemo.pocs.emailsummary.ollama.OllamaChatRequest;
import com.agentdemo.pocs.emailsummary.ollama.OllamaChatResponse;
import com.agentdemo.pocs.emailsummary.ollama.OllamaMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class SummaryAgent {

    private final RestClient ollamaRestClient;
    private final OllamaProperties ollamaProperties;

    public SummaryAgent(RestClient ollamaRestClient, OllamaProperties ollamaProperties) {
        this.ollamaRestClient = ollamaRestClient;
        this.ollamaProperties = ollamaProperties;
    }

    public String run(String text) {
        String prompt = """
                Summarize the following text in 2-3 concise sentences. \
                Focus on outcomes and metrics.

                %s""".formatted(text);

        OllamaChatRequest request = new OllamaChatRequest(
                ollamaProperties.model(),
                List.of(new OllamaMessage("user", prompt)),
                false
        );

        OllamaChatResponse response = ollamaRestClient.post()
                .uri("/api/chat")
                .body(request)
                .retrieve()
                .body(OllamaChatResponse.class);

        if (response == null || response.message() == null) {
            throw new IllegalStateException("Empty response from Ollama");
        }

        String content = response.message().content();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("Model returned no summary text");
        }

        return content.strip();
    }
}
