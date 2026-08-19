package com.knowledgegap.platform.dto.response;

import java.util.List;

public record RecommendationResponse(
        String concept,
        String conceptId,
        double priority,
        int rank,
        String reason,
        double mastery,
        double retentionRisk,
        String prerequisiteStatus,
        String recommendedActivity
) {}
