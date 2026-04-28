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

### T33: Gastric Protection & GI-Aware Scheduling
**Concept:** Automatically detect and mitigate medication-induced stomach discomfort.
- **Intelligence:** Use Gemini Nano to scan the medication list for high-risk GI irritants (NSAIDs, Bisphosphonates, Iron, etc.).
- **Dynamic Requirement:** Force "WITH_FOOD" status for identified irritants, regardless of initial setup.
- **Gastric Buffer Logic:** Propose a 30-minute "Stomach Buffer" window (e.g., suggest a snack) before doses of irritants if no meal is scheduled.
- **Symptom Correlation:** Correlate user-logged "Stomach Pain" symptoms with dose proximity to refine the "Gastric Sensitivity" profile.

### T34: Sleep Pressure Modeling (Adenosine Drive)
**Concept:** Use Health Connect sleep history and caffeine logs to model wakefulness.
- **Logic:** Accumulate "Adenosine Units" per hour awake; subtract units per hour of deep sleep.
- **Prediction:** Forecast "Nap Propensity" score (0-100) and suggest alertness injections (T32) before the score hits 80.

### T35: Beta-Blocker "Idle Speed" Monitoring (Bradycardia Safety)
**Concept:** Detect excessive heart-rate suppression (Morning RHR and 2 PM Slumps).
- **Morning Baseline:** Monitor for RHR < 50 BPM during the first 30 mins of T-Wake.
- **Slump Logic:** Automatically correlate the "6-hour post-dose peak" with Heart Rate data. If HR is >15% lower than the daily average during a detected slump, trigger a "Sluggishness Validation" check-in.
- **Intervention:** Suggest a 5-minute "Oxygenation Bout" (light movement) if HR drops below the biological idle threshold.

### T36: REM Architecture & "Dream Synthesis"
**Concept:** Quantify and manage Metoprolol-induced REM-rebound and vivid dreams.
- **REM Analysis:** Ingest `SleepStageRecord` from Health Connect. Monitor for fragmented REM (frequent "Awake" spikes during REM blocks).
- **Dream Journaling:** Use Gemini Nano to extract "Intensity" and "Vividness" from voice-logged dreams.
- **Correlation:** Map REM fragmentation vs. Dream Intensity to provide a weekly "Sleep Restoration Audit" for doctors.

### T37: Muscle-Heart Mismatch (Heavy Legs Detection)
**Concept:** Identify the "moving through mud" sensation caused by heart rate capping.
- **Heuristic:** Detect periods of high activity (Step Rate/Cadence) with abnormally low Cardio Load/HR relative to the historical baseline.
- **Detection:** If activity is "Moderate" but HR is "Low" (capped by med), and the user reports "Heavy Legs," classify as a Mismatch.
- **Readiness Integration:** Scale activity suggestions (T32) downward based on the Daily Readiness Score to prevent over-exertion during poor adaptation days.

### T38: Heart Rate Recovery (HRR) Orchestration
**Concept:** Measure autonomic nervous system (ANS) resilience and recovery.
- **Logic:** Following any activity with HR > 110 BPM, monitor the rate of decline at 1 and 2 minutes.
- **Correlation:** Track HRR performance across medication versions. If HRR slows by >15% over a 7-day period, trigger a "Medication-Induced Recovery Audit."

### T39: Multi-Metric Heart Failure (HF) Early-Warning System
**Concept:** Detect early signs of cardiac decompensation 7-14 days before symptoms.
- **Logic:** Use sensor fusion (HRV, RHR, Respiratory Rate, and SpO2).
- **Prediction:** Use Gemini Nano to perform multivariate trend analysis. (e.g., "Sustained increase in nocturnal RHR + 10% drop in HRV = Elevated Risk").
- **Intervention:** Automatically tighten "Safe Gaps" for diuretics and beta-blockers to ensure perfect adherence during high-risk windows.

### T40: Cardiac Output Efficiency (Pulse-Power Twin)
**Concept:** Quantify the efficiency of the "Heart-Muscle Bridge."
- **Logic:** Correlate e-bike `Power (Watts)` with `Heart Rate`.
- **Metric:** Calculate `Watts per BPM` during steady-state climbs.
- **Optimization:** If efficiency drops >10% while heart rate remains capped, suggest increasing motor assistance to protect the myocardium.

### T41: Nightly Respiration & Congestion Proxy
**Concept:** Monitor for sub-clinical pulmonary congestion (fluid in lungs).
- **Logic:** Ingest nocturnal `Respiratory Rate` and `Oxygen Saturation` (SpO2).
- **Detection:** Identify "Orthopnea Proxies" (e.g., increased RR/decreased SpO2 specifically during flat sleep stages vs. upright-propped stages).
- **Safety:** Suggest a "Dose Adjustment Consultation" if nocturnal respiratory strain increases for 3 consecutive nights.


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
