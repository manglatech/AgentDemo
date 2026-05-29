package com.agentdemo.platform.api;

import com.agentdemo.platform.model.PocDescriptor;
import com.agentdemo.platform.model.PocRunResult;
import com.agentdemo.platform.registry.PocRegistry;
import com.agentdemo.platform.registry.PocRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/pocs")
public class PocController {

    private final PocRegistry pocRegistry;
    private final PocRunner pocRunner;

    public PocController(PocRegistry pocRegistry, PocRunner pocRunner) {
        this.pocRegistry = pocRegistry;
        this.pocRunner = pocRunner;
    }

    @GetMapping
    public List<PocDescriptor> listPocs() {
        return pocRegistry.listDescriptors();
    }

    @PostMapping("/{pocId}/run")
    public PocRunResult runPoc(@PathVariable String pocId) {
        if (pocRegistry.findById(pocId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown POC: " + pocId);
        }
        return pocRunner.run(pocId);
    }
}
