package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.request.AddQuestionConceptRequest;
import com.knowledgegap.platform.dto.request.CreateQuestionRequest;
import com.knowledgegap.platform.dto.response.QuestionConceptResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;
import com.knowledgegap.platform.service.QuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody CreateQuestionRequest request) {
        QuestionResponse response = questionService.createQuestion(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable UUID id) {
        QuestionResponse response = questionService.getQuestionById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> getAllQuestions() {
        List<QuestionResponse> response = questionService.getAllQuestions();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{questionId}/concepts")
    public ResponseEntity<QuestionConceptResponse> addQuestionConcept(@PathVariable UUID questionId,
                                                                       @Valid @RequestBody AddQuestionConceptRequest request) {
        QuestionConceptResponse response = questionService.addQuestionConcept(questionId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{questionId}/concepts")
    public ResponseEntity<List<QuestionConceptResponse>> getQuestionConcepts(@PathVariable UUID questionId) {
        List<QuestionConceptResponse> response = questionService.getQuestionConcepts(questionId);
        return ResponseEntity.ok(response);
    }
}
