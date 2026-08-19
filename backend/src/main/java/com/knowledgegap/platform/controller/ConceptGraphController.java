package com.knowledgegap.platform.controller;

import com.knowledgegap.platform.dto.response.ConceptResponse;
import com.knowledgegap.platform.dto.response.PrerequisiteChainResponse;
import com.knowledgegap.platform.service.ConceptGraphService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/concepts/{conceptId}")
public class ConceptGraphController {

    private final ConceptGraphService conceptGraphService;

    public ConceptGraphController(ConceptGraphService conceptGraphService) {
        this.conceptGraphService = conceptGraphService;
    }

    @GetMapping("/graph")
    public ResponseEntity<PrerequisiteChainResponse> getConceptGraph(@PathVariable UUID conceptId) {
        PrerequisiteChainResponse response = conceptGraphService.getConceptGraph(conceptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prerequisite-chain")
    public ResponseEntity<List<ConceptResponse>> getPrerequisiteChain(@PathVariable UUID conceptId) {
        List<ConceptResponse> response = conceptGraphService.getPrerequisiteChain(conceptId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/downstream-concepts")
    public ResponseEntity<List<ConceptResponse>> getDownstreamConcepts(@PathVariable UUID conceptId) {
        List<ConceptResponse> response = conceptGraphService.getDownstreamConcepts(conceptId);
        return ResponseEntity.ok(response);
    }
}
