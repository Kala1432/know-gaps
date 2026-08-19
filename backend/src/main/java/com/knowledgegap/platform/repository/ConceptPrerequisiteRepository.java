package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.ConceptPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConceptPrerequisiteRepository extends JpaRepository<ConceptPrerequisite, UUID> {
    List<ConceptPrerequisite> findByTargetConceptId(UUID targetConceptId);
    List<ConceptPrerequisite> findByPrerequisiteConceptId(UUID prerequisiteConceptId);
    boolean existsByPrerequisiteConceptIdAndTargetConceptId(UUID prerequisiteConceptId, UUID targetConceptId);
}
