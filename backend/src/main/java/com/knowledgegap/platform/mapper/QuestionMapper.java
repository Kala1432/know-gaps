package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.QuestionConceptResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;
import com.knowledgegap.platform.entity.Question;
import com.knowledgegap.platform.entity.QuestionConcept;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class QuestionMapper {

    public QuestionResponse toResponse(Question question) {
        if (question == null) {
            return null;
        }

        List<QuestionConceptResponse> conceptResponses = question.getQuestionConcepts() != null
                ? question.getQuestionConcepts().stream().map(this::toQuestionConceptResponse).toList()
                : Collections.emptyList();

        return new QuestionResponse(
                question.getId(),
                question.getContent(),
                question.getQuestionType(),
                question.getBaseDifficulty(),
                question.getDiscriminationIndex(),
                question.getCorrectAnswer(),
                conceptResponses,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    public QuestionConceptResponse toQuestionConceptResponse(QuestionConcept questionConcept) {
        if (questionConcept == null) {
            return null;
        }
        return new QuestionConceptResponse(
                questionConcept.getId(),
                questionConcept.getQuestion().getId(),
                questionConcept.getConcept().getId(),
                questionConcept.getConcept().getName(),
                questionConcept.getWeight(),
                questionConcept.getCreatedAt()
        );
    }
}
