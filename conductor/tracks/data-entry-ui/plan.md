# Track Plan: T14 Manual Data Entry UI

## Specification
Build a robust UI in `app-phone` for manual medication entry, dose logging, and preference management, replacing the initial mock data.

## Implementation Steps
- [ ] Create a "Medication Addition" screen with fields for name, dosage, frequency, and categories.
- [ ] Implement a dose logging interface that allows users to manually confirm or skip doses if the automated detection (T10) isn't used.
- [ ] Build a settings screen to manage T-Wake offsets and other system preferences.
- [ ] Update `MainActivity` to transition from mock data to the dynamic `MedicationRecord` and `DoseLog` entries from the database.

## Verification Plan
- UI tests for form validation and data persistence in the Room database.
- Integration tests ensuring manual logs sync correctly across the system.
