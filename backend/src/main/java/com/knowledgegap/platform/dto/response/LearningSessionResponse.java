package com.knowledgegap.platform.dto.response;

import com.knowledgegap.platform.entity.SessionStatus;
import java.time.Instant;
import java.util.UUID;

public record LearningSessionResponse(
    UUID id,
    UUID studentId,
    SessionStatus status,
    Instant startedAt,
    Instant endedAt,
    int attemptCount
) {}
