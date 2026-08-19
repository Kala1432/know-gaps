package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.Attempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
    List<Attempt> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
    List<Attempt> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
    
    int countByStudentIdAndQuestionId(UUID studentId, UUID questionId);

    @Query("""
        SELECT a FROM Attempt a
        JOIN a.question q
        JOIN q.questionConcepts qc
        WHERE a.student.id = :studentId AND qc.concept.id = :conceptId
        ORDER BY a.createdAt DESC
    """)
    List<Attempt> findByStudentIdAndConceptId(@Param("studentId") UUID studentId, @Param("conceptId") UUID conceptId);
}
