# Track Plan: T10 Automated Dose Detection

## Specification
Utilize the Pixel Watch 3's onboard sensors (accelerometer and gyroscope) to recognize the physical motion of taking a pill (hand-to-mouth gesture), enabling fully automated or highly assisted logging.

## Implementation Steps
- [ ] Implement an efficient background sensor listener in `app-wear` to sample accelerometer and gyroscope data at a low frequency.
- [ ] Develop a lightweight heuristic or integrate a small TensorFlow Lite classification model to recognize the specific "hand-to-mouth" drinking/swallowing gesture.
- [ ] Tie the detection event to the "Fuzzy Window" logic: if the gesture is detected within a valid window for a scheduled medication, trigger a confirmation haptic (e.g., "Did you just take your dose? Tap to confirm").
- [ ] Build a setting in the phone app to allow users to toggle between "Suggested Logging" (requires confirmation) and "Auto-Logging" (logs automatically on detection).

## Verification Plan
- Unit tests for the sensor data processing pipeline.
- If using an ML model, evaluate precision/recall on a small local validation set of gesture data.
- On-device integration tests validating that the confirmation notification fires *only* when a dose is due.
