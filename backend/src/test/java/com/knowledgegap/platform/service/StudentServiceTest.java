package com.knowledgegap.platform.service;

import com.knowledgegap.platform.dto.request.CreateStudentRequest;
import com.knowledgegap.platform.dto.response.StudentResponse;
import com.knowledgegap.platform.entity.Student;
import com.knowledgegap.platform.exception.DuplicateResourceException;
import com.knowledgegap.platform.exception.ResourceNotFoundException;
import com.knowledgegap.platform.mapper.StudentMapper;
import com.knowledgegap.platform.repository.StudentRepository;
import com.knowledgegap.platform.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentMapper studentMapper;
    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentMapper = new StudentMapper();
        studentService = new StudentServiceImpl(studentRepository, studentMapper);
    }

    @Test
    void createStudent_Success() {
        CreateStudentRequest request = new CreateStudentRequest("alice@example.com", "Alice Smith");
        given(studentRepository.existsByEmail("alice@example.com")).willReturn(false);

        Student student = new Student("alice@example.com", "Alice Smith");
        student.setId(UUID.randomUUID());
        given(studentRepository.save(any(Student.class))).willReturn(student);

        StudentResponse response = studentService.createStudent(request);

        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.name()).isEqualTo("Alice Smith");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void createStudent_DuplicateEmail_ThrowsException() {
        CreateStudentRequest request = new CreateStudentRequest("alice@example.com", "Alice Smith");
        given(studentRepository.existsByEmail("alice@example.com")).willReturn(true);

        assertThatThrownBy(() -> studentService.createStudent(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void getStudentById_NotFound_ThrowsException() {
        UUID id = UUID.randomUUID();
        given(studentRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getStudentById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}
