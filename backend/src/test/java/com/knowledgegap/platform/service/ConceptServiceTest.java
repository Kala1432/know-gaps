package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.AddPrerequisiteRequest;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.ConceptPrerequisite;
import com.knowledgegap.platform.entity.DependencyType;
import com.knowledgegap.platform.exception.InvalidPrerequisiteException;
import com.knowledgegap.platform.mapper.ConceptMapper;
import com.knowledgegap.platform.repository.ConceptPrerequisiteRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.repository.TopicRepository;
import com.knowledgegap.platform.service.impl.ConceptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConceptServiceTest {

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private ConceptRepository conceptRepository;

    @Mock
    private ConceptPrerequisiteRepository prerequisiteRepository;

    private ConceptMapper conceptMapper;
    private ConceptServiceImpl conceptService;

    @BeforeEach
    void setUp() {
        conceptMapper = new ConceptMapper();
        conceptService = new ConceptServiceImpl(topicRepository, conceptRepository, prerequisiteRepository, conceptMapper);
    }

    @Test
    void addPrerequisite_SelfPrerequisite_ThrowsException() {
        UUID conceptId = UUID.randomUUID();
        AddPrerequisiteRequest request = new AddPrerequisiteRequest(conceptId, DependencyType.HARD_REQUIREMENT, 1.0);

        assertThatThrownBy(() -> conceptService.addPrerequisite(conceptId, request))
                .isInstanceOf(InvalidPrerequisiteException.class)
                .hasMessageContaining("cannot be a prerequisite of itself");
    }

    @Test
    void addPrerequisite_CyclicDependency_ThrowsException() {
        UUID conceptAId = UUID.randomUUID();
        UUID conceptBId = UUID.randomUUID();

        Concept conceptA = new Concept(null, "Concept A", "Desc A");
        conceptA.setId(conceptAId);

        Concept conceptB = new Concept(null, "Concept B", "Desc B");
        conceptB.setId(conceptBId);

        // Scenario: A -> B already exists. Now trying to add B -> A (so target = A, prerequisite = B).
        given(conceptRepository.findById(conceptAId)).willReturn(Optional.of(conceptA));
        given(conceptRepository.findById(conceptBId)).willReturn(Optional.of(conceptB));
        given(prerequisiteRepository.existsByPrerequisiteConceptIdAndTargetConceptId(conceptBId, conceptAId)).willReturn(false);

        // When checking reachability from startId=conceptAId to targetId=conceptBId:
        // A has dependent B
        ConceptPrerequisite aToB = new ConceptPrerequisite(conceptA, conceptB, DependencyType.HARD_REQUIREMENT, 1.0);
        given(prerequisiteRepository.findByPrerequisiteConceptId(conceptAId)).willReturn(List.of(aToB));

        AddPrerequisiteRequest request = new AddPrerequisiteRequest(conceptBId, DependencyType.HARD_REQUIREMENT, 1.0);

        assertThatThrownBy(() -> conceptService.addPrerequisite(conceptAId, request))
                .isInstanceOf(InvalidPrerequisiteException.class)
                .hasMessageContaining("cyclic dependency");
    }
}
