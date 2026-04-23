# Track Plan: T15 Interaction & Side Effect Intelligence

## Specification
Implement an advanced intelligence layer to monitor medication combinations for side effects and suggest optimal spacing (e.g., gut health optimization, absorption protection).

## Implementation Steps
- [ ] Define `SideEffectRule` and `AbsorptionRule` entities in `core-data`.
- [ ] Create a local knowledge base of common medication interactions (e.g., Sucralfate spacing, NSAID-induced GI issues).
- [ ] Enhance `CollisionResolver` to suggest "Spacing Insights" (e.g., "Take Sucralfate 2 hours before others for better gut protection").
- [ ] Update `VerticalTimeline` to display "Side Effect Watch" alerts if conflicting meds are scheduled.
- [ ] Integrate Gemini Nano to provide natural language explanations of *why* certain spacing is recommended.

## Verification Plan
- Unit tests verifying the collision logic for absorption-based spacing rules.
- Simulation tests with "High Sensitivity" meds to ensure proper alerts are triggered.
- Verify that insights are grounded in the local interaction database.
