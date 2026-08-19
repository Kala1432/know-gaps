package com.knowledgegap.platform.service.impl;

import com.knowledgegap.platform.dto.request.CreateStudentRequest;
import com.knowledgegap.platform.dto.response.StudentResponse;
import com.knowledgegap.platform.entity.Student;
import com.knowledgegap.platform.exception.DuplicateResourceException;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.StudentMapper;
import com.knowledgegap.platform.repository.StudentRepository;
import com.knowledgegap.platform.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    @Override
    @Transactional
    public StudentResponse createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Student with email '" + request.email() + "' already exists");
        }
        Student student = new Student(request.email(), request.name());
        Student savedStudent = studentRepository.save(student);
        return studentMapper.toResponse(savedStudent);
    }

    @Override
    public StudentResponse getStudentById(UUID id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student with ID '" + id + "' not found"));
        return studentMapper.toResponse(student);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(studentMapper::toResponse)
                .toList();
    }
}
