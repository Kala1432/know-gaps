package com.knowledgegap.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegap.platform.dto.request.AddPrerequisiteRequest;
import com.knowledgegap.platform.dto.request.CreateConceptRequest;
import com.knowledgegap.platform.dto.request.CreateSubjectRequest;
import com.knowledgegap.platform.dto.request.CreateTopicRequest;
import com.knowledgegap.platform.entity.DependencyType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CurriculumIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void endToEndCurriculumAndPrerequisiteFlow() throws Exception {
        // 1. Create Subject
        CreateSubjectRequest subjectReq = new CreateSubjectRequest("Computer Science", "Core CS domain");
        String subjectRes = mockMvc.perform(post("/api/v1/subjects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subjectReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Computer Science"))
                .andReturn().getResponse().getContentAsString();
        String subjectId = objectMapper.readTree(subjectRes).get("id").asText();

        // 2. Create Topic under Subject
        CreateTopicRequest topicReq = new CreateTopicRequest("Data Structures", "Linear and Non-linear structures");
        String topicRes = mockMvc.perform(post("/api/v1/subjects/" + subjectId + "/topics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(topicReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Data Structures"))
                .andReturn().getResponse().getContentAsString();
        String topicId = objectMapper.readTree(topicRes).get("id").asText();

        // 3. Create Concept 1 (Arrays)
        CreateConceptRequest concept1Req = new CreateConceptRequest("Arrays", "Sequential memory layout");
        String concept1Res = mockMvc.perform(post("/api/v1/topics/" + topicId + "/concepts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(concept1Req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String concept1Id = objectMapper.readTree(concept1Res).get("id").asText();

        // 4. Create Concept 2 (HashMap)
        CreateConceptRequest concept2Req = new CreateConceptRequest("HashMap", "Hash table key-value pairs");
        String concept2Res = mockMvc.perform(post("/api/v1/topics/" + topicId + "/concepts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(concept2Req)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String concept2Id = objectMapper.readTree(concept2Res).get("id").asText();

        // 5. Add Prerequisite: Arrays -> HashMap
        AddPrerequisiteRequest prereqReq = new AddPrerequisiteRequest(
                java.util.UUID.fromString(concept1Id),
                DependencyType.HARD_REQUIREMENT,
                1.0
        );
        mockMvc.perform(post("/api/v1/concepts/" + concept2Id + "/prerequisites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(prereqReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prerequisiteConceptName").value("Arrays"))
                .andExpect(jsonPath("$.targetConceptName").value("HashMap"));

        // 6. Get Prerequisites for HashMap
        mockMvc.perform(get("/api/v1/concepts/" + concept2Id + "/prerequisites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prerequisiteConceptId").value(concept1Id));
    }
}
