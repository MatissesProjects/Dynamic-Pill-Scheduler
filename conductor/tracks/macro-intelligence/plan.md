# Track Plan: T20 Nutrient Intelligence & Allergen Flagging

## Specification
Analyze identified food and "Nutrition Facts" labels to estimate macros/micros and detect allergens. Provide safety-first dietary advice grounded in the medication schedule.

## Implementation Steps
- [ ] Build a local `NutrientReference` database and `AllergenProfile` schema.
- [ ] Develop "Nutrition Label OCR" parser to extract macros and ingredients.
- [ ] Integrate Gemini Nano for ingredient-to-allergen cross-referencing.
- [ ] Implement `NutrientAdvisoryEngine` to flag interference (e.g., "High Calcium detected; wait 2 hours for Antibiotics").
- [ ] Create "Nutrition Guide" UI component that suggests "Good Idea" vs. "Warning" for scanned items.

## Verification Plan
- Unit tests for allergen detection using mock ingredient lists.
- OCR verification for standard and non-standard Nutrition Facts formats.
- Integration tests for med-food interference alerts.
