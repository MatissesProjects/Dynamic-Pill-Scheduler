# Product Definition: Dynamic Pill Scheduler (PHOS)

## Vision
To evolve from a static "pill tracker" into a **Personalized Health Operating System (PHOS)**, specifically optimized for the Pixel 9 Pro XL and Pixel Watch 3. The system moves from reactive logging to predictive orchestration, utilizing ambient computing, privacy-centric edge AI, and contextual awareness to manage medication schedules dynamically based on biological inputs.

## Core Features
1. **Dynamic Scheduling Engine (The Anchor):**
   - **T-Wake Logic:** Replaces static clocks with temporal offset logic anchored to wakeup time (`T-Wake`).
   - **Fuzzy Logic Grace Periods:** Suppresses alerts during high heart rate zones (e.g., workouts) and calculates optimal windows rather than rigid times.
   - **Collision Logic:** Detects conflicts, such as taking a fiber supplement too close to Metoprolol, and issues interaction warnings.

2. **Multi-Device Context (The Nervous System):**
   - **Passive Data (Stream A):** Continuous sync with Health Connect (HR, SpO2, Sleep Stages, Activity) from the Pixel Watch 3.
   - **Active Data (Stream B):** Low-friction logging (Complications, Voice-to-Data, Micro-Journaling Prompts) for symptoms and medication status.
   - **Environmental Data (Stream C):** Contextual inputs like barometric pressure via APIs.

3. **Ambient UI (The Interface):**
   - **Pixel Watch 3:** Actionable notifications, contextual haptic vocabulary (distinct vibration patterns for alerts vs. warnings), and biometric complication progress rings.
   - **Pixel 9 Pro XL Dashboard:** Material You vertical timelines, interactive schedule adjustments, and biometric correlation views.

4. **Predictive Analytics (The Intelligence):**
   - **Edge AI (Gemini Nano):** On-device machine learning for cross-stream correlation (e.g., medication timing vs. physiological stabilization) ensuring zero cloud exposure of biological data.
   - **Predictive Modeling:** Forecasting symptoms and calculating travel/jet-lag multi-day titration schedules.
   - **PRN Protocol:** Proactive suggestions for "as-needed" medications based on predictive inputs (e.g., pressure drops).

## Phased Implementation Roadmap
- **Phase 1: MVP (Manual Tracking):** Establish logging habits. Implement T-Wake core logic manually.
- **Phase 2: Automation (Sensors):** Integrate Health Connect API for auto-wakeup detection. Implement collision and interaction logic.
- **Phase 3: Intelligence (Edge AI):** Deploy the Gemini Nano-powered correlation engine. Introduce micro-journaling and predictive insights.
- **Phase 4: Ecosystem (Advanced context):** Integrate environmental APIs (weather) and the Jet Lag Simulator.