# Track Plan: T17 PRN (As-Needed) Decision Intelligence

## Specification
Implement an advisory layer for "As-Needed" (PRN) medications. The system will provide safety and efficacy guidance when a user initiates a PRN log, checking history, current biometrics, and interaction rules.

## Implementation Steps
- [ ] Define `PRNRecord` and `PRNHistory` entities in `core-data`.
- [ ] Create `PRNAdvisor` engine to evaluate if a PRN dose is appropriate based on last dose time and current heart rate/symptoms.
- [ ] Implement a "PRN Request" UI flow where users select a medication and receive an "Approve/Wait/Alternatives" recommendation.
- [ ] Use Gemini Nano to generate personalized alternatives (e.g., "HR is elevated, maybe try deep breathing before taking an inhaler").

## Verification Plan
- Unit tests for PRN dose frequency validation.
- Integration tests verifying that `CollisionResolver` rules are applied to PRN requests.
- Verification of HR-based suppression logic for PRN recommendations.
