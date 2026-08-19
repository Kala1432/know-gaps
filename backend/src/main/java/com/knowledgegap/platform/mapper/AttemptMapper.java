package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.AttemptResponse;
import com.knowledgegap.platform.entity.Attempt;
import com.knowledgegap.platform.entity.QuestionConcept;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class AttemptMapper {

    public AttemptResponse toResponse(Attempt attempt) {
        if (attempt == null) {
            return null;
        }

        List<String> testedConcepts = attempt.getQuestion() != null && attempt.getQuestion().getQuestionConcepts() != null
                ? attempt.getQuestion().getQuestionConcepts().stream()
                    .map(qc -> qc.getConcept().getName())
                    .toList()
                : Collections.emptyList();

        return new AttemptResponse(
                attempt.getId(),
                attempt.getStudent().getId(),
                attempt.getQuestion().getId(),
                attempt.getSession() != null ? attempt.getSession().getId() : null,
                attempt.getUserAnswer(),
                attempt.isCorrect(),
                attempt.getResponseTimeMs(),
                attempt.getAttemptNumber(),
                attempt.getQuestion().getBaseDifficulty(),
                testedConcepts,
                attempt.getCreatedAt()
        );
    }
}
