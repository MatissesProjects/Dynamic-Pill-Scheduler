# Track Plan: T21 Adaptive Meal & Hunger Orchestration

## Specification
Optimize the timing of meals relative to the medication schedule and hunger patterns. Propose specific windows for eating high-interference foods (e.g., high-calcium) based on scans.

## Implementation Steps
- [ ] Implement `AppetiteLog` and hunger-tracking dashboard logic.
- [ ] Create `MealScheduler` logic to suggest "Optimal Eating Windows" based on `T-Wake` and medication offsets.
- [ ] Integrate "Sacred Eating Window" reservation for high-difficulty days.
- [ ] Implement nutrition-aware scheduling (e.g., "Best time for this yogurt: 2:00 PM").
- [ ] Develop "Meal Sync" dashboard view showing medication windows overlaid with suggested meal times.

## Verification Plan
- Unit tests for `MealScheduler` windows under various medication configurations.
- Integration tests ensuring "Stay Upright" guidance is triggered after suggested meals.
- User flow verification for "Hunger Window" acceptance.
