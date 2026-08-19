package com.knowledgegap.platform.dto.response;

import com.knowledgegap.platform.entity.QuestionType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record QuestionResponse(
    UUID id,
    String content,
    QuestionType questionType,
    double baseDifficulty,
    double discriminationIndex,
    String correctAnswer,
    List<QuestionConceptResponse> concepts,
    Instant createdAt,
    Instant updatedAt
) {}
