# Track 48: Thermal Dysregulation Shield

## Overview
**Safety:** Correlating skin temperature deviations (Watch) with medications that inhibit sweating (anticholinergics) or cause flushing (niacin, calcium channel blockers). This shield provides real-time hyperthermia risk alerts during exercise or heatwaves when the user's natural cooling mechanism is compromised by their medication.

## Specification
- **Sensors:** Pixel Watch 3 Skin Temperature sensor via Health Connect.
- **Intelligence:** 
  - Identify "Sweat-Inhibiting" (SI) and "Flushing" (FL) medications.
  - Correlate SI medication peak windows with rising skin temperature deltas.
  - Integrate localized weather (T45) to detect heatwave conditions.

## Milestones
- [ ] **M1: Thermal Biometric Stream**
  - Update `HealthSyncManager` to pull `SkinTemperatureRecord` from Health Connect.
  - Add `SKIN_TEMPERATURE` to `BiometricType`.
  - Build `ThermalShieldEngine` in `core-intelligence`.
- [ ] **M2: Pharmacological Thermal Modeling**
  - Expand `GIProtectionEngine` or create `MedicationSafetyClassifier` to flag anticholinergics and vasodilators.
  - Define "Thermal Risk Thresholds" (e.g., +1.5°C delta from baseline during peak concentration).
- [ ] **M3: Hyperthermia & Flushing Alerts**
  - Implement "Active Cooling" reminders on Wear OS when SI meds are active and temp rises.
  - Correlate outdoor ambient temperature (T45) with SI risk.
  - Render "Thermal Strain" metrics in the Vertical Timeline.

## Verification Plan
- **Unit Tests:**
  - `ThermalShieldEngineTest`: Verify alert triggering on mock temp deltas.
  - `MedicationThermalFlagTest`: Verify correct classification of sweat-inhibiting meds.
- **Manual Verification:**
  - Simulate a temperature spike during a mock peak concentration window and verify the Wear OS alert.
  - Verify "Thermal Strain" card appears in the Vertical Timeline.
