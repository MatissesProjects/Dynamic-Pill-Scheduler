# Tracks Registry: Dynamic Pill Scheduler (PHOS)
| ID | Name | Description | Folder | Status |
|----|------|-------------|--------|--------|
| T1 | Core Engine | **Temporal Logic:** Room DB (validFrom/validTo), T-Wake logic, Collision Engine. | conductor/tracks/core-engine/ | Completed |
| T2 | Health Sync | **Platform Sync:** Health Connect (SleepSession), Horologist DataStore (Protobuf). | conductor/tracks/health-sync/ | Completed |
| T3 | Ambient UI | **Context UX:** PWLE Haptic Vocabulary, Wear OS 5 Complications, Material 3. | conductor/tracks/ambient-ui/ | Completed |
| T4 | Edge AI | **Intelligence:** Gemini Nano AICore, micro-journaling parsing, symptom forecasting. | conductor/tracks/edge-ai/ | Completed |
| T5 | Dynamic Titration | **Safety:** HR-based alert suppression, Jet Lag titration, predictive BP modeling. | conductor/tracks/dynamic-titration/ | Completed |
| T6 | Bio-Interaction Engine | **Safety Expansion:** Food interactions, Fuzzy Window stress optimization. | conductor/tracks/bio-interaction/ | Completed |
| T7 | Geo-Contextual Awareness | **Context:** Proximity alerts, Location-Anchored Doses (Geofencing). | conductor/tracks/geo-context/ | Completed |
| T8 | Data Sovereignty & Reporting | **Export:** On-device PDF generation, secure Intent sharing for doctors. | conductor/tracks/reporting/ | Completed |
| T9 | Zero-Touch Wearable Interaction | **Ambient UI:** Smart wrist-flick actions, gesture-based logging on Wear OS. | conductor/tracks/wearable-gestures/ | Completed |
| T10 | Automated Dose Detection | **Sensor Fusion:** Detect hand-to-mouth gesture via Watch for auto-logging. | conductor/tracks/auto-detection/ | Completed |
| T11 | Biometric Digital Twin | **Modeling:** Gemini Nano baseline vs. real-time physiological response. | conductor/tracks/digital-twin/ | Completed |
| T12 | Predictive Inventory | **Supply Chain:** Adherence-aware pill tracking and auto-refill prediction. | conductor/tracks/inventory/ | Completed |
| T13 | Visual Medication Onboarding | **Computer Vision:** On-device pill identification via CameraX. | conductor/tracks/visual-onboarding/ | Completed |
| T14 | Manual Data Entry UI | **Input UX:** Full dynamic logging UI and system settings. | conductor/tracks/data-entry-ui/ | Completed |
| T15 | Interaction & Side Effect Intel | **Intelligence:** AI spacing suggestions and side effect tracking. | conductor/tracks/interaction-intel/ | Completed |
| T16 | Advanced Travel Automation | **Titration:** Proactive jet lag schedule proposals via calendar. | conductor/tracks/travel-automation/ | Completed |
| T17 | PRN Decision Intelligence | **Safety:** Advisory layer for as-needed medication efficacy. | conductor/tracks/prn-intelligence/ | Completed |
| T18 | Context-Aware Voice Logging | **Input UX:** Hands-free logging via on-device Speech-to-Text & Nano. | conductor/tracks/voice-logging/ | Completed |
| T19 | On-Device Food Recognition | **CV:** Identifying dietary intake via camera and TFLite. | conductor/tracks/food-vision/ | Completed |
| T20 | Nutrient & Allergen Intelligence | **Intelligence:** Nutrition Label OCR, macro estimation, and allergen flagging. | conductor/tracks/macro-intelligence/ | Completed |
| T21 | Adaptive Meal & Hunger Orchestration | **Orchestration:** Optimal hunger-window reservation and dietary-med sync. | conductor/tracks/adaptive-meal-scheduling/ | Completed |
| T22 | Sleep Continuity & Interruption Logic | **Temporal Safety:** Bridge short wake gaps (bathroom breaks) for accurate T-Wake. | conductor/tracks/sleep-continuity/ | Completed |
| T23 | Goal-Oriented Scheduling | **Customization:** Health goals & custom user meal preferences syncing. | conductor/tracks/goal-oriented-scheduling/ | Completed |
| T24 | Nocturia Optimization | **Circadian Safety:** Track and minimize nighttime bathroom breaks. | conductor/tracks/nocturia-optimization/ | Completed |
| T25 | Sleep Quality Calibration | **Calibration:** Morning feedback loop to align Fitbit data with feeling. | conductor/tracks/sleep-calibration/ | Completed |
| T26 | High-Integrity Validation | **Reliability:** Expand test coverage to >80% for AI, Temporal, and UI layers. | conductor/tracks/high-integrity-validation/ | In Progress |

### Track Milestones

#### T1: Core Engine
- [x] Implement Room Entity with Temporal Versioning (`validFrom`, `validTo`).
- [x] Develop `T-Wake` offset calculator with unit tests.
- [x] Build "The Sponge Effect" Collision Resolver.

#### T2: Health Sync
- [x] Initialize `HealthConnectClient` and permission workflow.
- [x] Automate `SleepSessionRecord` polling for T-Wake anchoring.
- [x] Setup Horologist-based cross-device DataStore sync.

#### T3: Ambient UI
- [x] Define "Haptic Vocabulary" using `WaveformEnvelopeBuilder`.
- [x] Create Wear OS Complications for zero-tap med logging.
- [x] Implement Phone Dashboard with Material You vertical timeline.

#### T4: Edge AI
- [x] Integrate ML Kit GenAI Prompt API for on-device inference.
- [x] Develop prompt templates for barometric-to-symptom correlation.
- [x] Build the "Jet Lag Simulator" titration logic.

#### T5: Dynamic Titration
- [x] Implement `PhysiologicalSuppressor` for HR-based fuzzy windows.
- [x] Build `JetLagManager` for multi-day titration schedules.
- [x] Enhance AI engine with predictive sleep-debt modeling.

#### T6: Bio-Interaction Engine
- [x] Define `InteractionRule` entities for food interactions (e.g., Grapefruit).
- [x] Update `CollisionResolver` with food-related safety rules.
- [x] Enhance `PhysiologicalSuppressor` for HR-aware Fuzzy Window optimization.

#### T7: Geo-Contextual Awareness
- [x] Integrate Geofencing/Location APIs into `app-phone`.
- [x] Implement Home boundary proximity alerts for missed T-Wake doses.
- [x] Build Location-Anchored dose scheduling in `TemporalEngine`.

#### T8: Data Sovereignty & Reporting
- [x] Build SQLite data aggregation query for 30-day health metrics.
- [x] Implement on-device visual PDF generation.
- [x] Integrate Android ShareSheet (`Intent.ACTION_SEND`) for secure export.

#### T9: Zero-Touch Wearable Interaction
- [x] Implement Pixel Watch 3 accelerometer gesture recognition.
- [x] Map "Smart Wrist-Flick" to "Dose Taken" action.
- [x] Map "Wrist-Shake" to "Snooze Dose" action.

#### T10: Automated Dose Detection
- [x] Implement Wear OS background sensor listener (accelerometer/gyro).
- [x] Build hand-to-mouth gesture classifier (heuristic or TFLite).
- [x] Integrate gesture detection with Fuzzy Window validation.
- [x] Add Auto-Logging toggle in phone app settings.

#### T11: Biometric Digital Twin
- [x] Define `BiometricBaseline` schema for per-medication modeling.
- [x] Implement "Ghost Stream" expected-response simulation.
- [x] Develop AI prompt templates for anomaly detection.

#### T12: Predictive Inventory
- [x] Implement adherence-aware pill decrement logic.
- [x] Build depletion-date prediction engine.
- [x] Integrate "Refill Intelligence" with Android ShareSheet.

#### T13: Visual Medication Onboarding
- [x] Integrate CameraX in `app-phone`.
- [x] Build pill identification CV pipeline (ML Kit OCR + Heuristic Color/Shape).
- [x] Create "Scan-to-Onboard" UI flow.

#### T14: Manual Data Entry UI
- [x] Build medication creation/edit form.
- [x] Implement manual dose logging dashboard.
- [x] Create preference management settings screen.
- [x] Transition `MainActivity` from mock data to real-time Room flow.

#### T15: Interaction & Side Effect Intelligence
- [x] Define `SideEffectRule` and `AbsorptionRule` schemas.
- [x] Build local Knowledge Base of interaction guidelines.
- [x] Integrate "Spacing Insights" into Collision Engine.
- [x] Display proactive health alerts in Vertical Timeline.
- [x] Implement Nap Detection and dose shift suggestions.
- [x] Implement Post-Prandial Posture (Stay Upright) guidance.

#### T16: Advanced Travel Automation
- [x] Implement `TravelDetectionWorker` for future trip detection.
- [x] Build `proposeAdvanceTitration` logic in `JetLagManager`.
- [x] Create "Travel Alert" approval UI in dashboard.

#### T17: PRN Decision Intelligence
- [x] Define `PRNRecord` and `PRNHistory` entities.
- [x] Build `PRNAdvisor` engine with HR-aware safety checks.
- [x] Implement "PRN Request" UI flow with AI alternatives.

#### T18: Context-Aware Voice Logging
- [x] Integrate on-device `SpeechRecognizer`.
- [x] Develop multi-entity extraction prompt templates for Gemini Nano.
- [x] Implement `VoiceLogCoordinator` for automated data dispatch.

#### T19: On-Device Food Recognition
- [x] Integrate TFLite food classification model.
- [x] Create `FoodScannerEngine` for dietary identification.
- [x] Implement "Dietary Scan" UI mode.

#### T20: Nutrient & Allergen Intelligence
- [x] Build `NutrientReference` local database and `AllergenProfile` schema.
- [x] Implement `NutrientAdvisoryEngine` for interference detection.
- [x] Develop "Nutrition Label OCR" parser to extract macros and ingredients.
- [x] Create "Nutrition Guide" UI for food suggestions and sourcing.

#### T21: Adaptive Meal & Hunger Orchestration
- [x] Implement `AppetiteLog` and hunger-tracking dashboard logic.
- [x] Create `MealScheduler` logic to suggest "Optimal Eating Windows" based on `T-Wake` and medication offsets.
- [x] Integrate "Sacred Eating Window" reservation for high-difficulty days.
- [x] Implement nutrition-aware scheduling (e.g., "Best time for this yogurt: 2:00 PM").
- [x] Develop "Meal Sync" dashboard view showing medication windows overlaid with suggested meal times.

#### T22: Sleep Continuity & Interruption Logic
- [x] Implement `healSleepSessions` logic to merge sessions with < 30min gaps.
- [x] Update `fetchLatestTWake` to use stitched session data.
- [x] Add "Sleep Interruption" indicator in Vertical Timeline.

#### T23: Goal-Oriented Scheduling & Meal Preferences
- [x] Define `MealPreferences` in `phos_state.proto` to hold preferred meal times.
- [x] Implement `HealthGoal` entity in Room to track user-defined health targets.
- [x] Create `GoalOptimizationEngine` that suggests medication offsets based on active goals.
- [x] Update `MealScheduler` to combine "Optimal Eating Windows" with user-defined `MealPreferences`.
- [x] Build UI in the Dashboard (Settings & Meals tab) to input meal preferences and log health goals.

#### T24: Nocturia Optimization & Break Tracking
- [x] Define `NocturiaLog` entity and automate detection from bridged sessions.
- [x] Enhance `GoalOptimizationEngine` with nocturnal urination reduction rules.
- [x] Build "Sleep Quality" UI and break count visualization.

#### T26: High-Integrity Validation
- [ ] Implement `GeminiNanoEngineTest` with ML Kit mocks.
- [ ] Implement `MealSchedulerTest` for complex dietary-medication synchronization.
- [ ] Build exhaustive tests for `GoalOptimizationEngine` and `VoiceEntityParser`.
- [ ] Create `DashboardViewModelTest` and `WidgetStateMappingTest` for UI reliability.
- [ ] Implement `DataLayerRepositoryTest` for cross-device sync integrity.
