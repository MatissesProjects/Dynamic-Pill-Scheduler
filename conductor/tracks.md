# Tracks Registry: Dynamic Pill Scheduler (PHOS)
| ID | Name | Description | Folder | Status |
|----|------|-------------|--------|--------|
| T1 | Core Engine | **Temporal Logic:** Room DB (validFrom/validTo), T-Wake logic, Collision Engine. | conductor/tracks/core-engine/ | Completed |
| T2 | Health Sync | **Platform Sync:** Health Connect (SleepSession), Horologist DataStore (Protobuf). | conductor/tracks/health-sync/ | Completed |
| T3 | Ambient UI | **Context UX:** PWLE Haptic Vocabulary, Wear OS 5 Complications, Material 3. | conductor/tracks/ambient-ui/ | In Progress |
| T4 | Edge AI | **Intelligence:** Gemini Nano AICore, micro-journaling parsing, symptom forecasting. | conductor/tracks/edge-ai/ | Pending |

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
- [ ] Integrate ML Kit GenAI Prompt API for on-device inference.
- [ ] Develop prompt templates for barometric-to-symptom correlation.
- [ ] Build the "Jet Lag Simulator" titration logic.