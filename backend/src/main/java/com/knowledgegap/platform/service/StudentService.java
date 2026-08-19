package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.CreateStudentRequest;
import com.knowledgegap.platform.dto.response.StudentResponse;

import java.util.List;
import java.util.UUID;

public interface StudentService {
    StudentResponse createStudent(CreateStudentRequest request);
    StudentResponse getStudentById(UUID id);
    List<StudentResponse> getAllStudents();
}
