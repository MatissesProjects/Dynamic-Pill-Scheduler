# Track Plan: T39 - Heart Failure Warning System (HF Early-Warning)

## Objective
Build a high-integrity predictive warning system for heart failure decompensation by fusing multi-metric sensor data (HRV, RHR, RR, SpO2) and using Gemini Nano for on-device clinical reasoning.

## Milestones
- [ ] **M1: Build sensor-fusion aggregator**
    - Aggregate 7-day trends for HRV, Resting HR, Respiratory Rate, and SpO2.
    - Create `HFDecompensationEngine` to calculate a "Fluid Accumulation Proxy" score.
- [ ] **M2: Develop Gemini Nano predictive templates**
    - Feed aggregated trends into Nano to predict potential 48-hour decompensation risk.
    - Synthesize "Clinical-Ready" justification for physician review.
- [ ] **M3: Implement "Safety Tightening" for diuretics**
    - If risk is "Elevated", automatically tighten safe-gaps for diuretics (e.g., Furosemide).
    - Trigger "Weight Check" reminders during high-risk periods.
- [ ] **M4: Integration & Testing**
    - Add unit tests for `HFDecompensationEngine`.
    - Integrate with `DashboardViewModel` and `GeminiNanoEngine`.

## Tech Stack
- **Module:** `core-data`, `core-intelligence`, `app-phone`
- **Data:** Health Connect (`RespiratoryRateRecord`, `OxygenSaturationRecord`, `RestingHeartRateRecord`, `HeartRateVariabilityRmssdRecord`)
- **Intelligence:** Sensor fusion heuristics, Gemini Nano (AICore).

## Documentation Context
- **PHOS_OVERVIEW.md**: Core temporal and safety logic.
- **EXPANSION_PLAN_2026.md**: Concept details for T39.
