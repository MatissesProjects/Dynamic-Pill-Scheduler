# Track Plan: T8 Data Sovereignty & Reporting

## Specification
Provide users with a privacy-first mechanism to export their health data (medication adherence, biometric spikes, and symptom trends) for their doctors or caregivers.

## Implementation Steps
- [ ] Implement an on-device PDF generation service querying the local SQLite (`core-data`) database.
- [ ] Design a visual PDF layout summarizing 30-day trends (adherence rates, symptom logs, HR/BP metrics).
- [ ] Integrate the Android system `ShareSheet` using `Intent.ACTION_SEND` and `FileProvider` to securely export the generated PDF without cloud upload.
- [ ] Add a "Generate Report" action to the `app-phone` Dashboard UI.

## Verification Plan
- Unit tests verifying SQL query correctness for 30-day data aggregation.
- Integration tests ensuring the PDF generation service creates a valid file.
- Manual verification of the ShareSheet intent and PDF visual formatting.
