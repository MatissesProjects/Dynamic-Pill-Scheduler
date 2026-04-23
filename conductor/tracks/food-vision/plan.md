# Track Plan: T19 On-Device Food Recognition

## Specification
Enable users to identify food items using the on-device camera. This track builds upon the `PillScannerEngine` infrastructure to classify dietary intake without cloud dependencies.

## Implementation Steps
- [ ] Integrate a quantized Image Classification model (TFLite) for common food categories.
- [ ] Create `FoodScannerEngine` to process `ImageProxy` frames from CameraX.
- [ ] Implement a "Dietary Scan" mode in the Scanner UI.
- [ ] Use Gemini Nano to refine broad classifications (e.g., "Identify this specific type of pasta dish").

## Verification Plan
- Unit tests for `FoodScannerEngine` using mock bitmaps of various food groups.
- UI verification of the transition from food detection to the Macro Intelligence layer.
- Performance profiling to ensure real-time classification on Pixel 9 Pro XL.
