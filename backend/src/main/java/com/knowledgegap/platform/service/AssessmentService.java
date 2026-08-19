package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.SubmitAttemptRequest;
import com.knowledgegap.platform.dto.response.AttemptResponse;
import com.knowledgegap.platform.dto.response.ConceptPerformanceResponse;

import java.util.List;
import java.util.UUID;

public interface AssessmentService {
    AttemptResponse submitAttempt(UUID sessionId, SubmitAttemptRequest request);
    List<AttemptResponse> getStudentAttemptHistory(UUID studentId);
    List<AttemptResponse> getSessionAttempts(UUID sessionId);
    ConceptPerformanceResponse getConceptPerformance(UUID studentId, UUID conceptId);
    List<ConceptPerformanceResponse> getStudentOverallPerformance(UUID studentId);
}
