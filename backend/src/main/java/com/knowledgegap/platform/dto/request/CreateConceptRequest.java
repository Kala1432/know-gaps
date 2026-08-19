package com.knowledgegap.platform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateConceptRequest(
    @NotBlank(message = "Concept name is required")
    @Size(min = 2, max = 255, message = "Concept name must be between 2 and 255 characters")
    String name,

    String description
) {}
