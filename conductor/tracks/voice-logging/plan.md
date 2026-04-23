# Track Plan: T18 Context-Aware Voice Logging

## Specification
Provide a hands-free, natural language interface for logging health events. The system will use on-device Speech-to-Text and Gemini Nano to extract multiple entities (meds, symptoms, intake) from a single conversational sentence.

## Implementation Steps
- [ ] Integrate Android `SpeechRecognizer` (on-device) into `app-phone`.
- [ ] Develop Gemini Nano prompt templates for multi-entity extraction from spoken text.
- [ ] Implement `VoiceLogCoordinator` to dispatch extracted entities to their respective DAOs (Dose, Symptom, Food).
- [ ] Create a "Conversational Feedback" UI that summarizes what was understood and allows quick correction.

## Verification Plan
- Unit tests for entity extraction accuracy from various natural language strings.
- Integration tests for simultaneous logging of a dose and a symptom via a single voice command.
- Verification that `CollisionResolver` warnings are triggered immediately following a voice log.
