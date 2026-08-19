package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.dto.response.PrerequisiteChainResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.ConceptPrerequisite;
import com.knowledgegap.platform.entity.DependencyType;
import com.knowledgegap.platform.mapper.ConceptMapper;
import com.knowledgegap.platform.repository.ConceptPrerequisiteRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.service.impl.ConceptGraphServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ConceptGraphServiceTest {

    @Mock
    private ConceptRepository conceptRepository;

    @Mock
    private ConceptPrerequisiteRepository prerequisiteRepository;

    private ConceptMapper conceptMapper;
    private ConceptGraphServiceImpl conceptGraphService;

    private Concept arrays;
    private Concept searching;
    private Concept binarySearch;
    private Concept binarySearchTree;

    @BeforeEach
    void setUp() {
        conceptMapper = new ConceptMapper();
        conceptGraphService = new ConceptGraphServiceImpl(conceptRepository, prerequisiteRepository, conceptMapper);

        arrays = new Concept(null, "Arrays", "Array data structure");
        arrays.setId(UUID.randomUUID());

        searching = new Concept(null, "Searching", "Search algorithms");
        searching.setId(UUID.randomUUID());

        binarySearch = new Concept(null, "Binary Search", "O(log N) search");
        binarySearch.setId(UUID.randomUUID());

        binarySearchTree = new Concept(null, "Binary Search Tree", "BST tree structure");
        binarySearchTree.setId(UUID.randomUUID());
    }

    @Test
    void getPrerequisiteChain_ArraysToSearchingToBinarySearchToBST() {
        // Chain: Arrays -> Searching -> Binary Search -> Binary Search Tree
        given(conceptRepository.existsById(binarySearchTree.getId())).willReturn(true);

        ConceptPrerequisite bsToBst = new ConceptPrerequisite(binarySearch, binarySearchTree, DependencyType.HARD_REQUIREMENT, 1.0);
        ConceptPrerequisite searchToBs = new ConceptPrerequisite(searching, binarySearch, DependencyType.HARD_REQUIREMENT, 1.0);
        ConceptPrerequisite arrayToSearch = new ConceptPrerequisite(arrays, searching, DependencyType.HARD_REQUIREMENT, 1.0);

        given(prerequisiteRepository.findByTargetConceptId(binarySearchTree.getId())).willReturn(List.of(bsToBst));
        given(prerequisiteRepository.findByTargetConceptId(binarySearch.getId())).willReturn(List.of(searchToBs));
        given(prerequisiteRepository.findByTargetConceptId(searching.getId())).willReturn(List.of(arrayToSearch));
        given(prerequisiteRepository.findByTargetConceptId(arrays.getId())).willReturn(List.of());

        List<ConceptResponse> chain = conceptGraphService.getPrerequisiteChain(binarySearchTree.getId());

        assertThat(chain).hasSize(3);
        assertThat(chain.get(0).name()).isEqualTo("Arrays");
        assertThat(chain.get(1).name()).isEqualTo("Searching");
        assertThat(chain.get(2).name()).isEqualTo("Binary Search");
    }

    @Test
    void getDownstreamConcepts_ArraysUnlocksSearchingAndDownstream() {
        given(conceptRepository.existsById(arrays.getId())).willReturn(true);

        ConceptPrerequisite arrayToSearch = new ConceptPrerequisite(arrays, searching, DependencyType.HARD_REQUIREMENT, 1.0);
        ConceptPrerequisite searchToBs = new ConceptPrerequisite(searching, binarySearch, DependencyType.HARD_REQUIREMENT, 1.0);

        given(prerequisiteRepository.findByPrerequisiteConceptId(arrays.getId())).willReturn(List.of(arrayToSearch));
        given(prerequisiteRepository.findByPrerequisiteConceptId(searching.getId())).willReturn(List.of(searchToBs));
        given(prerequisiteRepository.findByPrerequisiteConceptId(binarySearch.getId())).willReturn(List.of());

        List<ConceptResponse> downstream = conceptGraphService.getDownstreamConcepts(arrays.getId());

        assertThat(downstream).extracting(ConceptResponse::name).containsExactly("Searching", "Binary Search");
    }
}
