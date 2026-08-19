package com.knowledgegap.platform.dto.request;

import com.knowledgegap.platform.entity.DependencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddPrerequisiteRequest(
    @NotNull(message = "Prerequisite concept ID is required")
    UUID prerequisiteConceptId,

    @NotNull(message = "Dependency type is required")
    DependencyType dependencyType,

    @DecimalMin(value = "0.1", message = "Weight must be at least 0.1")
    double weight
) {}
