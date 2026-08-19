package com.knowledgegap.platform.service.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class MlServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MlServiceClient.class);
    private final RestClient restClient;

    public MlServiceClient(@Value("${ml.service.url:http://localhost:8000}") String mlServiceUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(mlServiceUrl)
                .build();
    }

    public record AttemptPayload(
            boolean is_correct,
            double response_time_ms,
            double base_difficulty,
            int attempt_number
    ) {}

    public record PredictRequest(
            String concept_id,
            String concept_name,
            String student_id,
            List<AttemptPayload> attempts
    ) {}

    public record PredictResponse(
            String concept,
            String concept_id,
            double mastery,
            double confidence,
            int evidence_count,
            String trend,
            String risk,
            String explanation
    ) {}

    public record RetentionPredictRequest(
            String concept_id,
            String concept_name,
            String student_id,
            double current_mastery,
            List<AttemptPayload> attempts
    ) {}

    public record RetentionPredictResponse(
            String concept,
            String concept_id,
            double retention_risk,
            String risk_level,
            String recommended_review_window,
            String explanation,
            boolean is_prototype
    ) {}

    public record StudentConceptStatePayload(
            String concept_id,
            String concept_name,
            double mastery,
            double retention_risk,
            int evidence_count
    ) {}

    public record PrerequisiteEdgePayload(
            String prerequisite_concept_id,
            String target_concept_id
    ) {}

    public record RecommendationPayloadRequest(
            String student_id,
            List<StudentConceptStatePayload> concept_states,
            List<PrerequisiteEdgePayload> prerequisites,
            int limit
    ) {}

    public record ConceptRecommendationPayload(
            String concept,
            String concept_id,
            double priority,
            int rank,
            String reason,
            double mastery,
            double retention_risk,
            String prerequisite_status,
            String recommended_activity
    ) {}

    public record RecommendationPayloadResponse(
            String student_id,
            List<ConceptRecommendationPayload> recommendations,
            double prerequisite_constraint_satisfaction_rate
    ) {}

    public record PatternDetectionPayloadRequest(
            String concept_name,
            List<AttemptPayload> attempts,
            boolean has_unmastered_prerequisite,
            String prerequisite_name
    ) {}

    public record ObservablePatternPayload(
            String pattern_type,
            String concept_name,
            double severity,
            String description,
            String evidence_summary
    ) {}

    public PredictResponse predictMastery(PredictRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/mastery/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(PredictResponse.class);
        } catch (Exception ex) {
            log.warn("ML Service unavailable for mastery prediction. Falling back: {}", ex.getMessage());
            return null;
        }
    }

    public RetentionPredictResponse predictRetentionRisk(RetentionPredictRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/retention/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RetentionPredictResponse.class);
        } catch (Exception ex) {
            log.warn("ML Service unavailable for retention risk prediction. Falling back: {}", ex.getMessage());
            return null;
        }
    }

    public RecommendationPayloadResponse getRecommendations(RecommendationPayloadRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/recommendations/next-concept")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RecommendationPayloadResponse.class);
        } catch (Exception ex) {
            log.warn("ML Service unavailable for next concept recommendations. Falling back: {}", ex.getMessage());
            return null;
        }
    }

    public List<ObservablePatternPayload> detectPatterns(PatternDetectionPayloadRequest request) {
        try {
            return restClient.post()
                    .uri("/api/v1/patterns/detect")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ObservablePatternPayload>>() {});
        } catch (Exception ex) {
            log.warn("ML Service unavailable for pattern detection. Falling back: {}", ex.getMessage());
            return List.of();
        }
    }
}
