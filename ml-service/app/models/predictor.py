import os
import joblib
import numpy as np
from typing import List
from app.schemas.mastery import MasteryPredictionRequest, MasteryPredictionResponse
from app.preprocessing.feature_extractor import FeatureExtractor
from app.explainability.explainer import Explainer

class MasteryPredictor:

    def __init__(self, artifact_path: str = "app/models/artifacts/mastery_model_v1.joblib"):
        self.artifact_path = artifact_path
        self.model = None
        self.version = "1.0.0-fallback"
        self._load_model()

    def _load_model(self):
        if os.path.exists(self.artifact_path):
            try:
                payload = joblib.load(self.artifact_path)
                self.model = payload.get("model")
                self.version = payload.get("version", "1.0.0")
            except Exception as e:
                print(f"Warning: Failed to load model artifact '{self.artifact_path}': {e}. Using baseline estimator.")
                self.model = None

    def predict(self, request: MasteryPredictionRequest) -> MasteryPredictionResponse:
        attempts = request.attempts
        factors = FeatureExtractor.extract_features(attempts)
        count = factors.evidence_count

        # Confidence increases with sample size (scaled to 5 attempts for full confidence)
        confidence = float(np.minimum(1.0, count / 5.0))

        if count == 0:
            mastery_score = 0.50
        elif self.model is not None:
            feature_df = FeatureExtractor.to_dataframe(factors)
            raw_pred = float(self.model.predict(feature_df)[0])
            mastery_score = float(np.clip(raw_pred, 0.0, 1.0))
        else:
            # Baseline estimator: weighted combination of historical accuracy & difficulty accuracy
            raw_acc = (factors.historical_accuracy * 0.5) + (factors.difficulty_weighted_accuracy * 0.3) + (factors.recent_accuracy * 0.2)
            mastery_score = float((raw_acc * confidence) + (0.5 * (1.0 - confidence)))

        # Guarantee evidence accumulation principle: 1 attempt does NOT jump to 100% or 0%
        if count == 1:
            mastery_score = float(np.clip(mastery_score, 0.35, 0.65))

        explanation, trend, risk = Explainer.generate_explanation(
            request.concept_name,
            mastery_score,
            confidence,
            factors
        )

        return MasteryPredictionResponse(
            concept=request.concept_name,
            concept_id=request.concept_id,
            mastery=round(mastery_score, 2),
            confidence=round(confidence, 2),
            evidence_count=count,
            trend=trend,
            risk=risk,
            explanation=explanation,
            contributing_factors=factors
        )
