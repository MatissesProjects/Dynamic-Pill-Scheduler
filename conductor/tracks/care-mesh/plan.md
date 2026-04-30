# Track Plan: T51 Privacy-Preserving Care Mesh

## Implementation Steps

### Phase 1: Safety Aggregation
- [x] Implement `SafetyAggregator` in `core-data`.
- [x] Define `SafetyStatus` enum (Green, Yellow, Red).
- [x] Map engine outputs (Adherence, HF Risk, etc.) to the `SafetyStatus`.

### Phase 2: ZKP Core Implementation
- [x] Research and select the final ZKP library (verify `zkkrypto` compatibility or implement simple Schnorr proofs).
- [x] Build the `ZkpProver` to generate a proof for the current `SafetyStatus`.
- [x] Build the `ZkpVerifier` to validate the proof.

### Phase 3: Integration & UI
- [x] Add "Care Mesh" section to the Phone App settings.
- [x] Implement QR code generation for sharing the proof.
- [x] Implement a basic "Verifier Mode" for the app (to act as a caregiver device).

### Phase 4: Verification
- [x] Unit tests for `SafetyAggregator` logic.
- [x] Cryptographic validation tests for ZkpProver/Verifier.
- [x] End-to-end simulation of proof sharing and verification.

## Verification Plan
- **Safety Logic:** Ensure `SafetyStatus` correctly reflects underlying risks (e.g., missed Beta-Blocker + Bradycardia = Red).
- **ZKP Integrity:** Verify that the proof *fails* if the status is tampered with.
- **Privacy Audit:** Confirm no raw med/biometric data is included in the shared payload.
