package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.AddQuestionConceptRequest;
import com.knowledgegap.platform.dto.request.CreateQuestionRequest;
import com.knowledgegap.platform.dto.response.QuestionConceptResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface QuestionService {
    QuestionResponse createQuestion(CreateQuestionRequest request);
    QuestionResponse getQuestionById(UUID id);
    List<QuestionResponse> getAllQuestions();
    QuestionConceptResponse addQuestionConcept(UUID questionId, AddQuestionConceptRequest request);
    List<QuestionConceptResponse> getQuestionConcepts(UUID questionId);
}
