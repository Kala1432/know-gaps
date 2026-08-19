package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.StartSessionRequest;
import com.knowledgegap.platform.dto.response.LearningSessionResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;

import java.util.List;
import java.util.UUID;

public interface SessionService {
    LearningSessionResponse startSession(StartSessionRequest request);
    LearningSessionResponse completeSession(UUID sessionId);
    LearningSessionResponse getSessionById(UUID sessionId);
    List<QuestionResponse> getSessionQuestions(UUID sessionId);
}
