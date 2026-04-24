# Track Plan: T23 Goal-Oriented Scheduling & Meal Preferences

## Specification
Empower users to set specific health goals (e.g., "Prevent stomach pain at 4 AM") and define customized, optional meal windows. The AI engine will dynamically modify both eating windows and medication offsets to satisfy these goals.

## Implementation Steps
- [ ] Define `MealPreferences` in `phos_state.proto` to hold preferred meal times (Breakfast, Lunch, Dinner).
- [ ] Update `DataLayerRepository` to persist and retrieve meal preferences.
- [ ] Implement `HealthGoal` entity in Room to track user-defined health targets (e.g., symptom, time of day).
- [ ] Create `GoalOptimizationEngine` (using Gemini Nano) that suggests medication offsets and meal window adjustments based on active goals.
- [ ] Update `MealScheduler` to combine "Optimal Eating Windows" with user-defined `MealPreferences`.
- [ ] Build UI in the Dashboard (Settings & Meals tab) to input meal preferences and log health goals.

## Verification Plan
- Unit test `MealScheduler` to ensure preferred meal windows are prioritized or shifted appropriately.
- Mock `GoalOptimizationEngine` outputs to test if it generates actionable shift suggestions.
- Validate DataStore synchronization of `MealPreferences`.
