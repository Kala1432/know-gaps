# Graph Report - knowledge-gap-intell  (2026-08-19)

## Corpus Check
- 149 files · ~26,499 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 863 nodes · 1021 edges · 93 communities (60 shown, 33 thin omitted)
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 156 edges (avg confidence: 0.74)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]
- [[_COMMUNITY_Community 18|Community 18]]
- [[_COMMUNITY_Community 19|Community 19]]
- [[_COMMUNITY_Community 20|Community 20]]
- [[_COMMUNITY_Community 21|Community 21]]
- [[_COMMUNITY_Community 22|Community 22]]
- [[_COMMUNITY_Community 23|Community 23]]
- [[_COMMUNITY_Community 25|Community 25]]
- [[_COMMUNITY_Community 26|Community 26]]
- [[_COMMUNITY_Community 27|Community 27]]
- [[_COMMUNITY_Community 28|Community 28]]
- [[_COMMUNITY_Community 29|Community 29]]
- [[_COMMUNITY_Community 30|Community 30]]
- [[_COMMUNITY_Community 31|Community 31]]
- [[_COMMUNITY_Community 32|Community 32]]
- [[_COMMUNITY_Community 33|Community 33]]
- [[_COMMUNITY_Community 34|Community 34]]
- [[_COMMUNITY_Community 35|Community 35]]
- [[_COMMUNITY_Community 36|Community 36]]
- [[_COMMUNITY_Community 37|Community 37]]
- [[_COMMUNITY_Community 38|Community 38]]
- [[_COMMUNITY_Community 40|Community 40]]
- [[_COMMUNITY_Community 41|Community 41]]
- [[_COMMUNITY_Community 42|Community 42]]
- [[_COMMUNITY_Community 43|Community 43]]
- [[_COMMUNITY_Community 44|Community 44]]
- [[_COMMUNITY_Community 45|Community 45]]
- [[_COMMUNITY_Community 47|Community 47]]
- [[_COMMUNITY_Community 76|Community 76]]
- [[_COMMUNITY_Community 78|Community 78]]
- [[_COMMUNITY_Community 79|Community 79]]
- [[_COMMUNITY_Community 83|Community 83]]
- [[_COMMUNITY_Community 84|Community 84]]
- [[_COMMUNITY_Community 85|Community 85]]
- [[_COMMUNITY_Community 86|Community 86]]
- [[_COMMUNITY_Community 87|Community 87]]
- [[_COMMUNITY_Community 88|Community 88]]
- [[_COMMUNITY_Community 89|Community 89]]
- [[_COMMUNITY_Community 90|Community 90]]
- [[_COMMUNITY_Community 91|Community 91]]
- [[_COMMUNITY_Community 92|Community 92]]

## God Nodes (most connected - your core abstractions)
1. `Question` - 24 edges
2. `Attempt` - 23 edges
3. `Topic` - 20 edges
4. `Subject` - 18 edges
5. `Concept` - 18 edges
6. `LearningSession` - 17 edges
7. `ConceptPrerequisite` - 17 edges
8. `KnowledgeGap` - 17 edges
9. `MasteryState` - 17 edges
10. `compilerOptions` - 16 edges

## Surprising Connections (you probably didn't know these)
- `PatternDetectionRequest` --uses--> `MasteryPredictionRequest`  [INFERRED]
  ml-service/app/main.py → ml-service/app/schemas/mastery.py
- `PatternDetectionRequest` --uses--> `MasteryPredictionResponse`  [INFERRED]
  ml-service/app/main.py → ml-service/app/schemas/mastery.py
- `PatternDetectionRequest` --uses--> `AttemptItem`  [INFERRED]
  ml-service/app/main.py → ml-service/app/schemas/mastery.py
- `PatternDetectionRequest` --uses--> `RetentionPredictionRequest`  [INFERRED]
  ml-service/app/main.py → ml-service/app/schemas/retention.py
- `PatternDetectionRequest` --uses--> `RecommendationRequest`  [INFERRED]
  ml-service/app/main.py → ml-service/app/schemas/recommendation.py

## Communities (93 total, 33 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.16
Nodes (9): Explainer, MasteryPredictor, extract_features(), FeatureExtractor, ContributingFactors, MasteryPredictionRequest, MasteryPredictionResponse, test_multiple_attempts_build_confidence() (+1 more)

### Community 1 - "Community 1"
Cohesion: 0.13
Nodes (30): api, ErrorState(), ErrorStateProps, LoadingSpinner(), LoadingSpinnerProps, Navbar(), NavbarProps, StatusBadge() (+22 more)

### Community 2 - "Community 2"
Cohesion: 0.07
Nodes (4): MlServiceClient, PerformanceController, Attempt, AttemptMapper

### Community 3 - "Community 3"
Cohesion: 0.07
Nodes (4): ConceptGraphService, ConceptGraphController, ConceptPrerequisite, ConceptGraphServiceImpl

### Community 5 - "Community 5"
Cohesion: 0.05
Nodes (10): ConceptService, ConceptController, name, ConceptServiceImpl, ConceptPrerequisiteRepository, ConceptRepository, StudentRepository, ConceptGraphServiceTest (+2 more)

### Community 7 - "Community 7"
Cohesion: 0.06
Nodes (31): 1. Executive Summary & Purpose, 2. Inviolable Governance & Source-of-Truth Rules, 3.1 Tier Specifications, 3. Technology Stack & Architectural Boundaries, 4.1 Canonical Domain Entities, 4. Domain Model & Entity Definitions, 5.1 Core Principle: Evidence-Based Multi-Factor Assessment, 5.2 Concept Mastery Estimation (+23 more)

### Community 8 - "Community 8"
Cohesion: 0.09
Nodes (5): QuestionController, QuestionServiceImpl, QuestionMapper, QuestionService, QuestionConceptRepository

### Community 10 - "Community 10"
Cohesion: 0.07
Nodes (28): mae, name, r2, rmse, data_leakage_audit, student_isolation_pass, temporal_leakage_pass, blocked_advanced_concept_excluded (+20 more)

### Community 11 - "Community 11"
Cohesion: 0.09
Nodes (21): dependencies, lucide-react, react, react-dom, recharts, devDependencies, autoprefixer, postcss (+13 more)

### Community 12 - "Community 12"
Cohesion: 0.06
Nodes (6): SessionController, LearningSession, SessionServiceImpl, SessionMapper, LearningSessionRepository, SessionService

### Community 14 - "Community 14"
Cohesion: 0.08
Nodes (3): Student, StudentMapper, RecommendationServiceTest

### Community 16 - "Community 16"
Cohesion: 0.11
Nodes (17): compilerOptions, allowImportingTsExtensions, isolatedModules, jsx, lib, module, moduleResolution, noEmit (+9 more)

### Community 17 - "Community 17"
Cohesion: 0.10
Nodes (19): 1. Executive Summary & Purpose, 2.1 What the System Predicts, 2.2 What the System DOES NOT Predict, 2. Model Scope & Scientific Boundaries, 3. Evaluation Dataset Provenance & Reproducibility, 4.1 Concept Mastery Estimator Benchmark, 4.2 Retention Risk Model Benchmark, 4.3 Next Best Concept Recommendation Engine Benchmark (+11 more)

### Community 18 - "Community 18"
Cohesion: 0.07
Nodes (7): StudentController, SubjectController, StudentServiceImpl, SubjectServiceImpl, SubjectRepository, StudentService, SubjectService

### Community 19 - "Community 19"
Cohesion: 0.13
Nodes (4): TopicController, TopicServiceImpl, TopicRepository, TopicService

### Community 22 - "Community 22"
Cohesion: 0.12
Nodes (15): 1. Running Python ML Service & Adaptive Loop Tests, 2. Running Spring Boot Backend Tests, 3. Running Frontend Build & Type Check, Adaptive Learning Loop & Feedback Architecture, Adaptive Progression Rules, Build & Test Instructions, code:text (Student Question Attempt), code:bash (cd ml-service) (+7 more)

### Community 23 - "Community 23"
Cohesion: 0.20
Nodes (4): DuplicateResourceException, InvalidPrerequisiteException, ResourceNotFoundException, RuntimeException

### Community 36 - "Community 36"
Cohesion: 0.67
Nodes (3): generate_synthetic_benchmark_data(), RetentionTrainer, train_and_compare()

### Community 37 - "Community 37"
Cohesion: 0.67
Nodes (3): generate_synthetic_data(), ModelTrainer, train_and_compare()

### Community 83 - "Community 83"
Cohesion: 0.25
Nodes (12): BaseModel, recommend_next_concepts(), RecommendationEngine, ConceptRecommendation, PrerequisiteEdge, RecommendationRequest, RecommendationResponse, StudentConceptState (+4 more)

### Community 84 - "Community 84"
Cohesion: 0.21
Nodes (8): AdaptiveLoopEvaluator, AttemptItem, test_application_lag_pattern_detection(), test_consecutive_successes_increase_difficulty(), test_fluency_lag_pattern_detection(), test_repeated_failures_decrease_difficulty(), test_single_correct_attempt_does_not_jump_difficulty(), test_extract_features_multiple_attempts()

### Community 85 - "Community 85"
Cohesion: 0.26
Nodes (6): PatternDetectionRequest, RetentionPredictor, detect_patterns(), ObservablePattern, PatternDetector, RetentionPredictionResponse

### Community 86 - "Community 86"
Cohesion: 0.18
Nodes (4): RecommendationController, RecommendationControllerTest, RecommendationServiceImpl, RecommendationService

### Community 87 - "Community 87"
Cohesion: 0.24
Nodes (6): extract_features(), RetentionFeatureExtractor, RetentionAttemptItem, RetentionFactors, RetentionPredictionRequest, test_retention_predictor_baseline_and_prototype_flag()

### Community 90 - "Community 90"
Cohesion: 0.39
Nodes (7): evaluate_mastery_models(), evaluate_recommendation_engine(), generate_mastery_eval_dataset(), run_full_evaluation(), test_mastery_model_outperforms_baseline(), test_no_student_leakage_in_evaluation(), test_recommendation_engine_satisfies_prerequisite_constraints()

## Knowledge Gaps
- **107 isolated node(s):** `private`, `version`, `type`, `dev`, `preview` (+102 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **33 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `name` connect `Community 5` to `Community 19`, `Community 88`, `Community 18`, `Community 11`?**
  _High betweenness centrality (0.083) - this node is a cross-community bridge._
- **Why does `AssessmentServiceImpl` connect `Community 88` to `Community 89`, `Community 2`, `Community 91`?**
  _High betweenness centrality (0.057) - this node is a cross-community bridge._
- **Why does `MlServiceClient` connect `Community 2` to `Community 11`, `Community 14`?**
  _High betweenness centrality (0.034) - this node is a cross-community bridge._
- **What connects `private`, `version`, `type` to the rest of the system?**
  _112 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `Community 1` be split into smaller, more focused modules?**
  _Cohesion score 0.1303030303030303 - nodes in this community are weakly interconnected._
- **Should `Community 2` be split into smaller, more focused modules?**
  _Cohesion score 0.06533776301218161 - nodes in this community are weakly interconnected._
- **Should `Community 3` be split into smaller, more focused modules?**
  _Cohesion score 0.07386363636363637 - nodes in this community are weakly interconnected._