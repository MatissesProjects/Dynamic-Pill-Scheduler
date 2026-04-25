overview of the PHOS project architecture, testing strategy, and setup requirements.

  1. Project Architecture: Predictive Health Orchestration
  PHOS is a multi-module Android project built to provide a high-integrity, privacy-first health orchestration platform
  specifically optimized for the Pixel 9 Pro XL and Pixel Watch 3.

   * app-phone (The Hub): Handles the main user experience using Jetpack Compose. It includes the Vertical Timeline (the
     system's focal point) and the Pill Scanner (CameraX + ML Kit).
   * app-wear (The Ambient Layer): A Wear OS 5 module that uses Horologist for real-time data syncing. It utilizes a Haptic
     Vocabulary to provide unique vibrational feedback without requiring the user to look at their wrist.
   * core-data (The Engine): Manages the Temporal Database (Room) which versions every record (validFrom/validTo). It
     handles data from three distinct streams: Passive (Health Connect), Active (User Logs), and Environmental (Sensor
     APIs).
   * core-intelligence (The Brain): Integrates Gemini Nano via the ML Kit GenAI Prompt API for on-device reasoning. It
     powers symptom correlation, voice command parsing, and multimodal food identification.

  2. Testing Strategy: High-Integrity Mandates
  The project operates under a strict Testing-First mandate, focusing on the core temporal and safety logic.

   * Unit Testing: Concentrated in core-data and core-intelligence. Key test suites like AnchorManagerTest.kt and
     CollisionResolverTest.kt verify that medication schedules adapt correctly to shifting T-Wake anchors.
   * Mocking: Uses Mockito-Kotlin to simulate Health Connect and Wear OS data layers, allowing for offline validation of
     complex health scenarios.
   * Test-Driven Development (TDD): New features (Tracks) are required to include corresponding unit tests before being
     marked as "Completed" in the Tracks Registry.
   * Execution: Run all tests via ./gradlew test to ensure zero regressions across the multi-module dependencies.

  3. Proper Setup & Development Workflow
  To maintain the integrity of the Pixel-first experience, follow these setup guidelines:

   * API Requirements: Android 15 (SDK 35) with a minimum SDK of 31.
   * AI Access: Ensure the AICore system service is enabled on the development device (Pixel 9 Pro XL) to support real
     on-device Gemini Nano inference.
   * Git Strategy: Every major feature operates on a dedicated branch (e.g., feature/t[X]-[name]). Commits must be atomic
     and represent logical chunks of work.
   * Conductor Management: The conductor/ directory is the source of truth for project progress. Always update
     conductor/tracks.md after successfully merging a new feature.

  4. Semantic Search Findings
  The search confirmed that while the core architecture is highly sophisticated, current test coverage (~9.9%) is a primary
  area for expansion. We have a robust setup for Multimodal AI and Health Sync, but ensuring these complex asynchronous
  flows are comprehensively covered by automated tests is the next critical priority.
