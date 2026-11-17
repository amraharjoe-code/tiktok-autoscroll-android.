# TikTok AutoScroll — Android

Android native Kotlin project for building a debug APK with GitHub Actions.

## Features
- Timer Scroll: swipe after a configurable interval (5–3600 seconds).
- Auto Detect (best effort): watches TikTok accessibility labels such as "Replay"/"Watch again".
- Configurable random delay, Start/Stop, approx. 250 ms swipe.
- No server or network upload.

## Important limitations and privacy
Auto Detect depends on labels exposed by the installed TikTok version and is not guaranteed. Use Timer mode as a fallback.
The Accessibility Service can inspect visible accessibility structure in TikTok and send swipe gestures. Enable it manually only when needed. Use for personal automation and comply with applicable platform terms.

## Build APK with GitHub
1. Create a new GitHub repository, e.g. `tiktok-autoscroll-android`.
2. Upload all files/folders from this project to the repository root. Include hidden `.github` folder.
3. Open **Actions**.
4. Select **Build Android APK** and click **Run workflow** (or push to `main`).
5. Wait for the workflow to finish successfully.
6. Open the completed run and download the artifact `tiktok-autoscroll-debug-apk`.
7. Extract the artifact ZIP. It contains `app-debug.apk`.
8. Transfer APK to Android and install it. Allow installation from that source if Android asks.
9. Open the app, enable its Accessibility Service in Android Settings > Accessibility, return to app, choose mode, save settings, then press START.

## Local build
Requires JDK 17 and Gradle 8.7:
```bash
gradle assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`.
