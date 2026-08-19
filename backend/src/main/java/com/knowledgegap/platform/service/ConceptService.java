package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.AddPrerequisiteRequest;
import com.knowledgegap.platform.dto.request.CreateConceptRequest;
import com.knowledgegap.platform.dto.response.ConceptPrerequisiteResponse;
import com.knowledgegap.platform.dto.response.ConceptResponse;

import java.util.List;
import java.util.UUID;

public interface ConceptService {
    ConceptResponse createConcept(UUID topicId, CreateConceptRequest request);
    ConceptResponse getConceptById(UUID id);
    List<ConceptResponse> getConceptsByTopicId(UUID topicId);
    ConceptPrerequisiteResponse addPrerequisite(UUID targetConceptId, AddPrerequisiteRequest request);
    List<ConceptPrerequisiteResponse> getPrerequisites(UUID targetConceptId);
}
