package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.TopicResponse;
import com.knowledgegap.platform.entity.Topic;
import org.springframework.stereotype.Component;

@Component
public class TopicMapper {

    public TopicResponse toResponse(Topic topic) {
        if (topic == null) {
            return null;
        }
        return new TopicResponse(
                topic.getId(),
                topic.getSubject() != null ? topic.getSubject().getId() : null,
                topic.getName(),
                topic.getDescription(),
                topic.getCreatedAt(),
                topic.getUpdatedAt()
        );
    }
}
