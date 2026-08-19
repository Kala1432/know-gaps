from app.schemas.mastery import AttemptItem
from app.preprocessing.feature_extractor import FeatureExtractor

def test_extract_features_empty():
    factors = FeatureExtractor.extract_features([])
    assert factors.evidence_count == 0
    assert factors.historical_accuracy == 0.5
    assert factors.successful_recalls == 0
    assert factors.failed_recalls == 0

def test_extract_features_multiple_attempts():
    attempts = [
        AttemptItem(is_correct=True, response_time_ms=3000, base_difficulty=0.3, attempt_number=1),
        AttemptItem(is_correct=False, response_time_ms=5000, base_difficulty=0.5, attempt_number=2),
        AttemptItem(is_correct=True, response_time_ms=2500, base_difficulty=0.7, attempt_number=3)
    ]
    factors = FeatureExtractor.extract_features(attempts)
    assert factors.evidence_count == 3
    assert factors.successful_recalls == 2
    assert factors.failed_recalls == 1
    assert round(factors.historical_accuracy, 2) == 0.67
    assert factors.avg_response_time_ms == 3500.0
