package com.knowledgegap.platform.repository;

import com.knowledgegap.platform.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TopicRepository extends JpaRepository<Topic, UUID> {
    List<Topic> findBySubjectId(UUID subjectId);
    boolean existsBySubjectIdAndName(UUID subjectId, String name);
}
