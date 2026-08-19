package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.request.AddPrerequisiteRequest;
import com.knowledgegap.platform.dto.request.CreateConceptRequest;
import com.knowledgegap.platform.dto.response.ConceptPrerequisiteResponse;
import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.service.ConceptService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ConceptController {

    private final ConceptService conceptService;

    public ConceptController(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    @PostMapping("/topics/{topicId}/concepts")
    public ResponseEntity<ConceptResponse> createConcept(@PathVariable UUID topicId,
                                                         @Valid @RequestBody CreateConceptRequest request) {
        ConceptResponse response = conceptService.createConcept(topicId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/concepts/{id}")
    public ResponseEntity<ConceptResponse> getConceptById(@PathVariable UUID id) {
        ConceptResponse response = conceptService.getConceptById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/topics/{topicId}/concepts")
    public ResponseEntity<List<ConceptResponse>> getConceptsByTopicId(@PathVariable UUID topicId) {
        List<ConceptResponse> response = conceptService.getConceptsByTopicId(topicId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/concepts/{conceptId}/prerequisites")
    public ResponseEntity<ConceptPrerequisiteResponse> addPrerequisite(@PathVariable UUID conceptId,
                                                                        @Valid @RequestBody AddPrerequisiteRequest request) {
        ConceptPrerequisiteResponse response = conceptService.addPrerequisite(conceptId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/concepts/{conceptId}/prerequisites")
    public ResponseEntity<List<ConceptPrerequisiteResponse>> getPrerequisites(@PathVariable UUID conceptId) {
        List<ConceptPrerequisiteResponse> response = conceptService.getPrerequisites(conceptId);
        return ResponseEntity.ok(response);
    }
}
