package com.knowledgegap.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    @Column(name = "base_difficulty", nullable = false)
    private double baseDifficulty;

    @Column(name = "discrimination_index", nullable = false)
    private double discriminationIndex = 1.0;

    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionConcept> questionConcepts = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Question() {
    }

    public Question(String content, QuestionType questionType, double baseDifficulty, double discriminationIndex, String correctAnswer) {
        this.content = content;
        this.questionType = questionType;
        this.baseDifficulty = baseDifficulty;
        this.discriminationIndex = discriminationIndex;
        this.correctAnswer = correctAnswer;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public double getBaseDifficulty() {
        return baseDifficulty;
    }

    public void setBaseDifficulty(double baseDifficulty) {
        this.baseDifficulty = baseDifficulty;
    }

    public double getDiscriminationIndex() {
        return discriminationIndex;
    }

    public void setDiscriminationIndex(double discriminationIndex) {
        this.discriminationIndex = discriminationIndex;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<QuestionConcept> getQuestionConcepts() {
        return questionConcepts;
    }

    public void setQuestionConcepts(List<QuestionConcept> questionConcepts) {
        this.questionConcepts = questionConcepts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Question question = (Question) o;
        return Objects.equals(id, question.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
