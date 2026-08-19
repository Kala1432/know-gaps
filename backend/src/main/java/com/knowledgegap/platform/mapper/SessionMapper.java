package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.LearningSessionResponse;
import com.knowledgegap.platform.entity.LearningSession;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {

    public LearningSessionResponse toResponse(LearningSession session) {
        if (session == null) {
            return null;
        }
        int attemptCount = session.getAttempts() != null ? session.getAttempts().size() : 0;
        return new LearningSessionResponse(
                session.getId(),
                session.getStudent().getId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getEndedAt(),
                attemptCount
        );
    }
}
