# Knowledge Gap Intelligence Platform

An evidence-based learning intelligence platform designed to analyze, track, and diagnose student learning behavior. The platform provides transparent, explainable insights into cognitive mastery, knowledge gaps, retention risk, next best concept recommendations, and adaptive mistake pattern detection.

> **Project Constitution & Single Source of Truth:**  
> All architectural boundaries, governance principles, domain entities, and engineering standards are defined in [`docs/PROJECT_CONSTITUTION.md`](docs/PROJECT_CONSTITUTION.md).

---

## 1. Project Overview

The Knowledge Gap Intelligence Platform helps students understand their learning status across 6 fundamental questions:
1. **What concepts do I actually understand?** (Mastery)
2. **What concepts do I partially understand?** (Partial Mastery)
3. **What concepts am I weak at?** (Knowledge Gaps)
4. **Which concepts am I likely to forget?** (Retention Risk)
5. **Which concept should I study next?** (Next Best Concept)
6. **Why did the system make each recommendation?** (Explainability)

---

## 2. Problem Statement

Traditional Learning Management Systems (LMS) and quiz apps recommend content based on clicks, popularity, superficial view counts, or arbitrary linear rules. They treat a single correct answer as instant mastery and a single wrong answer as total weakness.

The Knowledge Gap Intelligence Platform solves this by relying strictly on **empirical multi-attempt evidence** (correctness trajectory, response speed relative to difficulty, attempt recency, inter-attempt spacing, and prerequisite dependency graphs).

---

## 3. Architecture Overview

The platform enforces a strict 4-tier architecture:
- **Frontend (`frontend/`):** React 18 + TypeScript + Vite + Tailwind CSS (UI presentation, visual dashboards, active assessment studio, prerequisite graph inspector).
- **Backend Gateway (`backend/`):** Java 21 + Spring Boot 3.3 (Domain orchestration, REST APIs, Flyway DB migrations, validation).
- **ML Service (`ml-service/`):** Python 3.13 + FastAPI + scikit-learn (Cognitive mastery estimation, retention risk modeling, prerequisite-gated recommendation engine, adaptive loop evaluator, observable pattern detector).
- **Database:** PostgreSQL 16 (Relational persistence with Flyway schema migrations `V1`, `V2`).

Detailed architectural specification: [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## 4. Machine Learning Methodology

The intelligence engine combines probabilistic cognitive evaluation with multi-factor prerequisite graph traversal:
- **Mastery Estimation:** Probabilistic continuous scoring ($0.00 - 1.00$) evaluating attempt accuracy trajectories, question difficulty weighting, and trend slope.
- **Retention Decay Modeling:** Spaced-repetition half-life model ($R = e^{-\lambda t}$) estimating recall decay probability without claiming absolute predictive certainty.
- **Adaptive Progression:** Target question difficulty advances ($0.70 - 0.90$) **only** after $\ge 3$ consecutive successful attempts. Single correct answers **never** trigger difficulty jumps.

---

## 5. Dataset Provenance

- **Evaluation Dataset:** $N = 500$ trajectories across $50$ distinct student profiles and $10$ atomic concepts generated with fixed seed `seed=42`.
- **Prototype Flagging:** Synthetic evaluation benchmark outputs are flagged with `"is_prototype": true`.

---

## 6. Feature Engineering

Feature extraction (`FeatureExtractor` in `ml-service/app/preprocessing/feature_extractor.py`) computes 9 interaction features from attempt vectors:
1. `historical_accuracy`
2. `recent_accuracy` (last 3 attempts)
3. `evidence_count`
4. `successful_recalls`
5. `failed_recalls`
6. `avg_response_time_sec`
7. `difficulty_weighted_accuracy`
8. `recent_trend_slope`
9. `time_since_last_attempt_hours`

---

## 7. Model Selection

We compare transparent statistical baselines against machine learning models:
- **Mastery Estimator:** Weighted Moving Accuracy Baseline vs Random Forest Regressor ($100$ estimators, `max_depth=8`).
- **Retention Risk Model:** Ebbinghaus Exponential Decay Baseline vs Random Forest Regressor ($100$ estimators, `max_depth=6`).

---

## 8. Evaluation & Metrics

Scientific evaluation results (documented in [`docs/ML_EVALUATION.md`](docs/ML_EVALUATION.md)):

| Component | Metric | Baseline | ML Model | Improvement |
| :--- | :--- | :---: | :---: | :---: |
| **Mastery Estimator** | $R^2$ Score | $0.9506$ | **$0.9677$** | **$+0.0171$** |
| **Mastery Estimator** | MAE | $0.0494$ | **$0.0406$** | **$-0.0088$** |
| **Recommendation Engine** | Prereq Satisfaction Rate | $0.00$ | **$1.00$ ($100\%$)** | **$+1.00$** |

---

## 9. Next Best Concept Recommendation Methodology

Recommendations enforce prerequisite dependency DAG constraints via transitive ancestor traversal:
$$\text{Score} = w_1 \cdot \text{PrereqDeficitBonus} + w_2 \cdot \text{RetentionRisk} + w_3 \cdot (1 - \text{Mastery}) + w_4 \cdot \text{DownstreamImpact}$$
If any prerequisite ancestor of concept $B$ has mastery $< 0.60$, concept $B$ is marked `BLOCKED` and excluded from candidate recommendations.

---

## 10. User Interface & Main Screens

- **Dashboard:** Core spotlight card answering **"What should I study next and why?"**, summary statistics, strongest/weakest concepts, retention risk alerts, and recent learning timeline.
- **Concept Explorer:** Subject/topic hierarchy selection and visual prerequisite dependency graph inspector.
- **Assessment Studio:** Active practice session view, question prompt, difficulty rating, real-time response timer (`responseTimeMs`), answer submission, and instant evidence feedback.
- **Progress Analytics:** Recharts mastery vs accuracy bar charts and diagnosed knowledge vulnerability cards.
- **Recommendation View:** Priority ranking, prerequisite status (`SATISFIED`, `DEFICIT`, `BLOCKED`), evidence-based justifications, and practice action triggers.

---

## 11. Local Setup

### Prerequisites
- Java 21 JDK
- Maven 3.9+
- Python 3.13+
- Node.js 20+

### Step-by-Step Local Run
```bash
# 1. Run Python ML Service
cd ml-service
python3 -m venv venv && source venv/bin/activate
pip install -r requirements.txt
python3 app/models/trainer.py
python3 app/models/retention_trainer.py
uvicorn app.main:app --port 8000 &

# 2. Run Spring Boot Backend
cd ../backend
mvn spring-boot:run &

# 3. Run React Frontend
cd ../frontend
npm install
npm run dev
```

---

## 12. Docker Setup

To build and launch the full 4-tier stack in Docker Compose:

```bash
cp .env.example .env
docker compose build
docker compose up -d
```
- **Frontend Dashboard:** `http://localhost:80`
- **Spring Boot Backend REST API:** `http://localhost:8080`
- **FastAPI ML Service:** `http://localhost:8000`
- **PostgreSQL Database:** `localhost:5432`

---

## 13. Environment Variables

All secrets are managed via environment variables (defined in `.env.example`):
- `POSTGRES_DB`: Database name (`knowledgegap`)
- `POSTGRES_USER`: Database username (`kguser`)
- `POSTGRES_PASSWORD`: Database password
- `ML_SERVICE_URL`: Internal URL to ML service (`http://ml-service:8000`)
- `JWT_SECRET`: Secret key for authentication tokens

---

## 14. Deployment & CI/CD

Continuous Integration is automated via GitHub Actions ([`.github/workflows/ci.yml`](.github/workflows/ci.yml)):
1. Checks out repository
2. Builds React frontend (`npm run build`)
3. Builds & tests Spring Boot backend (`mvn clean test`)
4. Runs Python ML tests (`pytest tests/`) and evaluation checks (`evaluate_all.py`)
5. Validates Docker Compose compilation (`docker compose build`)

> **Production Approval Mechanism:** Manual approval is required before deploying Docker artifacts to production environments.

---

## 15. Limitations

- **Synthetic Evaluation Data:** Benchmark models utilize synthetic interaction trajectories (`is_prototype: true`). Fine-tuning on real student cohort logs is recommended for production scaling.
- **Reading Time Ambiguity:** Questions with lengthy descriptions may inflate response time without reflecting cognitive difficulty.

---

## 16. Future Improvements

- Incorporate vector similarity search using `pgvector` for question embedding clustering.
- Integrate item discrimination parameters from Item Response Theory (2PL IRT).
- Expand frontend visual graph rendering using D3.js or Cytoscape.js.
# know-gaps
