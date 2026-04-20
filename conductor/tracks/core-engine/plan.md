# Track Plan: T1 Core Engine

## Specification
Implement the core temporal logic, Room database with versioning, and the collision engine.

## Implementation Steps
- [x] Initialize Android Project Structure (Multi-module)
- [x] Configure `core-data` module with Room and dependencies
- [x] Implement Room Entities with Temporal Versioning (`validFrom`, `validTo`)
- [x] Implement `T-Wake` logic and Offset Calculator
- [x] Implement "The Sponge Effect" Collision Resolver
- [ ] Unit tests for Temporal and Collision logic (Tests written, need execution environment)

## Verification Plan
- Unit tests for `T-Wake` shift calculations.
- Unit tests for `CollisionResolver` logic.
- Verify Room DB schema with temporal columns.
