-- Schema migration V2: Learning Sessions, Attempts, Mastery States, Knowledge Gaps

ALTER TABLE questions ADD COLUMN correct_answer TEXT;

CREATE TABLE learning_sessions (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL,
    ended_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE attempts (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    question_id UUID NOT NULL REFERENCES questions(id) ON DELETE CASCADE,
    session_id UUID REFERENCES learning_sessions(id) ON DELETE SET NULL,
    user_answer TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    response_time_ms BIGINT NOT NULL,
    attempt_number INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE mastery_states (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    mastery_score DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (mastery_score >= 0.0 AND mastery_score <= 1.0),
    confidence_level DOUBLE PRECISION NOT NULL DEFAULT 0.0 CHECK (confidence_level >= 0.0 AND confidence_level <= 1.0),
    last_evaluated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_student_concept_mastery UNIQUE (student_id, concept_id)
);

CREATE TABLE knowledge_gaps (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    concept_id UUID NOT NULL REFERENCES concepts(id) ON DELETE CASCADE,
    gap_type VARCHAR(50) NOT NULL,
    severity DOUBLE PRECISION NOT NULL DEFAULT 1.0 CHECK (severity >= 0.0 AND severity <= 1.0),
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL
);

-- Performance Indexes
CREATE INDEX idx_sessions_student ON learning_sessions(student_id);
CREATE INDEX idx_attempts_student ON attempts(student_id);
CREATE INDEX idx_attempts_question ON attempts(question_id);
CREATE INDEX idx_attempts_session ON attempts(session_id);
CREATE INDEX idx_mastery_student_concept ON mastery_states(student_id, concept_id);
CREATE INDEX idx_gaps_student_concept ON knowledge_gaps(student_id, concept_id);
