package com.knowledgegap.platform.dto.response;

public record PatternResponse(
        String patternType,
        String conceptName,
        double severity,
        String description,
        String evidenceSummary
) {}
