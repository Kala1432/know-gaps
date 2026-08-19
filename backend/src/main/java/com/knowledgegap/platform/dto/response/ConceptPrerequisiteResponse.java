package com.knowledgegap.platform.dto.response;

import com.knowledgegap.platform.entity.DependencyType;
import java.time.Instant;
import java.util.UUID;

public record ConceptPrerequisiteResponse(
    UUID id,
    UUID prerequisiteConceptId,
    String prerequisiteConceptName,
    UUID targetConceptId,
    String targetConceptName,
    DependencyType dependencyType,
    double weight,
    Instant createdAt
) {}
