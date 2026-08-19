import os
import joblib
import numpy as np
from app.schemas.retention import RetentionPredictionRequest, RetentionPredictionResponse, RetentionFactors
from app.preprocessing.retention_extractor import RetentionFeatureExtractor

class RetentionPredictor:

    def __init__(self, artifact_path: str = "app/models/artifacts/retention_model_v1.joblib"):
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
                print(f"Warning: Failed to load retention model '{self.artifact_path}': {e}. Using baseline estimator.")
                self.model = None

    def predict(self, request: RetentionPredictionRequest) -> RetentionPredictionResponse:
        factors = RetentionFeatureExtractor.extract_features(request.current_mastery, request.attempts)

        if len(request.attempts) == 0:
            risk_score = 0.50
        elif self.model is not None:
            feature_df = RetentionFeatureExtractor.to_dataframe(factors)
            raw_pred = float(self.model.predict(feature_df)[0])
            risk_score = float(np.clip(raw_pred, 0.0, 1.0))
        else:
            # Baseline Half-Life model calculation
            succ = factors.successful_recalls
            spacing = factors.avg_spacing_days
            days = factors.days_since_last_successful_recall
            mastery = factors.current_mastery

            half_life = 2.0 * (1.0 + 0.5 * succ) * (1.0 + 0.2 * spacing)
            decay_rate = np.log(2.0) / max(0.5, half_life)
            retention_prob = np.exp(-decay_rate * days)
            risk_score = float(np.clip((1.0 - retention_prob) + (0.15 * (1.0 - mastery)), 0.0, 1.0))

        # Determine risk level and recommended review window
        if risk_score >= 0.65:
            risk_level = "high"
            review_window = "today"
        elif risk_score >= 0.35:
            risk_level = "medium"
            review_window = "soon"
        else:
            risk_level = "low"
            review_window = "later"

        explanation = self._generate_explanation(
            request.concept_name,
            risk_score,
            risk_level,
            review_window,
            factors
        )

        return RetentionPredictionResponse(
            concept=request.concept_name,
            concept_id=request.concept_id,
            retention_risk=round(risk_score, 2),
            risk_level=risk_level,
            recommended_review_window=review_window,
            explanation=explanation,
            is_prototype=True,
            contributing_factors=factors
        )

    def _generate_explanation(
        self,
        concept_name: str,
        risk_score: float,
        risk_level: str,
        review_window: str,
        factors: RetentionFactors
    ) -> str:
        parts = []

        if risk_level == "high":
            parts.append(f"Retention risk for '{concept_name}' is elevated ({int(round(risk_score * 100))}%).")
        elif risk_level == "medium":
            parts.append(f"Retention risk for '{concept_name}' is moderate ({int(round(risk_score * 100))}%).")
        else:
            parts.append(f"Retention risk for '{concept_name}' is low ({int(round(risk_score * 100))}%).")

        days_succ = factors.days_since_last_successful_recall
        if days_succ > 0:
            parts.append(f"The concept has not been successfully recalled in {days_succ:.1f} days.")
        else:
            parts.append("No successful recall has been recorded yet.")

        if factors.failed_recalls > factors.successful_recalls:
            parts.append("Previous recall attempts showed weak performance.")
        elif factors.successful_recalls > 0:
            parts.append(f"Student has completed {factors.successful_recalls} successful recall(s).")

        parts.append(f"Recommended review window is '{review_window}'.")

        return " ".join(parts)
