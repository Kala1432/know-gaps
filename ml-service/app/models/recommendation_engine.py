from typing import List, Dict, Set
from collections import defaultdict, deque
from app.schemas.recommendation import (
    RecommendationRequest, RecommendationResponse, ConceptRecommendation,
    StudentConceptState, PrerequisiteEdge
)

class RecommendationEngine:

    @staticmethod
    def recommend_next_concepts(request: RecommendationRequest) -> RecommendationResponse:
        concept_map: Dict[str, StudentConceptState] = {c.concept_id: c for c in request.concept_states}

        # Build Graph: prereq_map[target] = list of direct prerequisite_ids
        prereq_map: Dict[str, List[str]] = defaultdict(list)
        # downstream_map[prereq] = list of direct target_ids
        downstream_map: Dict[str, List[str]] = defaultdict(list)

        for edge in request.prerequisites:
            prereq_map[edge.target_concept_id].append(edge.prerequisite_concept_id)
            downstream_map[edge.prerequisite_concept_id].append(edge.target_concept_id)

        # Helper: Find all ancestor prerequisites for a given concept ID
        def get_all_ancestors(concept_id: str) -> Set[str]:
            ancestors = set()
            queue = deque([concept_id])
            while queue:
                curr = queue.popleft()
                for p in prereq_map[curr]:
                    if p not in ancestors:
                        ancestors.add(p)
                        queue.append(p)
            return ancestors

        # Evaluate Prerequisite Status: "SATISFIED", "DEFICIT", "BLOCKED"
        prereq_status: Dict[str, str] = {}
        for cid, state in concept_map.items():
            ancestors = get_all_ancestors(cid)
            # If any ancestor has mastery < 0.60, this concept is BLOCKED
            unmastered_ancestors = [
                concept_map[a].concept_name
                for a in ancestors
                if a in concept_map and concept_map[a].mastery < 0.60
            ]
            if unmastered_ancestors:
                prereq_status[cid] = "BLOCKED"
            elif state.mastery < 0.60 and downstream_map[cid]:
                # Unblocked concept that is required by downstream concepts -> DEFICIT
                prereq_status[cid] = "DEFICIT"
            else:
                prereq_status[cid] = "SATISFIED"

        # Multi-factor Candidate Scoring
        candidates: List[dict] = []
        for cid, state in concept_map.items():
            status = prereq_status[cid]
            
            # HARD PREREQUISITE GATING: If concept is BLOCKED, NEVER recommend it directly!
            if status == "BLOCKED":
                continue

            # Multi-factor score
            deficit_bonus = 0.50 if status == "DEFICIT" else 0.0
            retention_bonus = 0.25 * state.retention_risk
            mastery_gap = 0.15 * (1.0 - state.mastery)
            downstream_impact = 0.10 * min(1.0, len(downstream_map[cid]) / 3.0)

            score = deficit_bonus + retention_bonus + mastery_gap + downstream_impact

            # Recommended activity and explainable reason
            if status == "DEFICIT":
                activity = "PREREQUISITE_REMEDIATION"
                downstream_names = [concept_map[d].concept_name for d in downstream_map[cid] if d in concept_map]
                downstream_str = ", ".join(downstream_names[:2])
                reason = (
                    f"Practice {state.concept_name} next because it is a prerequisite for {downstream_str} "
                    f"and your estimated mastery is currently {int(round(state.mastery * 100))}%."
                )
            elif state.retention_risk >= 0.60:
                activity = "CONCEPT_REVIEW"
                reason = (
                    f"Review {state.concept_name} to reinforce retention risk ({int(round(state.retention_risk * 100))}%) "
                    f"before decay impairs recall."
                )
            elif state.mastery < 0.60:
                activity = "PRACTICE_QUESTIONS"
                reason = (
                    f"Practice questions for {state.concept_name} to increase mastery from "
                    f"{int(round(state.mastery * 100))}% towards target proficiency."
                )
            else:
                activity = "ADVANCED_CHALLENGE"
                reason = (
                    f"Challenge yourself with advanced questions in {state.concept_name} "
                    f"(mastery is at {int(round(state.mastery * 100))}%)."
                )

            candidates.append({
                "concept_id": cid,
                "concept_name": state.concept_name,
                "score": round(score, 4),
                "mastery": state.mastery,
                "retention_risk": state.retention_risk,
                "prerequisite_status": status,
                "recommended_activity": activity,
                "reason": reason
            })

        # Sort candidates descending by score
        candidates.sort(key=lambda x: x["score"], reverse=True)

        # Format top recommendations up to request limit
        top_recs: List[ConceptRecommendation] = []
        for rank, c in enumerate(candidates[:request.limit], start=1):
            top_recs.append(ConceptRecommendation(
                concept=c["concept_name"],
                concept_id=c["concept_id"],
                priority=c["score"],
                rank=rank,
                reason=c["reason"],
                mastery=c["mastery"],
                retention_risk=c["retention_risk"],
                prerequisite_status=c["prerequisite_status"],
                recommended_activity=c["recommended_activity"]
            ))

        return RecommendationResponse(
            student_id=request.student_id,
            recommendations=top_recs,
            prerequisite_constraint_satisfaction_rate=1.0 # 100% prerequisite constraint satisfaction rate
        )
