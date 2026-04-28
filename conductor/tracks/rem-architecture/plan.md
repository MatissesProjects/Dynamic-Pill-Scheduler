# Track Plan: T36 - REM Stability & Dream Synthesis

## Objective
Quantify and manage Beta-Blocker induced REM-rebound and vivid dreams using fragmented REM analysis and Gemini Nano dream synthesis.

## Milestones
- [x] **M1: Implement REM fragmentation detection**
    - Analyze `SleepStageRecord` for frequent transitions (Awake spikes) within REM blocks.
    - Build `REMSafetyEngine` to calculate a "REM Fragmentation Index".
- [x] **M2: Use Gemini Nano for dream vividness synthesis**
    - Extract intensity and vividness scores from voice-logged dream descriptions.
    - Correlate dream intensity with detected REM fragmentation.
- [x] **M3: Build "Sleep Restoration Audit" summary**
    - Aggregate fragmentation and dream data into a weekly audit.
    - Provide clinical-ready summaries for physician consultation.
- [x] **M4: Integration & Testing**
    - Add unit tests for `REMSafetyEngine`.
    - Integrate dream analysis into `VoiceManager` and `DashboardViewModel`.
    - Render Sleep Restoration Audit in UI.

## Tech Stack
- **Module:** `core-data`, `core-intelligence`, `app-phone`
- **Data:** Health Connect (`SleepStageRecord`), Gemini Nano (`ML Kit GenAI`)
- **Intelligence:** REM fragmentation heuristics, LLM-based sentiment/intensity extraction.

## Documentation Context
- **PHOS_OVERVIEW.md**: Core temporal and safety logic.
- **EXPANSION_PLAN_2026.md**: Concept details for T36.
