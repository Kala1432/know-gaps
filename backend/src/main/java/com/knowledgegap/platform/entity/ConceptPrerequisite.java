package com.knowledgegap.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "concept_prerequisites", uniqueConstraints = {
    @UniqueConstraint(name = "uk_concept_prereq", columnNames = {"prerequisite_concept_id", "target_concept_id"})
})
public class ConceptPrerequisite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "prerequisite_concept_id", nullable = false)
    private Concept prerequisiteConcept;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_concept_id", nullable = false)
    private Concept targetConcept;

    @Enumerated(EnumType.STRING)
    @Column(name = "dependency_type", nullable = false)
    private DependencyType dependencyType;

    @Column(nullable = false)
    private double weight = 1.0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ConceptPrerequisite() {
    }

    public ConceptPrerequisite(Concept prerequisiteConcept, Concept targetConcept, DependencyType dependencyType, double weight) {
        this.prerequisiteConcept = prerequisiteConcept;
        this.targetConcept = targetConcept;
        this.dependencyType = dependencyType;
        this.weight = weight;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Concept getPrerequisiteConcept() {
        return prerequisiteConcept;
    }

    public void setPrerequisiteConcept(Concept prerequisiteConcept) {
        this.prerequisiteConcept = prerequisiteConcept;
    }

    public Concept getTargetConcept() {
        return targetConcept;
    }

    public void setTargetConcept(Concept targetConcept) {
        this.targetConcept = targetConcept;
    }

    public DependencyType getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(DependencyType dependencyType) {
        this.dependencyType = dependencyType;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConceptPrerequisite that = (ConceptPrerequisite) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
