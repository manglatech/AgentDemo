package com.agentdemo.pocs.crewaiemailsummary;

import com.agentdemo.platform.model.PocDescriptor;
import com.agentdemo.platform.model.PocRunResult;
import com.agentdemo.platform.model.PocStep;
import com.agentdemo.platform.spi.Poc;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrewAiEmailSummaryPoc implements Poc {

    public static final String ID = "crewai-email-summary";
    private static final String FOLDER = "pocs/crewai-email-summary";

    private final CrewAiScriptRunner scriptRunner;

    public CrewAiEmailSummaryPoc(CrewAiScriptRunner scriptRunner) {
        this.scriptRunner = scriptRunner;
    }

    @Override
    public PocDescriptor descriptor() {
        return new PocDescriptor(
                ID,
                "Email Summary (CrewAI)",
                "CrewAI crew: hardcoded text → Llama/Ollama summary → Azure Email",
                FOLDER
        );
    }

    @Override
    public PocRunResult execute() {
        try {
            return scriptRunner.run(ID);
        } catch (Exception ex) {
            return new PocRunResult(
                    ID,
                    PocRunResult.STATUS_FAILED,
                    List.of(new PocStep("CrewAI", "FAILED", ex.getMessage())),
                    ex.getMessage()
            );
        }
    }
}
