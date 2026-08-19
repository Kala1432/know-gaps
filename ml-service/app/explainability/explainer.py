from app.schemas.mastery import ContributingFactors

class Explainer:

    @staticmethod
    def generate_explanation(
        concept_name: str,
        mastery_score: float,
        confidence: float,
        factors: ContributingFactors
    ) -> tuple[str, str, str]:
        """
        Generates (explanation_text, trend_string, risk_level_string)
        based on evidence vectors without unsupported claims.
        """
        count = factors.evidence_count
        if count == 0:
            return (
                f"No evidence collected yet for concept '{concept_name}'. Initial retention risk is unverified.",
                "insufficient_data",
                "high"
            )

        success = factors.successful_recalls
        failed = factors.failed_recalls
        slope = factors.recent_trend_slope
        mastery_pct = int(round(mastery_score * 100))

        # Determine trend
        if slope > 0.05:
            trend = "improving"
        elif slope < -0.05:
            trend = "declining"
        else:
            trend = "stable"

        # Determine retention risk level
        if mastery_score >= 0.80 and confidence >= 0.60:
            risk = "low"
        elif mastery_score >= 0.50:
            risk = "medium"
        else:
            risk = "high"

        # Construct evidence-based explanation text
        explanation_parts = []
        explanation_parts.append(
            f"Estimated mastery for '{concept_name}' is {mastery_pct}% based on {count} attempt(s) "
            f"({success} correct, {failed} incorrect)."
        )

        if count == 1:
            explanation_parts.append(
                "A single attempt provides low confidence; additional evidence is needed to confirm mastery."
            )
        elif trend == "improving":
            explanation_parts.append(
                f"Mastery increased because recent performance shows positive improvement across recent attempts."
            )
        elif trend == "declining":
            explanation_parts.append(
                f"Estimated mastery decreased due to recent incorrect attempts or deteriorating recall accuracy."
            )
        else:
            explanation_parts.append(
                f"Performance has remained consistent across recent attempts."
            )

        if factors.avg_response_time_ms > 15000:
            explanation_parts.append(
                "Higher average response time suggests high cognitive effort or potential difficulty."
            )

        explanation = " ".join(explanation_parts)
        return explanation, trend, risk
