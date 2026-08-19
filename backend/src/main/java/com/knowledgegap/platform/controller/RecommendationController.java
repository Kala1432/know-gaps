package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.response.RecommendationResponse;
import com.knowledgegap.platform.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/{studentId}/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/next-concept")
    public ResponseEntity<List<RecommendationResponse>> getNextConceptRecommendations(
            @PathVariable UUID studentId,
            @RequestParam(defaultValue = "3") int limit) {
        List<RecommendationResponse> recommendations = recommendationService.getNextConceptRecommendations(studentId, limit);
        return ResponseEntity.ok(recommendations);
    }
}
