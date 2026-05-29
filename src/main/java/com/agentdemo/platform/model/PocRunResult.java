package com.agentdemo.platform.model;

import java.util.List;

public record PocRunResult(
        String pocId,
        String status,
        List<PocStep> steps,
        String errorMessage
) {
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_PARTIAL = "PARTIAL";
    public static final String STATUS_FAILED = "FAILED";
}
