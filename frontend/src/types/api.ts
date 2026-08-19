export interface Student {
  id: string;
  email: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface Subject {
  id: string;
  name: string;
  description: string;
  createdAt: string;
}

export interface Topic {
  id: string;
  subjectId: string;
  name: string;
  description: string;
  createdAt: string;
}

export interface Concept {
  id: string;
  topicId: string;
  name: string;
  description: string;
  createdAt: string;
}

export interface PrerequisiteChain {
  conceptId: string;
  conceptName: string;
  prerequisitePath: Concept[];
  downstreamConcepts: Concept[];
}

export interface Question {
  id: string;
  content: string;
  questionType: 'MULTIPLE_CHOICE' | 'FREE_TEXT' | 'CODE_INPUT' | 'TEXT';
  baseDifficulty: number;
  discriminationIndex: number;
  correctAnswer?: string;
  concepts?: any[];
}

export interface LearningSession {
  id: string;
  studentId: string;
  status: 'ACTIVE' | 'COMPLETED' | 'ABANDONED';
  startedAt: string;
  endedAt?: string;
  attemptCount: number;
}

export interface Attempt {
  id: string;
  studentId: string;
  questionId: string;
  sessionId?: string;
  userAnswer: string;
  isCorrect: boolean;
  responseTimeMs: number;
  attemptNumber: number;
  questionDifficulty: number;
  testedConceptNames: string[];
  createdAt: string;
}

export interface ConceptPerformance {
  studentId: string;
  conceptId: string;
  conceptName: string;
  totalAttempts: number;
  correctAttempts: number;
  accuracyRate: number;
  averageResponseTimeMs: number;
  masteryScore: number;
  confidenceLevel: number;
  activeGapTypes: string[];
  lastEvaluatedAt: string;
}

export interface RetentionRiskResponse {
  concept: string;
  concept_id: string;
  retention_risk: number;
  risk_level: 'low' | 'medium' | 'high';
  recommended_review_window: 'today' | 'soon' | 'later';
  explanation: string;
  is_prototype: boolean;
}

export interface Recommendation {
  concept: string;
  conceptId: string;
  priority: number;
  rank: number;
  reason: string;
  mastery: number;
  retentionRisk: number;
  prerequisiteStatus: 'SATISFIED' | 'DEFICIT' | 'BLOCKED';
  recommendedActivity: 'PREREQUISITE_REMEDIATION' | 'CONCEPT_REVIEW' | 'PRACTICE_QUESTIONS' | 'ADVANCED_CHALLENGE';
}
