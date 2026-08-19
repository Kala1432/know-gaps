package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.ConceptPrerequisiteResponse;
import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.entity.Concept;
import com.knowledgegap.platform.entity.ConceptPrerequisite;
import org.springframework.stereotype.Component;

@Component
public class ConceptMapper {

    public ConceptResponse toResponse(Concept concept) {
        if (concept == null) {
            return null;
        }
        return new ConceptResponse(
                concept.getId(),
                concept.getTopic() != null ? concept.getTopic().getId() : null,
                concept.getName(),
                concept.getDescription(),
                concept.getCreatedAt(),
                concept.getUpdatedAt()
        );
    }

    public ConceptPrerequisiteResponse toPrerequisiteResponse(ConceptPrerequisite prerequisite) {
        if (prerequisite == null) {
            return null;
        }
        return new ConceptPrerequisiteResponse(
                prerequisite.getId(),
                prerequisite.getPrerequisiteConcept().getId(),
                prerequisite.getPrerequisiteConcept().getName(),
                prerequisite.getTargetConcept().getId(),
                prerequisite.getTargetConcept().getName(),
                prerequisite.getDependencyType(),
                prerequisite.getWeight(),
                prerequisite.getCreatedAt()
        );
    }
}
