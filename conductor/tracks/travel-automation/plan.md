# Track Plan: T16 Advanced Travel & Jet Lag Automation

## Specification
Automate the titration of T-Wake schedules for upcoming travel. The system will proactively detect future timezone shifts from the user's calendar and suggest a multi-day titration plan to align the circadian rhythm before departure.

## Implementation Steps
- [ ] Implement `TravelDetectionWorker` using on-device `CalendarContract` to find future travel events.
- [ ] Enhance `JetLagManager` with `proposeAdvanceTitration` logic that calculates the optimal start date for shifting T-Wake.
- [ ] Create a "Travel Alert" card in `VerticalTimeline` to present proposed schedules for user approval.
- [ ] Integrate Gemini Nano to explain the benefits of the proposed shift based on flight duration and direction (East vs. West).

## Verification Plan
- Unit tests for travel event parsing and titration start-date calculation.
- UI verification of the "Travel Proposal" acceptance flow.
- Mocking future calendar events to verify proactive alerting.
