package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.AddQuestionConceptRequest;
import com.knowledgegap.platform.dto.request.CreateQuestionRequest;
import com.knowledgegap.platform.dto.response.QuestionConceptResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.Question;
import com.knowledgegap.platform.entity.QuestionConcept;
import com.knowledgegap.platform.exception.DuplicateResourceException;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.QuestionMapper;
import com.knowledgegap.platform.repository.ConceptRepository;
import com.knowledgegap.platform.repository.QuestionConceptRepository;
import com.knowledgegap.platform.repository.QuestionRepository;
import com.knowledgegap.platform.service.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final ConceptRepository conceptRepository;
    private final QuestionConceptRepository questionConceptRepository;
    private final QuestionMapper questionMapper;

    public QuestionServiceImpl(QuestionRepository questionRepository,
                               ConceptRepository conceptRepository,
                               QuestionConceptRepository questionConceptRepository,
                               QuestionMapper questionMapper) {
        this.questionRepository = questionRepository;
        this.conceptRepository = conceptRepository;
        this.questionConceptRepository = questionConceptRepository;
        this.questionMapper = questionMapper;
    }

    @Override
    @Transactional
    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        Question question = new Question(
                request.content(),
                request.questionType(),
                request.baseDifficulty(),
                request.discriminationIndex(),
                request.correctAnswer()
        );
        Question savedQuestion = questionRepository.save(question);
        return questionMapper.toResponse(savedQuestion);
    }

    @Override
    public QuestionResponse getQuestionById(UUID id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question with ID '" + id + "' not found"));
        return questionMapper.toResponse(question);
    }

    @Override
    public List<QuestionResponse> getAllQuestions() {
        return questionRepository.findAll().stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public QuestionConceptResponse addQuestionConcept(UUID questionId, AddQuestionConceptRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question with ID '" + questionId + "' not found"));

        Concept concept = conceptRepository.findById(request.conceptId())
                .orElseThrow(() -> new ResourceNotFoundException("Concept with ID '" + request.conceptId() + "' not found"));

        if (questionConceptRepository.existsByQuestionIdAndConceptId(questionId, request.conceptId())) {
            throw new DuplicateResourceException("Association already exists between question '" + questionId + "' and concept '" + request.conceptId() + "'");
        }

        QuestionConcept questionConcept = new QuestionConcept(question, concept, request.weight());
        QuestionConcept savedQuestionConcept = questionConceptRepository.save(questionConcept);

        return questionMapper.toQuestionConceptResponse(savedQuestionConcept);
    }

    @Override
    public List<QuestionConceptResponse> getQuestionConcepts(UUID questionId) {
        if (!questionRepository.existsById(questionId)) {
            throw new ResourceNotFoundException("Question with ID '" + questionId + "' not found");
        }
        return questionConceptRepository.findByQuestionId(questionId).stream()
                .map(questionMapper::toQuestionConceptResponse)
                .toList();
    }
}
