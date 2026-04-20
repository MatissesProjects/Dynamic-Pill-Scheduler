# Product Guidelines: Dynamic Pill Scheduler (PHOS)

## Aesthetic & Design
- **Material You:** Strictly follow Material 3 and Material You guidelines. The UI should adapt to the user's system wallpaper colors.
- **Ambient Feedback:** Use gradients and "breathing" UI elements to represent biometric states (e.g., HR variability).
- **Pixel Watch 3 Optimization:** Utilize the high-resolution display for glanceable complications. Use the refined haptic motor to communicate without visuals.

## Interaction Patterns
- **Glanceability:** Information should be readable in < 2 seconds.
- **Contextual Friction:** Logging should be zero-tap (Complications) or one-tap (Actionable Notifications).
- **Proactive Silence:** Do not interrupt the user during detected high-focus or high-intensity activities unless a critical "Medication Collision" or safety risk is detected.

## Privacy & Security
- **Local-Only:** No biometric data or medication logs should be transmitted to a cloud server.
- **Edge Intelligence:** Use AICore/Gemini Nano for all natural language and predictive processing.
- **Encryption:** Use the Android EncryptedSharedPreferences or encrypted Room databases for sensitive storage.