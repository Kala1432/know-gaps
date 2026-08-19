package com.knowledgegap.platform.dto.response;

import java.time.Instant;
import java.util.UUID;

public record StudentResponse(
    UUID id,
    String email,
    String name,
    Instant createdAt,
    Instant updatedAt
) {}
