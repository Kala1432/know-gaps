# Knowledge Gap Intelligence Platform: System Architecture Specification

**Version:** 1.0.0  
**Status:** APPROVED & AUTHORITATIVE ARCHITECTURE DOCUMENT  
**Date:** August 19, 2026  
**File Location:** `docs/ARCHITECTURE.md`  
**Governing Document:** [`docs/PROJECT_CONSTITUTION.md`](PROJECT_CONSTITUTION.md)

---

## 1. System Overview & Core Philosophy

The **Knowledge Gap Intelligence Platform** is an evidence-based learning intelligence system. The core architecture evaluates student attempt behavior (correctness, response speed, difficulty weighting, attempt history, and prerequisite dependency graphs) to deliver probabilistic continuous cognitive mastery estimations, retention decay windows, and prerequisite-gated recommendations.

---

## 2. Multi-Tier Architecture Topology

The platform enforces a strict multi-tier architecture isolating user presentation, business domain orchestration, machine learning intelligence, and relational persistence.

```
+-------------------------------------------------------------+
|                      React + TypeScript                     |
|            (Frontend UI Presentation - Nginx:80)            |
+-------------------------------------------------------------+
                               |
                               | HTTP REST APIs (/api/v1)
                               v
+-------------------------------------------------------------+
|                     Spring Boot + Java 21                   |
|           (Backend Business Domain Gateway - Port 8080)     |
+-------------------------------------------------------------+
             |                                   |
             | Internal REST                     | Spring Data JPA
             v                                   v
+--------------------------+       +--------------------------+
|  Python 3.13 + FastAPI   |       |      PostgreSQL 16       |
|  scikit-learn ML Service |       | (Application Persistence |
|       (Port 8000)        |       |    & Flyway Migrations)  |
+--------------------------+       +--------------------------+
```

---

## 3. Tier Responsibilities & Strict Boundaries

| Tier | Primary Technologies | Key Responsibilities | Inviolable Boundary Constraints |
| :--- | :--- | :--- | :--- |
| **Frontend** | React 18, TypeScript, Vite, Tailwind CSS, Recharts | Presentation, visual dashboarding, active assessment studio, prerequisite graph inspector. | **MUST NOT** contain any ML logic, cognitive calculations, or recommendation scoring rules. |
| **Backend Gateway** | Java 21, Spring Boot 3.3, Flyway, Spring Security | Domain orchestration, REST API routing, JPA persistence, input validation, calling internal ML service. | Serves as the sole entry point for client requests and database interactions. |
| **ML Intelligence** | Python 3.13, FastAPI, scikit-learn, pandas | Continuous mastery estimation, retention risk modeling, adaptive difficulty evaluation, mistake pattern detection. | Serves internal REST endpoints consumed strictly by the Java backend. Never mutates frontend state directly. |
| **Persistence** | PostgreSQL 16 | Relational storage for entities, sessions, attempts, mastery states, gaps, and prerequisite edges. | Managed via Flyway schema migrations (`V1`, `V2`). |

---

## 4. Canonical Domain Entity Diagram

```
+---------+           +---------+           +-------+
| Student | 1     *  | Subject | 1     *  | Topic |
+---------+           +---------+           +-------+
     |                                          | 1
     |                                          | *
     |                                      +---------+
     |                                      | Concept |<---+ (Prerequisites)
     |                                      +---------+    |
     |                                           | 1       |
     |                                           | *       |
     |                                    +--------------+ |
     |                                    | QuestionConcept |
     |                                    +--------------+ |
     |                                           | *       |
     |             +----------+                  | 1       |
     |  1       *  | Learning |           +--------------+ |
     +------------>| Session  |           |   Question   | |
     |             +----------+           +--------------+ |
     |                  | 1                      | 1       |
     |                  | *                      | *       |
     |             +----------+                  |         |
     +------------>| Attempt  |<-----------------+         |
     |             +----------+                            |
     |                  | 1                                |
     |                  v 1                                |
     |             +----------+                            |
     +------------>| Evidence |                            |
     |             +----------+                            |
     |                  |                                  |
     |                  v                                  |
     |             +--------------+                        |
     +------------>| MasteryState |                        |
     |             +--------------+                        |
     |                  |                                  |
     |                  v                                  |
     +------------>| KnowledgeGap |------------------------+
     |             +--------------+
     |                  |
     |                  v
     +------------>| Recommendation |
                   +----------------+
```

---

## 5. System Interaction Flows

### 5.1 Question Attempt & Adaptive Feedback Loop

```text
User (React Frontend)          Spring Boot Backend           Python ML Service            PostgreSQL
        │                              │                            │                         │
        │── Submit Attempt ───────────>│                            │                         │
        │   (qId, answer, timeMs)      │── Persist Attempt ──────────────────────────────────>│
        │                              │                            │                         │
        │                              │── POST /mastery/predict ──>│                         │
        │                              │   (Attempts vector)        │                         │
        │                              │<── Mastery & Confidence ───│                         │
        │                              │                            │                         │
        │                              │── POST /patterns/detect ──>│                         │
        │                              │<── Observable Patterns ────│                         │
        │                              │                            │                         │
        │                              │── Update Mastery & Gaps ────────────────────────────>│
        │<── Attempt Feedback Result ──│                            │                         │
```

---

## 6. Containerization & Production Deployment Architecture

Production-style deployment is managed via `docker-compose.yml`:

- `frontend`: Containerized Nginx serving Vite static assets on port `80`, proxying `/api/` requests to `backend:8080`.
- `backend`: Containerized JDK 21 JRE running Spring Boot on port `8080`, communicating with `postgres:5432` and `ml-service:8000`.
- `ml-service`: Containerized Python 3.13 environment running Uvicorn on port `8000`.
- `postgres`: Containerized PostgreSQL 16-alpine on port `5432` with volume persistence (`pgdata`).

---

## 7. Telemetry & Telemetry Monitoring

- **Inference Latency Tracking:** FastAPI middleware attaches `X-Inference-Latency-MS` header and logs request execution times.
- **Service Health Probes:** `GET /health` endpoints exposed on ML service and Spring Boot backend.
- **Model Version Tracking:** `GET /version` returns active model version (`mastery_model_v1.joblib`), training date, and prototype parameters (`is_prototype: true`).
