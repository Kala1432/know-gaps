package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AttemptResponse(
    UUID id,
    UUID studentId,
    UUID questionId,
    UUID sessionId,
    String userAnswer,
    boolean isCorrect,
    long responseTimeMs,
    int attemptNumber,
    double questionDifficulty,
    List<String> testedConceptNames,
    Instant createdAt
) {}
