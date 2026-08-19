import time
import logging
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from typing import List
from pydantic import BaseModel

from app.schemas.mastery import MasteryPredictionRequest, MasteryPredictionResponse, AttemptItem
from app.schemas.retention import RetentionPredictionRequest, RetentionPredictionResponse
from app.schemas.recommendation import RecommendationRequest, RecommendationResponse
from app.models.predictor import MasteryPredictor
from app.models.retention_predictor import RetentionPredictor
from app.models.recommendation_engine import RecommendationEngine
from app.preprocessing.pattern_detector import PatternDetector, ObservablePattern

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("ml-service")

app = FastAPI(
    title="Knowledge Gap Intelligence Platform - ML Service",
    description="Python FastAPI service providing concept mastery estimation, retention risk analysis, adaptive recommendation, and mistake pattern detection.",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.middleware("http")
async def add_inference_latency_header(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time_ms = (time.time() - start_time) * 1000
    response.headers["X-Inference-Latency-MS"] = f"{process_time_ms:.2f}"
    logger.info(f"Path: {request.url.path} | Status: {response.status_code} | Latency: {process_time_ms:.2f}ms")
    return response

mastery_predictor = MasteryPredictor()
retention_predictor = RetentionPredictor()

class PatternDetectionRequest(BaseModel):
    concept_name: str
    attempts: List[AttemptItem]
    has_unmastered_prerequisite: bool = False
    prerequisite_name: str = ""

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "knowledge-gap-ml-service"}

@app.get("/version")
def version_info():
    return {
        "version": mastery_predictor.version,
        "mastery_model": "Random Forest Regressor",
        "retention_model": "Random Forest Regressor",
        "recommendation_engine": "Prerequisite-Gated Multi-Factor Deterministic Engine",
        "adaptive_loop": "Multi-Attempt Difficulty Progression & Observable Pattern Detector",
        "is_prototype": True
    }

@app.post("/api/v1/mastery/predict", response_model=MasteryPredictionResponse)
def predict_mastery(request: MasteryPredictionRequest):
    try:
        return mastery_predictor.predict(request)
    except Exception as e:
        logger.error(f"Mastery prediction failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/mastery/batch-predict", response_model=List[MasteryPredictionResponse])
def batch_predict_mastery(requests: List[MasteryPredictionRequest]):
    try:
        return [mastery_predictor.predict(req) for req in requests]
    except Exception as e:
        logger.error(f"Batch mastery prediction failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/retention/predict", response_model=RetentionPredictionResponse)
def predict_retention(request: RetentionPredictionRequest):
    try:
        return retention_predictor.predict(request)
    except Exception as e:
        logger.error(f"Retention risk prediction failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/retention/batch-predict", response_model=List[RetentionPredictionResponse])
def batch_predict_retention(requests: List[RetentionPredictionRequest]):
    try:
        return [retention_predictor.predict(req) for req in requests]
    except Exception as e:
        logger.error(f"Batch retention risk prediction failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/recommendations/next-concept", response_model=RecommendationResponse)
def recommend_next_concept(request: RecommendationRequest):
    try:
        return RecommendationEngine.recommend_next_concepts(request)
    except Exception as e:
        logger.error(f"Recommendation engine failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/recommendations/batch", response_model=List[RecommendationResponse])
def batch_recommend_next_concept(requests: List[RecommendationRequest]):
    try:
        return [RecommendationEngine.recommend_next_concepts(req) for req in requests]
    except Exception as e:
        logger.error(f"Batch recommendation failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/v1/patterns/detect", response_model=List[ObservablePattern])
def detect_mistake_patterns(request: PatternDetectionRequest):
    try:
        return PatternDetector.detect_patterns(
            concept_name=request.concept_name,
            attempts=request.attempts,
            has_unmastered_prerequisite=request.has_unmastered_prerequisite,
            prerequisite_name=request.prerequisite_name
        )
    except Exception as e:
        logger.error(f"Pattern detection failure: {str(e)}")
        raise HTTPException(status_code=500, detail=str(e))
