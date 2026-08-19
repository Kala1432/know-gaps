from pydantic import BaseModel, Field
from typing import List, Optional, Dict

class StudentConceptState(BaseModel):
    concept_id: str
    concept_name: str
    mastery: float = Field(..., ge=0.0, le=1.0)
    retention_risk: float = Field(..., ge=0.0, le=1.0)
    evidence_count: int = Field(..., ge=0)

class PrerequisiteEdge(BaseModel):
    prerequisite_concept_id: str
    target_concept_id: str

class RecommendationRequest(BaseModel):
    student_id: str
    concept_states: List[StudentConceptState]
    prerequisites: List[PrerequisiteEdge]
    limit: int = Field(default=3, ge=1, le=10)

class ConceptRecommendation(BaseModel):
    concept: str
    concept_id: str
    priority: float
    rank: int
    reason: str
    mastery: float
    retention_risk: float
    prerequisite_status: str # "SATISFIED", "DEFICIT", "BLOCKED"
    recommended_activity: str # "PREREQUISITE_REMEDIATION", "CONCEPT_REVIEW", "PRACTICE_QUESTIONS", "ADVANCED_CHALLENGE"

class RecommendationResponse(BaseModel):
    student_id: str
    recommendations: List[ConceptRecommendation]
    prerequisite_constraint_satisfaction_rate: float = 1.0
