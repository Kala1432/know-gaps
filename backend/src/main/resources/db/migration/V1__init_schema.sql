-- Schema migration V1: Initial schema for Knowledge Gap Intelligence Platform

CREATE TABLE students (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE topics (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_topic_subject_name UNIQUE (subject_id, name)
);

CREATE TABLE concepts (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_concept_topic_name UNIQUE (topic_id, name)
);

CREATE TABLE concept_prerequisites (
    id UUID PRIMARY KEY,
    prerequisite_concept_id UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    target_concept_id UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    dependency_type VARCHAR(50) NOT NULL,
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_concept_prereq UNIQUE (prerequisite_concept_id, target_concept_id),
    CONSTRAINT chk_no_self_prereq CHECK (prerequisite_concept_id <> target_concept_id)
);

CREATE TABLE questions (
    id UUID PRIMARY KEY,
    content TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL,
    base_difficulty DOUBLE PRECISION NOT NULL CHECK (base_difficulty >= 0.0 AND base_difficulty <= 1.0),
    discrimination_index DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE question_concepts (
    id UUID PRIMARY KEY,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_question_concept UNIQUE (question_id, concept_id)
);

-- Indexes for performance
CREATE INDEX idx_topics_subject_id ON topics(subject_id);
CREATE INDEX idx_concepts_topic_id ON concepts(topic_id);
CREATE INDEX idx_prereq_target ON concept_prerequisites(target_concept_id);
CREATE INDEX idx_prereq_prereq ON concept_prerequisites(prerequisite_concept_id);
CREATE INDEX idx_qc_question ON question_concepts(question_id);
CREATE INDEX idx_qc_concept ON question_concepts(concept_id);
