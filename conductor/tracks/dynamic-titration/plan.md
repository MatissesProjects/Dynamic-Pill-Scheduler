# Track Plan: T5 Dynamic Titration & Contextual Safety

## Specification
Transition PHOS from a static temporal engine to a dynamic, physiological-aware orchestration system. This includes implementing HR-triggered alert suppression (Fuzzy Grace Periods), Jet Lag titration logic, and predictive modeling placeholders.

## Implementation Steps
- [x] Implement `PhysiologicalSuppressor` in `core-data` to handle alert suppression based on real-time HR/HRV data.
- [x] Integrate `PhysiologicalSuppressor` into the notification trigger flow.
- [x] Build the `JetLagManager` in `core-data` to handle circadian rhythm shifts and suggest titration schedules for time-zone changes.
- [x] Enhance `SymptomCorrelationEngine` in `core-intelligence` to provide predictive BP/Symptom spikes based on sleep debt.
- [x] Add "Jet Lag Simulator" UI component to `app-phone` for manual trip planning.
- [x] Write unit tests for HR suppression logic and travel titration.

## Verification Plan
- Verify that simulated high HR (e.g., 140 BPM) successfully delays a medication notification.
- Verify that shifting `T-Wake` by 8 hours (travel simulation) generates a 3-day titration schedule.
- Verify predictive alerts appear in the dashboard when sleep data shows a significant debt.
