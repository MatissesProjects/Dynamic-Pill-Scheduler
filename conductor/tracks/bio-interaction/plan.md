# Track Plan: T6 Bio-Interaction Engine

## Specification
Expand the existing Collision Engine to support food, supplement, and advanced physiological interactions (Fuzzy Window Optimization).

## Implementation Steps
- [ ] Define `InteractionRule` entities for common food interactions (e.g., Grapefruit vs. Statins, Dairy vs. Antibiotics).
- [ ] Update `CollisionResolver` to parse and apply food-related rules against scheduled doses.
- [ ] Enhance `PhysiologicalSuppressor` to handle "Fuzzy Window Optimization": dynamically delay notifications for non-critical supplements during high-stress/high-HR events until baseline HR is restored.
- [ ] Write exhaustive unit tests for the expanded food collision logic.

## Verification Plan
- Unit tests verifying Grapefruit/Statin interaction blocks.
- Unit tests verifying Fuzzy Window delays based on mocked HR data.
- Integration tests ensuring notifications are suppressed and rescheduled correctly.
