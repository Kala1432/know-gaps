package com.knowledgegap.platform.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SubmitAttemptRequest(
    @NotNull(message = "Student ID is required")
    UUID studentId,

    @NotNull(message = "Question ID is required")
    UUID questionId,

    @NotBlank(message = "User answer is required")
    String userAnswer,

    @Min(value = 1, message = "Response time must be positive")
    long responseTimeMs
) {}
