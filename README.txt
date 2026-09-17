LifeTracker 1.1.1
=================

Development build of a Java Swing academic tracking and portfolio app.
LifeTracker stays on version 1.1.1 during development. Version 1.1.2 is reserved for the user's live/release build.

Core structure in this build
----------------------------
- Registration and login with PBKDF2 password hashing
- Per-user persistent local storage under ~/.lifetracker
- Dashboard overview
- Grades workspace organized around real classes
- Click/open a class to add assignments, quizzes, tests, projects, exams, and other graded work
- Per-class percentage and letter grade calculation
- Optional assignment weighting
- Category breakdown inside each class
- Grade planning tools available from each class
- Academic History showing class results year to year
- Unweighted GPA with a 4.0 cap
- Configurable weighted GPA for Regular, Honors, AP/IB, and Dual Enrollment courses
- Academic goals
- Portfolio for Best Work and Degree/Career-Focused Work
- Portfolio metadata: description, class, year, grade, skills, reflection, file, and link
- Portfolio files can be imported into LifeTracker local storage
- Target degree/career saved to the student's academic profile
- Settings for Light, Dark, or System appearance
- Accent colors: Blue, Purple, Green, Pink, and Orange
- Configurable weighted GPA bonuses and cap
- Consent-based Sharing & Access preparation for teachers, counselors, and college reviewers
- Stable user IDs and future account roles
- Scoped, expiring, revocable read-only access grants with hashed invitation codes and audit entries
- Logout back to login

Main navigation
---------------
Dashboard
Grades
Academic History
Goals
Portfolio
Sharing
Settings

How Grades works
----------------
The Grades tab is the main academic workspace. Add a class for an academic year, then open that class to add individual work. LifeTracker calculates the current class average and letter grade from the saved work. Academic History rolls the class data upward so the student can compare results year to year.

How Portfolio works
-------------------
The student can set a target degree or career in registration or Settings. Portfolio work can be marked Best Work, Degree/Career Focused, or both. A portfolio item can include a local file or external link plus the work's description, class, academic year, grade, skills, and reflection.

Run in VS Code
--------------
1. Open the LifeTracker_V1.1.1 folder only.
2. Make sure Java is installed.
3. Open SRC/Main.java.
4. Run Main.java.
5. Create an account on the login screen.

Windows quick start
-------------------
Double-click run_lifetracker.bat. It compiles the source and then starts the app.

Storage
-------
LifeTracker saves local data in:
  C:\Users\<your-user>\.lifetracker\

No external Java libraries are required for this version.

Security note
-------------
Passwords are salted and hashed. Academic records are still local development files and are not fully encrypted at rest yet. The Sharing tab prepares the access-control model but does not enable remote viewing in 1.1.1. See SECURITY_ARCHITECTURE.md for the production/mobile plan.
