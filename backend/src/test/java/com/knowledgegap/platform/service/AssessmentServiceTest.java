package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.SubmitAttemptRequest;
import com.knowledgegap.platform.dto.response.AttemptResponse;
import com.knowledgegap.platform.dto.response.ConceptPerformanceResponse;
import com.knowledgegap.platform.entity.*;
import com.knowledgegap.platform.mapper.AttemptMapper;
import com.knowledgegap.platform.repository.*;
import com.knowledgegap.platform.service.impl.AssessmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private StudentRepository studentRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private LearningSessionRepository sessionRepository;
    @Mock
    private AttemptRepository attemptRepository;
    @Mock
    private MasteryStateRepository masteryStateRepository;
    @Mock
    private KnowledgeGapRepository knowledgeGapRepository;
    @Mock
    private ConceptRepository conceptRepository;

    private AttemptMapper attemptMapper;
    private AssessmentServiceImpl assessmentService;

    private Student student;
    private Question question;
    private Concept concept;

    @BeforeEach
    void setUp() {
        attemptMapper = new AttemptMapper();
        assessmentService = new AssessmentServiceImpl(
                studentRepository,
                questionRepository,
                sessionRepository,
                attemptRepository,
                masteryStateRepository,
                knowledgeGapRepository,
                conceptRepository,
                attemptMapper,
                null // MlServiceClient null for baseline unit testing fallback
        );

        UUID studentId = UUID.randomUUID();
        student = new Student("alice@example.com", "Alice");
        student.setId(studentId);

        UUID conceptId = UUID.randomUUID();
        concept = new Concept(null, "HashMap", "Hash table");
        concept.setId(conceptId);

        UUID questionId = UUID.randomUUID();
        question = new Question("What is O(1) lookup?", QuestionType.TEXT, 0.3, 1.0, "HashMap");
        question.setId(questionId);

        QuestionConcept qc = new QuestionConcept(question, concept, 1.0);
        question.setQuestionConcepts(List.of(qc));
    }

    @Test
    void submitAttempt_ValidCorrectAnswer_RecordsAttemptAndResponseTime() {
        given(studentRepository.findById(student.getId())).willReturn(Optional.of(student));
        given(questionRepository.findById(question.getId())).willReturn(Optional.of(question));
        given(attemptRepository.countByStudentIdAndQuestionId(student.getId(), question.getId())).willReturn(0);

        Attempt savedAttempt = new Attempt(student, question, null, "HashMap", true, 4500L, 1);
        savedAttempt.setId(UUID.randomUUID());
        given(attemptRepository.save(any(Attempt.class))).willReturn(savedAttempt);

        SubmitAttemptRequest request = new SubmitAttemptRequest(student.getId(), question.getId(), "HashMap", 4500L);
        AttemptResponse response = assessmentService.submitAttempt(null, request);

        assertThat(response).isNotNull();
        assertThat(response.isCorrect()).isTrue();
        assertThat(response.responseTimeMs()).isEqualTo(4500L);
        assertThat(response.attemptNumber()).isEqualTo(1);
        verify(attemptRepository).save(any(Attempt.class));
    }

    @Test
    void submitAttempt_RepeatedAttempt_IncrementsAttemptNumber() {
        given(studentRepository.findById(student.getId())).willReturn(Optional.of(student));
        given(questionRepository.findById(question.getId())).willReturn(Optional.of(question));
        given(attemptRepository.countByStudentIdAndQuestionId(student.getId(), question.getId())).willReturn(2);

        Attempt savedAttempt = new Attempt(student, question, null, "HashMap", true, 3000L, 3);
        savedAttempt.setId(UUID.randomUUID());
        given(attemptRepository.save(any(Attempt.class))).willReturn(savedAttempt);

        SubmitAttemptRequest request = new SubmitAttemptRequest(student.getId(), question.getId(), "HashMap", 3000L);
        AttemptResponse response = assessmentService.submitAttempt(null, request);

        assertThat(response.attemptNumber()).isEqualTo(3);
    }

    @Test
    void evidenceAccumulation_SingleCorrectAttempt_DoesNotJumpTo100Mastery() {
        given(studentRepository.findById(student.getId())).willReturn(Optional.of(student));
        given(conceptRepository.findById(concept.getId())).willReturn(Optional.of(concept));

        Attempt attempt1 = new Attempt(student, question, null, "HashMap", true, 5000L, 1);
        given(attemptRepository.findByStudentIdAndConceptId(student.getId(), concept.getId())).willReturn(List.of(attempt1));

        MasteryState masteryState = new MasteryState(student, concept, 0.60, 0.20);
        given(masteryStateRepository.findByStudentIdAndConceptId(student.getId(), concept.getId())).willReturn(Optional.of(masteryState));
        given(knowledgeGapRepository.findByStudentIdAndConceptId(student.getId(), concept.getId())).willReturn(new ArrayList<>());

        ConceptPerformanceResponse performance = assessmentService.getConceptPerformance(student.getId(), concept.getId());

        assertThat(performance.accuracyRate()).isEqualTo(1.0);
        assertThat(performance.masteryScore()).isLessThan(1.0); // Verifies NO instant 100% mastery on 1 attempt
    }
}
