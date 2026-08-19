package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.CreateTopicRequest;
import com.knowledgegap.platform.dto.response.TopicResponse;
import com.knowledgegap.platform.entity.Subject;
import com.knowledgegap.platform.entity.Topic;
import com.knowledgegap.platform.exception.DuplicateResourceException;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.TopicMapper;
import com.knowledgegap.platform.repository.SubjectRepository;
import com.knowledgegap.platform.repository.TopicRepository;
import com.knowledgegap.platform.service.TopicService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TopicServiceImpl implements TopicService {

    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;

    public TopicServiceImpl(SubjectRepository subjectRepository, TopicRepository topicRepository, TopicMapper topicMapper) {
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.topicMapper = topicMapper;
    }

    @Override
    @Transactional
    public TopicResponse createTopic(UUID subjectId, CreateTopicRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found"));

        if (topicRepository.existsBySubjectIdAndName(subjectId, request.name())) {
            throw new DuplicateResourceException("Topic with name '" + request.name() + "' already exists under subject ID '" + subjectId + "'");
        }

        Topic topic = new Topic(subject, request.name(), request.description());
        Topic savedTopic = topicRepository.save(topic);
        return topicMapper.toResponse(savedTopic);
    }

    @Override
    public TopicResponse getTopicById(UUID id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic with ID '" + id + "' not found"));
        return topicMapper.toResponse(topic);
    }

    @Override
    public List<TopicResponse> getTopicsBySubjectId(UUID subjectId) {
        if (!subjectRepository.existsById(subjectId)) {
            throw new ResourceNotFoundException("Subject with ID '" + subjectId + "' not found");
        }
        return topicRepository.findBySubjectId(subjectId).stream()
                .map(topicMapper::toResponse)
                .toList();
    }
}
