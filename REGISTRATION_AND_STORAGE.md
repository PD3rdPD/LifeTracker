# LifeTracker 1.1.1 Registration and Storage

LifeTracker 1.1.1 includes working local registration, login, and persistent student data without external libraries.

## Registration
- Name
- Unique username
- Education level
- Password + confirmation
- Password minimum: 8 characters
- Passwords are hashed with PBKDF2WithHmacSHA256 and a random salt.
- Plain-text passwords are never written to storage.

## Stored data
Saved under the user's home folder in `.lifetracker`:
- `users.tsv`
- `<username>_classes.tsv`
- `<username>_grades.tsv`
- `<username>_goals.tsv`
- `<username>_gpa_records.tsv`

## App flow
Main → Login → Registration (optional) → LifeTracker Dashboard

Dashboard areas:
- Dashboard
- Classes
- Calculator
- GPA
- Academic Goals
- Storage

## GPA behavior
- Unweighted GPA remains capped at 4.0.
- Weighted GPA is configurable in the GPA screen.
- Default bonuses: Honors +0.5, AP/IB +1.0, Dual Enrollment +1.0.
- Default weighted cap: 5.0.
- Change those settings to match a specific school.

## Future production storage
For a larger public desktop release, migrate local TSV storage to SQLite with JDBC. For cross-device accounts and sync, use a backend service/database with account recovery and secure authentication.
