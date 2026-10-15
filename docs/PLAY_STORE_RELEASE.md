# Google Play Store Release & Compliance Checklist

This document details Google Play requirements, permission policy compliance, release bundle generation, and disclosure checklists for TapShield.

---

## 1. App Bundle Generation & Signing
To build the signed release bundle for Google Play Console:
```bash
./gradlew bundleRelease
```
Output artifact: `app/build/outputs/bundle/release/app-release.aab`

> **Security Note**: Never commit signing keystores (`*.jks`, `*.keystore`) or passwords to version control. Set `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD` via environment variables or local `keystore.properties` (ignored in `.gitignore`).

---

## 2. Sensitive Permissions & Google Play Policy

### A. Location Permissions (`ACCESS_BACKGROUND_LOCATION`)
- **Policy**: Google Play requires a prominent in-app disclosure before requesting background location.
- **Compliance**: Implemented in `SafetySetupScreen.kt` clearly explaining why background location is required (so trusted contacts receive uninterrupted updates when the phone is locked).
- **Video Submission**: Google Play review requires a short video demonstrating background location tracking during an active SOS session.

### B. SMS Permissions (`SEND_SMS`)
- **Policy**: Google Play's SMS and Call Log policy permits `SEND_SMS` only for specific core features (e.g., Default SMS handler, Physical safety / personal safety apps).
- **Declaration**: If requesting `SEND_SMS` on Google Play, submit the **Permissions Declaration Form** categorizing TapShield under *Physical safety / emergency alert*.
- **Store Alternative**: If the declaration is rejected or distribution is standard consumer, enable the prefilled system SMS composer (`ACTION_SENDTO`) which does not require the high-risk permission.

---

## 3. Data Safety Section Declarations
When filling out the Google Play Data Safety form:
- **Location**: Collected, ephemeral, shared with user-selected trusted contacts during emergency sessions.
- **Contacts**: Stored locally in encrypted Room database; not synchronized to third-party ad networks or brokers.
- **Data Deletion**: Users can delete contacts, clear session history, or revoke emergency share links at any time.

---

## 4. Release Verification Checklist
- [x] Unit test suite passed (`./gradlew testDebugUnitTest` - 13/13 passed)
- [x] Debug APK build and resource linking verified (`./gradlew assembleDebug`)
- [ ] Physical device smoke test (Screen-locked location updates)
- [ ] Signed App Bundle generated (`./gradlew bundleRelease`)
- [ ] Proguard / R8 optimization tested without reflection breaks
