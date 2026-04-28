# Track 46: Local Intelligence Harness Sync

## Overview
**Architecture:** Async offloading of complex correlations to local, desktop-grade LLM nodes during overnight charging. This track aims to bypass the on-device constraints of Gemini Nano for long-term pattern analysis, while maintaining privacy by keeping the data on the local network.

## Specification
- **Security:** Use mTLS or locally-scoped API tokens for mobile-to-node communication.
- **Protocol:** JSON-RPC or Protobuf over HTTP/2.
- **Intelligence:** Deep correlation of biometric trends (months of data) which exceeds Nano's context window.

## Milestones
- [ ] **M1: Secure Local API Relay**
  - Implement a discovery mechanism (mDNS) to find the local LLM node.
  - Build the authentication handshake.
  - Create the `BiometricRelayManager` in `core-intelligence`.
- [ ] **M2: Overnight Charging Synthesis**
  - Use `WorkManager` to trigger sync only when:
    - Device is charging.
    - On unmetered Wi-Fi.
    - Time is between 02:00 and 05:00.
  - Batch export `SymptomRecord` and `HealthConnect` deltas.
- [ ] **M3: Model Sync & Feedback**
  - Receive "Optimized Schedule" and "Deep Insights" from the node.
  - Update `TemporalDatabase` with versioned scheduling improvements.
  - Trigger "Morning Insight" notification on Phone and Watch.

## Verification Plan
- **Unit Tests:**
  - `mDNSDiscoveryTest`: Verify node discovery logic.
  - `RelayAuthTest`: Verify secure handshake.
- **Integration Tests:**
  - `OvernightSyncWorkflowTest`: Simulate charging/Wi-Fi conditions and verify WorkManager triggering.
- **Manual Verification:**
  - Logs showing successful data offload and response processing.
