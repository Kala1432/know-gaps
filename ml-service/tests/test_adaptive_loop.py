from app.schemas.mastery import AttemptItem
from app.models.adaptive_loop import AdaptiveLoopEvaluator
from app.preprocessing.pattern_detector import PatternDetector

def test_single_correct_attempt_does_not_jump_difficulty():
    attempts = [
        AttemptItem(is_correct=True, response_time_ms=2500, base_difficulty=0.3, attempt_number=1)
    ]
    target_diff = AdaptiveLoopEvaluator.evaluate_target_difficulty(attempts, base_mastery=0.60)
    # Verifies conservative baseline, NO jump to high difficulty on 1 attempt
    assert target_diff <= 0.40

def test_consecutive_successes_increase_difficulty():
    attempts = [
        AttemptItem(is_correct=True, response_time_ms=2000, base_difficulty=0.5, attempt_number=1),
        AttemptItem(is_correct=True, response_time_ms=1800, base_difficulty=0.6, attempt_number=2),
        AttemptItem(is_correct=True, response_time_ms=1500, base_difficulty=0.7, attempt_number=3)
    ]
    target_diff = AdaptiveLoopEvaluator.evaluate_target_difficulty(attempts, base_mastery=0.75)
    assert target_diff >= 0.70

def test_repeated_failures_decrease_difficulty():
    attempts = [
        AttemptItem(is_correct=False, response_time_ms=4000, base_difficulty=0.5, attempt_number=1),
        AttemptItem(is_correct=False, response_time_ms=4500, base_difficulty=0.5, attempt_number=2)
    ]
    target_diff = AdaptiveLoopEvaluator.evaluate_target_difficulty(attempts, base_mastery=0.40)
    assert target_diff <= 0.25

def test_fluency_lag_pattern_detection():
    attempts = [
        AttemptItem(is_correct=True, response_time_ms=18000, base_difficulty=0.5, attempt_number=1),
        AttemptItem(is_correct=True, response_time_ms=19000, base_difficulty=0.5, attempt_number=2)
    ]
    patterns = PatternDetector.detect_patterns("Binary Search", attempts)
    pattern_types = [p.pattern_type for p in patterns]
    assert "FLUENCY_LAG" in pattern_types

def test_application_lag_pattern_detection():
    attempts = [
        AttemptItem(is_correct=True, response_time_ms=2000, base_difficulty=0.2, attempt_number=1),
        AttemptItem(is_correct=True, response_time_ms=2100, base_difficulty=0.3, attempt_number=2),
        AttemptItem(is_correct=False, response_time_ms=5000, base_difficulty=0.8, attempt_number=3),
        AttemptItem(is_correct=False, response_time_ms=6000, base_difficulty=0.9, attempt_number=4)
    ]
    patterns = PatternDetector.detect_patterns("Sorting", attempts)
    pattern_types = [p.pattern_type for p in patterns]
    assert "APPLICATION_LAG" in pattern_types
