package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.request.SubmitAttemptRequest;
import com.knowledgegap.platform.dto.response.AttemptResponse;
import com.knowledgegap.platform.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AttemptController {

    private final AssessmentService assessmentService;

    public AttemptController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/sessions/{sessionId}/attempts")
    public ResponseEntity<AttemptResponse> submitSessionAttempt(@PathVariable UUID sessionId,
                                                                 @Valid @RequestBody SubmitAttemptRequest request) {
        AttemptResponse response = assessmentService.submitAttempt(sessionId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/attempts")
    public ResponseEntity<AttemptResponse> submitAttempt(@Valid @RequestBody SubmitAttemptRequest request) {
        AttemptResponse response = assessmentService.submitAttempt(null, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/sessions/{sessionId}/attempts")
    public ResponseEntity<List<AttemptResponse>> getSessionAttempts(@PathVariable UUID sessionId) {
        List<AttemptResponse> attempts = assessmentService.getSessionAttempts(sessionId);
        return ResponseEntity.ok(attempts);
    }

    @GetMapping("/students/{studentId}/attempts")
    public ResponseEntity<List<AttemptResponse>> getStudentAttemptHistory(@PathVariable UUID studentId) {
        List<AttemptResponse> attempts = assessmentService.getStudentAttemptHistory(studentId);
        return ResponseEntity.ok(attempts);
    }
}
