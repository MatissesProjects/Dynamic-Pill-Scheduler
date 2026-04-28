# Track Plan: T38 - Heart Rate Recovery (HRR) Orchestration

## Objective
Monitor Autonomic Nervous System (ANS) resilience by tracking Heart Rate Recovery (HRR) after physical exertion, specifically correlating recovery speed with medication versions to detect potential autonomic strain.

## Milestones
- [x] **M1: Build HRR monitor logic**
    - Identify workout end-points via `ExerciseSessionRecord`.
    - Fetch HR data for the 2-minute window following the workout.
    - Calculate 1-min and 2-min HRR deltas.
- [x] **M2: Build trend analysis for Medication-Induced Recovery Audit**
    - Correlate HRR speed with medication version history.
    - Flag 7-day trend slowdowns (>15% decrease) as "Autonomic Strain."
- [x] **M3: Create "Autonomic Strain" alerts**
    - Surface trend insights in the Phone UI.
    - Trigger "Recovery Watch" haptic alerts on Wear OS.
- [x] **M4: Integration & Testing**
    - Add unit tests for `HRROrchestrator`.
    - Integrate with `DashboardViewModel` and UI rendering.

## Tech Stack
- **Module:** `core-data`, `app-phone`, `app-wear`
- **Data:** Health Connect (`ExerciseSessionRecord`, `HeartRateRecord`), Room (`MedicationRecord` versions)
- **Intelligence:** HRR trend heuristics.

## Documentation Context
- **PHOS_OVERVIEW.md**: Core temporal and safety logic.
- **EXPANSION_PLAN_2026.md**: Concept details for T38.
