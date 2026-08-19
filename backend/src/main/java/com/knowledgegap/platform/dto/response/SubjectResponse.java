package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.UUID;

public record SubjectResponse(
    UUID id,
    String name,
    String description,
    Instant createdAt,
    Instant updatedAt
) {}
