# PHOS Expansion Plan: 2026 Breakthroughs

## Overview
This plan outlines the integration of cutting-edge physiological research and Pixel Watch 3 sensor fusion into the PHOS ecosystem. We are moving from "Medication Scheduling" to **"Holistic Biological Orchestration."**

## New Tracks

### T27: Neuromotor Side-Effect Detection (Gait Analysis)
**Concept:** Use Wear OS 5's new `Stride Length` and `Cadence` metrics to detect subtle neuromotor changes.
- **Logic:** Establish a 7-day gait baseline. Monitor for a >15% drop in stride length (a clinical marker for dizziness/ataxia) following new medication versions.
- **Intervention:** Proactive "Fall Risk" alerts and suggestions to consult a doctor about dosage.

### T28: Circadian Chronotype Alignment
**Concept:** Shift from "T-Wake" (physiological) to "Chronotype" (biological).
- **Logic:** Use 14 days of sleep/wake data to classify the user (Lark, Owl, or Third Bird).
- **Optimization:** Adjust BP and Statin medication windows to align with chronotype-specific efficacy peaks (e.g., shifting Night Owls to bedtime BP dosing).

### T29: Metabolic Load Prediction
**Concept:** Correlate "Cardio Load" with dose timing.
- **Logic:** Ingest `Cardio Load` data from Google Health API.
- **Prediction:** If Cardio Load is "High" (>80th percentile), suggest a 15% increase in water intake or a slight shift in medication timing to avoid peak physiological stress.

### T30: Sentiment & Stress Synthesis (Gemini Nano)
**Concept:** Correlate voice logging sentiment with HRV.
- **Logic:** Use Gemini Nano to perform local sentiment analysis on voice logs.
- **Correlation:** Map "High Stress" sentiment to "Low HRV" periods to predict "Burnout Risk" and suggest "Reserved Rest Windows" in the timeline.

### T31: Micro-Mobility & Adaptive Effort Orchestration (e-Bike)
**Concept:** Orchestrate physical activity for users with medication-blunted heart rates.
- **Logic:** Detect e-bike activity via Health Connect (high speed/distance with 10-15% lower HR than traditional cycling).
- **Assistance Optimization:** For users on Beta-Blockers, suggest higher e-bike assistance levels during steep inclines to prevent "hitting the heart rate ceiling."
- **Safety:** Trigger a "Cool-Down Alert" after vigorous rides to prevent post-exercise hypotension (common with BP meds).

### T32: Activity-Based Wakefulness Orchestration (Nap Prevention)
**Concept:** Use exercise as a non-pharmacological tool to suppress daytime sleepiness.
- **Logic:** Identify "Nap Vulnerability Zones" based on current-day sleep debt and the 7-9 hour post-wake "circadian dip."
- **Intervention:** 30 minutes before a predicted dip, suggest a 10-20 minute "Alertness Micro-bout" (e.g., e-bike ride or brisk walk).
- **Mechanism:** Leverage acute exercise neurotransmitter release (norepinephrine/dopamine) to reset sleep pressure (adenosine).


## Implementation Strategy
- **M1:** Implement `GaitManager` in `core-data`.
- **M2:** Build `ChronotypeClassifier` in `core-intelligence`.
- **M3:** Expand `DashboardViewModel` to handle Cardio Load alerts.
- **M4:** Update `VoiceLogCoordinator` for sentiment extraction.

## Verification & High-Integrity Testing
Every new expansion track must be accompanied by exhaustive unit tests:
- `GaitManagerTest`: Verify baseline calculations and 15% drop detection.
- `ChronotypeClassifierTest`: Validate MSFsc formula and Lark/Owl classification.
- `MetabolicEngineTest`: Ensure TRIMP scores correctly weight HR zones.
- `StressSynthesisEngineTest`: Verify burnout risk correlation (Sentiment + HRV).
- `EbikeEffortNormalizerTest`: Validate e-bike vs traditional detection heuristics.
