package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.SubjectResponse;
import com.knowledgegap.platform.entity.Subject;
import org.springframework.stereotype.Component;

@Component
public class SubjectMapper {

    public SubjectResponse toResponse(Subject subject) {
        if (subject == null) {
            return null;
        }
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getDescription(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }
}
