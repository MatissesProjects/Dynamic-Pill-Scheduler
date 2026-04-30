# Track Specification: T51 Privacy-Preserving Care Mesh

## Overview
The Privacy-Preserving Care Mesh enables users to share their "Safety Status" (Red/Yellow/Green) with trusted contacts (caregivers, family, doctors) without revealing sensitive medication names, dosages, or raw biometric values. This is achieved using Zero-Knowledge Proofs (ZKP), ensuring that only the final status and the proof of its validity are shared.

## Goals
- **Privacy:** Medication adherence and biometric data never leave the device in raw form.
- **Trust:** Caregivers receive a mathematically verifiable proof that the shared status is accurate and based on the user's actual data.
- **Simplicity:** Status is represented as a simple traffic light (Red/Yellow/Green).

## Requirements
- Define a "Safety Score" algorithm that aggregates adherence, collision risks, and physiological alerts.
- Implement a ZKP Prover that generates a proof for a specific Safety Score range.
- Implement a ZKP Verifier that can validate the proof on a separate device (or a web-based companion).
- Integrate with the existing `core-data` engines to pull real-time safety data.

## Architecture
- **SafetyAggregator:** A new component in `core-data` that queries multiple engines (Temporal, Collision, HF, BetaBlocker, etc.) to produce a unified `SafetyStatus`.
- **ZKP Engine:** A cryptographic module that takes the `SafetyStatus` and private inputs to generate a proof.
- **Mesh Sharing Service:** A UI and transport layer (QR/NFC/Encrypted Message) to share the proof and status.

## Tech Stack Addition
- **Library:** `zkkrypto` (Native Kotlin) or a simple implementation of Schnorr/Pedersen proofs if complex circuits are not required.
- **Data:** Uses Room DB (Dose Logs) and Health Connect (Biometrics) as private inputs.
