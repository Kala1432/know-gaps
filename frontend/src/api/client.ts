import {
  Student, Subject, Topic, Concept, PrerequisiteChain, Question,
  LearningSession, Attempt, ConceptPerformance, RetentionRiskResponse, Recommendation
} from '../types/api';

const API_BASE = '/api/v1';

async function fetchJson<T>(url: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${url}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`HTTP ${response.status}: ${errorText || response.statusText}`);
  }

  return response.json();
}

export const api = {
  // Students
  getStudents: () => fetchJson<Student[]>('/students'),
  createStudent: (name: string, email: string) =>
    fetchJson<Student>('/students', {
      method: 'POST',
      body: JSON.stringify({ name, email }),
    }),

  // Curriculum
  getSubjects: () => fetchJson<Subject[]>('/subjects'),
  getTopics: (subjectId: string) => fetchJson<Topic[]>(`/subjects/${subjectId}/topics`),
  getConcepts: (topicId: string) => fetchJson<Concept[]>(`/topics/${topicId}/concepts`),
  getConceptGraph: (conceptId: string) => fetchJson<PrerequisiteChain>(`/concepts/${conceptId}/graph`),

  // Questions
  getQuestions: () => fetchJson<Question[]>('/questions'),

  // Learning Sessions
  startSession: (studentId: string) =>
    fetchJson<LearningSession>('/sessions', {
      method: 'POST',
      body: JSON.stringify({ studentId }),
    }),
  completeSession: (sessionId: string) =>
    fetchJson<LearningSession>(`/sessions/${sessionId}/complete`, {
      method: 'POST',
    }),
  getSessionQuestions: (sessionId: string) => fetchJson<Question[]>(`/sessions/${sessionId}/questions`),

  // Attempts
  submitAttempt: (sessionId: string | null, studentId: string, questionId: string, userAnswer: string, responseTimeMs: number) => {
    const url = sessionId ? `/sessions/${sessionId}/attempts` : '/attempts';
    return fetchJson<Attempt>(url, {
      method: 'POST',
      body: JSON.stringify({ studentId, questionId, userAnswer, responseTimeMs }),
    });
  },
  getStudentAttempts: (studentId: string) => fetchJson<Attempt[]>(`/students/${studentId}/attempts`),

  // Performance & Retention
  getStudentPerformance: (studentId: string) => fetchJson<ConceptPerformance[]>(`/students/${studentId}/performance`),
  getConceptPerformance: (studentId: string, conceptId: string) =>
    fetchJson<ConceptPerformance>(`/students/${studentId}/concepts/${conceptId}/performance`),
  getRetentionRisk: (studentId: string, conceptId: string) =>
    fetchJson<RetentionRiskResponse>(`/students/${studentId}/concepts/${conceptId}/retention-risk`),

  // Recommendations
  getNextConceptRecommendations: (studentId: string, limit: number = 3) =>
    fetchJson<Recommendation[]>(`/students/${studentId}/recommendations/next-concept?limit=${limit}`),
};
