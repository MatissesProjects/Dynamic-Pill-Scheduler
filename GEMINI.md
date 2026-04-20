# Gemini Development Mandates

## Core Principles
- **Source of Truth:** Always refer to [PHOS_OVERVIEW.md](./PHOS_OVERVIEW.md) for core logic and architecture.
- **Mandatory Testing:** Always run tests before considering a task complete.
- **Logical Commits:** Always commit to git when a logical chunk of work is done.
- **Feature Branching:** Always create a new branch when starting a major feature change.
- **Privacy-First:** All health data must remain on-device. Use Gemini Nano for local intelligence.
- **Context-Aware:** UI interactions must be grounded in physiological or temporal context.

## Branching & Commit Strategy
- **Feature Branches:** Every major track (T1-T4) must operate on a dedicated `feature/t[X]-[name]` branch.
- **Atomic Commits:** Commits should be logical chunks (e.g., "Implement Room entities", "Add unit tests").
- **PR-Based Merging:** All features must be merged via `gh pr create` and reviewed before moving to the next track.
- **Main Protection:** `main` branch is for validated, stable releases only.

## Current Implementation Status (T1 Core Engine)
- **Architecture:** Multi-module Android project initialized (`app-phone`, `app-wear`, `core-data`, `core-intelligence`).
- **Core Engine:**
    - Room entities with temporal versioning (`validFrom`, `validTo`) implemented.
    - `T-Wake` temporal offset logic implemented in `TemporalEngine`.
    - "Sponge Effect" collision detection and resolution implemented in `CollisionResolver`.
- **Testing:**
    - Exhaustive unit tests added for `TemporalEngine` (fuzzy windows, late wake shifts).
    - Exhaustive unit tests added for `CollisionResolver` (safe gaps, conflict rules).
- **Tooling:**
    - Feature branch `feature/t1-core-engine` active and pushed.
    - `gh` CLI integrated and authenticated.
    - Pull Request #1 created for Core Engine review.
    - Gradle wrapper and CI environment verification pending.

## Next Steps
1. Verify Gradle build and run unit tests.
2. Initialize `HealthConnectClient` in `core-data` for T-Wake sync.
3. Develop Wear OS haptic vocabulary prototypes.

