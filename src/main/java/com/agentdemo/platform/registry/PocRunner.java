package com.agentdemo.platform.registry;

import com.agentdemo.platform.model.PocRunResult;
import com.agentdemo.platform.spi.Poc;
import org.springframework.stereotype.Service;

@Service
public class PocRunner {

    private final PocRegistry pocRegistry;

    public PocRunner(PocRegistry pocRegistry) {
        this.pocRegistry = pocRegistry;
    }

    public PocRunResult run(String pocId) {
        Poc poc = pocRegistry.findById(pocId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown POC: " + pocId));
        return poc.execute();
    }
}
