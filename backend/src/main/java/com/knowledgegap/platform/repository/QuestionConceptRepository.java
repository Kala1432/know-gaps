package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.QuestionConcept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionConceptRepository extends JpaRepository<QuestionConcept, UUID> {
    List<QuestionConcept> findByQuestionId(UUID questionId);
    List<QuestionConcept> findByConceptId(UUID conceptId);
    boolean existsByQuestionIdAndConceptId(UUID questionId, UUID conceptId);
}
