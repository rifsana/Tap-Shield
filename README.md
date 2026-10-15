# TapShield — Discreet, Offline-First Personal Safety for Android

TapShield is an offline-first personal safety Android application built with native Kotlin, Jetpack Compose, Material 3, Room, and modern Android architecture. It features a tactical, discreet everyday utility aesthetic while maintaining real-time emergency responsiveness, local-first offline event queues, and multi-channel delivery redundancy (Local SQLite persistence, Cloud real-time sync, and SMS fallback).

---

## Architectural Principles & Highlights

1. **Discreet Everyday Utility Design**:
   - Styled as an unobtrusive system hardware & network monitor in standby mode to prevent unwanted attention.
   - Built-in **Stealth Disguise Mode** transforming UI headers and labels into a diagnostics utility.
   - Distinct, high-visibility emergency radar aesthetic activated only during active SOS sessions.

2. **Offline-First Resilience**:
   - **Room Database** acts as the local source of truth for trusted contacts, SOS session logs, and durable pending events (`sos_pending_events`).
   - Every emergency trigger and location update is immediately committed to local persistent storage before attempting network or SMS transport.
   - Durable event queue designed for automated WorkManager synchronization upon network restoration.

3. **Centralized SOS Session Coordinator**:
   - Unified `SosCoordinator` controls emergency state across all triggers (In-App SOS button, Home Widget, Quick Settings Tile, and Motion Gestures).
   - Enforces strict thread-safe duplicate activation prevention so concurrent taps cannot spawn duplicate sessions.
   - 3-5 second safety countdown overlay enabling cancellation of accidental triggers.

4. **Transparent Safety & Regulatory Compliance**:
   - India Emergency Helpline **112** statutory guidance and instant 1-tap dialer integration.
   - Clear disclosure that TapShield supplements rather than replaces national emergency services.
   - Privacy-first data minimization: coordinates are never logged to production logs or shared without user-initiated authorization.

---

## Architecture Overview

```
com.tapshield.app
├── data
│   ├── local
│   │   ├── dao (ContactDao, SosSessionDao, SosEventDao)
│   │   ├── entity (ContactEntity, SosSessionEntity, SosEventEntity)
│   │   └── TapShieldDatabase
│   └── repository (ContactRepository, SosRepository)
├── domain
│   ├── coordinator (SosCoordinator)
│   ├── model (Contact, SosSession, LocationPoint, DeliveryChannel)
│   ├── state (SosState)
│   └── validation (ContactValidator)
├── di
│   ├── AppContainer
│   └── DefaultAppContainer
└── ui
    ├── components (EmergencyButton, StatusCard, CountdownOverlay)
    ├── navigation (Screen, TapShieldNavGraph)
    ├── screens (HomeScreen, ContactsScreen, ActiveSosScreen, SafetySetupScreen)
    ├── theme (Color, Theme, Type)
    └── viewmodel (HomeViewModel, ContactsViewModel, ActiveSosViewModel, SafetySetupViewModel)
```

---

## Screen Highlights

| Screen | Purpose |
|---|---|
| **Shield (Home)** | Readiness status, stealth disguise toggle, tactile tactical SOS button, offline queue telemetry, 112 emergency dialer shortcut. |
| **Contacts** | Full CRUD for trusted guardians, real-time phone number and name validation, primary guardian star toggle, and SMS alert preferences. |
| **Active SOS** | Live emergency dashboard, elapsed timer, real-time GPS coordinates (lat, lng, accuracy ±Xm), multi-channel delivery matrix, tokenized web tracking link, and safe resolution confirmation. |
| **Safety Setup** | Comprehensive permissions checklist (Fine GPS, Background Location, Notifications, SMS), battery optimization whitelist guide, widget/tile configuration guide, and shake gesture sensitivity slider. |

---

## Building and Running

### Prerequisites
- Android Studio Ladybug / Koala or CLI with Android SDK 35
- JDK 21 configured (e.g. bundled Android Studio JBR)
- Min SDK: 29 (Android 10) | Target SDK: 35 (Android 15)

### Running Unit Tests
```bash
./gradlew testDebugUnitTest
```

### Assembling Debug APK
```bash
./gradlew assembleDebug
```
The output APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Verification & Test Results
- **Unit Test Suite**: 13 tests passed (0 failures, 0 errors).
- **Core Coverage**:
  - `SosCoordinatorTest`: State machine transitions (Idle → Countdown → Active → SafeResolved), duplicate session prevention, countdown cancellation, and multi-channel delivery updates.
  - `ContactValidatorTest`: Strict validation for national/international phone numbers, sanitization, blank name handling, and boundary checks.
