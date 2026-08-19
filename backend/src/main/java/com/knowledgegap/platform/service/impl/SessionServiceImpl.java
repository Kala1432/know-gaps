package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.StartSessionRequest;
import com.knowledgegap.platform.dto.response.LearningSessionResponse;
import com.knowledgegap.platform.dto.response.QuestionResponse;
import com.knowledgegap.platform.entity.LearningSession;
import com.knowledgegap.platform.entity.SessionStatus;
import com.knowledgegap.platform.entity.Student;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.QuestionMapper;
import com.knowledgegap.platform.mapper.SessionMapper;
import com.knowledgegap.platform.repository.LearningSessionRepository;
import com.knowledgegap.platform.repository.QuestionRepository;
import com.knowledgegap.platform.repository.StudentRepository;
import com.knowledgegap.platform.service.SessionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SessionServiceImpl implements SessionService {

    private final StudentRepository studentRepository;
    private final LearningSessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final SessionMapper sessionMapper;
    private final QuestionMapper questionMapper;

    public SessionServiceImpl(StudentRepository studentRepository,
                               LearningSessionRepository sessionRepository,
                               QuestionRepository questionRepository,
                               SessionMapper sessionMapper,
                               QuestionMapper questionMapper) {
        this.studentRepository = studentRepository;
        this.sessionRepository = sessionRepository;
        this.questionRepository = questionRepository;
        this.sessionMapper = sessionMapper;
        this.questionMapper = questionMapper;
    }

    @Override
    @Transactional
    public LearningSessionResponse startSession(StartSessionRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + request.studentId() + "' not found"));

        // Reuse active session if present, or create a new active session
        Optional<LearningSession> activeSession = sessionRepository
                .findFirstByStudentIdAndStatusOrderByStartedAtDesc(student.getId(), SessionStatus.ACTIVE);

        if (activeSession.isPresent()) {
            return sessionMapper.toResponse(activeSession.get());
        }

        LearningSession session = new LearningSession(student);
        LearningSession savedSession = sessionRepository.save(session);
        return sessionMapper.toResponse(savedSession);
    }

    @Override
    @Transactional
    public LearningSessionResponse completeSession(UUID sessionId) {
        LearningSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning session with ID '" + sessionId + "' not found"));

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(Instant.now());
        LearningSession savedSession = sessionRepository.save(session);
        return sessionMapper.toResponse(savedSession);
    }

    @Override
    public LearningSessionResponse getSessionById(UUID sessionId) {
        LearningSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Learning session with ID '" + sessionId + "' not found"));
        return sessionMapper.toResponse(session);
    }

    @Override
    public List<QuestionResponse> getSessionQuestions(UUID sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Learning session with ID '" + sessionId + "' not found");
        }
        return questionRepository.findAll().stream()
                .map(questionMapper::toResponse)
                .toList();
    }
}
