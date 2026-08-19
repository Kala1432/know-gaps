package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.CreateSubjectRequest;
import com.knowledgegap.platform.dto.response.SubjectResponse;

import java.util.List;
import java.util.UUID;

public interface SubjectService {
    SubjectResponse createSubject(CreateSubjectRequest request);
    SubjectResponse getSubjectById(UUID id);
    List<SubjectResponse> getAllSubjects();
}
