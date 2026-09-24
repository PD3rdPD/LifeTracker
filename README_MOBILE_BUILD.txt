LifeTracker 1.1.4 Mobile-First Functional Build

This build replaces the legacy pop-up workflow inside the signed-in application with single-window navigation.

Navigation behavior:
- Bottom navigation changes the content inside the same LifeTracker window.
- Detail/form screens use a Back button.
- Back discards unsaved form changes.
- Data is written only when an explicit Save button is pressed.

Implemented in-app flows:
- Classes -> Add Class -> Class Detail -> Add/Edit Grade -> Grade Planning
- Assignments -> Add/Edit Assignment
- Goals -> Add Goal
- SAT -> Setup/Edit SAT readiness
- Portfolio -> Add Portfolio Item
- History
- Settings / Accessibility

Input controls use dark, visible mobile-style fields rather than default white desktop fields.
