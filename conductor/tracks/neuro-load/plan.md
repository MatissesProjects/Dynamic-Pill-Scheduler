# Track 47: Neuro-Cognitive Load Tracking

## Overview
**Intelligence:** Gemini Nano analysis of "Word-Finding" latency and speech fluidity as a proxy for brain fog or medication-induced cognitive blunting. By analyzing speech patterns during voice logs, PHOS can detect early signals of neurotoxicity or peak-concentration "brain fog."

## Specification
- **Metrics:**
  - **Speech Fluidity:** Calculation of "Speech-to-Pause" ratio.
  - **Filler Density:** Frequency of "um," "ah," and extended silences.
  - **Word-Finding Latency:** Detection of "tip-of-the-tongue" pauses before health-related nouns.
- **Intelligence:** Correlate these metrics with medication half-life peaks.

## Milestones
- [ ] **M1: Speech Metadata Capture**
  - Update `VoiceManager` (in `app-phone`) to capture timestamped speech segments.
  - Calculate "Inter-word Latency" from speech-to-text metadata.
  - Implement a `CognitiveMetrics` data structure.
- [ ] **M2: Nano Fluidity Analysis**
  - Develop Gemini Nano prompt for "Semantic Fluidity Audit."
  - Build `NeuroLoadEngine` to process speech metadata and raw text.
  - Detect "Circumlocution" (talking around a word) via Nano.
- [ ] **M3: Peak Correlation & Alerts**
  - Correlate fluidity drops with Medication Peak Concentration ($C_{max}$).
  - Implement "Brain Fog" alerts in the Vertical Timeline.
  - Propose dose-timing adjustments if cognitive load is consistently high post-dose.

## Verification Plan
- **Unit Tests:**
  - `FluidityCalculatorTest`: Verify ratio calculations from mock metadata.
  - `NeuroLoadPromptTest`: Verify Nano prompt construction.
- **Manual Verification:**
  - Log voice entries with intentional pauses/fillers and verify "Fluidity" score drops.
  - Verify correlation alerts appear during simulated peak windows.
