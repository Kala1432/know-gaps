package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.dto.response.PrerequisiteChainResponse;

import java.util.List;
import java.util.UUID;

public interface ConceptGraphService {
    PrerequisiteChainResponse getConceptGraph(UUID conceptId);
    List<ConceptResponse> getPrerequisiteChain(UUID conceptId);
    List<ConceptResponse> getDownstreamConcepts(UUID conceptId);
}
