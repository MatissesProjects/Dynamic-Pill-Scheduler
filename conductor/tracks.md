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
| T9 | Zero-Touch Wearable | **Ambient UI:** Smart wrist-flick actions, gesture-based logging on Wear OS. | conductor/tracks/wearable-gestures/ | Completed |
| T10 | Automated Dose Detection | **Sensor Fusion:** Detect hand-to-mouth gesture via Watch for auto-logging. | conductor/tracks/auto-detection/ | Completed |
| T11 | Biometric Digital Twin | **Modeling:** Gemini Nano baseline vs. real-time physiological response. | conductor/tracks/digital-twin/ | Completed |
| T12 | Predictive Inventory | **Supply Chain:** Adherence-aware pill tracking and auto-refill prediction. | conductor/tracks/inventory/ | Completed |

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
