package com.agentdemo.pocs.emailsummary.agent;

import org.springframework.stereotype.Component;

@Component
public class ContentAgent {

    private static final String HARDCODED_TEXT = """
            Our team shipped a multi-region deployment for the payment service last quarter.
            We migrated three legacy endpoints to a unified API gateway, cut p99 latency from
            420ms to 180ms, and added circuit breakers on all downstream calls. Onboarding
            for new merchants dropped from two weeks to four days after we automated KYC
            checks. We still have open work on fraud-model retraining and PCI audit prep,
            but customer-reported checkout failures fell by 37% since January.
            """.strip();

    public String run() {
        return HARDCODED_TEXT;
    }
}
