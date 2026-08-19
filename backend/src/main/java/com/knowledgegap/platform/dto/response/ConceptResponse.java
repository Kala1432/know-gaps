package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ConceptResponse(
    UUID id,
    UUID topicId,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
