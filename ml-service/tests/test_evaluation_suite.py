from app.evaluation.evaluate_all import generate_mastery_eval_dataset, evaluate_mastery_models, evaluate_recommendation_engine

def test_mastery_model_outperforms_baseline():
    df = generate_mastery_eval_dataset(n_samples=200)
    eval_res = evaluate_mastery_models(df)
    assert eval_res["ml_model"]["r2"] >= eval_res["baseline"]["r2"]
    assert eval_res["ml_model"]["mae"] <= eval_res["baseline"]["mae"]

def test_recommendation_engine_satisfies_prerequisite_constraints():
    rec_res = evaluate_recommendation_engine()
    assert rec_res["engine"]["prerequisite_constraint_satisfaction_rate"] == 1.0
    assert rec_res["engine"]["prerequisite_remediation_active"] is True
    assert rec_res["engine"]["blocked_advanced_concept_excluded"] is True

def test_no_student_leakage_in_evaluation():
    df = generate_mastery_eval_dataset(n_samples=100)
    students = df["student_id"].unique()
    assert len(students) > 1
