package com.knowledgegap.platform.dto.response;

import java.util.List;
import java.util.UUID;

public record PrerequisiteChainResponse(
    UUID conceptId,
    String conceptName,
    List<ConceptResponse> prerequisitePath,
    List<ConceptResponse> downstreamConcepts
) {}
