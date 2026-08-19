from app.schemas.mastery import AttemptItem, MasteryPredictionRequest
from app.models.predictor import MasteryPredictor

def test_single_attempt_does_not_jump_to_extremes():
    predictor = MasteryPredictor()
    
    # 1 correct attempt
    req1 = MasteryPredictionRequest(
        concept_id="c1",
        concept_name="Binary Search",
        student_id="s1",
        attempts=[AttemptItem(is_correct=True, response_time_ms=2000, base_difficulty=0.5, attempt_number=1)]
    )
    res1 = predictor.predict(req1)
    assert res1.evidence_count == 1
    assert res1.mastery < 1.0  # Not 100% mastered on 1 attempt
    assert res1.confidence == 0.2

    # 1 incorrect attempt
    req2 = MasteryPredictionRequest(
        concept_id="c1",
        concept_name="Binary Search",
        student_id="s1",
        attempts=[AttemptItem(is_correct=False, response_time_ms=2000, base_difficulty=0.5, attempt_number=1)]
    )
    res2 = predictor.predict(req2)
    assert res2.evidence_count == 1
    assert res2.mastery > 0.0  # Not 0% weak on 1 attempt

def test_multiple_attempts_build_confidence():
    predictor = MasteryPredictor()
    attempts = [
        AttemptItem(is_correct=True, response_time_ms=2000, base_difficulty=0.5, attempt_number=i+1)
        for i in range(5)
    ]
    req = MasteryPredictionRequest(
        concept_id="c1",
        concept_name="Binary Search",
        student_id="s1",
        attempts=attempts
    )
    res = predictor.predict(req)
    assert res.evidence_count == 5
    assert res.confidence == 1.0
    assert res.mastery >= 0.70
    assert "improving" in res.trend or "stable" in res.trend
    assert "Binary Search" in res.explanation
