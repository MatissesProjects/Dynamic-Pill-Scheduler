# Track Plan: T37 - Muscle-Heart Mismatch (Heavy Legs)

## Objective
Identify and mitigate medication-induced physiological mismatch where physical output (Step Rate) exceeds cardiac response (Heart Rate), resulting in "Heavy Legs" or perceived exhaustion.

## Milestones
- [x] **M1: Build "Muscle-Heart Mismatch" detector**
    - Correlate `StepsRecord` (Cadence/Step Rate) with `HeartRateRecord`.
    - Detect "High Output, Low Load" zones (e.g., Step Rate > 120 but HR < (RHR + 20)).
- [x] **M2: Implement Daily Readiness scaling**
    - Map Fitbit/Health-Connect "Readiness" or calculate a internal PHOS Readiness score.
    - Scale activity suggestions (E-Bike assist, walking duration) based on readiness.
- [x] **M3: Create "Heavy Leg" validation check-ins**
    - Trigger a notification if mismatch is detected to confirm perceived fatigue.
    - Log "Mismatch Confirmation" for physician reports.
- [x] **M4: Integration & Testing**
    - Add unit tests for `CardioMismatchEngine`.
    - Integrate with `DashboardViewModel` and UI components.

## Tech Stack
- **Module:** `core-data`, `app-phone`, `app-wear`
- **Data:** Health Connect (`StepsRecord`, `HeartRateRecord`, `RestingHeartRateRecord`)
- **Intelligence:** Mismatch heuristics, Readiness scaling logic.

## Documentation Context
- **PHOS_OVERVIEW.md**: Core temporal and safety logic.
- **EXPANSION_PLAN_2026.md**: Concept details for T37.
