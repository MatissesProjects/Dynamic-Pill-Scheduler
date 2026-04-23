# Track Plan: T21 Adaptive Meal & Supplement Scheduling

## Specification
Optimize the timing of meals relative to the medication schedule to maximize absorption and minimize side effects.

## Implementation Steps
- [ ] Create `MealScheduler` logic to suggest "Optimal Eating Windows" based on `T-Wake` and medication offsets.
- [ ] Integrate "Empty Stomach" vs. "With Food" requirements from the persistent knowledge base.
- [ ] Implement proactive notifications: "Time for a high-protein snack to prepare for your next dose."
- [ ] Develop a "Meal Sync" dashboard view showing medication windows overlaid with suggested meal times.

## Verification Plan
- Unit tests for `MealScheduler` windows under various medication configurations.
- Integration tests ensuring "Stay Upright" guidance is triggered after suggested meals.
- User flow verification for accepting a suggested meal window.
