package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.response.ConceptPerformanceResponse;
import com.knowledgegap.platform.dto.response.RecommendationResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.ConceptPrerequisite;
import com.knowledgegap.platform.entity.MasteryState;
import com.knowledgegap.platform.entity.Student;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.repository.ConceptPrerequisiteRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.repository.MasteryStateRepository;
import com.knowledgegap.platform.repository.StudentRepository;
import com.knowledgegap.platform.service.AssessmentService;
import com.knowledgegap.platform.service.RecommendationService;
import com.knowledgegap.platform.service.client.MlServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RecommendationServiceImpl implements RecommendationService {

    private final StudentRepository studentRepository;
    private final ConceptRepository conceptRepository;
    private final ConceptPrerequisiteRepository prerequisiteRepository;
    private final MasteryStateRepository masteryStateRepository;
    private final AssessmentService assessmentService;
    private final MlServiceClient mlServiceClient;

    @Autowired
    public RecommendationServiceImpl(StudentRepository studentRepository,
                                      ConceptRepository conceptRepository,
                                      ConceptPrerequisiteRepository prerequisiteRepository,
                                      MasteryStateRepository masteryStateRepository,
                                      AssessmentService assessmentService,
                                      @Autowired(required = false) MlServiceClient mlServiceClient) {
        this.studentRepository = studentRepository;
        this.conceptRepository = conceptRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.masteryStateRepository = masteryStateRepository;
        this.assessmentService = assessmentService;
        this.mlServiceClient = mlServiceClient;
    }

    @Override
    public List<RecommendationResponse> getNextConceptRecommendations(UUID studentId, int limit) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + studentId + "' not found"));

        List<Concept> allConcepts = conceptRepository.findAll();
        if (allConcepts.isEmpty()) {
            return List.of();
        }

        List<MlServiceClient.StudentConceptStatePayload> states = new ArrayList<>();
        for (Concept c : allConcepts) {
            ConceptPerformanceResponse perf = assessmentService.getConceptPerformance(student.getId(), c.getId());
            double retentionRisk = Math.min(1.0, Math.max(0.0, 1.0 - perf.masteryScore()));
            states.add(new MlServiceClient.StudentConceptStatePayload(
                    c.getId().toString(),
                    c.getName(),
                    perf.masteryScore(),
                    retentionRisk,
                    perf.totalAttempts()
            ));
        }

        List<ConceptPrerequisite> allPrereqs = prerequisiteRepository.findAll();
        List<MlServiceClient.PrerequisiteEdgePayload> edges = allPrereqs.stream()
                .map(p -> new MlServiceClient.PrerequisiteEdgePayload(
                        p.getPrerequisiteConcept().getId().toString(),
                        p.getTargetConcept().getId().toString()
                )).toList();

        MlServiceClient.RecommendationPayloadResponse mlResponse = null;
        if (mlServiceClient != null) {
            MlServiceClient.RecommendationPayloadRequest req = new MlServiceClient.RecommendationPayloadRequest(
                    student.getId().toString(),
                    states,
                    edges,
                    limit
            );
            mlResponse = mlServiceClient.getRecommendations(req);
        }

        if (mlResponse != null && mlResponse.recommendations() != null) {
            return mlResponse.recommendations().stream()
                    .map(r -> new RecommendationResponse(
                            r.concept(),
                            r.concept_id(),
                            r.priority(),
                            r.rank(),
                            r.reason(),
                            r.mastery(),
                            r.retention_risk(),
                            r.prerequisite_status(),
                            r.recommended_activity()
                    )).toList();
        }

        // Fallback Java deterministic recommendation logic
        List<RecommendationResponse> fallbacks = new ArrayList<>();
        int rank = 1;
        for (Concept c : allConcepts) {
            if (rank > limit) break;
            ConceptPerformanceResponse perf = assessmentService.getConceptPerformance(student.getId(), c.getId());
            double risk = Math.min(1.0, Math.max(0.0, 1.0 - perf.masteryScore()));
            String activity = perf.masteryScore() < 0.60 ? "PRACTICE_QUESTIONS" : "CONCEPT_REVIEW";
            String reason = String.format("Practice %s next to build mastery (currently at %.0f%%).", c.getName(), perf.masteryScore() * 100);
            fallbacks.add(new RecommendationResponse(
                    c.getName(),
                    c.getId().toString(),
                    1.0 - perf.masteryScore(),
                    rank++,
                    reason,
                    perf.masteryScore(),
                    risk,
                    "SATISFIED",
                    activity
            ));
        }
        return fallbacks;
    }
}
