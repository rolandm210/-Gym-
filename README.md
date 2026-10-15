# /Gym\

Offline-first Android gym tracker. No coach, no account, no Firebase.

## Features
- Your own editable workout plans and exercise catalog
- Track sets, reps, weight and notes
- Workout history grouped by date
- Measurements and personal records
- Exercise performance charts
- Dark lime-green design, custom barbell icon
- Data stored locally on your device

## Build
Go to **Actions → Build Gym APK**. Download the `Gym-APK` artifact when the build finishes.

Build locally with JDK 17 and Android SDK 35:

```sh
gradle :app:assembleDebug --no-daemon
```

Package: `ee.gym.app`. This is a separate app; it doesn't overwrite JussuGoBig.

## Privacy
App data lives in Android private app storage. Uninstalling the app can remove your data. Cloud sync and backup export have not been implemented.
