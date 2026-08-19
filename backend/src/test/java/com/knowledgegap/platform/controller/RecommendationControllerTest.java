package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.response.RecommendationResponse;
import com.knowledgegap.platform.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationControllerTest {

    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private RecommendationController recommendationController;

    private UUID studentId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentId = UUID.randomUUID();
    }

    @Test
    void getNextConceptRecommendations_ReturnsValidRecommendations() {
        UUID conceptId = UUID.randomUUID();
        RecommendationResponse rec = new RecommendationResponse(
                "Graph Traversal",
                conceptId.toString(),
                0.85,
                1,
                "Prerequisite remediation required for Graph Algorithms",
                0.45,
                0.50,
                "DEFICIT",
                "PREREQUISITE_REMEDIATION"
        );

        when(recommendationService.getNextConceptRecommendations(studentId, 3)).thenReturn(List.of(rec));

        ResponseEntity<List<RecommendationResponse>> response = recommendationController.getNextConceptRecommendations(studentId, 3);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Graph Traversal", response.getBody().get(0).concept());
        assertEquals("DEFICIT", response.getBody().get(0).prerequisiteStatus());

        verify(recommendationService, times(1)).getNextConceptRecommendations(studentId, 3);
    }
}
