# Knowledge Gap Intelligence Platform: Project Constitution

**Version:** 1.0.0  
**Status:** APPROVED & AUTHORITATIVE  
**Effective Date:** August 19, 2026  
**File Location:** `docs/PROJECT_CONSTITUTION.md`

---

## 1. Executive Summary & Purpose

The **Knowledge Gap Intelligence Platform** is an evidence-based learning intelligence platform designed to analyze, track, and diagnose student learning behavior. The primary goal of the system is to provide transparent, explainable, and scientifically-grounded insights into a student's cognitive mastery, knowledge gaps, and memory decay.

The platform MUST answer six fundamental questions for every student:
1. **What concepts do I actually understand?** (Mastery)
2. **What concepts do I partially understand?** (Partial Mastery)
3. **What concepts am I weak at?** (Knowledge Gaps)
4. **Which concepts am I likely to forget?** (Retention Risk)
5. **Which concept should I study next?** (Next Best Concept)
6. **Why did the system make this recommendation?** (Explainability)

### Core Philosophy
The platform **MUST NOT** recommend content based on clicks, popularity, arbitrary heuristics, or static linear sequences. The core intelligence relies strictly on empirical evidence gathered from student interactions, including correctness, response time, attempt history, question difficulty, concept dependencies, and spaced recall signals.

---

## 2. Inviolable Governance & Source-of-Truth Rules

Every developer, maintainer, and AI agent working on this repository MUST strictly abide by the following governing rules:

1. **Authoritative Status:** `docs/PROJECT_CONSTITUTION.md` is the SINGLE SOURCE OF TRUTH for architecture, domain modeling, intelligence boundaries, and engineering practices.
2. **Mandatory Pre-flight Read:** Every AI agent MUST read this file prior to analyzing, planning, or executing any modifications to the codebase.
3. **Explicit Conflict Resolution:** If a requested task conflicts with any directive in this document, the developer/agent **MUST STOP** immediately and explain the conflict to the project owner. Silent deviation is strictly prohibited.
4. **No Reinterpretation:** Do NOT reinterpret the product into a general-purpose LMS, generic course marketplace, or superficial quiz app.
5. **Dependency Discipline:** Do NOT introduce new libraries, databases, frameworks, or external APIs without explicit architectural justification and approval.
6. **Contract Preservation:** API specifications (OpenAPI/DTOs) and domain schemas must not be altered, broken, or deprecated without explicit versioning and approval.
7. **No Destruction:** Existing core functionality and domain logic must not be deleted or bypassed without explicit approval.

---

## 3. Technology Stack & Architectural Boundaries

The platform strictly enforces a modular, multi-tier architecture with clear boundaries between frontend presentation, business orchestration, and intelligence services.

```
+-------------------------------------------------------------+
|                      React + TypeScript                     |
|                   (Frontend Presentation)                   |
+-------------------------------------------------------------+
                               |
                               | REST / HTTP APIs
                               v
+-------------------------------------------------------------+
|                     Spring Boot + Java                      |
|            (Application & Business Domain APIs)             |
+-------------------------------------------------------------+
             |                                   |
             | Internal REST                     | Database Access
             v                                   v
+--------------------------+       +--------------------------+
|  Python + FastAPI (ML)   |       |        PostgreSQL        |
|  scikit-learn / pandas   |       |  (Application Data &     |
|  NumPy / SciPy           |       |   pgvector if required)  |
+--------------------------+       +--------------------------+
```

### 3.1 Tier Specifications

| Component | Technology | Primary Responsibilities | Strict Boundary Constraints |
| :--- | :--- | :--- | :--- |
| **Frontend** | React, TypeScript, Tailwind CSS (or similar UI library) | User interface, state presentation, visual dashboards, attempt submission, user interactions. | **MUST NOT** contain any ML logic, mastery calculations, or recommendation scoring algorithms. |
| **Backend** | Java, Spring Boot, Spring Data JPA, Security | Application orchestration, business rules, API routing, user/session management, persistence, domain validation, calling ML service endpoints. | Handles all user authentication, persistence, and external orchestration. Serves as the gateway to the ML service. |
| **ML Service** | Python, FastAPI, scikit-learn, pandas, NumPy, SciPy | Mastery state estimation, knowledge gap detection algorithms, retention risk/forgetting models, recommendation scoring engine. | **MUST NEVER** directly mutate frontend state or access user session tokens directly. Serves internal REST endpoints consumed by Spring Boot. |
| **Database** | PostgreSQL | Relational storage for domain entities, interaction histories, mastery snapshots, and recommendations. | `pgvector` extension may ONLY be enabled if vector similarity search is explicitly required (e.g., semantic question embedding clustering). |
| **Containerization** | Docker, Docker Compose | Service containerization, local execution consistency, isolated test environments. | All services must be reproducible via a single `docker-compose up` execution. |
| **Version Control** | Git + GitHub | Branch management, PR reviews, commit tracking, CI/CD workflows. | Main branch protected; all PRs require CI validation. |

---

## 4. Domain Model & Entity Definitions

The platform domain strictly revolves around the core entity chain:
`Student → Subject → Topic → Concept → Question → Attempt → Evidence → Mastery → Knowledge Gap → Recommendation → LearningSession`

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

### 4.1 Canonical Domain Entities

1. **Student:** The learner using the platform. Attributes include `student_id`, `email`, `created_at`, and preferences.
2. **Subject:** High-level academic domain (e.g., *Computer Science*, *Mathematics*).
3. **Topic:** Logical sub-division within a subject (e.g., *Data Structures*, *Linear Algebra*).
4. **Concept:** The atomic unit of knowledge to be mastered (e.g., *HashMap*, *Binary Search Trees*, *Pointers*).
5. **ConceptPrerequisite:** Directed dependency between concepts (`prerequisite_concept_id` → `target_concept_id`). Captures dependency type (*HARD_REQUIREMENT*, *RECOMMENDED_PRIOR*) and weight.
6. **Question:** Assessment item evaluating one or more concepts. Attributes: `question_id`, `content`, `question_type`, `base_difficulty` (0.0 to 1.0), `discrimination_index`.
7. **QuestionConcept:** Association mapping `question_id` to `concept_id` with weight/importance relative to the question.
8. **Attempt:** The record of a student answering a question. Attributes: `attempt_id`, `student_id`, `question_id`, `session_id`, `user_answer`, `is_correct`, `response_time_ms`, `attempt_number`, `timestamp`.
9. **Evidence:** Normalized interaction feature vector derived from attempts (e.g., accuracy trend, response speed relative to expected time, recency, attempt count).
10. **MasteryState:** Probabilistic estimation of a student's current understanding of a specific concept. Attributes: `student_id`, `concept_id`, `mastery_score` (0.00 to 1.00 / 0% to 100%), `confidence_level`, `last_evaluated_at`.
11. **KnowledgeGap:** Diagnosed learning vulnerability for a `(student_id, concept_id)` pair. Attributes: `gap_type` (*PREREQUISITE_DEFICIT*, *MEMORY_DECAY*, *PERSISTENT_MISCONCEPTION*, *FLUENCY_LAG*), `severity`, `detected_at`.
12. **Recommendation:** Actionable study suggestion. Attributes: `recommendation_id`, `student_id`, `recommended_concept_id`, `priority_score`, `explanation_text`, `generated_at`.
13. **LearningSession:** A continuous interaction timeframe grouping attempts and active feedback.

---

## 5. Intelligence Engine & Algorithmic Principles

### 5.1 Core Principle: Evidence-Based Multi-Factor Assessment
The platform enforces the fundamental product principle:
> **Knowledge weakness MUST be distinguished from temporary poor performance.**

- **Rule 1 (No Instant Weakness):** A single incorrect answer **MUST NOT** automatically classify a student as weak in a concept.
- **Rule 2 (No Instant Mastery):** A single correct answer **MUST NOT** automatically classify a concept as mastered.
- **Rule 3 (Evidence Convergence):** Classifications must require multiple data points across time, attempts, difficulty levels, and response speeds.

---

### 5.2 Concept Mastery Estimation
Mastery calculation MUST be continuous and probabilistic (e.g., Bayesian Knowledge Tracing - BKT, Item Response Theory - IRT, or exponential moving evidence models).

Factors incorporated into mastery evaluation:
- Historical correctness trajectory on relevant questions.
- Question difficulty parameters.
- Response time standard deviations (detecting guessing vs. fluent recall).
- Spacing and recency of attempts.
- Performance on prerequisite concepts.

---

### 5.3 Knowledge Gap Detection
The system identifies knowledge gaps when evidence crosses specific threshold criteria:

| Gap Type | Trigger Criteria |
| :--- | :--- |
| **Prerequisite Deficit** | Low performance on concept $C$ while prerequisite concept $P$ has unverified or low mastery. |
| **Memory Decay** | Concept $C$ previously held high mastery, but elapsed time without recall increases retention risk beyond threshold. |
| **Persistent Misconception** | Repeated incorrect attempts on questions testing concept $C$, specifically targeting recurring incorrect choice patterns. |
| **Fluency Lag** | Correct answers on concept $C$, but response time is significantly higher than normative baseline, indicating high cognitive load. |

---

### 5.4 Forgetting & Retention Prediction

The system models memory decay over time based on cognitive science principles (e.g., half-life regression, spaced repetition decay curves).

**Inputs for Retention Risk:**
- Elapsed time since last successful recall.
- Previous peak mastery level.
- Cumulative count of successful vs. failed recalls.
- Inter-attempt interval spacing.

#### Strict Terminology Boundary
- **PROHIBITED:** The system **MUST NEVER** claim absolute predictive certainty about human memory (e.g., *"You will forget this tomorrow"*).
- **MANDATORY:** The system **MUST ALWAYS** use probabilistic, non-alarmist language (e.g., *"estimated retention risk"*, *"recommended for review based on recall decay"*).

---

### 5.5 Next Best Concept Recommendation Engine

The recommendation engine ranks concepts using a deterministic scoring algorithm combining:
1. **Prerequisite Readiness Score:** Are all prerequisite concepts sufficiently mastered?
2. **Retention Risk Score:** Is a high-value previously mastered concept decaying?
3. **Zone of Proximal Development (ZPD) Fit:** Is the concept at an optimal difficulty delta for the student's current skill level?
4. **Strategic Importance:** Position of the concept in the curriculum tree.

#### Mandatory Explainability Rule
Every recommendation **MUST** be accompanied by a human-readable, deterministic explanation detailing *why* it was chosen.

*Example Approved Explanation:*
> "Study **Binary Search Trees** next because **Binary Search** is mastered (91%), **Tree Traversal** is partially mastered (68%), and **BST** is a critical prerequisite for the upcoming **AVL Trees** topic."

---

### 5.6 Adaptive Learning Loop & Feedback Loop Architecture

The platform enforces a closed-loop continuous feedback cycle that adjusts difficulty targets and recommendations dynamically upon receiving each student attempt:

```text
Student Question Attempt
   │
   ├── 1. Submit Attempt (Evidence Recorded: correctness, response_time_ms, base_difficulty)
   ├── 2. Update Mastery & Retention Models (Probabilistic continuous updating)
   ├── 3. Recalculate Knowledge Gaps & Detect Mistake Patterns (PatternDetector: FLUENCY_LAG, RETENTION_DECAY, etc.)
   └── 4. Recalculate Next Best Concept & Adaptive Practice Difficulty Target
```

#### Adaptive Difficulty Rules:
- **Repeated Failures:** If a student repeatedly fails questions ($\ge 2$ consecutive failures), target question difficulty drops ($0.20 - 0.35$) to recommend foundational questions and prerequisite remediation.
- **Consistent Success:** Target question difficulty increases ($0.70 - 0.90$) **only** after $\ge 3$ consecutive successful attempts (confidence $\ge 0.60$). Single correct attempts **MUST NEVER** trigger difficulty jumps.

#### Observable Mistake Pattern Detection:
The system identifies empirical, observable mistake patterns without inferring subjective psychological traits:
- `PREREQUISITE_DEFICIT`: Repeated failures on a concept coinciding with an unmastered prerequisite.
- `FLUENCY_LAG`: High response times ($> 15\text{s}$) despite high accuracy ($\ge 70\%$).
- `RETENTION_DECAY`: Sharp drop in recent attempt accuracy compared to historical baseline.
- `APPLICATION_LAG`: High accuracy on low difficulty questions ($< 0.40$), but failure on higher difficulty questions ($> 0.60$).


---

## 6. AI & Large Language Model (LLM) Governance

LLMs are permitted ONLY as complementary synthesis and content-generation utilities. They are strictly prohibited from serving as core cognitive or analytical authorities.

```
+-------------------------------------------------------------------+
|                        PERMITTED LLM USES                         |
|  - Generating explanatory hints & step-by-step solutions          |
|  - Authoring draft practice questions (subject to validation)     |
|  - Transforming raw educational text into structured reading      |
+-------------------------------------------------------------------+

                                 VS.

+-------------------------------------------------------------------+
|                        FORBIDDEN LLM USES                         |
|  - Mastery score calculation                                      |
|  - Retention decay / forgetting prediction                        |
|  - Knowledge gap detection & classification                       |
|  - Recommendation ranking & priority scoring                      |
+-------------------------------------------------------------------+
```

### Authoritative Boundary Rule
All numerical mastery values, retention risks, knowledge gap classifications, and recommendation scores **MUST** be calculated by deterministic business logic and validated ML models in the Python/Spring Boot services. **LLMs MUST NOT be the source of truth for these computations.**

---

## 7. Explicit Non-Goals & Product Scope Boundaries

To prevent scope creep and product degeneration, the following are explicitly designated as **NON-GOALS**. Building any of the following features is strictly forbidden:

1. **NOT a Generic ChatGPT/LLM Wrapper:** The system must not be a conversational wrapper over an LLM API.
2. **NOT a Generic Course Marketplace:** No payment systems, instructor storefronts, or content sales engines.
3. **NOT a Simple Quiz Application:** Simple flashcard or naive quiz apps without knowledge-tracing intelligence are out of scope.
4. **NOT a Popularity-Based Recommender:** No recommendations based on click rates, user upvotes, or trending views.
5. **NOT a Generic Chatbot Interface:** Interactions must be structured around learning diagnostics, not open-ended conversation.
6. **NOT an Intelligence Diagnostic Tool:** The system must NEVER claim to measure psychological IQ, innate intelligence, or cognitive capability.
7. **NO Academic Guarantees:** The system must NEVER promise specific grade outcomes or academic success guarantees.

---

## 8. Engineering Standards & Quality Requirements

### 8.1 Architecture & Design
- **Clean / Layered Architecture:** Domain logic must remain independent of frameworks, databases, and UI implementations.
- **Typed API Contracts:** OpenAPI 3.0 / Swagger definitions must govern all Spring Boot REST endpoints. TypeScript interfaces must strictly mirror API DTOs.
- **Modularity:** ML models, persistence, authentication, and presentation layers must be strictly decoupled.

### 8.2 Testing & Verification
- **Backend:** Unit test coverage (>80%) for domain services using JUnit 5 and Mockito. Integration tests using Testcontainers for PostgreSQL.
- **ML Service:** Deterministic unit testing using `pytest` with fixed RNG seeds. Validation datasets for memory decay and mastery algorithms.
- **Frontend:** Component testing using React Testing Library and end-to-end user flow tests.

### 8.3 Security & Performance
- All API endpoints must enforce input validation (e.g., `jakarta.validation` in Java, `pydantic` in Python).
- Zero storage of plain-text credentials or API keys. Environment variables via `.env` configuration.
- Database access must be parameterized (Spring Data JPA / Hibernate) to prevent SQL injection.

---

## 9. Constitution Revision & Audit Protocol

1. **Immutability:** Revisions to this constitution require explicit user approval and a documented decision record.
2. **Audit Requirement:** Every major pull request must be audited against Section 2 (Governance), Section 3 (Architecture Boundaries), and Section 6 (LLM Governance).
3. **Automated Verification:** Future updates to the code must be accompanied by graph updates (`graphify update .`) to maintain AST knowledge graphs when configured.

---
**Approved by Lead Software Architect**  
*Knowledge Gap Intelligence Platform Team*
