package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.UUID;

public record TopicResponse(
    UUID id,
    UUID subjectId,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
