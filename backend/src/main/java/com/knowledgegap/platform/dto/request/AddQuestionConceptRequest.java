package com.knowledgegap.platform.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddQuestionConceptRequest(
    @NotNull(message = "Concept ID is required")
    UUID conceptId,

    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1")
    double weight
) {}
