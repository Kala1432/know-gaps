package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.UUID;

public record QuestionConceptResponse(
    UUID id,
    UUID questionId,
    UUID conceptId,
    String conceptName,
    double weight,
    Instant createdAt
) {}
