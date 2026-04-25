# Track Plan: T24 Nocturia Optimization & Break Tracking

## Specification
Track the frequency of nighttime bathroom breaks and optimize the medication/fluid schedule to minimize them. This track leverages the "Sleep Session Bridging" logic (T22) to automatically identify interruptions and suggests shifts for medications that contribute to frequent nighttime urination.

## Implementation Steps
- [ ] Define `NocturiaLog` entity in Room to track bathroom breaks (automatic + manual).
- [ ] Update `HealthSyncManager` to automatically log a `NocturiaLog` whenever a sleep session gap is bridged.
- [ ] Enhance `GoalOptimizationEngine` with "Nocturia Prevention" rules (e.g., shifting diuretics or evening fluids 2-3 hours earlier).
- [ ] Implement a "Sleep Quality" card in the dashboard that displays the break count and optimization suggestions.
- [ ] Create a UI toggle to "Prioritize Uninterrupted Sleep" which aggressively shifts non-critical evening medications forward.

## Verification Plan
- Unit tests for automatic break detection from bridged sleep sessions.
- Verification of `GoalOptimizationEngine` shifting diuretics based on break frequency.
- UI testing of the "Break Count" visualization and shift acceptance flow.
