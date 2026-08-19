package com.knowledgegap.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "mastery_states", uniqueConstraints = {
    @UniqueConstraint(name = "uk_student_concept_mastery", columnNames = {"student_id", "concept_id"})
})
public class MasteryState {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Column(name = "mastery_score", nullable = false)
    private double masteryScore = 0.0;

    @Column(name = "confidence_level", nullable = false)
    private double confidenceLevel = 0.0;

    @Column(name = "last_evaluated_at", nullable = false)
    private Instant lastEvaluatedAt;

    public MasteryState() {
    }

    public MasteryState(Student student, Concept concept, double masteryScore, double confidenceLevel) {
        this.student = student;
        this.concept = concept;
        this.masteryScore = masteryScore;
        this.confidenceLevel = confidenceLevel;
        this.lastEvaluatedAt = Instant.now();
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        this.lastEvaluatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

    public double getMasteryScore() {
        return masteryScore;
    }

    public void setMasteryScore(double masteryScore) {
        this.masteryScore = masteryScore;
    }

    public double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }

    public Instant getLastEvaluatedAt() {
        return lastEvaluatedAt;
    }

    public void setLastEvaluatedAt(Instant lastEvaluatedAt) {
        this.lastEvaluatedAt = lastEvaluatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MasteryState that = (MasteryState) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
