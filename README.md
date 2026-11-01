# Spam Call Block — Phase 1

Native Kotlin app for Android 10+. The fixed rule rejects **all Indian mobile numbers starting with +918697**, including equivalent 10-digit, 91, 0091, and domestic 0-prefixed formats. Other prefixes and foreign country codes are allowed. Custom rules come later.

## Build and install

1. Push this project to GitHub, including `.github/` and `gradle/`.
2. Open **Actions → Build Android APK**. Pushes and pull requests trigger builds; manual **Run workflow** is also available.
3. Download **spam-call-block-debug** from a successful run's Artifacts and extract `app-debug.apk`.
4. Install on your connected, USB-debugging-authorized phone:

```sh
/Users/codewavesandy/Desktop/learning/platform-tools/adb install -r /Users/codewavesandy/Desktop/learning/app-debug.apk
```

A fresh CI machine may use a different debug signing key. If installation reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, uninstall the previous test app from the phone and install again. This clears its permissions; repeat setup below.

## Enable and test

1. Open **Spam Call Block** and tap **Enable call screening**. Select this app for caller ID / spam / call screening when Android asks. This can replace another screening app such as Truecaller.
2. Tap **Allow Contacts for saved callers** and grant permission, especially if the test number is saved. Android otherwise omits contact calls from screening. The app does not query or upload contacts; it requests the permission to receive those screening callbacks.
3. Confirm the screen says **Ready**. If permission was denied repeatedly, use **App permissions** to grant it in Settings.
4. Lock the phone and call from the other SIM beginning `8697`. The call should be rejected before ringing, with missed-call notifications suppressed. Confirm no ring, vibration, or screen wake on your actual device. A blocked entry can remain in the system call history.
5. Call from a different prefix and confirm it rings normally. Also test after closing the app and after rebooting.
6. To stop blocking, use **Default apps / disable screening** and change the caller ID / spam app selection.

Only ordinary incoming phone calls supplied by Android Telecom are screened. WhatsApp calls, hidden/unavailable numbers not supplied by Android, and carrier-generated alerts are outside this rule. The app starts no call UI, notification, background polling, or network requests.

## Development and validation

GitHub Actions uses Java 17, Gradle 8.11.1, AGP 8.9.2, and SDK 35. It runs prefix unit tests, builds the APK, and runs Android lint. Build artifacts expire after seven days.

With JDK 17 and SDK 35 installed locally:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

The matching logic has been compiled and checked locally against 18 cases using a cached Kotlin compiler. Full Android build/lint and JUnit execution run in GitHub Actions because the local machine has no Android SDK. Real-device call screening still requires the test procedure above.
