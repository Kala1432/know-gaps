from pydantic import BaseModel, Field
from typing import List, Optional
from datetime import datetime

class RetentionAttemptItem(BaseModel):
    attempt_id: Optional[str] = None
    is_correct: bool
    response_time_ms: float = Field(..., gt=0)
    base_difficulty: float = Field(..., ge=0.0, le=1.0)
    attempt_number: int = Field(..., ge=1)
    timestamp: Optional[datetime] = None

class RetentionPredictionRequest(BaseModel):
    concept_id: str
    concept_name: str
    student_id: str
    current_mastery: float = Field(..., ge=0.0, le=1.0)
    attempts: List[RetentionAttemptItem]

class RetentionFactors(BaseModel):
    days_since_last_successful_recall: float
    days_since_last_attempt: float
    successful_recalls: int
    failed_recalls: int
    avg_spacing_days: float
    current_mastery: float
    avg_question_difficulty: float

class RetentionPredictionResponse(BaseModel):
    concept: str
    concept_id: str
    retention_risk: float = Field(..., ge=0.0, le=1.0)
    risk_level: str # "low", "medium", "high"
    recommended_review_window: str # "today", "soon", "later"
    explanation: str
    is_prototype: bool = True
    contributing_factors: RetentionFactors
