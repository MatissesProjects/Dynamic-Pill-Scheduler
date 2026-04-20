# Track Plan: T2 Health Sync

## Specification
Integrate Android Health Connect to automate `T-Wake` anchoring via `SleepSessionRecord` and establish cross-device sync using Horologist DataStore.

## Implementation Steps
- [x] Add Health Connect dependencies and manifest permissions
- [x] Initialize `HealthConnectClient` and permission request flow
- [x] Implement `HealthSyncManager` to query `SleepSessionRecord`
- [x] Implement `T-Wake` auto-anchoring logic (latest sleep session end time)
- [x] Configure Horologist DataStore for Wear OS <-> Phone synchronization
- [ ] Unit/Integration tests for record polling and sync logic

## Verification Plan
- Verify Health Connect permission flow.
- Mock `SleepSessionRecord` and verify `TemporalAnchor` updates.
- Verify Protobuf-based sync between modules.
