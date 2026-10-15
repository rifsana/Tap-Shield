# Android Platform & Device Limitations

This document details Android OS constraints, OEM-specific battery management, background execution limits, and hardware dependencies relevant to TapShield.

---

## 1. Foreground Service & Background Execution Limits
- **Android 14 (API 34) Foreground Service Types**:
  Foreground services must explicitly declare a type. TapShield uses `location` (`android:foregroundServiceType="location"`).
- **Background Service Starts (Android 12+ / API 31+)**:
  Starting a foreground service directly from the background is restricted unless one of the platform exemptions applies (e.g., Quick Settings tile click, high-priority notification pending intent, or companion device event). Triggers from Quick Settings tiles or Widgets initiate within allowed windows.
- **Notification Alert Throttling & Deduplication**:
  Ongoing foreground services that post periodic updates (e.g., GPS telemetry fixes) must declare `.setOnlyAlertOnce(true)`. Without this flag, each call to `notificationManager.notify()` with `IMPORTANCE_HIGH` causes repeated audible or vibrational alerts, creating user notification fatigue.

---

## 2. GPS & Location Acquisition
- **Cold Fix Latency**:
  Under cold-start conditions or indoors, GPS satellite acquisition (TTFF - Time to First Fix) can take between 15 and 90 seconds. Fused Location Provider supplements with Wi-Fi and cell tower triangulation (`PRIORITY_HIGH_ACCURACY`).
- **Availability State Transitions**:
  `LocationCallback.onLocationAvailability()` fires frequently when indoor reception fluctuates. TapShield caches availability transitions to prevent flood of spurious `UNAVAILABLE` errors.
- **Battery Saver Mode**:
  When aggressive battery saver mode is enabled by the user or OS, GPS polling intervals may be throttled or suspended when the screen turns off. TapShield guides users in the **Safety Setup** screen to request exemption from battery optimizations (`ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`).

---

## 3. Quick Settings Tile (`TileService`) Constraints (Android 7.0 - 15)
- **User Addition Requirement**:
  Quick Settings tiles cannot be automatically inserted into the active notification shade by an application. The user must manually edit their Quick Settings panel and pull the TapShield SOS tile into their active tray.
- **Locked Device Behavior**:
  Android restricts certain background activities when the keyguard is active. TapShield uses `startActivityAndCollapse` (with PendingIntent on API 34+) to safely transition from the locked shade to the emergency UI.
- **Tile Lifecycle**:
  `onStartListening()` and `onStopListening()` govern tile state collection. State collectors must be cleanly scoped and cancelled to prevent leaking system services.

---

## 4. OEM Aggressive Task Killers
Manufacturers such as Xiaomi (MIUI/HyperOS), Huawei (EMUI), Samsung (OneUI sleeping apps), and OnePlus (OxygenOS) include proprietary battery management software that may kill long-running foreground services despite persistent notifications:
- **Mitigation**: TapShield provides dedicated guidance in Safety Setup for disabling app hibernation and enabling auto-start permissions.

---

## 5. SMS Fallback Policies & Carrier Constraints
- **Google Play SMS Permissions (`SEND_SMS`)**:
  Google Play policy strictly restricts `SEND_SMS` to designated default SMS apps and personal safety apps with an approved Permissions Declaration Form. Direct background SMS dispatch is attempted only if runtime permission is granted.
- **Intent Fallback (`ACTION_SENDTO`)**:
  If `SEND_SMS` is not granted, TapShield provides a pre-filled system SMS intent fallback (`smsto:`) so users can dispatch coordinates with a single tap in their preferred SMS app.
- **No Cellular / No SIM**:
  In airplane mode, absent SIM card, or zero carrier signal, SMS dispatch fails. TapShield marks SMS delivery as `FAILED` or `SKIPPED` and keeps the event queued in Room.
- **Delivery Receipts**:
  Carrier delivery confirmations depend on SMSC support and cellular network coverage. TapShield records submission to the radio as `SENT` and only reports `DELIVERED` when a genuine carrier delivery receipt is received.

---

## 6. Shake Gesture Hardware Nuances
- **Screen Off / Doze Mode**:
  Accelerometers and motion sensors may be powered down by device hardware or kernel drivers during deep sleep / Doze mode to conserve power.
- **Accidental Triggers**:
  Running, walking, or placing the phone on vibrating surfaces can produce false positives. TapShield employs configurable threshold windows (1.5G - 4.5G) and a 3-second countdown cancellation period.

---

## 7. Home-Screen Widget Constraints (Android 10 - 15)
- **Lock-Screen Widgets**:
  Android removed universal lock-screen widgets starting with Android 5.0 Lollipop. Widgets reside on the user's home screen launcher. For locked-screen quick activation, the Android Quick Settings tile (`TileService`) is the platform-compliant mechanism.
- **Background Broadcast Limits (Android 8.0+ / API 26+)**:
  Widget clicks utilize explicit pending intents (`PendingIntent.getBroadcast` with explicit component), ensuring widget actions reliably trigger even if the app process is currently stopped or suspended.
- **Update Frequency & Battery Conservation**:
  `updatePeriodMillis` is set to `0` to prevent unnecessary scheduled battery drain. TapShield drives widget updates reactively via high-efficiency local broadcasts whenever `SosCoordinator.state` changes.
