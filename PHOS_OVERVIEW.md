# PHOS: Personalized Health Operating System
## System Overview & Agent Onboarding Manifest

PHOS is a next-generation health orchestration system designed for the Google Pixel ecosystem (Pixel 9 Pro XL & Pixel Watch 3). It moves beyond static tracking into **Predictive Orchestration**—adjusting the user's health schedule based on real-time biological and environmental context.

---

### 1. The Core Innovation: Temporal Anchor Logic (T-Wake)
Traditional health apps use static alarms (e.g., "Take Meds at 9:00 AM"). PHOS uses **Temporal Offsets** anchored to the user's actual physiological start of the day (`T-Wake`).

- **Logic:** `Schedule_Time = T-Wake + Offset_Hours`
- **Dynamic Recalibration:** If a user wakes up late (detected via Pixel Watch 3 sleep stages), the entire day's cascade shifts instantly.
- **Fuzzy Windows:** Meds aren't due at a point; they are due in a window. If the system detects high HR (Workout), it suppresses notifications until HR returns to baseline.

### 2. The Collision Engine (Safety Layer)
PHOS maintains a set of interaction rules (e.g., "The Sponge Effect").
- **Example:** Fiber supplements must be taken >2 hours away from Heart/BP medication to ensure absorption.
- **Conflict Resolution:** If a shifted `T-Wake` causes a collision, the system must proactively suggest a schedule adjustment (e.g., "Shift Fiber to 5:00 PM to protect morning Metoprolol absorption").

### 3. Data Architecture: The Three Streams
All data is stored in a **Local-Only SQLite Temporal Database**.
- **Stream A (Passive):** Continuous HR, SpO2, HRV, and Sleep data from Health Connect.
- **Stream B (Active):** User-logged symptoms (1-10 scale), water intake, and medication confirmation.
- **Stream C (Environmental):** Barometric pressure and weather via external APIs (used to predict symptom flare-ups).

### 4. Intelligence Layer: On-Device Edge AI (Gemini Nano)
To ensure absolute privacy, PHOS utilizes **Gemini Nano (AICore)** on the Pixel 9 Pro XL.
- **Correlation Engine:** "Does a drop in barometric pressure correlate with the user's logged 'Stomach Pain' level?"
- **Natural Language Parsing:** "Log 8oz of water and a slight headache."
- **Predictive Modeling:** Forecasting BP spikes based on sleep debt and medication timing.

### 5. Multi-Device UX Principles
- **Pixel Watch 3:**
  - **Haptic Vocabulary:** Unique vibration patterns for "Dose Due" (double-pulse) vs. "Collision Warning" (low rumble).
  - **Zero-Tap Logging:** Watch face complications for instant status updates.
- **Pixel 9 Pro XL:**
  - **Material You:** Fluid, wallpaper-aware UI.
  - **The Vertical Timeline:** A unified visual stream of meds, symptoms, and biometric trends.

---

### 6. Multi-Track Coordination for Agents
To support parallel development, agents must adhere to the following:
- **Modular Boundaries:**
  - `core-data`: All Room DB models and T-Wake logic. No UI code.
  - `core-intelligence`: Gemini Nano prompts and correlation logic.
  - `app-phone`: Phone-specific Compose UI.
  - `app-wear`: Watch-specific Compose for Wear OS UI.
- **Branching Strategy:** Every major feature (Track) starts from its own branch (e.g., `feature/t1-core-engine`).
- **Communication:** Update `conductor/tracks.md` immediately upon task completion to prevent overlaps.

---

### 7. Core Development Mandates
1. **Privacy-First:** Never send biometric or med data to a cloud.
2. **Testing-First:** Logic changes in `T-Wake` or `Collision Engine` require exhaustive unit tests.
3. **Surgical Edits:** Only modify files related to your assigned track.
