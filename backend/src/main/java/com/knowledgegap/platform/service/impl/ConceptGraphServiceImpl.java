package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.dto.response.PrerequisiteChainResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.ConceptPrerequisite;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.ConceptMapper;
import com.knowledgegap.platform.repository.ConceptPrerequisiteRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.service.ConceptGraphService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class ConceptGraphServiceImpl implements ConceptGraphService {

    private final ConceptRepository conceptRepository;
    private final ConceptPrerequisiteRepository prerequisiteRepository;
    private final ConceptMapper conceptMapper;

    public ConceptGraphServiceImpl(ConceptRepository conceptRepository,
                                    ConceptPrerequisiteRepository prerequisiteRepository,
                                    ConceptMapper conceptMapper) {
        this.conceptRepository = conceptRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.conceptMapper = conceptMapper;
    }

    @Override
    public PrerequisiteChainResponse getConceptGraph(UUID conceptId) {
        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept with ID '" + conceptId + "' not found"));

        List<ConceptResponse> prereqs = getPrerequisiteChain(conceptId);
        List<ConceptResponse> downstream = getDownstreamConcepts(conceptId);

        return new PrerequisiteChainResponse(
                concept.getId(),
                concept.getName(),
                prereqs,
                downstream
        );
    }

    @Override
    public List<ConceptResponse> getPrerequisiteChain(UUID conceptId) {
        if (!conceptRepository.existsById(conceptId)) {
            throw new ResourceNotFoundException("Concept with ID '" + conceptId + "' not found");
        }

        // BFS to find all ancestor prerequisites (ordered from root prerequisite to target concept)
        List<Concept> orderedAncestors = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();

        queue.add(conceptId);
        visited.add(conceptId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            List<ConceptPrerequisite> prereqs = prerequisiteRepository.findByTargetConceptId(current);
            for (ConceptPrerequisite p : prereqs) {
                Concept prereqConcept = p.getPrerequisiteConcept();
                if (!visited.contains(prereqConcept.getId())) {
                    visited.add(prereqConcept.getId());
                    orderedAncestors.add(0, prereqConcept); // Add to beginning for topological ordering
                    queue.add(prereqConcept.getId());
                }
            }
        }

        return orderedAncestors.stream()
                .map(conceptMapper::toResponse)
                .toList();
    }

    @Override
    public List<ConceptResponse> getDownstreamConcepts(UUID conceptId) {
        if (!conceptRepository.existsById(conceptId)) {
            throw new ResourceNotFoundException("Concept with ID '" + conceptId + "' not found");
        }

        // BFS to find all downstream dependent concepts
        List<Concept> downstreamList = new ArrayList<>();
        Set<UUID> visited = new HashSet<>();
        Queue<UUID> queue = new LinkedList<>();

        queue.add(conceptId);
        visited.add(conceptId);

        while (!queue.isEmpty()) {
            UUID current = queue.poll();
            List<ConceptPrerequisite> dependents = prerequisiteRepository.findByPrerequisiteConceptId(current);
            for (ConceptPrerequisite p : dependents) {
                Concept targetConcept = p.getTargetConcept();
                if (!visited.contains(targetConcept.getId())) {
                    visited.add(targetConcept.getId());
                    downstreamList.add(targetConcept);
                    queue.add(targetConcept.getId());
                }
            }
        }

        return downstreamList.stream()
                .map(conceptMapper::toResponse)
                .toList();
    }
}
