# Track Specification: T52 Acoustic Stress Correlation

## Overview
The Acoustic Stress Correlation engine leverages the Pixel Watch 3's microphone to monitor ambient noise pollution. By correlating high-decibel environments with physiological stress markers (elevated Heart Rate, lowered HRV) and medication regimens, PHOS can provide "Acoustic Shielding" recommendations to protect users from cardiovascular strain that might blunt the efficacy of anti-hypertensive or anxiety medications.

## Goals
- **Ambient Noise Tracking:** Periodically sample environmental noise (dB) without recording or storing actual audio content.
- **Cardiovascular Correlation:** Link prolonged acoustic stress (>80 dB) with real-time biometric changes.
- **Medication Efficacy:** Identify if noise pollution is counteracting medications like Lisinopril (ACE inhibitors) or Beta-Blockers.

## Requirements
- **Wear OS Service:** An `AmbientNoiseMonitor` that runs periodically to calculate dB levels.
- **Privacy:** Process audio chunks entirely in memory; only save the resulting numeric dB value.
- **Data Sync:** Transmit `AcousticLog` data from Watch to Phone.
- **Acoustic Engine:** Analyze logs against biometric trends and generate `AcousticInsight` alerts.

## Architecture
- `app-wear`: `AmbientNoiseMonitor` (Foreground Service or Periodic WorkManager) utilizing `AudioRecord`.
- `core-data`: `AcousticLog` entity, DAO, and `AcousticStressEngine`.
- `app-phone`: UI surfacing of "Acoustic Shielding" recommendations via the Dashboard.
