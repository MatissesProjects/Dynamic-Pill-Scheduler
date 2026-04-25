# Track Plan: T25 Sleep Quality Feedback & Calibration

## Specification
Implement a morning feedback loop to calibrate objective Fitbit/Health Connect data with subjective user experience. Use discrepancies between data and feeling to drive schedule optimizations (med offsets and meal windows).

## Implementation Steps
- [ ] Define `SleepSubjectiveLog` entity to track restfulness, morning mood, and quality (1-10).
- [ ] Create `SleepCalibrationEngine` to detect "Subjective-Objective Discrepancies" (e.g., Fitbit reports good sleep, but user feels poorly).
- [ ] Add a "Morning Sleep Check-in" dialog that triggers during the first medication reminder of the day.
- [ ] Enhance `GoalOptimizationEngine` to treat "Bad Sleep Feeling" as a primary trigger for schedule audits.
- [ ] Implement proactive suggestions for "Bad Sleep" days: shifting late-night doses earlier or reserving longer "Post-Dinner Calm" windows.

## Verification Plan
- Unit tests for discrepancy detection (Mock Fitbit data vs. Mock Subjective logs).
- Integration tests ensuring "Bad Sleep" reports trigger relevant optimization suggestions.
- UI verification of the morning check-in flow and resulting "Calibration Insight" cards.
