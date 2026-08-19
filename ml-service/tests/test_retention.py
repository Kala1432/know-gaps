from fastapi.testclient import TestClient
from datetime import datetime, timezone, timedelta
from app.main import app
from app.schemas.retention import RetentionAttemptItem, RetentionPredictionRequest
from app.models.retention_predictor import RetentionPredictor

client = TestClient(app)

def test_retention_predictor_baseline_and_prototype_flag():
    predictor = RetentionPredictor()
    now = datetime.now(timezone.utc)
    
    # 18 days since last attempt
    old_time = now - timedelta(days=18)
    attempts = [
        RetentionAttemptItem(is_correct=False, response_time_ms=5000, base_difficulty=0.6, attempt_number=1, timestamp=old_time),
        RetentionAttemptItem(is_correct=False, response_time_ms=6000, base_difficulty=0.7, attempt_number=2, timestamp=old_time)
    ]
    
    req = RetentionPredictionRequest(
        concept_id="c-456",
        concept_name="Java Generics",
        student_id="s-789",
        current_mastery=0.40,
        attempts=attempts
    )
    
    res = predictor.predict(req)
    assert res.concept == "Java Generics"
    assert res.is_prototype is True
    assert res.retention_risk >= 0.50
    assert res.risk_level in ["medium", "high"]
    assert res.recommended_review_window in ["soon", "today"]
    assert "Java Generics" in res.explanation
    assert "days" in res.explanation

def test_retention_endpoint():
    payload = {
        "concept_id": "c-999",
        "concept_name": "Recursion",
        "student_id": "s-111",
        "current_mastery": 0.85,
        "attempts": [
            {"is_correct": True, "response_time_ms": 2000.0, "base_difficulty": 0.3, "attempt_number": 1}
        ]
    }
    response = client.post("/api/v1/retention/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["concept"] == "Recursion"
    assert data["is_prototype"] is True
    assert "retention_risk" in data
    assert "risk_level" in data
    assert "recommended_review_window" in data
    assert "explanation" in data
