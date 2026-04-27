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
| T26 | High-Integrity Validation | **Reliability:** Expand test coverage to >80% for AI, Temporal, and UI layers. | conductor/tracks/high-integrity-validation/ | Completed |
| T27 | Neuromotor Orchestration | **Safety:** Wear OS 5 gait analysis (Stride Length) for side-effect detection. | conductor/tracks/neuromotor-gait/ | Completed |
| T28 | Chronotype Chronotherapy | **Circadian:** Tailor dose windows to biological clocks (Night Owl vs. Lark). | conductor/tracks/chronotype-align/ | Completed |
| T29 | Metabolic Load Digital Twin | **Performance:** Correlate Cardio Load with dose efficacy and hydration. | conductor/tracks/metabolic-twin/ | Completed |
| T30 | Sentiment & Stress Synthesis | **Mental Health:** Gemini Nano sentiment analysis vs. HRV correlation. | conductor/tracks/stress-synthesis/ | Completed |
| T31 | Micro-Mobility Orchestration | **Mobility:** e-bike assistance optimization for heart-medication users. | conductor/tracks/ebike-orchestration/ | Completed |
| T32 | Activity-Based Wakefulness | **Alertness:** Suppress nap drives using scheduled workout reminders. | conductor/tracks/alertness-orchestration/ | Completed |
| T33 | Gastric Protection | **Safety:** AI-driven GI irritant detection and food-aligned buffering. | conductor/tracks/gastric-protection/ | Completed |
| T34 | Sleep Pressure Modeling | **Circadian:** AI-driven adenosine drive tracking and nap forecasting. | conductor/tracks/sleep-pressure/ | Pending |

### Track Milestones

#### T30: Sentiment & Stress Synthesis (Gemini Nano)
- [x] Update `GeminiNanoEngine` to extract sentiment scores from voice logs.
- [x] Correlate sentiment trends with HRV data in `SymptomCorrelationEngine`.
- [x] Propose "Reserved Rest Windows" during high-stress/low-HRV periods.

#### T31: Micro-Mobility Orchestration (e-Bike)
- [x] Implement `EbikeEffortNormalizer` to calculate activity load relative to heart-rate blunting medications.
- [x] Build "Safety Bridge" climb-assistance alerts for users on Beta-Blockers.
- [x] Implement "Post-Exercise Hypotension" cooldown reminders.

#### T32: Activity-Based Wakefulness (Nap Prevention)
- [x] Implement `AlertnessOrchestrator` to identify "Nap Vulnerability Zones" via Gemini Nano.
- [x] Build workout reminder logic for pre-dip alertness injection.
- [x] Correlate sleep debt with next-day alertness interventions.

#### T33: Gastric Protection & GI-Aware Scheduling
- [x] Implement `GIProtectionEngine` to scan meds for gastric irritants.
- [x] Build "Stomach Buffer" suggestion logic in `GoalOptimizationEngine`.
- [x] Correlate stomach pain logs with irritant dose proximity.

#### T34: Sleep Pressure Modeling (Adenosine Drive)
- [ ] Implement `AdenosineEngine` to model sleep pressure accumulation.
- [ ] Build "Nap Propensity" forecasting UI.
- [ ] Correlate caffeine intake with adenosine receptor blocking.
