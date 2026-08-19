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
class AssessmentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void endToEndAssessmentFlowAndPerformanceAccumulation() throws Exception {
        // 1. Create Student
        CreateStudentRequest studentReq = new CreateStudentRequest("bob@example.com", "Bob");
        String studentRes = mockMvc.perform(post("/api/v1/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentReq)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String studentId = objectMapper.readTree(studentRes).get("id").asText();

        // 2. Create Subject, Topic, Concept
        CreateSubjectRequest subjectReq = new CreateSubjectRequest("Computer Science", "CS");
        String subjectRes = mockMvc.perform(post("/api/v1/subjects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(subjectReq)))
                .andReturn().getResponse().getContentAsString();
        String subjectId = objectMapper.readTree(subjectRes).get("id").asText();

        CreateTopicRequest topicReq = new CreateTopicRequest("Algorithms", "Algo");
        String topicRes = mockMvc.perform(post("/api/v1/subjects/" + subjectId + "/topics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(topicReq)))
                .andReturn().getResponse().getContentAsString();
        String topicId = objectMapper.readTree(topicRes).get("id").asText();

        CreateConceptRequest conceptReq = new CreateConceptRequest("Sorting", "Sort algorithms");
        String conceptRes = mockMvc.perform(post("/api/v1/topics/" + topicId + "/concepts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(conceptReq)))
                .andReturn().getResponse().getContentAsString();
        String conceptId = objectMapper.readTree(conceptRes).get("id").asText();

        // 3. Create Question & Link to Concept
        CreateQuestionRequest questionReq = new CreateQuestionRequest(
                "What is worst case time of QuickSort?",
                QuestionType.MULTIPLE_CHOICE,
                0.6,
                1.0,
                "O(N^2)"
        );
        String questionRes = mockMvc.perform(post("/api/v1/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(questionReq)))
                .andReturn().getResponse().getContentAsString();
        String questionId = objectMapper.readTree(questionRes).get("id").asText();

        AddQuestionConceptRequest assocReq = new AddQuestionConceptRequest(UUID.fromString(conceptId), 1.0);
        mockMvc.perform(post("/api/v1/questions/" + questionId + "/concepts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assocReq)))
                .andExpect(status().isCreated());

        // 4. Start Learning Session
        StartSessionRequest sessionReq = new StartSessionRequest(UUID.fromString(studentId));
        String sessionRes = mockMvc.perform(post("/api/v1/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sessionReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        String sessionId = objectMapper.readTree(sessionRes).get("id").asText();

        // 5. Submit Attempt 1 (Incorrect answer, response time 4000ms)
        SubmitAttemptRequest attempt1Req = new SubmitAttemptRequest(
                UUID.fromString(studentId),
                UUID.fromString(questionId),
                "O(N log N)",
                4000L
        );
        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attempt1Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isCorrect").value(false))
                .andExpect(jsonPath("$.attemptNumber").value(1));

        // 6. Submit Attempt 2 (Correct answer, response time 2500ms)
        SubmitAttemptRequest attempt2Req = new SubmitAttemptRequest(
                UUID.fromString(studentId),
                UUID.fromString(questionId),
                "O(N^2)",
                2500L
        );
        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/attempts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attempt2Req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isCorrect").value(true))
                .andExpect(jsonPath("$.attemptNumber").value(2));

        // 7. Verify Concept Performance & Evidence Accumulation
        mockMvc.perform(get("/api/v1/students/" + studentId + "/concepts/" + conceptId + "/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAttempts").value(2))
                .andExpect(jsonPath("$.correctAttempts").value(1))
                .andExpect(jsonPath("$.accuracyRate").value(0.5))
                .andExpect(jsonPath("$.averageResponseTimeMs").value(3250.0));

        // 8. Complete Session
        mockMvc.perform(post("/api/v1/sessions/" + sessionId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
