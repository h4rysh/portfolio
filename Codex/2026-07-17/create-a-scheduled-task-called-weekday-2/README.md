# RemindCare

An offline-first Android reminder app for patients and caregivers. It uses no API keys, paid service, cloud subscription, Firebase, or network backend.

## Open and run

Open this directory in Android Studio, allow Gradle to sync, then run the `app` configuration on an Android 8.0+ device or emulator. The first screen lets you choose Patient or Caregiver. Patient mode includes a few local sample reminders.

## Included

- Kotlin, Jetpack Compose, Material 3 and MVVM
- Room database for profiles, reminders and completion history
- Local exact alarms, persistent high-priority full-screen alarm notification, sound and vibration
- Snooze and completion actions; three 5-minute retry attempts before a missed status; rescheduling after boot
- Patient and caregiver dashboards, local pairing-code simulation, reminder editor and history
- Optional local reminder image and camera proof stored in application storage

## Architecture

`data/` contains Room models, DAOs and repository; `alarm/` owns scheduling/notifications; `ui/` owns Compose screens; `CareViewModel` coordinates UI state. A future sync implementation can be added behind the repository without replacing screens or local storage.

## Platform notes

Android users must approve notification, camera (when required), and exact-alarm permissions where Android exposes them. Full-screen alarm behavior is ultimately controlled by the device's notification and battery policy.
