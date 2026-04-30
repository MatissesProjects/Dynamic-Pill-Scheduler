# Track Plan: T52 Acoustic Stress Correlation

## Implementation Steps

### Phase 1: Data Models & Entities
- [x] Add `AcousticLog` entity to `core-data`.
- [x] Implement `AcousticDao` and register in `PhosDatabase`.
- [x] Define `AcousticInsight` model and `AcousticLogType`.

### Phase 2: Watch Noise Monitoring (`app-wear`)
- [x] Add `RECORD_AUDIO` permission to `app-wear` manifest.
- [x] Implement `AmbientNoiseWorker` in `app-wear` to calculate dB from `AudioRecord`.
- [x] Integrate with `DataLayerRepository` to sync `AcousticLog` to the Phone.

### Phase 3: Acoustic Stress Engine (`core-data`)
- [x] Implement `AcousticStressEngine` in `core-data/engine`.
- [x] Build logic to correlate noise peaks with Heart Rate and HRV data from `BiometricDao`.
- [x] Implement "Acoustic Shielding" advice logic (Lisinopril/Beta-Blocker aware).

### Phase 4: UI Surfacing (`app-phone`)
- [x] Update `DashboardViewModel` to collect `AcousticLogs`.
- [x] Display Acoustic Insights and Shielding alerts in the Dashboard timeline.

### Phase 5: Verification
- [x] Unit tests for `AcousticStressEngine` correlation logic.
- [x] Simulated end-to-end test of Watch dB logs triggering Phone alerts.
- [x] Verify `RECORD_AUDIO` permission handling and privacy (no storage of audio).

## Verification Plan
- **Acoustic Accuracy:** Verify dB calculation logic against known amplitude levels.
- **Correlation Logic:** Ensure alerts are only triggered for *prolonged* high noise when a sensitive medication is active.
- **Privacy Audit:** Confirm no raw audio data is persisted or transmitted.
