package com.agentdemo.platform.spi;

import com.agentdemo.platform.model.PocDescriptor;
import com.agentdemo.platform.model.PocRunResult;

public interface Poc {

    PocDescriptor descriptor();

    PocRunResult execute();
}
