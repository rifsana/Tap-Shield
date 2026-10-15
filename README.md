# TapShield — Discreet, Offline-First Personal Safety for Android

TapShield is an offline-first, discreet personal safety Android application built with native Kotlin, Jetpack Compose, Material 3, Room SQLite, Google Play Services Location, and Android architecture components. It balances a calm, trustworthy utility design with high-availability emergency responsiveness, multi-channel alerting redundancy (Local Room persistence, Cloud real-time tracking links, and direct Cellular SMS dispatch), and system-level emergency entry points (Home-Screen AppWidget and Native Quick Settings Tile).

---

## Key Features & Highlights

1. **Refined, Product-Grade UI/UX**:
   - Built on a coherent design token scale (`AppSpacing` and `AppRadius` with soft 16dp surfaces).
   - Calm, understated Midnight Navy, Porcelain, and Sapphire Blue visual identity with an unmistakable Emergency Coral alert state.
   - Smooth tactile haptic feedback on touch (`HapticFeedbackConstants.LONG_PRESS` and `CONFIRM`).
   - Clean visual hierarchy with zero card spam or unnecessary decorative clutter.

2. **Centralized SOS Session Coordinator (`SosCoordinator`)**:
   - Single thread-safe state machine governing session lifecycle across all trigger sources (In-App Button, Home Widget, Quick Settings Tile).
   - Strict duplicate prevention: concurrent or repeated taps never spawn redundant sessions or duplicate alerts.
   - Configurable 3-second safety countdown enabling cancellation of accidental triggers.

3. **High-Precision GPS & Location Foreground Service (`SosForegroundService`)**:
   - Integrates Google Play Services `FusedLocationProviderClient` for continuous high-accuracy coordinates (`PRIORITY_HIGH_ACCURACY`).
   - Declared as `foregroundServiceType="location"` running with a persistent notification.
   - **Throttled & Deduplicated Alerts**: Features transition caching and `.setOnlyAlertOnce(true)` to prevent notification spam or vibration loops while acquiring satellite lock.
   - Actionable notification button: `"I'M SAFE (STOP)"` allows one-tap safe resolution directly from the notification tray or lock screen.

4. **Native Quick Settings Tile (`SosTileService`)**:
   - Standard Android `TileService` registered with `BIND_QUICK_SETTINGS_TILE`.
   - Accessible directly from the notification shade or lock screen.
   - Reflects live application state: `STATE_INACTIVE` ("Standby"), `STATE_ACTIVE` ("Cancel SOS (Xs)"), and `STATE_ACTIVE` ("ACTIVE • BROADCASTING").
   - Safely launches emergency UI via `startActivityAndCollapse` complying with Android 14+ background launch rules.

5. **Home-Screen Emergency Widget (`TapShieldWidgetProvider`)**:
   - Native Android `AppWidgetProvider` with `RemoteViews` compatible with Android 10–15.
   - Reactively displays real-time readiness and live countdown/active session status.
   - One-tap emergency dispatch directly from the user's home screen launcher.

6. **Cellular SMS Dispatch Engine (`AndroidSmsDispatcher`)**:
   - Production-grade SMS dispatching via Android's `SmsManager`.
   - Pre-flight checks for telephony hardware (`FEATURE_TELEPHONY`), active SIM status (`SIM_STATE_READY`), and runtime permissions (`SEND_SMS`).
   - Dispatches emergency alert messages with tokenized web tracking link (`https://tapshield.live/s/{token}`) and live GPS coordinates.
   - Transparent delivery statuses: `NOT_CONFIGURED`, `PERMISSION_REQUIRED`, `SENDING`, `SENT_TO_CARRIER`, `DELIVERED`, and `FAILED`.
   - Compliant fallback: Provides `createSystemSmsIntent` (`ACTION_SENDTO`) to open the default SMS composer with pre-filled coordinates if direct background dispatch is restricted by policy.

7. **Safety & Regulatory Compliance (India)**:
   - Statutory guidance and one-tap direct dialer for **112** (National Emergency Response Center).
   - Clear disclosures that TapShield supplements rather than replaces national emergency services.
   - Privacy-first data minimization: coordinates are never logged in standby or leaked to production Logcat.

---

## Architecture Overview

```
com.tapshield.app
├── data
│   ├── local
│   │   ├── dao (ContactDao, SosSessionDao, SosEventDao)
│   │   ├── entity (ContactEntity, SosSessionEntity, SosEventEntity)
│   │   └── TapShieldDatabase
│   ├── location (FusedLocationTracker)
│   ├── repository (ContactRepository, SosRepository)
│   └── sms (AndroidSmsDispatcher)
├── di
│   ├── AppContainer
│   └── DefaultAppContainer
├── domain
│   ├── coordinator (SosCoordinator)
│   ├── location (LocationTracker, LocationPoint, LocationResult)
│   ├── model (Contact, SosSession, DeliveryChannel, DeliveryStatus, TriggerSource)
│   ├── service (SosServiceLauncher)
│   ├── sms (SmsDispatcher, SmsDispatchResult)
│   ├── state (SosState)
│   └── validation (ContactValidator)
├── service
│   ├── AndroidSosServiceLauncher
│   ├── SosForegroundService
│   └── SosTileService
├── ui
│   ├── components (EmergencyButton, CountdownOverlay)
│   ├── navigation (Screen, TapShieldNavGraph)
│   ├── screens (HomeScreen, ContactsScreen, ActiveSosScreen, SafetySetupScreen)
│   ├── theme (Color, Theme, Type, DesignTokens)
│   └── viewmodel (HomeViewModel, ContactsViewModel, ActiveSosViewModel, SafetySetupViewModel)
└── widget
    └── TapShieldWidgetProvider
```

---

## Screen Directory

| Screen | Purpose & Features |
|---|---|
| **Dashboard (Home)** | Compact header, live readiness pill, prominent tactile SOS button, consolidated Preparedness card (Guardians, 112 dialer, Offline storage queue). |
| **Active SOS** | Emergency broadcast banner, elapsed timer, live GPS telemetry (lat/lng, ±m accuracy, satellite lock status), dynamic delivery channels card, and "I AM SAFE" resolution. |
| **Trusted Contacts** | Initials avatar cards, primary guardian star toggle, SMS dispatch preference toggle, add/edit dialog with inline validation, and empty state. |
| **Safety Setup** | Categorized permissions checklist (GPS, Notifications, SMS), battery optimization whitelist guide, Quick Settings tile setup guide, and shake gesture sensitivity slider. |

---

## Project Status & Milestones

| Milestone | Description | Status |
|---|---|---|
| **Milestone A** | **Foundation & Architecture**: Room entities, DAOs, repositories, contact CRUD, centralized SOS coordinator, unit tests. | **COMPLETED** |
| **Milestone B** | **Real Location & Foreground Service**: Fused Location Provider, Location Foreground Service, persistent notification with stop action, wake lock. | **COMPLETED** |
| **Milestone C** | **Home-Screen Widget Provider**: Native AppWidget (`TapShieldWidgetProvider`), shared SOS coordinator integration, live state push. | **COMPLETED** |
| **Milestone D** | **Native Quick Settings Tile + SOS SMS Dispatch Engine + UI Refinement**: `SosTileService`, `AndroidSmsDispatcher`, notification debouncing, design tokens. | **COMPLETED** |
| **Milestone E** | **Offline-First Durable Queue & WorkManager**: Durable Room event queue processing with exponential backoff and connectivity network listener. | Scheduled Next |
| **Milestone F** | **Online Location Sharing & Firebase**: Firebase Realtime DB sync, tokenized web tracking link viewer, session expiration & revocation. | Planned |
| **Milestone G** | **Experimental Shake Gesture Trigger**: Accelerometer monitoring, false-trigger prevention, threshold configuration. | Planned |
| **Milestone H** | **Security Hardening, End-to-End Tests & Release**: Signed App Bundle preparation, Proguard rules, Play Store release checklist. | Planned |

---

## Building and Verification

### Prerequisites
- Android Studio Ladybug / Koala or CLI with Android SDK 35
- JDK 21 (e.g. bundled Android Studio JBR)
- Min SDK: 29 (Android 10) | Compile / Target SDK: 35 (Android 15)

### Running Automated Unit Tests
```bash
./gradlew testDebugUnitTest
```
**Current Status**: 37 unit tests passing (0 failures, 0 errors).

### Assembling Debug APK
```bash
./gradlew assembleDebug
```
Output artifact location:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Physical-Device Verification Checklist

1. **Quick Settings Tile**: Pull down the notification shade, edit tiles, add **TapShield SOS**, and verify it shows "Standby". Tap to initiate 3-second countdown; tap again to cancel.
2. **Foreground Location Tracking**: Activate SOS; verify the persistent foreground notification starts with live accuracy and fix timestamp. Confirm no vibration spam occurs during satellite search.
3. **SMS Dispatch**: Add a trusted contact with SMS enabled. Activate SOS with `SEND_SMS` permission; verify the Active SOS screen reports `"SUBMITTED"` and the recipient receives coordinates.
4. **SMS Fallback**: Revoke `SEND_SMS` permission. Activate SOS; confirm the screen reports `"PERMISSION NEEDED"` and provides the `"Open System SMS App Fallback"` action.
5. **Safe Resolution**: Tap `"I AM SAFE (STOP TRACKING)"` in-app or from the notification tray action. Confirm tracking terminates, notifications dismiss, and the tile resets to "Standby".
