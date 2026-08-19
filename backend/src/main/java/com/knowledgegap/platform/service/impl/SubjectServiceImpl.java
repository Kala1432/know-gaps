package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.CreateSubjectRequest;
import com.knowledgegap.platform.dto.response.SubjectResponse;
import com.knowledgegap.platform.entity.Subject;
import com.knowledgegap.platform.exception.DuplicateResourceException;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.SubjectMapper;
import com.knowledgegap.platform.repository.SubjectRepository;
import com.knowledgegap.platform.service.SubjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final SubjectMapper subjectMapper;

    public SubjectServiceImpl(SubjectRepository subjectRepository, SubjectMapper subjectMapper) {
        this.subjectRepository = subjectRepository;
        this.subjectMapper = subjectMapper;
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(CreateSubjectRequest request) {
        if (subjectRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Subject with name '" + request.name() + "' already exists");
        }
        Subject subject = new Subject(request.name(), request.description());
        Subject savedSubject = subjectRepository.save(subject);
        return subjectMapper.toResponse(savedSubject);
    }

    @Override
    public SubjectResponse getSubjectById(UUID id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject with ID '" + id + "' not found"));
        return subjectMapper.toResponse(subject);
    }

    @Override
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subjectMapper::toResponse)
                .toList();
    }
}
