# Dynamic Pill Scheduler (PHOS)

PHOS (Personalized Health Operating System) is a next-generation health orchestration system designed for the Google Pixel ecosystem (Pixel 9 Pro XL and Pixel Watch 3). It moves beyond static medication tracking into **Predictive Orchestration**, adjusting the user's health schedule based on real-time biological and environmental context.

## Core Innovation: Temporal Anchor Logic (T-Wake)
Traditional health apps use static alarms (e.g., "Take Meds at 9:00 AM"). PHOS uses **Temporal Offsets** anchored to the user's actual physiological start of the day (`T-Wake`).

*   **Logic:** `Schedule_Time = T-Wake + Offset_Hours`
*   **Dynamic Recalibration:** If a user wakes up late (detected via Pixel Watch 3 sleep stages), the entire day's cascade shifts instantly.
*   **Fuzzy Windows:** Medications are due within a window rather than at a single point in time. For example, if the system detects high heart rate (e.g., during a workout), it suppresses notifications until the heart rate returns to baseline.

## The Collision Engine (Safety Layer)
PHOS maintains a strict set of interaction rules to ensure user safety and medication efficacy (e.g., "The Sponge Effect").

*   **Example:** Fiber supplements must be taken >2 hours away from Heart/BP medication to ensure absorption.
*   **Conflict Resolution:** If a shifted `T-Wake` causes a collision, the system proactively suggests a schedule adjustment (e.g., "Shift Fiber to 5:00 PM to protect morning Metoprolol absorption").

## Data Architecture: The Three Streams
All data is stored in a **Local-Only SQLite Temporal Database** to ensure privacy.

1.  **Stream A (Passive):** Continuous HR, SpO2, HRV, and Sleep data from Health Connect.
2.  **Stream B (Active):** User-logged symptoms (1-10 scale), water intake, and medication confirmation.
3.  **Stream C (Environmental):** Barometric pressure and weather via external APIs (used to predict symptom flare-ups).

## Intelligence Layer: On-Device Edge AI
To ensure absolute privacy, PHOS utilizes **Gemini Nano (AICore)** on the Pixel 9 Pro XL.

*   **Correlation Engine:** Analyzes potential correlations between environmental factors (like barometric pressure drops) and user symptoms.
*   **Natural Language Parsing:** Processes natural language logs like "Log 8oz of water and a slight headache."
*   **Predictive Modeling:** Forecasts biometric spikes based on sleep debt and medication timing.

## Multi-Device UX Principles
*   **Pixel Watch 3:**
    *   **Haptic Vocabulary:** Unique vibration patterns for "Dose Due" (double-pulse) vs. "Collision Warning" (low rumble).
    *   **Zero-Tap Logging:** Watch face complications for instant status updates.
*   **Pixel 9 Pro XL:**
    *   **Material You:** Fluid, wallpaper-aware UI.
    *   **The Vertical Timeline:** A unified visual stream of medications, symptoms, and biometric trends.

## Project Structure
The project is built as a multi-module Android application to support parallel development:

*   `app-phone`: The primary user interface for the phone.
    *   `ui`: Contains `DashboardViewModel`, `JetLagSimulator`, and `VerticalTimeline` for managing user state and schedule visualization.
    *   `widget`: Implements the `BiometricDashboardWidget` for home screen status updates.
*   `app-wear`: Dedicated UI and service layer for Wear OS.
    *   `complications`: Provides the `MedicationStatusComplicationService` for watch face integration.
    *   `haptics`: Defines the `HapticVocabulary` for standardized tactile feedback.
*   `core-data`: The persistence and data management layer.
    *   `db` and `dao`: Core SQLite/Room database implementation.
    *   `model`: Shared data structures and entities.
    *   `engine`: Core repository logic and management (e.g., `AnchorManager`, `CollisionResolver`).
    *   `sync`: Manages cross-device and Health Connect synchronization.
*   `core-intelligence`: The business logic and complex processing layer.
    *   `intelligence`: Contains the `SymptomCorrelationEngine` for advanced health analysis and `IntelligenceWorker` for background processing.

## Getting Started
To get started with development, ensure you have the following tools installed:

*   Android Studio
*   Java Development Kit (JDK)
*   GitHub CLI (`gh`) for PR management

### Build Instructions
The project uses Gradle for its build system. Currently, a local Gradle wrapper generation might be required upon first clone.

## Development Mandates
1.  **Privacy-First:** Never send biometric or medication data to a cloud.
2.  **Testing-First:** Logic changes in `T-Wake` or `Collision Engine` require exhaustive unit tests.
3.  **Surgical Edits:** Only modify files related to your assigned track.

## Current Status
*   **Track 1 (Core Engine):** Implemented Temporal Anchor logic and Collision Engine.
*   **Track 2 (Health Sync):** Integrated Health Connect, T-Wake anchoring, and cross-device sync.
*   **Track 3 (Ambient UI):** Added watch haptics, complications, and phone dashboard.
*   **Track 4 (Edge AI):** Pending integration of Gemini Nano for symptom correlation.
