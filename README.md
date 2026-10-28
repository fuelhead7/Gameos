# GameOS Controller

Android accessibility-based calibration companion for Samsung Good Lock / Home Up DIY Home.

## v0.1
- Open accessibility settings and request user opt-in.
- Save screen display dimensions.
- Test one delayed gesture.
- Launch Samsung home screen.
- Build a debug APK automatically on GitHub Actions.

**Not yet implemented:** drag-and-drop layout mapping, moving app icons, auto-configuring Good Lock, or restoring a saved layout.

Accessibility service performs gestures only when explicitly requested; it doesn't inspect application content or collect data.

## Install
Open GitHub Actions > Build Android APK > successful run > artifact GameOS-Controller-debug. Extract the ZIP and install app-debug.apk. Android may require permission to install APKs from the chosen file manager.

If Samsung blocks the accessibility service as a restricted setting for a sideloaded app, review the app details and Android's additional confirmation procedure before enabling it. Only grant accessibility access to code you trust.
