LifeTracker 1.1.1 - Security and Multi-User Preparation
======================================================

Current development build
-------------------------
LifeTracker 1.1.1 is still a local desktop development build. It does not provide remote accounts, cloud synchronization, institutional sign-in, or remote viewing yet.

What is already prepared in the data model
-------------------------------------------
- Every user receives a stable UUID in addition to the username.
- Account roles exist for Student, Teacher, Counselor, College Reviewer, and Administrator.
- Normal registration creates Student accounts only.
- Passwords are salted and hashed with PBKDF2-HMAC-SHA256. Plain passwords are not saved.
- Sharing is owner-controlled and read-only by default.
- Sharing grants can be limited to Grades, Academic History, Portfolio, and/or Goals.
- Sharing grants have expiration and revocation support.
- Invitation codes are random and only their SHA-256 hash is persisted.
- Share creation and revocation create audit entries.
- Local POSIX file permissions are restricted to the current OS user where supported.

Important current limitation
----------------------------
Academic records and portfolio metadata are still stored as local development files and are NOT fully encrypted at rest. Base64 encoding used in some storage records is not encryption. The Sharing tab prepares access rules and invitation data but does not make the information remotely accessible in 1.1.1.

Production direction for 1.1.2+ / mobile-cloud work
---------------------------------------------------
A live multi-user release should move authentication and authorization to a secure server rather than trusting the client application. Recommended production architecture:

1. Identity and authentication
   - Server-managed user identities using the stable user UUID.
   - Student self-registration where appropriate.
   - Verified institutional accounts for teachers, counselors, and college reviewers.
   - Email verification and password reset.
   - Optional MFA/passkeys.
   - Institutional SSO/OIDC support later if needed.

2. Authorization
   - Server-enforced role-based access control.
   - Student/owner consent for every external viewer.
   - Read-only access by default for teachers, counselors, and colleges.
   - Scope-limited grants instead of all-or-nothing access.
   - Expiration and immediate revocation.
   - College access can be limited to portfolio-only or academic-history-only when desired.

3. Secure storage and transport
   - TLS for all network traffic.
   - Authenticated encryption at rest such as AES-256-GCM.
   - Encryption keys held in a platform keystore or cloud KMS, not hard-coded in the app.
   - Mobile secrets stored in Android Keystore / iOS Keychain.
   - Encrypted backups and controlled retention.

4. Audit and privacy
   - Record sign-ins, share creation, share redemption, views, downloads, and revocations.
   - Give students a visible history of who accessed their records and when.
   - Minimize data shared to each recipient.
   - Provide account deletion/export workflows.
   - Review applicable FERPA/COPPA/privacy requirements before handling real school records at scale.

5. Cloud migration
   - Replace local TSV persistence with a repository/API layer backed by a database.
   - Keep the existing UUIDs, roles, sharing scopes, access-grant model, and academic data model.
   - Never rely on client-side UI hiding as authorization; permissions must be checked on the server for every protected request.

The goal of the 1.1.1 security work is to avoid redesigning the academic model when LifeTracker later becomes a phone app with real multi-user access.
