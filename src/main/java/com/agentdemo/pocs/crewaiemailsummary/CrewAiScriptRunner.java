package com.agentdemo.pocs.crewaiemailsummary;

import com.agentdemo.platform.model.PocRunResult;
import com.agentdemo.platform.model.PocStep;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@EnableConfigurationProperties(CrewAiEmailSummaryProperties.class)
public class CrewAiScriptRunner {

    private final CrewAiEmailSummaryProperties properties;
    private final ObjectMapper objectMapper;

    public CrewAiScriptRunner(CrewAiEmailSummaryProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public PocRunResult run(String pocId) throws Exception {
        Path scriptPath = Path.of(properties.script()).toAbsolutePath().normalize();
        if (!Files.exists(scriptPath)) {
            throw new IllegalStateException("CrewAI script not found: " + scriptPath);
        }

        String python = resolvePythonExecutable(scriptPath.getParent());

        ProcessBuilder processBuilder = new ProcessBuilder(python, scriptPath.toString());
        processBuilder.directory(scriptPath.getParent().toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().putAll(buildEnvironment());

        Process process = processBuilder.start();

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            output = reader.lines().collect(Collectors.joining("\n"));
        }

        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IllegalStateException("CrewAI script timed out after 5 minutes");
        }

        if (process.exitValue() != 0) {
            throw new IllegalStateException(
                    "CrewAI script failed (exit " + process.exitValue() + "): " + output);
        }

        return parseResult(pocId, output);
    }

    private String resolvePythonExecutable(Path pocDirectory) {
        Path venvPython = pocDirectory.resolve(".venv/bin/python");
        if (Files.exists(venvPython)) {
            return venvPython.toAbsolutePath().toString();
        }
        return properties.pythonExecutable();
    }

    private Map<String, String> buildEnvironment() {
        CrewAiEmailSummaryProperties.Ollama ollama = properties.ollama();
        CrewAiEmailSummaryProperties.Email email = properties.azure().communication().email();

        return Map.of(
                "OLLAMA_BASE_URL", ollama.baseUrl(),
                "OLLAMA_MODEL", ollama.model(),
                "AZURE_EMAIL_ENABLED", String.valueOf(email.enabled()),
                "AZURE_COMMUNICATION_CONNECTION_STRING", nullToEmpty(email.connectionString()),
                "AZURE_EMAIL_SENDER", nullToEmpty(email.sender()),
                "AZURE_EMAIL_RECIPIENT", nullToEmpty(email.recipient()),
                "AZURE_EMAIL_SUBJECT", nullToEmpty(email.subject()),
                "CREWAI_TRACING_ENABLED", "false"
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private PocRunResult parseResult(String pocId, String output) throws Exception {
        String jsonLine = extractJsonLine(output);
        JsonNode root = objectMapper.readTree(jsonLine);

        List<PocStep> steps = new ArrayList<>();
        JsonNode stepsNode = root.path("steps");
        if (stepsNode.isArray()) {
            for (JsonNode step : stepsNode) {
                steps.add(new PocStep(
                        step.path("agent").asText(),
                        step.path("status").asText(),
                        step.path("detail").asText()
                ));
            }
        }

        String status = root.path("status").asText(PocRunResult.STATUS_FAILED);
        String errorMessage = root.path("errorMessage").isNull() || root.path("errorMessage").isMissingNode()
                ? null
                : root.path("errorMessage").asText();

        return new PocRunResult(
                root.path("pocId").asText(pocId),
                status,
                steps,
                errorMessage
        );
    }

    private static String extractJsonLine(String output) {
        for (int i = output.length() - 1; i >= 0; i--) {
            if (output.charAt(i) == '{') {
                return output.substring(i);
            }
        }
        throw new IllegalStateException("CrewAI script did not print JSON result");
    }
}
