# Spam Call Block

Minimal native Kotlin Android scaffold to test GitHub Actions → APK download → phone installation. Android 10 or newer is required.

The app shows a welcome screen and a button that displays a confirmation message. **It does not screen or block calls yet**, and requests no permissions.

## Build on GitHub without Android Studio

1. Create a GitHub repository and push this entire project, including `.github/` and `gradle/`.
2. Open the repository's **Actions** tab and select **Build Android APK**.
3. The workflow runs on pushes and pull requests. You can also select **Run workflow** on the default branch.
4. Open a successful run. Under **Artifacts**, download **spam-call-block-debug** while signed into GitHub.
5. Extract the downloaded ZIP to get `app-debug.apk`.
6. Transfer the APK to your Android phone, open it, and allow installation from that source if Android asks.
7. Launch **Spam Call Block**, then tap **Test the app**.

No Android Studio installation, signing secrets, or Play Store account is needed for this debug build. The workflow installs its SDK and uses Java 17, Gradle 8.11.1, Android Gradle Plugin 8.9.2, and Android SDK 35. GitHub Actions usage is subject to your account's plan and quotas.

The APK is for testing. Fresh CI machines may generate different debug signing keys; if an update fails with a signature mismatch, uninstall the previous test app first. A stable signing key can be configured when persistent test installs are needed.

## Optional local build

With JDK 17 and Android SDK 35 installed, set `ANDROID_HOME` to your SDK directory (or set `sdk.dir` in an untracked `local.properties`), then run:

```sh
./gradlew :app:assembleDebug :app:lintDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Next step

Add local prefix rules and a `CallScreeningService`, then request the Android call-screening role. This scaffold intentionally leaves those features for the next iteration.
