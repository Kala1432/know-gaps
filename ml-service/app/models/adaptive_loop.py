from typing import List
from app.schemas.mastery import AttemptItem

class AdaptiveLoopEvaluator:

    @staticmethod
    def evaluate_target_difficulty(attempts: List[AttemptItem], base_mastery: float) -> float:
        """
        Determines target practice question difficulty (0.1 - 1.0).
        Enforces rule: Single correct answers MUST NOT trigger difficulty jumps.
        Requires >= 3 consecutive correct attempts before advancing difficulty.
        """
        n = len(attempts)
        if n == 0:
            return 0.30 # Default entry level difficulty

        recent = attempts[-3:]
        consecutive_correct = 0
        for a in reversed(attempts):
            if a.is_correct:
                consecutive_correct += 1
            else:
                break

        consecutive_failed = 0
        for a in reversed(attempts):
            if not a.is_correct:
                consecutive_failed += 1
            else:
                break

        # Single correct answer -> Maintain conservative baseline (no difficulty jump)
        if n < 3:
            if consecutive_failed >= 1:
                return 0.25 # Recommend easier foundational questions
            return 0.35 # Entry level baseline

        # Repeated failures -> Recommend easier questions (0.20 - 0.35)
        if consecutive_failed >= 2:
            return 0.20

        # Consistent success across 3+ consecutive attempts -> Increase difficulty (0.70 - 0.90)
        if consecutive_correct >= 3 and base_mastery >= 0.70:
            return min(0.90, round(base_mastery + 0.15, 2))

        # Moderate steady performance -> Intermediate difficulty (0.50 - 0.65)
        return min(0.65, round(max(0.40, base_mastery), 2))
