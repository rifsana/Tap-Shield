# Android Platform & Device Limitations

This document details Android OS constraints, OEM-specific battery management, background execution limits, and hardware dependencies relevant to TapShield.

---

## 1. Foreground Service & Background Execution Limits
- **Android 14 (API 34) Foreground Service Types**:
  Foreground services must explicitly declare a type. TapShield uses `location` (`android:foregroundServiceType="location"`).
- **Background Service Starts (Android 12+ / API 31+)**:
  Starting a foreground service directly from the background is restricted unless one of the platform exemptions applies (e.g., Quick Settings tile click, high-priority notification pending intent, or companion device event). Triggers from Quick Settings tiles or Widgets initiate within allowed windows.

---

## 2. GPS & Location Acquisition
- **Cold Fix Latency**:
  Under cold-start conditions or indoors, GPS satellite acquisition (TTFF - Time to First Fix) can take between 15 and 90 seconds. Fused Location Provider supplements with Wi-Fi and cell tower triangulation (`PRIORITY_HIGH_ACCURACY`).
- **Battery Saver Mode**:
  When aggressive battery saver mode is enabled by the user or OS, GPS polling intervals may be throttled or suspended when the screen turns off. TapShield guides users in the **Safety Setup** screen to request exemption from battery optimizations (`ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS`).

---

## 3. OEM Aggressive Task Killers
Manufacturers such as Xiaomi (MIUI/HyperOS), Huawei (EMUI), Samsung (OneUI sleeping apps), and OnePlus (OxygenOS) include proprietary battery management software that may kill long-running foreground services despite persistent notifications:
- **Mitigation**: TapShield provides a dedicated guidance card in Safety Setup for disabling app hibernation and enabling auto-start permissions.

---

## 4. SMS Fallback Policies & Carrier Constraints
- **Google Play SMS Permissions (`SEND_SMS`)**:
  Google Play policy strictly restricts `SEND_SMS` to designated default SMS apps. For personal safety apps distributed on Google Play, direct background SMS dispatch requires an approved Policy Declaration or must fall back to the Android system SMS composer (`ACTION_SENDTO`) where the user confirms the dispatch.
- **No Cellular / No SIM**:
  In airplane mode, absent SIM card, or zero carrier signal, SMS dispatch fails. TapShield marks SMS delivery as `FAILED` or `SKIPPED` and keeps the event queued in Room.

---

## 5. Shake Gesture Hardware Nuances
- **Screen Off / Doze Mode**:
  Accelerometers and motion sensors may be powered down by device hardware or kernel drivers during deep sleep / Doze mode to conserve power.
- **Accidental Triggers**:
  Running, walking, or placing the phone on vibrating surfaces can produce false positives. TapShield employs configurable threshold windows (1.5G - 4.5G) and a 3-second countdown cancellation period.
