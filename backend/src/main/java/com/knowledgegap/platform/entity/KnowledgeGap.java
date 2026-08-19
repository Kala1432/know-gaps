package com.knowledgegap.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "knowledge_gaps")
public class KnowledgeGap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "concept_id", nullable = false)
    private Concept concept;

    @Enumerated(EnumType.STRING)
    @Column(name = "gap_type", nullable = false)
    private GapType gapType;

    @Column(nullable = false)
    private double severity = 1.0;

    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;

    public KnowledgeGap() {
    }

    public KnowledgeGap(Student student, Concept concept, GapType gapType, double severity) {
        this.student = student;
        this.concept = concept;
        this.gapType = gapType;
        this.severity = severity;
    }

    @PrePersist
    protected void onCreate() {
        this.detectedAt = Instant.now();
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

    public GapType getGapType() {
        return gapType;
    }

    public void setGapType(GapType gapType) {
        this.gapType = gapType;
    }

    public double getSeverity() {
        return severity;
    }

    public void setSeverity(double severity) {
        this.severity = severity;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(Instant detectedAt) {
        this.detectedAt = detectedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeGap that = (KnowledgeGap) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
