from pydantic import BaseModel, Field
from typing import List, Optional, Dict
from datetime import datetime

class AttemptItem(BaseModel):
    attempt_id: Optional[str] = None
    is_correct: bool
    response_time_ms: float = Field(..., gt=0)
    base_difficulty: float = Field(..., ge=0.0, le=1.0)
    attempt_number: int = Field(..., ge=1)
    timestamp: Optional[datetime] = None

class MasteryPredictionRequest(BaseModel):
    concept_id: str
    concept_name: str
    student_id: str
    attempts: List[AttemptItem]

class ContributingFactors(BaseModel):
    historical_accuracy: float
    recent_accuracy: float
    evidence_count: int
    successful_recalls: int
    failed_recalls: int
    avg_response_time_ms: float
    difficulty_weighted_accuracy: float
    recent_trend_slope: float
    time_since_last_attempt_hours: float

class MasteryPredictionResponse(BaseModel):
    concept: str
    concept_id: str
    mastery: float = Field(..., ge=0.0, le=1.0)
    confidence: float = Field(..., ge=0.0, le=1.0)
    evidence_count: int
    trend: str # "improving", "declining", "stable", "insufficient_data"
    risk: str  # "low", "medium", "high"
    explanation: str
    contributing_factors: ContributingFactors
