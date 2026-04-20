# Tech Stack: Dynamic Pill Scheduler (PHOS)

## Architecture Overview
A phased multi-module Android project built to strictly leverage the Google Pixel ecosystem capabilities (Pixel 9 Pro XL & Pixel Watch 3) while ensuring absolute data privacy.

### Core Modules
1. **Phone App (app-phone):** The command center and comprehensive dashboard.
2. **Watch App (app-wear):** The ambient sensor layer and low-friction entry point.
3. **Core Data Engine (core-data):** Shared data models, temporal calculation logic, and local storage.
4. **Intelligence (core-intelligence):** Gemini Nano integration, predictive modeling, and fuzzy logic.

## Technical Components

- **User Interface:**
  - Phone: Jetpack Compose (Material 3 / Material You).
  - Watch: Compose for Wear OS 5 / Horologist library for standard Wear UI components.
- **Local Storage & Database:**
  - **Room Database:** Utilizing the **History/Versioning Pattern** (Valid Time).
  - Schema: `validFrom` and `validTo` (Instant) columns for temporal state tracking.
  - Data Privacy: Encrypted Room / EncryptedSharedPreferences for sensitive tokens.
- **Health Data & Sensors:**
  - **Android Health Connect API:** `androidx.health.connect:connect-client:1.1.0`.
  - Records: `SleepSessionRecord` (Auto-Wake), `HeartRateRecord` (Workouts).
  - Sync: `Wear OS Data Layer API` via **Horologist DataStore** integration (Protobuf-based).
- **Background Processing & Scheduling:**
  - WorkManager / AlarmManager with `setUrgent()` for time-sensitive med alerts.
- **On-Device Machine Learning:**
  - **Google Gemini Nano / AICore:** via `com.google.mlkit:genai-prompt` (GenAI Prompt API).
  - Task: Local correlation of barometric pressure (Stream C) vs. symptom logs (Stream B).
- **Haptics:**
  - **Waveform Envelope API (PWLE):** `VibrationEffect.WaveformEnvelopeBuilder` for context-aware haptic vocabulary.