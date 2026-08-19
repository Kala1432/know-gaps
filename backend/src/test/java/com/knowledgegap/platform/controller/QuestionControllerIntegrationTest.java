package com.knowledgegap.platform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowledgegap.platform.dto.request.*;
import com.knowledgegap.platform.entity.QuestionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuestionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createQuestionAndAssociateConcept_Success() throws Exception {
        // 1. Create Subject, Topic, Concept
        CreateSubjectRequest subjectReq = new CreateSubjectRequest("Math", "Mathematics");
        String subjectRes = mockMvc.perform(post("/api/v1/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subjectReq)))
                .andReturn().getResponse().getContentAsString();
        String subjectId = objectMapper.readTree(subjectRes).get("id").asText();

        CreateTopicRequest topicReq = new CreateTopicRequest("Algebra", "Basic Algebra");
        String topicRes = mockMvc.perform(post("/api/v1/subjects/" + subjectId + "/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topicReq)))
                .andReturn().getResponse().getContentAsString();
        String topicId = objectMapper.readTree(topicRes).get("id").asText();

        CreateConceptRequest conceptReq = new CreateConceptRequest("Quadratic Equations", "Ax^2 + Bx + C = 0");
        String conceptRes = mockMvc.perform(post("/api/v1/topics/" + topicId + "/concepts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conceptReq)))
                .andReturn().getResponse().getContentAsString();
        String conceptId = objectMapper.readTree(conceptRes).get("id").asText();

        // 2. Create Question
        CreateQuestionRequest questionReq = new CreateQuestionRequest(
                "Solve x^2 - 5x + 6 = 0",
                QuestionType.MULTIPLE_CHOICE,
                0.4,
                1.2,
                "x=2, x=3"
        );

        String questionRes = mockMvc.perform(post("/api/v1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(questionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Solve x^2 - 5x + 6 = 0"))
                .andExpect(jsonPath("$.baseDifficulty").value(0.4))
                .andExpect(jsonPath("$.correctAnswer").value("x=2, x=3"))
                .andReturn().getResponse().getContentAsString();

        String questionId = objectMapper.readTree(questionRes).get("id").asText();

        // 3. Associate Question to Concept
        AddQuestionConceptRequest assocReq = new AddQuestionConceptRequest(UUID.fromString(conceptId), 1.0);
        mockMvc.perform(post("/api/v1/questions/" + questionId + "/concepts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assocReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conceptName").value("Quadratic Equations"));

        // 4. Retrieve Question Concepts
        mockMvc.perform(get("/api/v1/questions/" + questionId + "/concepts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].conceptId").value(conceptId));
    }
}
