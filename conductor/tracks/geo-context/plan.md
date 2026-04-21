# Track Plan: T7 Geo-Contextual Awareness

## Specification
Introduce location awareness into the scheduling logic to provide proximity alerts and location-anchored doses.

## Implementation Steps
- [ ] Integrate Google Play Services Location APIs or custom Geofencing logic into `app-phone`.
- [ ] Implement Proximity Alerts: Trigger specific haptic warnings (e.g., on Pixel Watch 3) if a user leaves a designated "Home" boundary without taking critical morning (T-Wake) medications.
- [ ] Enhance `TemporalEngine` to support "Location-Anchored Doses" (e.g., schedule medication X to be due 10 minutes after arriving at location Y).
- [ ] Create UI components in `app-phone` for users to set home boundaries and link locations to specific doses.

## Verification Plan
- Unit tests for location-anchored dose calculations.
- Mock location tests triggering boundary exit events and verifying haptic alert generation.
- UI tests for location selection and dose linking.
