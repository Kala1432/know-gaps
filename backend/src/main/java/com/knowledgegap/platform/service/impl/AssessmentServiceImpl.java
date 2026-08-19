package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.SubmitAttemptRequest;
import com.knowledgegap.platform.dto.response.AttemptResponse;
import com.knowledgegap.platform.dto.response.ConceptPerformanceResponse;
import com.knowledgegap.platform.entity.*;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.AttemptMapper;
import com.knowledgegap.platform.repository.*;
import com.knowledgegap.platform.service.AssessmentService;
import com.knowledgegap.platform.service.client.MlServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AssessmentServiceImpl implements AssessmentService {

    private final StudentRepository studentRepository;
    private final QuestionRepository questionRepository;
    private final LearningSessionRepository sessionRepository;
    private final AttemptRepository attemptRepository;
    private final MasteryStateRepository masteryStateRepository;
    private final KnowledgeGapRepository knowledgeGapRepository;
    private final ConceptRepository conceptRepository;
    private final AttemptMapper attemptMapper;
    private final MlServiceClient mlServiceClient;

    @Autowired
    public AssessmentServiceImpl(StudentRepository studentRepository,
                                  QuestionRepository questionRepository,
                                  LearningSessionRepository sessionRepository,
                                  AttemptRepository attemptRepository,
                                  MasteryStateRepository masteryStateRepository,
                                  KnowledgeGapRepository knowledgeGapRepository,
                                  ConceptRepository conceptRepository,
                                  AttemptMapper attemptMapper,
                                  @Autowired(required = false) MlServiceClient mlServiceClient) {
        this.studentRepository = studentRepository;
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
        this.attemptRepository = attemptRepository;
        this.masteryStateRepository = masteryStateRepository;
        this.knowledgeGapRepository = knowledgeGapRepository;
        this.conceptRepository = conceptRepository;
        this.attemptMapper = attemptMapper;
        this.mlServiceClient = mlServiceClient;
    }

    @Override
    @Transactional
    public AttemptResponse submitAttempt(UUID sessionId, SubmitAttemptRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + request.studentId() + "' not found"));

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question with ID '" + request.questionId() + "' not found"));

        LearningSession session = null;
        if (sessionId != null) {
            session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Learning session with ID '" + sessionId + "' not found"));
        }

        // Evaluate answer correctness
        boolean isCorrect = evaluateAnswer(question, request.userAnswer());

        // Determine attempt number for this (student, question)
        int previousAttempts = attemptRepository.countByStudentIdAndQuestionId(student.getId(), question.getId());
        int attemptNumber = previousAttempts + 1;

        Attempt attempt = new Attempt(
                student,
                question,
                session,
                request.userAnswer(),
                isCorrect,
                request.responseTimeMs(),
                attemptNumber
        );

        Attempt savedAttempt = attemptRepository.save(attempt);

        // Accumulate evidence and update MasteryState & KnowledgeGaps for concepts tested by question
        if (question.getQuestionConcepts() != null) {
            for (QuestionConcept qc : question.getQuestionConcepts()) {
                accumulateEvidence(student, qc.getConcept());
            }
        }

        return attemptMapper.toResponse(savedAttempt);
    }

    @Override
    public List<AttemptResponse> getStudentAttemptHistory(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student with ID '" + studentId + "' not found");
        }
        return attemptRepository.findByStudentIdOrderByCreatedAtDesc(studentId).stream()
                .map(attemptMapper::toResponse)
                .toList();
    }

    @Override
    public List<AttemptResponse> getSessionAttempts(UUID sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Learning session with ID '" + sessionId + "' not found");
        }
        return attemptRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(attemptMapper::toResponse)
                .toList();
    }

    @Override
    public ConceptPerformanceResponse getConceptPerformance(UUID studentId, UUID conceptId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + studentId + "' not found"));

        Concept concept = conceptRepository.findById(conceptId)
                .orElseThrow(() -> new ResourceNotFoundException("Concept with ID '" + conceptId + "' not found"));

        List<Attempt> conceptAttempts = attemptRepository.findByStudentIdAndConceptId(studentId, conceptId);
        int totalAttempts = conceptAttempts.size();
        int correctAttempts = (int) conceptAttempts.stream().filter(Attempt::isCorrect).count();
        double accuracyRate = totalAttempts > 0 ? (double) correctAttempts / totalAttempts : 0.0;
        double avgResponseTime = totalAttempts > 0
                ? conceptAttempts.stream().mapToLong(Attempt::getResponseTimeMs).average().orElse(0.0)
                : 0.0;

        Optional<MasteryState> masteryStateOpt = masteryStateRepository.findByStudentIdAndConceptId(studentId, conceptId);
        double masteryScore = masteryStateOpt.map(MasteryState::getMasteryScore).orElse(0.5);
        double confidenceLevel = masteryStateOpt.map(MasteryState::getConfidenceLevel).orElse(0.0);
        Instant lastEvaluated = masteryStateOpt.map(MasteryState::getLastEvaluatedAt).orElse(Instant.now());

        List<KnowledgeGap> gaps = knowledgeGapRepository.findByStudentIdAndConceptId(studentId, conceptId);
        List<String> gapTypes = gaps.stream().map(g -> g.getGapType().name()).toList();

        return new ConceptPerformanceResponse(
                studentId,
                conceptId,
                concept.getName(),
                totalAttempts,
                correctAttempts,
                accuracyRate,
                avgResponseTime,
                masteryScore,
                confidenceLevel,
                gapTypes,
                lastEvaluated
        );
    }

    @Override
    public List<ConceptPerformanceResponse> getStudentOverallPerformance(UUID studentId) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student with ID '" + studentId + "' not found");
        }

        List<MasteryState> masteryStates = masteryStateRepository.findByStudentId(studentId);
        List<ConceptPerformanceResponse> responses = new ArrayList<>();
        for (MasteryState ms : masteryStates) {
            responses.add(getConceptPerformance(studentId, ms.getConcept().getId()));
        }
        return responses;
    }

    private boolean evaluateAnswer(Question question, String userAnswer) {
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().isBlank()) {
            // Default evaluation fallback: non-empty answer considered recorded
            return userAnswer != null && !userAnswer.trim().isBlank();
        }
        return question.getCorrectAnswer().trim().equalsIgnoreCase(userAnswer.trim());
    }

    /**
     * Accumulates evidence for a (student, concept) pair across all attempts.
     * Delegates to ML Service if available, otherwise falls back to continuous Java calculation.
     * Enforces the Constitution rule: Single attempts MUST NOT cause instant 100% or 0% mastery jumps.
     */
    private void accumulateEvidence(Student student, Concept concept) {
        List<Attempt> attempts = attemptRepository.findByStudentIdAndConceptId(student.getId(), concept.getId());
        int n = attempts.size();
        if (n == 0) return;

        int correctCount = (int) attempts.stream().filter(Attempt::isCorrect).count();
        double rawAccuracy = (double) correctCount / n;
        double avgResponseTime = attempts.stream().mapToLong(Attempt::getResponseTimeMs).average().orElse(0.0);

        double confidenceLevel = Math.min(1.0, n / 5.0);
        double masteryScore;

        // Call Python ML Service if available
        MlServiceClient.PredictResponse mlResponse = null;
        if (mlServiceClient != null) {
            List<MlServiceClient.AttemptPayload> payloads = new ArrayList<>();
            for (Attempt a : attempts) {
                payloads.add(new MlServiceClient.AttemptPayload(
                        a.isCorrect(),
                        (double) a.getResponseTimeMs(),
                        a.getQuestion().getBaseDifficulty(),
                        a.getAttemptNumber()
                ));
            }

            MlServiceClient.PredictRequest mlReq = new MlServiceClient.PredictRequest(
                    concept.getId().toString(),
                    concept.getName(),
                    student.getId().toString(),
                    payloads
            );

            mlResponse = mlServiceClient.predictMastery(mlReq);
        }

        if (mlResponse != null) {
            masteryScore = mlResponse.mastery();
            confidenceLevel = mlResponse.confidence();
        } else {
            // Fallback continuous Bayesian-like evidence calculation: prior = 0.5 (neutral)
            masteryScore = (rawAccuracy * confidenceLevel) + (0.5 * (1.0 - confidenceLevel));
        }

        final double finalMasteryScore = masteryScore;
        final double finalConfidenceLevel = confidenceLevel;

        MasteryState masteryState = masteryStateRepository.findByStudentIdAndConceptId(student.getId(), concept.getId())
                .orElseGet(() -> new MasteryState(student, concept, finalMasteryScore, finalConfidenceLevel));

        masteryState.setMasteryScore(finalMasteryScore);
        masteryState.setConfidenceLevel(finalConfidenceLevel);
        masteryStateRepository.save(masteryState);

        // Knowledge Gap Evaluation
        // 1. Persistent Misconception: If student has 2+ attempts and accuracy < 50%
        if (n >= 2 && rawAccuracy < 0.5) {
            ensureKnowledgeGap(student, concept, GapType.PERSISTENT_MISCONCEPTION, 1.0 - rawAccuracy);
        }

        // 2. Fluency Lag: If student gets answers right but average response time is > 15,000 ms (high cognitive load)
        if (rawAccuracy >= 0.7 && avgResponseTime > 15000) {
            ensureKnowledgeGap(student, concept, GapType.FLUENCY_LAG, 0.7);
        }
    }

    private void ensureKnowledgeGap(Student student, Concept concept, GapType gapType, double severity) {
        List<KnowledgeGap> existing = knowledgeGapRepository.findByStudentIdAndConceptId(student.getId(), concept.getId());
        boolean hasGap = existing.stream().anyMatch(g -> g.getGapType() == gapType);
        if (!hasGap) {
            KnowledgeGap gap = new KnowledgeGap(student, concept, gapType, severity);
            knowledgeGapRepository.save(gap);
        }
    }
}
