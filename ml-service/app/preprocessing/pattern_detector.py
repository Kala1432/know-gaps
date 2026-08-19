import numpy as np
from typing import List, Dict, Any
from pydantic import BaseModel, Field
from app.schemas.mastery import AttemptItem

class ObservablePattern(BaseModel):
    pattern_type: str # "PREREQUISITE_DEFICIT", "FLUENCY_LAG", "RETENTION_DECAY", "CONCEPT_CONFUSION", "APPLICATION_LAG"
    concept_name: str
    severity: float = Field(..., ge=0.0, le=1.0)
    description: str
    evidence_summary: str

class PatternDetector:

    @staticmethod
    def detect_patterns(
        concept_name: str,
        attempts: List[AttemptItem],
        has_unmastered_prerequisite: bool = False,
        prerequisite_name: str = ""
    ) -> List[ObservablePattern]:
        patterns: List[ObservablePattern] = []
        n = len(attempts)
        if n == 0:
            return patterns

        successful = [a for a in attempts if a.is_correct]
        failed = [a for a in attempts if not a.is_correct]
        accuracy = len(successful) / n
        avg_resp_time = float(np.mean([a.response_time_ms for a in attempts]))

        # 1. PREREQUISITE_DEFICIT: Failing attempts when an unmastered prerequisite exists
        if len(failed) >= 2 and has_unmastered_prerequisite:
            patterns.append(ObservablePattern(
                pattern_type="PREREQUISITE_DEFICIT",
                concept_name=concept_name,
                severity=min(1.0, len(failed) * 0.3),
                description=f"Repeated failures on '{concept_name}' coincide with an unmastered prerequisite ('{prerequisite_name}').",
                evidence_summary=f"Failed {len(failed)} attempt(s) while prerequisite '{prerequisite_name}' remains unmastered."
            ))

        # 2. FLUENCY_LAG: Correct answers but high response time (> 15,000ms)
        if accuracy >= 0.70 and avg_resp_time > 15000:
            patterns.append(ObservablePattern(
                pattern_type="FLUENCY_LAG",
                concept_name=concept_name,
                severity=min(1.0, (avg_resp_time - 15000) / 15000),
                description=f"High response time ({avg_resp_time / 1000:.1f}s) despite correct answers indicates high cognitive load.",
                evidence_summary=f"Accuracy is {int(accuracy * 100)}% but average response time is {avg_resp_time / 1000:.1f}s."
            ))

        # 3. RETENTION_DECAY: High historical performance but recent accuracy drop
        if n >= 4:
            early_acc = sum(1 for a in attempts[:2] if a.is_correct) / 2.0
            recent_acc = sum(1 for a in attempts[-2:] if a.is_correct) / 2.0
            if early_acc >= 0.75 and recent_acc <= 0.25:
                patterns.append(ObservablePattern(
                    pattern_type="RETENTION_DECAY",
                    concept_name=concept_name,
                    severity=0.8,
                    description=f"Recent attempt accuracy dropped sharply compared to earlier successful attempts.",
                    evidence_summary=f"Early accuracy was {int(early_acc * 100)}%, but recent accuracy dropped to {int(recent_acc * 100)}%."
                ))

        # 4. APPLICATION_LAG: Success on easy questions (< 0.4 difficulty) but failure on harder questions (> 0.6 difficulty)
        easy_attempts = [a for a in attempts if a.base_difficulty < 0.4]
        hard_attempts = [a for a in attempts if a.base_difficulty > 0.6]

        if easy_attempts and hard_attempts:
            easy_acc = sum(1 for a in easy_attempts if a.is_correct) / len(easy_attempts)
            hard_acc = sum(1 for a in hard_attempts if a.is_correct) / len(hard_attempts)
            if easy_acc >= 0.75 and hard_acc <= 0.25:
                patterns.append(ObservablePattern(
                    pattern_type="APPLICATION_LAG",
                    concept_name=concept_name,
                    severity=0.75,
                    description=f"Strong recall on basic questions, but struggling with higher difficulty application questions.",
                    evidence_summary=f"Basic question accuracy is {int(easy_acc * 100)}%, but advanced application accuracy is {int(hard_acc * 100)}%."
                ))

        return patterns
