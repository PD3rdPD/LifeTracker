# LifeTracker LifeTracker Port

This version keeps the existing LifeTracker classes and adds the LifeTracker calculator logic from the Python version.

## Calculator features now present

- Standard average calculation
- A-F letter grade conversion
- Standalone extra-credit points
- Comma-separated grade parsing and validation
- Negative-grade validation
- Optional grades over 100
- Weighted category calculation
- 100% total-weight validation
- Grade Goal Prediction
- Grade Path Planning
  - Consistent
  - Strong Finish
  - Strong Start
  - Buffer
- Impossible-target handling through an empty path result
- Already-secured target handling
- Academic progress feedback
- Above/below/exactly-at-target feedback
- Existing assignment-level LifeTracker percentage and weighted calculations retained

## New/updated Java files

- `SRC/grades/GradeCalculator.java`
- `SRC/grades/WeightedCategory.java`
- `SRC/grades/GradePathPlanner.java`
- `SRC/grades/GradeService.java`
- `SRC/grades/Grade.java`
- `SRC/grades/CalculatorFeatureTest.java`

## Run the calculator tests

From the `SRC` directory:

```text
javac $(find . -name "*.java")
java grades.CalculatorFeatureTest
```

Expected result begins with:

```text
All LifeTracker calculator feature tests passed.
```

## Still to connect later

The calculation engine is now present, but these Python app features still need to be wired into a Java user interface/application layer:

- Standard/Weighted mode controls
- Extra-credit toggles and inputs
- Grade Goal input/result display
- Grade Path display
- Clear/Reset UI behavior
- CSV export
- Multiple languages
- Light/dark mode
- Accessibility/larger text
- Saved language/preferences

The LifeTracker GPA, classes, accounts, and academic-goal work remains in place.
