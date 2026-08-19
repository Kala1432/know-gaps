from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health_check():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "ok", "service": "knowledge-gap-ml-service"}

def test_version_info():
    response = client.get("/version")
    assert response.status_code == 200
    data = response.json()
    assert "version" in data
    assert "mastery_model" in data
    assert "retention_model" in data
    assert data["is_prototype"] is True

def test_predict_mastery_endpoint():
    payload = {
        "concept_id": "c-123",
        "concept_name": "Binary Search Trees",
        "student_id": "s-456",
        "attempts": [
            {"is_correct": True, "response_time_ms": 3500.0, "base_difficulty": 0.4, "attempt_number": 1},
            {"is_correct": True, "response_time_ms": 2800.0, "base_difficulty": 0.5, "attempt_number": 2}
        ]
    }
    response = client.post("/api/v1/mastery/predict", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["concept"] == "Binary Search Trees"
    assert data["evidence_count"] == 2
    assert "mastery" in data
    assert "confidence" in data
    assert "explanation" in data
    assert "contributing_factors" in data
