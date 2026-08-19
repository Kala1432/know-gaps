package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.MasteryState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MasteryStateRepository extends JpaRepository<MasteryState, UUID> {
    Optional<MasteryState> findByStudentIdAndConceptId(UUID studentId, UUID conceptId);
    List<MasteryState> findByStudentId(UUID studentId);
}
