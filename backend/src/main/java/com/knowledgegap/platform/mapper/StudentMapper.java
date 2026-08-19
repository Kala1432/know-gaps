package com.knowledgegap.platform.mapper;

import com.knowledgegap.platform.dto.response.StudentResponse;
import com.knowledgegap.platform.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }
        return new StudentResponse(
                student.getId(),
                student.getEmail(),
                student.getName(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }
}
