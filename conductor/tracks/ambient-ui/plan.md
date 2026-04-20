# Track Plan: T3 Ambient UI

## Specification
Implement the "Ambient UX" for Pixel Watch 3 and Pixel 9 Pro XL, focusing on haptic vocabulary, Wear OS 5 complications, and the Material You Vertical Timeline.

## Implementation Steps
- [x] Define "Haptic Vocabulary" in `app-wear` using `WaveformEnvelopeBuilder`
- [x] Implement Wear OS 5 Complications for zero-tap medication status/logging
- [x] Build the "Vertical Timeline" Dashboard in `app-phone` (Compose + Material You)
- [x] Implement the "Live Activity" Widget for the Phone Lock Screen
- [x] Integrate real-time DataStore synchronization into all UI components


- [ ] Unit tests for UI state mapping and time-aware rendering

## Verification Plan
- Verify haptic vibration patterns on physical hardware (Pixel Watch 3).
- Verify Complication updates based on DataLayer sync.
- Verify Material You dynamic color integration on Phone UI.
