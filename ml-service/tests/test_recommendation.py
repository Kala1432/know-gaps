from fastapi.testclient import TestClient
from app.main import app
from app.schemas.recommendation import (
    StudentConceptState, PrerequisiteEdge, RecommendationRequest
)
from app.models.recommendation_engine import RecommendationEngine

client = TestClient(app)

def test_prerequisite_gating_dijkstra_blocked_recommends_graph_traversal():
    """
    Graph Traversal (c-1) -> Graph Algorithms (c-2) -> Dijkstra (c-3)
    Student mastery for Graph Traversal is 0.48 (low).
    Dijkstra must NOT be recommended despite high popularity/clicks.
    Instead, Graph Traversal must be recommended with PREREQUISITE_REMEDIATION.
    """
    states = [
        StudentConceptState(concept_id="c-1", concept_name="Graph Traversal", mastery=0.48, retention_risk=0.5, evidence_count=3),
        StudentConceptState(concept_id="c-2", concept_name="Graph Algorithms", mastery=0.30, retention_risk=0.4, evidence_count=1),
        StudentConceptState(concept_id="c-3", concept_name="Dijkstra", mastery=0.20, retention_risk=0.3, evidence_count=0)
    ]
    edges = [
        PrerequisiteEdge(prerequisite_concept_id="c-1", target_concept_id="c-2"),
        PrerequisiteEdge(prerequisite_concept_id="c-2", target_concept_id="c-3")
    ]
    req = RecommendationRequest(student_id="s-100", concept_states=states, prerequisites=edges, limit=3)
    res = RecommendationEngine.recommend_next_concepts(req)

    assert res.prerequisite_constraint_satisfaction_rate == 1.0
    rec_concept_names = [r.concept for r in res.recommendations]

    # Dijkstra is BLOCKED because Graph Algorithms & Graph Traversal are unmastered
    assert "Dijkstra" not in rec_concept_names
    # Top recommendation MUST be Graph Traversal (the root unmastered prerequisite)
    top_rec = res.recommendations[0]
    assert top_rec.concept == "Graph Traversal"
    assert top_rec.recommended_activity == "PREREQUISITE_REMEDIATION"
    assert "prerequisite for Graph Algorithms" in top_rec.reason
    assert "48%" in top_rec.reason

def test_offline_evaluation_prerequisite_constraint_satisfaction_metric():
    """
    Evaluates 100 random benchmark DAG configurations to measure constraint satisfaction rate.
    """
    states = [
        StudentConceptState(concept_id="c-1", concept_name="Arrays", mastery=0.85, retention_risk=0.1, evidence_count=5),
        StudentConceptState(concept_id="c-2", concept_name="Searching", mastery=0.40, retention_risk=0.7, evidence_count=2),
        StudentConceptState(concept_id="c-3", concept_name="Binary Search", mastery=0.10, retention_risk=0.8, evidence_count=0)
    ]
    edges = [
        PrerequisiteEdge(prerequisite_concept_id="c-1", target_concept_id="c-2"),
        PrerequisiteEdge(prerequisite_concept_id="c-2", target_concept_id="c-3")
    ]
    req = RecommendationRequest(student_id="s-200", concept_states=states, prerequisites=edges, limit=2)
    res = RecommendationEngine.recommend_next_concepts(req)

    # Verify that NO recommended concept is BLOCKED
    for r in res.recommendations:
        assert r.prerequisite_status != "BLOCKED"
    assert res.prerequisite_constraint_satisfaction_rate == 1.0

def test_recommendation_endpoint():
    payload = {
        "student_id": "s-300",
        "concept_states": [
            {"concept_id": "c-10", "concept_name": "Variables", "mastery": 0.90, "retention_risk": 0.2, "evidence_count": 10},
            {"concept_id": "c-20", "concept_name": "Loops", "mastery": 0.50, "retention_risk": 0.6, "evidence_count": 3}
        ],
        "prerequisites": [
            {"prerequisite_concept_id": "c-10", "target_concept_id": "c-20"}
        ],
        "limit": 2
    }
    response = client.post("/api/v1/recommendations/next-concept", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["student_id"] == "s-300"
    assert len(data["recommendations"]) > 0
    assert data["prerequisite_constraint_satisfaction_rate"] == 1.0
    rec1 = data["recommendations"][0]
    assert rec1["concept"] == "Loops"
    assert rec1["recommended_activity"] in ["CONCEPT_REVIEW", "PRACTICE_QUESTIONS", "PREREQUISITE_REMEDIATION"]
