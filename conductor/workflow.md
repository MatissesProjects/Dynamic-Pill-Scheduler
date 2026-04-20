# Workflow: Dynamic Pill Scheduler (PHOS)

## Development Principles
- **Privacy-First:** All health data must remain on-device. Use Gemini Nano for local intelligence.
- **Context-Aware:** Every UI interaction should be grounded in current physiological or temporal context.
- **Modular by Design:** Core logic must be decoupled from UI to support Phone and Watch experiences seamlessly.
- **Mandatory Testing:** We always run tests before considering a task complete.
- **Logical Commits:** We always commit to git when a logical chunk of work is done.
- **Feature Branching:** Always create a new branch when starting a major feature change.

## Testing Strategy
1. **Unit Tests:** Focus on the `Temporal Offset Engine` and `Collision Logic`.
2. **Integration Tests:** Verify Health Connect sync and Wear OS Data Layer communication.
3. **Hardware-in-Loop:** Manual validation of haptic vocabulary on the Pixel Watch 3.

## Distribution & Feedback
- Internal testing via Firebase App Distribution (Phone) and Play Store Internal Sharing (Watch).
- Micro-journaling logs used to refine correlation patterns locally.