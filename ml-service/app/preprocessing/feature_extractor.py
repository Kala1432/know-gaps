import numpy as np
import pandas as pd
from typing import List
from datetime import datetime, timezone
from app.schemas.mastery import AttemptItem, ContributingFactors

FEATURE_COLS = [
    "historical_accuracy", "recent_accuracy", "evidence_count",
    "successful_recalls", "failed_recalls", "avg_response_time_sec",
    "difficulty_weighted_accuracy", "recent_trend_slope",
    "time_since_last_attempt_hours"
]

class FeatureExtractor:

    @staticmethod
    def extract_features(attempts: List[AttemptItem]) -> ContributingFactors:
        if not attempts:
            return ContributingFactors(
                historical_accuracy=0.5,
                recent_accuracy=0.5,
                evidence_count=0,
                successful_recalls=0,
                failed_recalls=0,
                avg_response_time_ms=0.0,
                difficulty_weighted_accuracy=0.5,
                recent_trend_slope=0.0,
                time_since_last_attempt_hours=0.0
            )

        n = len(attempts)
        successful_recalls = sum(1 for a in attempts if a.is_correct)
        failed_recalls = n - successful_recalls
        historical_accuracy = successful_recalls / n

        # Recent accuracy (last 3 attempts)
        recent_window = attempts[-3:]
        recent_accuracy = sum(1 for a in recent_window if a.is_correct) / len(recent_window)

        # Average response time
        avg_response_time_ms = float(np.mean([a.response_time_ms for a in attempts]))

        # Difficulty-weighted accuracy: harder questions carry more weight when solved correctly
        weights = [1.0 + 0.5 * a.base_difficulty for a in attempts]
        weighted_correct = sum(w for a, w in zip(attempts, weights) if a.is_correct)
        difficulty_weighted_accuracy = float(weighted_correct / sum(weights))

        # Recent performance trend slope (linear regression slope of correctness)
        if n >= 2:
            x = np.arange(n)
            y = np.array([1.0 if a.is_correct else 0.0 for a in attempts])
            slope = float(np.polyfit(x, y, 1)[0])
        else:
            slope = 0.0

        # Time since last attempt
        last_attempt = attempts[-1]
        if last_attempt.timestamp:
            now = datetime.now(timezone.utc)
            ts = last_attempt.timestamp
            if ts.tzinfo is None:
                ts = ts.replace(tzinfo=timezone.utc)
            hours_diff = max(0.0, (now - ts).total_seconds() / 3600.0)
        else:
            hours_diff = 0.0

        return ContributingFactors(
            historical_accuracy=round(historical_accuracy, 4),
            recent_accuracy=round(recent_accuracy, 4),
            evidence_count=n,
            successful_recalls=successful_recalls,
            failed_recalls=failed_recalls,
            avg_response_time_ms=round(avg_response_time_ms, 2),
            difficulty_weighted_accuracy=round(difficulty_weighted_accuracy, 4),
            recent_trend_slope=round(slope, 4),
            time_since_last_attempt_hours=round(hours_diff, 2)
        )

    @staticmethod
    def to_dataframe(factors: ContributingFactors) -> pd.DataFrame:
        data = {
            "historical_accuracy": [factors.historical_accuracy],
            "recent_accuracy": [factors.recent_accuracy],
            "evidence_count": [factors.evidence_count],
            "successful_recalls": [factors.successful_recalls],
            "failed_recalls": [factors.failed_recalls],
            "avg_response_time_sec": [factors.avg_response_time_ms / 1000.0],
            "difficulty_weighted_accuracy": [factors.difficulty_weighted_accuracy],
            "recent_trend_slope": [factors.recent_trend_slope],
            "time_since_last_attempt_hours": [factors.time_since_last_attempt_hours]
        }
        return pd.DataFrame(data, columns=FEATURE_COLS)
