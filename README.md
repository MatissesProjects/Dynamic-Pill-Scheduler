# Dynamic Pill Scheduler (PHOS)

PHOS (Personalized Health Operating System) is a next-generation health orchestration system designed for the Google Pixel ecosystem (Pixel 9 Pro XL and Pixel Watch 3). It moves beyond static medication tracking into **Predictive Orchestration**, adjusting the user's health schedule based on real-time biological, behavioral, and environmental context.

## Core Innovation: Temporal Anchor Logic (T-Wake)
Traditional health apps use static alarms (e.g., "Take Meds at 9:00 AM"). PHOS uses **Temporal Offsets** anchored to the user's actual physiological start of the day (`T-Wake`).

*   **Logic:** `Schedule_Time = T-Wake + Offset_Hours`
*   **Dynamic Recalibration:** If a user wakes up late (detected via Pixel Watch 3 sleep stages), the entire day's cascade shifts instantly.
*   **Nap Detection:** Short sleep sessions (15-180 mins) are detected as naps. If a scheduled dose overlaps with a nap, the system automatically suggests a shift (e.g., "Shift dose to 15 mins after nap end").
*   **Fuzzy Windows:** Medications are due within a window rather than at a single point in time. For example, if the system detects high heart rate (e.g., during a workout), it suppresses notifications until the heart rate returns to baseline.

## The Collision Engine & Knowledge Base
PHOS maintains a dynamic, persistent knowledge base of interaction and absorption rules.

*   **Absorption Spacing:** Automatically monitors rules like "Take Sucralfate 2 hours away from other meds" or "Take Levothyroxine 60 mins before food."
*   **Side Effect Monitoring:** Proactively alerts users to watch for specific side effects (e.g., "Dizziness" for Lisinopril) based on their current medication profile.
*   **Food Interactions:** Detects collisions with logged dietary intake (e.g., Grapefruit/Statin interaction) and adjusts windows or triggers alerts.

## Specialized Intelligence Layers
### 1. PRN (As-Needed) Decision Intelligence
A safety advisory layer for non-scheduled medications (e.g., NSAIDs, rescue inhalers).
*   **Safety Checks:** Validates minimum gaps between doses and 24-hour maximum limits.
*   **Biometric Awareness:** Triggers warnings if a user attempts to take a stimulant while their real-time heart rate is elevated.

### 2. Context-Aware Voice Logging
A hands-free, natural language interface powered by on-device Speech-to-Text and **Gemini Nano**.
*   **Multi-Entity Extraction:** Parses complex sentences like *"Took my Lisinopril but I'm feeling a slight headache"* to simultaneously log doses and symptoms.
*   **Conversational Feedback:** Provides immediate visual confirmation of understood entities with a one-tap dismissal.

### 3. Advanced Travel & Jet Lag Automation
Proactively prepares the user's circadian rhythm for upcoming timezone shifts.
*   **Calendar Integration:** Automatically detects upcoming trips via flight/travel events.
*   **Proactive Titration:** Proposes a multi-day titration plan (max 1.5h shift per day) *days before* departure, allowing the user to arrive already adjusted.

### 4. Post-Prandial Posture Guidance
Monitors food intake to provide "Stay Upright" recommendations (30-60 mins) to optimize digestion and prevent acid reflux (GERD) based on clinical guidelines.

### 5. Goal-Oriented Scheduling & Meal Preferences
Users can define specific health goals (e.g., "Prevent stomach pain at 4 AM") and set preferred meal windows.
*   **Goal Optimization Engine:** Analyzes current schedules and proposes actionable shifts (e.g., "Move evening medications earlier to prevent late-night irritation").
*   **Meal Schedular Boost:** Highlights optimal eating windows that safely align with the user's customized meal preferences (Breakfast, Lunch, Dinner).

## Data Architecture: The Three Streams
All data is stored in a **Local-Only SQLite Temporal Database** to ensure absolute privacy.

1.  **Stream A (Passive):** Continuous HR, SpO2, HRV, and Sleep data from Health Connect.
2.  **Stream B (Active):** User-logged symptoms, voice logs, and "Scan-to-Onboard" pill identification (CameraX + ML Kit).
3.  **Stream C (Environmental):** Barometric pressure and weather context used to correlate with user-logged symptoms.

## Project Structure
*   `app-phone`: Primary UI (Vertical Timeline, Jet Lag Simulator, Voice Overlay, Pill Scanner).
*   `app-wear`: Wear OS 5 integration (Haptic Vocabulary, Zero-Tap Complications, Gesture Detection).
*   `core-data`: Persistence, Health Connect sync, and engines (`AnchorManager`, `CollisionResolver`, `NapManager`, `PRNAdvisor`, `MealScheduler`).
*   `core-intelligence`: Edge AI logic (`SymptomCorrelationEngine`, `VoiceLogCoordinator`, `PostureIntelligence`, `GoalOptimizationEngine`).

## Current Status (Completed Tracks)
*   ✅ **T1-T10:** Core Engine, Health Sync, Ambient UI, Edge AI (Gemini Nano), Dynamic Titration, Bio-Interactions, Geo-Context, Reporting, Wearable Gestures, and Automated Dose Detection.
*   ✅ **T11-T13:** Biometric Digital Twin, Predictive Inventory, and Visual Medication Onboarding (CV).
*   ✅ **T14-T15:** Manual Data Entry UI and Interaction/Side Effect Intelligence.
*   ✅ **T16:** Advanced Travel & Jet Lag Automation.
*   ✅ **T17:** PRN (As-Needed) Decision Intelligence.
*   ✅ **T18:** Context-Aware Voice Logging.
*   ✅ **T20:** Nutrient & Allergen Intelligence.
*   ✅ **T21:** Adaptive Meal & Hunger Orchestration.
*   ✅ **T22:** Sleep Continuity & Interruption Logic.
*   ✅ **T23:** Goal-Oriented Scheduling & Meal Preferences.

## Development Mandates
1.  **Privacy-First:** Never send biometric or medication data to a cloud.
2.  **Testing-First:** All logic changes require unit tests (verified via `./gradlew test`).
3.  **Surgical Edits:** Maintain module boundaries and follow established naming conventions.
