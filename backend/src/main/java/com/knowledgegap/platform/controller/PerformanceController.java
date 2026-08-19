package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.response.ConceptPerformanceResponse;
import com.knowledgegap.platform.dto.response.PatternResponse;
import com.knowledgegap.platform.entity.Attempt;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.Student;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.repository.AttemptRepository;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.repository.StudentRepository;
import com.knowledgegap.platform.service.AssessmentService;
import com.knowledgegap.platform.service.client.MlServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/{studentId}")
public class PerformanceController {

    private final AssessmentService assessmentService;
    private final StudentRepository studentRepository;
    private final ConceptRepository conceptRepository;
    private final AttemptRepository attemptRepository;
    private final MlServiceClient mlServiceClient;

    @Autowired
    public PerformanceController(AssessmentService assessmentService,
                                 StudentRepository studentRepository,
                                 ConceptRepository conceptRepository,
                                 AttemptRepository attemptRepository,
                                 @Autowired(required = false) MlServiceClient mlServiceClient) {
        this.assessmentService = assessmentService;
        this.studentRepository = studentRepository;
        this.conceptRepository = conceptRepository;
        this.attemptRepository = attemptRepository;
        this.mlServiceClient = mlServiceClient;
    }

    @GetMapping("/concepts/{conceptId}/performance")
    public ResponseEntity<ConceptPerformanceResponse> getConceptPerformance(@PathVariable UUID studentId,
                                                                             @PathVariable UUID conceptId) {
        ConceptPerformanceResponse response = assessmentService.getConceptPerformance(studentId, conceptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/performance")
    public ResponseEntity<List<ConceptPerformanceResponse>> getOverallPerformance(@PathVariable UUID studentId) {
        List<ConceptPerformanceResponse> response = assessmentService.getStudentOverallPerformance(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/concepts/{conceptId}/retention-risk")
    public ResponseEntity<?> getRetentionRisk(@PathVariable UUID studentId, @PathVariable UUID conceptId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + studentId + "' not found"));

        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept with ID '" + conceptId + "' not found"));

        ConceptPerformanceResponse perf = assessmentService.getConceptPerformance(studentId, conceptId);
        List<Attempt> attempts = attemptRepository.findByStudentIdAndConceptId(studentId, conceptId);

        MlServiceClient.RetentionPredictResponse mlResponse = null;
        if (mlServiceClient != null) {
            List<MlServiceClient.AttemptPayload> payloads = attempts.stream()
                    .map(a -> new MlServiceClient.AttemptPayload(
                            a.isCorrect(),
                            (double) a.getResponseTimeMs(),
                            a.getQuestion().getBaseDifficulty(),
                            a.getAttemptNumber()
                    )).toList();

            MlServiceClient.RetentionPredictRequest mlReq = new MlServiceClient.RetentionPredictRequest(
                    concept.getId().toString(),
                    concept.getName(),
                    student.getId().toString(),
                    perf.masteryScore(),
                    payloads
            );

            mlResponse = mlServiceClient.predictRetentionRisk(mlReq);
        }

        if (mlResponse != null) {
            return ResponseEntity.ok(mlResponse);
        }

        // Fallback transparent baseline response
        double fallbackRisk = Math.min(1.0, Math.max(0.0, 1.0 - perf.masteryScore()));
        String riskLevel = fallbackRisk >= 0.65 ? "high" : (fallbackRisk >= 0.35 ? "medium" : "low");
        String reviewWindow = fallbackRisk >= 0.65 ? "today" : (fallbackRisk >= 0.35 ? "soon" : "later");
        String explanation = String.format("Retention risk for '%s' is estimated at %.2f. Recommended review window is '%s'.",
                concept.getName(), fallbackRisk, reviewWindow);

        return ResponseEntity.ok(Map.of(
                "concept", concept.getName(),
                "concept_id", concept.getId().toString(),
                "retention_risk", Math.round(fallbackRisk * 100.0) / 100.0,
                "risk_level", riskLevel,
                "recommended_review_window", reviewWindow,
                "explanation", explanation,
                "is_prototype", true
        ));
    }

    @GetMapping("/patterns")
    public ResponseEntity<List<PatternResponse>> getObservableMistakePatterns(@PathVariable UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student with ID '" + studentId + "' not found");
        }

        List<Concept> concepts = conceptRepository.findAll();
        List<PatternResponse> patterns = new ArrayList<>();

        if (mlServiceClient != null) {
            for (Concept c : concepts) {
                List<Attempt> attempts = attemptRepository.findByStudentIdAndConceptId(studentId, c.getId());
                if (attempts.isEmpty()) continue;

                List<MlServiceClient.AttemptPayload> payloads = attempts.stream()
                        .map(a -> new MlServiceClient.AttemptPayload(
                                a.isCorrect(),
                                (double) a.getResponseTimeMs(),
                                a.getQuestion().getBaseDifficulty(),
                                a.getAttemptNumber()
                        )).toList();

                MlServiceClient.PatternDetectionPayloadRequest req = new MlServiceClient.PatternDetectionPayloadRequest(
                        c.getName(),
                        payloads,
                        false,
                        ""
                );

                List<MlServiceClient.ObservablePatternPayload> detected = mlServiceClient.detectPatterns(req);
                for (MlServiceClient.ObservablePatternPayload p : detected) {
                    patterns.add(new PatternResponse(
                            p.pattern_type(),
                            p.concept_name(),
                            p.severity(),
                            p.description(),
                            p.evidence_summary()
                    ));
                }
            }
        }

        return ResponseEntity.ok(patterns);
    }
}
