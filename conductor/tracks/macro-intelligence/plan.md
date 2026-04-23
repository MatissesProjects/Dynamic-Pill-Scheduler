# Track Plan: T20 Nutrient Intelligence & Sourcing

## Specification
Analyze identified food to estimate macronutrients (Protein, Carbs, Fats) and micronutrients (Calcium, Iron, etc.) that may interfere with medication absorption. Provide personalized food suggestions based on nutritional needs.

## Implementation Steps
- [ ] Build a local `NutrientReference` database for common food items.
- [ ] Develop Gemini Nano prompt templates for macro estimation and "Food Sourcing" suggestions (e.g., "Where to find 20g of protein quickly").
- [ ] Implement `NutrientAdvisoryEngine` to flag interference (e.g., "Dairy detected; wait 2 hours before taking Ciprofloxacin").
- [ ] Add a "Nutrition Guide" UI component that suggests what to eat to meet daily targets or complement specific medications.

## Verification Plan
- Unit tests for `NutrientAdvisoryEngine` with known med-food interference rules.
- Verification of macro estimation accuracy via Gemini Nano simulation.
- UI testing of the "Food Suggestions" and "Where to Find" recommendation cards.
