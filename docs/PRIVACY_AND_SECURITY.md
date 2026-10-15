# TapShield — Privacy & Security Architecture

This document outlines data flows, access controls, token lifecycles, and privacy safeguards implemented in TapShield.

---

## 1. Privacy Principles & Data Minimization

- **No Routine Tracking**:
  TapShield never collects, logs, or transmits location data in standby / idle state. Location sensors are activated *only* during an explicitly triggered SOS session (Countdown or Active).
- **No Coordinate Logging in Production**:
  Precise geographic coordinates (latitude, longitude, altitude) are strictly scrubbed from Logcat and standard debug logs to prevent leakages to third-party log capture services.
- **Local-First Data Ownership**:
  Contact lists, user preferences, and offline event histories remain inside the app's encrypted/sandboxed private Room database (`tapshield_database.db`).

---

## 2. Emergency Session Tokens & Scoped Sharing

- **Cryptographic Random Tokens**:
  Every active emergency session generates a unique, unguessable 16-character alphanumeric session token (e.g., `https://tapshield.live/s/{token}`).
- **Session Expiration & Revocation**:
  When the user taps "I Am Safe" or cancels the session, the session token is marked expired, access to live telemetry is severed, and remote database nodes are updated to report `RESOLVED_SAFE`.
- **Zero Account Wall for Responders**:
  Trusted contacts opening the tracking link do not need to install an app or create an account; access is gated purely by the temporary emergency token.

---

## 3. Permissions Transparency & Justification

| Permission | Android API Level | Purpose & Justification |
|---|---|---|
| `ACCESS_FINE_LOCATION` | All | Acquires precise GPS satellite coordinates during active SOS. |
| `ACCESS_COARSE_LOCATION` | All | Fallback location triangulation via Wi-Fi/cellular towers. |
| `ACCESS_BACKGROUND_LOCATION` | API 29+ | Allows tracking to continue if user navigates away or screen locks. |
| `POST_NOTIFICATIONS` | API 33+ | Required to post the persistent foreground service notification. |
| `FOREGROUND_SERVICE_LOCATION` | API 34+ | Mandatory declaration for Android 14+ location foreground services. |
| `SEND_SMS` | All | Emergency SMS alerts to primary guardians when data connectivity is absent. |

---

## 4. Emergency Helpline Guidance (India)
TapShield explicitly states on the Home and Safety Setup screens:
> **Notice**: TapShield supplements and does not replace official national emergency response services. In life-threatening emergencies, dial **112** directly.
