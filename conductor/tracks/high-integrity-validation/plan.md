# T26: High-Integrity Validation (Test Expansion)

## Overview
This track focuses on increasing the system's test coverage from ~10% to >80%, specifically targeting the high-risk intelligence, temporal logic, and synchronization layers. PHOS is a medical-grade application; therefore, every logic branch must be validated to ensure patient safety.

## Strategy
1. **Intelligence Mocking:** Utilize mock generation for ML Kit GenAI Prompt API to test the logic of `GeminiNanoEngine` without requiring real hardware.
2. **Temporal Edge Cases:** Expand `TemporalEngine` and `CollisionResolver` tests to include extreme edge cases (e.g., 24-hour travel shifts, overlapping PRN doses).
3. **UI/UX Stability:** Implement unit tests for all ViewModels to ensure state mapping (e.g., Vertical Timeline rendering) is correct under all physiological contexts.
4. **Sync Integrity:** Verify Protobuf serialization and Horologist DataStore sync logic to prevent data corruption between Phone and Watch.

## Milestones
- [x] **M1: Core Intelligence Tests**
    - [x] Implement `GeminiNanoEngineTest` (multimodal analysis & JSON extraction).
    - [x] Implement `VoiceEntityParserTest` (NLP entity extraction accuracy).
    - [x] Implement `GoalOptimizationEngineTest` (goal-to-offset logic).
- [x] **M2: Temporal & Safety Engine Expansion**
    - [x] Implement `MealSchedulerTest` (dietary-medication sync logic).
    - [x] Expand `CollisionResolverTest` with complex multi-pill conflict scenarios.
    - [x] Expand `PhysiologicalSuppressorTest` with real-world sensor data mocks.
- [x] **M3: Sync & Data Layer Validation**
    - [x] Implement `DataLayerRepositoryTest` (serialization/deserialization).
    - [x] Build `RoomTemporalVersioningTest` to verify `validFrom`/`validTo` logic.
- [x] **M4: UI State Mapping & Reliability**
    - [x] Implement `DashboardViewModelTest` (state flow and UI events).
    - [x] Create `WidgetStateMappingTest` for Jetpack Glance widget reliability.
    - [x] Implement `AmbientUXTest` for Wear OS haptic pattern triggers.

## Verification
- Run `./gradlew test` to ensure all modules pass.
- Generate coverage report via Jacoco or Kover to verify >80% coverage in core modules.
- Perform a manual "Chaos Test" by simulating rapid health data changes in Health Connect.
