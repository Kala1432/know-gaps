package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.request.CreateTopicRequest;
import com.knowledgegap.platform.dto.response.TopicResponse;
import com.knowledgegap.platform.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class TopicController {

    private final TopicService topicService;

    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @PostMapping("/subjects/{subjectId}/topics")
    public ResponseEntity<TopicResponse> createTopic(@PathVariable UUID subjectId,
                                                     @Valid @RequestBody CreateTopicRequest request) {
        TopicResponse response = topicService.createTopic(subjectId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/topics/{id}")
    public ResponseEntity<TopicResponse> getTopicById(@PathVariable UUID id) {
        TopicResponse response = topicService.getTopicById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/subjects/{subjectId}/topics")
    public ResponseEntity<List<TopicResponse>> getTopicsBySubjectId(@PathVariable UUID subjectId) {
        List<TopicResponse> response = topicService.getTopicsBySubjectId(subjectId);
        return ResponseEntity.ok(response);
    }
}
