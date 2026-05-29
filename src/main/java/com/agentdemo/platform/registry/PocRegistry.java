package com.agentdemo.platform.registry;

import com.agentdemo.platform.model.PocDescriptor;
import com.agentdemo.platform.spi.Poc;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PocRegistry {

    private final Map<String, Poc> pocsById;

    public PocRegistry(List<Poc> pocs) {
        this.pocsById = pocs.stream()
                .collect(Collectors.toMap(poc -> poc.descriptor().id(), Function.identity()));
    }

    public List<PocDescriptor> listDescriptors() {
        return pocsById.values().stream()
                .map(Poc::descriptor)
                .sorted(Comparator.comparing(PocDescriptor::name))
                .toList();
    }

    public Optional<Poc> findById(String id) {
        return Optional.ofNullable(pocsById.get(id));
    }
}
