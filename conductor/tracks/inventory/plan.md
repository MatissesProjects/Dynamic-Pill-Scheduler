# Track Plan: T12 Predictive Inventory

## Specification
Implement an intelligent medication inventory system that tracks pill counts and predicts refill dates based on dynamic adherence and titration shifts.

## Implementation Steps
- [ ] Add `InventoryRecord` entity to `core-data` (pill count, refill threshold, pharmacy intent data).
- [ ] Update `DoseLog` processing to automatically decrement inventory upon "Taken" events.
- [ ] Build a prediction engine that accounts for Titration/Jet Lag shifts to calculate the exact "Date of Depletion."
- [ ] Implement "Refill Intelligence": Auto-generate secure pharmacy refill intents or a "Refill PDF" 7 days before inventory hits the critical threshold.

## Verification Plan
- Unit tests for inventory decrement logic including "Skipped" vs "Taken" doses.
- Simulation tests for multi-day titration shifts and their impact on the predicted refill date.
- Verify the secure export of the Refill PDF via Android ShareSheet.
