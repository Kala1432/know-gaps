package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ConceptPerformanceResponse(
    UUID studentId,
    UUID conceptId,
    String conceptName,
    int totalAttempts,
    int correctAttempts,
    double accuracyRate,
    double averageResponseTimeMs,
    double masteryScore,
    double confidenceLevel,
    List<String> activeGapTypes,
    Instant lastEvaluatedAt
) {}
