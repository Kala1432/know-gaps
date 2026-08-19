package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.LearningSession;
import com.knowledgegap.platform.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LearningSessionRepository extends JpaRepository<LearningSession, UUID> {
    List<LearningSession> findByStudentId(UUID studentId);
    Optional<LearningSession> findFirstByStudentIdAndStatusOrderByStartedAtDesc(UUID studentId, SessionStatus status);
}
