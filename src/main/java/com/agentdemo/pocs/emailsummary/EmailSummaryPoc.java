package com.agentdemo.pocs.emailsummary;

import com.agentdemo.platform.model.PocDescriptor;
import com.agentdemo.platform.model.PocRunResult;
import com.agentdemo.platform.model.PocStep;
import com.agentdemo.platform.spi.Poc;
import com.agentdemo.pocs.emailsummary.agent.ContentAgent;
import com.agentdemo.pocs.emailsummary.agent.EmailAgent;
import com.agentdemo.pocs.emailsummary.agent.SummaryAgent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmailSummaryPoc implements Poc {

    public static final String ID = "email-summary";
    private static final String FOLDER = "pocs/email-summary";

    private final ContentAgent contentAgent;
    private final SummaryAgent summaryAgent;
    private final ObjectProvider<EmailAgent> emailAgent;

    public EmailSummaryPoc(
            ContentAgent contentAgent,
            SummaryAgent summaryAgent,
            ObjectProvider<EmailAgent> emailAgent
    ) {
        this.contentAgent = contentAgent;
        this.summaryAgent = summaryAgent;
        this.emailAgent = emailAgent;
    }

    @Override
    public PocDescriptor descriptor() {
        return new PocDescriptor(
                ID,
                "Email Summary (Multi-Agent)",
                "Hardcoded text → Llama summary via Ollama → send summary with Azure Email",
                FOLDER
        );
    }

    @Override
    public PocRunResult execute() {
        List<PocStep> steps = new ArrayList<>();

        try {
            String sourceText = contentAgent.run();
            steps.add(new PocStep("ContentAgent", "SUCCESS", sourceText));

            String summary = summaryAgent.run(sourceText);
            steps.add(new PocStep("SummaryAgent", "SUCCESS", summary));

            EmailAgent sender = emailAgent.getIfAvailable();
            if (sender == null) {
                steps.add(new PocStep(
                        "EmailAgent",
                        "SKIPPED",
                        "Azure email disabled or connection-string missing (see application-local.yml)"
                ));
                return new PocRunResult(ID, PocRunResult.STATUS_PARTIAL, steps, null);
            }

            try {
                String operationId = sender.run(summary);
                steps.add(new PocStep("EmailAgent", "SUCCESS", "Operation id: " + operationId));
                return new PocRunResult(ID, PocRunResult.STATUS_SUCCESS, steps, null);
            } catch (Exception ex) {
                String detail = ex.getMessage();
                if (detail != null && detail.contains("DomainNotLinked")) {
                    detail += " — Link your email domain to Communication Services in Azure Portal.";
                }
                steps.add(new PocStep("EmailAgent", "FAILED", detail));
                return new PocRunResult(ID, PocRunResult.STATUS_PARTIAL, steps, detail);
            }
        } catch (Exception ex) {
            steps.add(new PocStep("POC", "FAILED", ex.getMessage()));
            return new PocRunResult(ID, PocRunResult.STATUS_FAILED, steps, ex.getMessage());
        }
    }
}
