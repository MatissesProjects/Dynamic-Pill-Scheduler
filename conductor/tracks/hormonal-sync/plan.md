# Track 49: Endocrine & Hormonal Sync

## Overview
**Orchestration:** Aligning thyroid, insulin, and cortisol medications with circadian cortisol peaks and strictly managed fasting windows. This track ensures that endocrine-disrupting or hormone-replacement medications are timed to maximize efficacy (e.g., thyroid on an empty stomach) and minimize circadian disruption (e.g., cortisol replacement aligned with the morning peak).

## Specification
- **Circadian Modeling:** Model the Cortisol Awakening Response (CAR) as a 30-45 minute peak post-T-Wake.
- **Fasting Rules:** 
  - Thyroid: 30-60 minutes before any food/caffeine.
  - Cortisol: Align with morning peak; avoid late-night doses to prevent insomnia.
- **Metabolic Intelligence:** Correlate dose timing with subjective energy logs.

## Milestones
- [ ] **M1: Circadian Cortisol Mapping**
  - Implement `HormonalSyncEngine` to model the CAR relative to the `T-Wake` anchor.
  - Define "Hormonal Utility Windows" for different classes of endocrine medications.
- [ ] **M2: Fasting & Metabolic Safeguards**
  - Integrate with `AdaptiveMealEngine` to ensure thyroid doses are locked into fasting windows.
  - Implement "Dawn Phenomenon" alerts for diabetic users taking morning insulin.
- [ ] **M3: Optimization Report**
  - Build a "Hormonal Harmony" dashboard card showing alignment scores.
  - Trigger "Sync Alerts" when a T-Wake shift puts a hormonal dose out of circadian alignment.

## Verification Plan
- **Unit Tests:**
  - `HormonalSyncEngineTest`: Verify CAR peak calculation.
  - `EndocrineAlignmentTest`: Verify thyroid doses are correctly flagged if overlapping with meal windows.
- **Manual Verification:**
  - Shift T-Wake by 2 hours and verify that hormonal doses are proactively rescheduled or flagged as "Misaligned."
  - Log a meal immediately after a thyroid dose and verify the "Absorption Warning."
