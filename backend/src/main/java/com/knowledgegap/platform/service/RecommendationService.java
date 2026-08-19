package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.response.RecommendationResponse;

import java.util.List;
import java.util.UUID;

public interface RecommendationService {
    List<RecommendationResponse> getNextConceptRecommendations(UUID studentId, int limit);
}
