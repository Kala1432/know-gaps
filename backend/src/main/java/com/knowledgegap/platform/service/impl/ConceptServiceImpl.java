package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.AddPrerequisiteRequest;
import com.knowledgegap.platform.dto.request.CreateConceptRequest;
import com.knowledgegap.platform.dto.response.ConceptPrerequisiteResponse;
import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.ConceptPrerequisite;
import com.knowledgegap.platform.entity.Topic;
import com.knowledgegap.platform.exception.DuplicateResourceException;
import com.knowledgegap.platform.exception.InvalidPrerequisiteException;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.ConceptMapper;
import com.knowledgegap.platform.repository.ConceptPrerequisiteRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.repository.TopicRepository;
import com.knowledgegap.platform.service.ConceptService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ConceptServiceImpl implements ConceptService {

    private final TopicRepository topicRepository;
    private final ConceptRepository conceptRepository;
    private final ConceptPrerequisiteRepository prerequisiteRepository;
    private final ConceptMapper conceptMapper;

    public ConceptServiceImpl(TopicRepository topicRepository,
                              ConceptRepository conceptRepository,
                              ConceptPrerequisiteRepository prerequisiteRepository,
                              ConceptMapper conceptMapper) {
        this.topicRepository = topicRepository;
        this.conceptRepository = conceptRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.conceptMapper = conceptMapper;
    }

    @Override
    @Transactional
    public ConceptResponse createConcept(UUID topicId, CreateConceptRequest request) {
        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic with ID '" + topicId + "' not found"));

        if (conceptRepository.existsByTopicIdAndName(topicId, request.name())) {
            throw new DuplicateResourceException("Concept with name '" + request.name() + "' already exists under topic ID '" + topicId + "'");
        }

        Concept concept = new Concept(topic, request.name(), request.description());
        Concept savedConcept = conceptRepository.save(concept);
        return conceptMapper.toResponse(savedConcept);
    }

    @Override
    public ConceptResponse getConceptById(UUID id) {
        Concept concept = conceptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Concept with ID '" + id + "' not found"));
        return conceptMapper.toResponse(concept);
    }

    @Override
    public List<ConceptResponse> getConceptsByTopicId(UUID topicId) {
        if (!topicRepository.existsById(topicId)) {
            throw new ResourceNotFoundException("Topic with ID '" + topicId + "' not found");
        }
        return conceptRepository.findByTopicId(topicId).stream()
                .map(conceptMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ConceptPrerequisiteResponse addPrerequisite(UUID targetConceptId, AddPrerequisiteRequest request) {
        UUID prerequisiteConceptId = request.prerequisiteConceptId();

        if (targetConceptId.equals(prerequisiteConceptId)) {
            throw new InvalidPrerequisiteException("A concept cannot be a prerequisite of itself");
        }

        Concept targetConcept = conceptRepository.findById(targetConceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Target concept with ID '" + targetConceptId + "' not found"));

        Concept prerequisiteConcept = conceptRepository.findById(prerequisiteConceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prerequisite concept with ID '" + prerequisiteConceptId + "' not found"));

        if (prerequisiteRepository.existsByPrerequisiteConceptIdAndTargetConceptId(prerequisiteConceptId, targetConceptId)) {
            throw new DuplicateResourceException("Prerequisite relationship already exists between concept '" + prerequisiteConceptId + "' and '" + targetConceptId + "'");
        }

        // Cycle Detection: Check if targetConcept is a prerequisite (directly or transitively) of prerequisiteConcept
        if (isReachable(targetConceptId, prerequisiteConceptId)) {
            throw new InvalidPrerequisiteException("Adding this prerequisite would create a cyclic dependency between concepts");
        }

        ConceptPrerequisite prerequisite = new ConceptPrerequisite(
                prerequisiteConcept,
                targetConcept,
                request.dependencyType(),
                request.weight()
        );

        ConceptPrerequisite savedPrerequisite = prerequisiteRepository.save(prerequisite);
        return conceptMapper.toPrerequisiteResponse(savedPrerequisite);
    }

    @Override
    public List<ConceptPrerequisiteResponse> getPrerequisites(UUID targetConceptId) {
        if (!conceptRepository.existsById(targetConceptId)) {
            throw new ResourceNotFoundException("Concept with ID '" + targetConceptId + "' not found");
        }
        return prerequisiteRepository.findByTargetConceptId(targetConceptId).stream()
                .map(conceptMapper::toPrerequisiteResponse)
                .toList();
    }

    /**
     * Checks if targetId is reachable from startId via prerequisite links (BFS).
     * If startId can reach targetId, adding (startId -> targetId) would cause a cycle.
     */
    private boolean isReachable(UUID startId, UUID targetId) {
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();

        queue.add(startId);
        visited.add(startId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            if (current.equals(targetId)) {
                return true;
            }

            // Find all concepts that depend on 'current' (where 'current' is the prerequisite)
            List<ConceptPrerequisite> dependents = prerequisiteRepository.findByPrerequisiteConceptId(current);
            for (ConceptPrerequisite prereq : dependents) {
                UUID dependentId = prereq.getTargetConcept().getId();
                if (!visited.contains(dependentId)) {
                    visited.add(dependentId);
                    queue.add(dependentId);
                }
            }
        }

        return false;
    }
}
