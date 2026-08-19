package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.request.StartSessionRequest;
import com.knowledgegap.platform.dto.response.LearningSessionResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;
import com.knowledgegap.platform.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<LearningSessionResponse> startSession(@Valid @RequestBody StartSessionRequest request) {
        LearningSessionResponse response = sessionService.startSession(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<LearningSessionResponse> completeSession(@PathVariable UUID sessionId) {
        LearningSessionResponse response = sessionService.completeSession(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<LearningSessionResponse> getSessionById(@PathVariable UUID sessionId) {
        LearningSessionResponse response = sessionService.getSessionById(sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/questions")
    public ResponseEntity<List<QuestionResponse>> getSessionQuestions(@PathVariable UUID sessionId) {
        List<QuestionResponse> questions = sessionService.getSessionQuestions(sessionId);
        return ResponseEntity.ok(questions);
    }
}
