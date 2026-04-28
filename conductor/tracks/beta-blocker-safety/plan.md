# Track Plan: T35 - Beta-Blocker Side Effects (Bradycardia & Slump Monitoring)

## Objective
Monitor and mitigate Beta-Blocker side effects, specifically morning bradycardia ("Idle Speed") and afternoon fatigue ("6-hour Slump"), using automated HR correlation and "Oxygenation Bout" reminders.

## Milestones
- [x] **M1: Build "Idle Speed" RHR monitor**
    - Monitor for RHR < 50 BPM during the first 30 mins after `T-Wake`.
    - Implement `BetaBlockerSafetyEngine` logic for bradycardia detection.
- [x] **M2: Implement "6-hour Slump" correlation logic**
    - Correlate 6-hour post-dose peak with HR data.
    - Trigger "Sluggishness Validation" if HR is >15% lower than daily average.
- [x] **M3: Create "Oxygenation Bout" reminders**
    - Suggest 5-minute light movement if HR drops below threshold during slump.
- [x] **M4: Integration & Testing**
    - Add unit tests for `BetaBlockerSafetyEngine`.
    - Integrate with `DashboardViewModel`.
    - Render insights in `VerticalTimeline` UI.

## Tech Stack
- **Module:** `core-data`, `app-phone`
- **Data:** Health Connect (`RestingHeartRateRecord`, `HeartRateRecord`), Room (`DoseLog`)
- **Intelligence:** Automated HR/Dose correlation logic.

## Documentation Context
- **PHOS_OVERVIEW.md**: Core temporal and safety logic.
- **EXPANSION_PLAN_2026.md**: Concept details for T35.
