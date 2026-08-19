package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.KnowledgeGap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface KnowledgeGapRepository extends JpaRepository<KnowledgeGap, UUID> {
    List<KnowledgeGap> findByStudentIdAndConceptId(UUID studentId, UUID conceptId);
    List<KnowledgeGap> findByStudentId(UUID studentId);
}
