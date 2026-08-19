package com.knowledgegap.platform.dto.request;

import com.knowledgegap.platform.entity.QuestionType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateQuestionRequest(
    @NotBlank(message = "Question content is required")
    String content,

    @NotNull(message = "Question type is required")
    QuestionType questionType,

    @DecimalMin(value = "0.0", message = "Base difficulty must be between 0.0 and 1.0")
    @DecimalMax(value = "1.0", message = "Base difficulty must be between 0.0 and 1.0")
    double baseDifficulty,

    @DecimalMin(value = "0.1", message = "Discrimination index must be greater than 0")
    double discriminationIndex,

    String correctAnswer
) {}
