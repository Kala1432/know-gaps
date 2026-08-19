import numpy as np
import pandas as pd
from typing import List
from datetime import datetime, timezone
from app.schemas.retention import RetentionAttemptItem, RetentionFactors

RETENTION_FEATURE_COLS = [
    "days_since_last_successful_recall",
    "days_since_last_attempt",
    "successful_recalls",
    "failed_recalls",
    "avg_spacing_days",
    "current_mastery",
    "avg_question_difficulty"
]

class RetentionFeatureExtractor:

    @staticmethod
    def extract_features(
        current_mastery: float,
        attempts: List[RetentionAttemptItem]
    ) -> RetentionFactors:
        if not attempts:
            return RetentionFactors(
                days_since_last_successful_recall=0.0,
                days_since_last_attempt=0.0,
                successful_recalls=0,
                failed_recalls=0,
                avg_spacing_days=0.0,
                current_mastery=current_mastery,
                avg_question_difficulty=0.5
            )

        now = datetime.now(timezone.utc)
        successful_recalls = sum(1 for a in attempts if a.is_correct)
        failed_recalls = len(attempts) - successful_recalls
        avg_difficulty = float(np.mean([a.base_difficulty for a in attempts]))

        # Time since last attempt
        last_attempt = attempts[-1]
        ts_last = last_attempt.timestamp or now
        if ts_last.tzinfo is None:
            ts_last = ts_last.replace(tzinfo=timezone.utc)
        days_since_last_attempt = max(0.0, (now - ts_last).total_seconds() / 86400.0)

        # Time since last SUCCESSFUL recall
        successful_attempts = [a for a in attempts if a.is_correct]
        if successful_attempts:
            last_success = successful_attempts[-1]
            ts_succ = last_success.timestamp or now
            if ts_succ.tzinfo is None:
                ts_succ = ts_succ.replace(tzinfo=timezone.utc)
            days_since_last_successful_recall = max(0.0, (now - ts_succ).total_seconds() / 86400.0)
        else:
            # If no successful recall has ever occurred, set to days_since_last_attempt + 7 days
            days_since_last_successful_recall = days_since_last_attempt + 7.0

        # Average spacing between attempts
        if len(attempts) >= 2:
            intervals = []
            for i in range(1, len(attempts)):
                t1 = attempts[i-1].timestamp or now
                t2 = attempts[i].timestamp or now
                if t1.tzinfo is None: t1 = t1.replace(tzinfo=timezone.utc)
                if t2.tzinfo is None: t2 = t2.replace(tzinfo=timezone.utc)
                diff = max(0.1, (t2 - t1).total_seconds() / 86400.0)
                intervals.append(diff)
            avg_spacing_days = float(np.mean(intervals))
        else:
            avg_spacing_days = 0.0

        return RetentionFactors(
            days_since_last_successful_recall=round(days_since_last_successful_recall, 2),
            days_since_last_attempt=round(days_since_last_attempt, 2),
            successful_recalls=successful_recalls,
            failed_recalls=failed_recalls,
            avg_spacing_days=round(avg_spacing_days, 2),
            current_mastery=round(current_mastery, 4),
            avg_question_difficulty=round(avg_difficulty, 4)
        )

    @staticmethod
    def to_dataframe(factors: RetentionFactors) -> pd.DataFrame:
        data = {
            "days_since_last_successful_recall": [factors.days_since_last_successful_recall],
            "days_since_last_attempt": [factors.days_since_last_attempt],
            "successful_recalls": [factors.successful_recalls],
            "failed_recalls": [factors.failed_recalls],
            "avg_spacing_days": [factors.avg_spacing_days],
            "current_mastery": [factors.current_mastery],
            "avg_question_difficulty": [factors.avg_question_difficulty]
        }
        return pd.DataFrame(data, columns=RETENTION_FEATURE_COLS)
