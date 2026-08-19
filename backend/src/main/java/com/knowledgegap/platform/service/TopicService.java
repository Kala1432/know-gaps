package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.CreateTopicRequest;
import com.knowledgegap.platform.dto.response.TopicResponse;

import java.util.List;
import java.util.UUID;

public interface TopicService {
    TopicResponse createTopic(UUID subjectId, CreateTopicRequest request);
    TopicResponse getTopicById(UUID id);
    List<TopicResponse> getTopicsBySubjectId(UUID subjectId);
}
