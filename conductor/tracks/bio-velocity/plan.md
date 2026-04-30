# Track Plan: T50 - Biological Velocity Modeling

## Objective
Implement a real-time biological age and "Pace of Aging" modeling system based on HRV and RHR trends, providing users with a high-level metric of their biological health velocity.

## Milestones
- [x] **M1: Define Bio-Velocity Data Models**
    - Created `BioVelocityLog` and `BioBaseline` entities in `core-data`.
    - Implemented `BioVelocityDao` and registered in `PhosDatabase`.
- [x] **M2: Implement "Pace of Aging" Algorithm**
    - Developed `BioVelocityEngine` in `core-intelligence` with heuristic-based Pace of Aging logic.
- [x] **M3: Adherence Correlation Logic**
    - Integrated adherence rate from `DoseLogDao` into bio-velocity calculations.
    - Implemented Gemini Nano insight generation for bio-velocity.
- [x] **M4: UI Integration & Visualization**
    - Integrated `BioVelocityEngine` into `DashboardViewModel`.
    - Added Biological Age card to `MainDashboard` and `VerticalTimeline`.

## Tech Stack
- **Module:** `core-data`, `core-intelligence`, `app-phone`
- **Data:** Health Connect (`HeartRateVariabilityRmssdRecord`, `RestingHeartRateRecord`, `SleepSessionRecord`), Room (`DoseLog`, `MedicationRecord`)
- **Intelligence:** Heuristic-based modeling, Gemini Nano (AICore) for insight synthesis.

## Documentation Context
- **PHOS_OVERVIEW.md**: Core temporal and safety logic.
- **POSSIBLE_EXPANSIONS.md**: Concept details for T50.
