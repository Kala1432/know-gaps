package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.response.ConceptPerformanceResponse;
import com.knowledgegap.platform.dto.response.RecommendationResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.Student;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.repository.ConceptPrerequisiteRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.repository.StudentRepository;
import com.knowledgegap.platform.service.client.MlServiceClient;
import com.knowledgegap.platform.service.impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private ConceptRepository conceptRepository;

    @Mock
    private ConceptPrerequisiteRepository prerequisiteRepository;

    @Mock
    private AssessmentService assessmentService;

    @Mock
    private MlServiceClient mlServiceClient;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private UUID studentId;
    private Student student;
    private Concept concept;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentId = UUID.randomUUID();
        student = new Student();
        student.setId(studentId);
        student.setName("Alice");
        student.setEmail("alice@example.com");

        concept = new Concept();
        concept.setId(UUID.randomUUID());
        concept.setName("Arrays");
    }

    @Test
    void getRecommendations_ThrowsException_WhenStudentNotFound() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                recommendationService.getNextConceptRecommendations(studentId, 3));
    }

    @Test
    void getRecommendations_ReturnsFallback_WhenMlServiceFailsOrEmpty() {
        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(conceptRepository.findAll()).thenReturn(List.of(concept));
        when(prerequisiteRepository.findAll()).thenReturn(List.of());
        when(mlServiceClient.getRecommendations(any())).thenReturn(null);

        ConceptPerformanceResponse mockPerf = new ConceptPerformanceResponse(
                studentId, concept.getId(), "Arrays", 5, 3, 0.60, 4000.0, 0.50, 0.80, List.of(), Instant.now()
        );
        when(assessmentService.getConceptPerformance(any(), any())).thenReturn(mockPerf);

        List<RecommendationResponse> response = recommendationService.getNextConceptRecommendations(studentId, 3);

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals("Arrays", response.get(0).concept());
    }
}
