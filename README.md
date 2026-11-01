# Quietly

A minimal Android call blocker with on-device rules. Android 10 or newer.

## Behavior

- **Fresh installs and upgrades from the fixed-prefix prototype start with protection off and no rules.** The old `8697` test block is removed.
- The master protection switch activates all enabled rules. Turning it off allows calls without deleting rules.
- Create, edit, pause, resume, or delete individual rules. Changes persist locally, including across restarts.
- An incoming call is rejected if **any enabled rule matches** while protection is on.
- Matching calls are rejected before ringing, with missed-call notifications suppressed. System call history may still contain blocked entries.
- No backend, account, network access, contact queries, or uploads.

## Matching rules

| Type | Example | Meaning |
| --- | --- | --- |
| Starts with | `+91140` | Any international number starting with these digits |
| Ends with | `1234` | Any number ending in these digits |
| Exact number | `+918697123456` | This complete number only |
| Contains | `8697` | These consecutive digits anywhere in the number |
| Pattern | `8697??????` | `8697` followed by exactly six digits |
| Pattern | `+91140*` | `+91140` followed by any number of digits |

Spaces, hyphens, and parentheses are ignored. `+` is allowed only at the start. A rule beginning with `+` matches an incoming international number including its country code. For rules without `+`, an Indian incoming `+91` number with ten national digits is matched using its national digits. Other country codes are preserved. For example, `8697` matches the start of `+918697123456`, but not `+448697123456`. There is no inferred country code for an incoming national number: use a local rule if your carrier supplies numbers without `+`.

Wildcard patterns match the whole number. `?` matches one digit; `*` matches zero or more digits. Patterns must include at least one digit. Arbitrary regular expressions are not supported. Exact rules require 7–15 digits; all rules are limited to 32 characters. Broad prefixes can block many legitimate callers, so use **Try a number** before enabling protection.

## Build and install

1. Push this project to GitHub, including `.github/` and `gradle/`.
2. Open **Actions → Build Android APK**. Pushes and pull requests trigger builds; **Run workflow** also works.
3. Download **spam-call-block-debug** from the successful run's Artifacts and extract `app-debug.apk`.
4. Install on the USB-debugging-authorized phone:

```sh
/Users/codewavesandy/Desktop/learning/platform-tools/adb install -r /Users/codewavesandy/Desktop/learning/app-debug.apk
```

Fresh CI machines may use different debug signing keys. If installation reports `INSTALL_FAILED_UPDATE_INCOMPATIBLE`, uninstall the previous test app and install again. Uninstalling clears rules and permissions. A stable signing key is needed for reliable upgrades that preserve data.

## Set up and test

1. Open **Quietly**. No calls are blocked initially.
2. Create a **Starts with** rule containing `+918697` for the test phone.
3. Turn on protection. Select Quietly as the default caller ID / spam screening app when Android asks. Your normal Phone app remains your dialer; another screening provider may be replaced.
4. Allow **Contacts** access to include saved callers. Android otherwise omits them from screening. Quietly does not read your address book. If permission was denied repeatedly, use **Settings → Manage permissions**.
5. Use **Try a number** to preview the result. It checks the current master switch, selected screening role, and enabled rules. It cannot simulate carrier behavior or determine whether an entered number is a saved contact.
6. Lock the phone and call from the matching phone. Confirm no ringing, vibration, or screen wake on the actual device.
7. Test a nonmatching caller, pause a rule, and pause protection: these calls should ring normally. Reopen the app and reboot the phone to confirm preferences persist.
8. Use **Settings → Default apps** to remove Quietly as the screening provider if needed.

Only ordinary incoming calls supplied by Android Telecom are screened. WhatsApp calls and hidden/unavailable callers omitted by Android cannot be matched. Carrier alerts and manufacturer-specific call UI require real-device verification.

## Development and validation

The app uses native Android views, system typography, warm white cards, and violet accents without adding UI framework dependencies. Rules are stored in app-private SharedPreferences. The service reads the latest saved rules on each call and responds locally, without launching any UI.

GitHub Actions uses Java 17, Gradle 8.11.1, AGP 8.9.2, and SDK 35. It runs JUnit rule-engine tests, builds the APK, and runs lint. Build artifacts expire after seven days.

With JDK 17 and SDK 35 installed:

```sh
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

The engine was compiled locally with a cached Kotlin compiler and passed 2,994 checks, including randomized wildcard comparisons. Android resource XML and workflow YAML were parsed. Full Android compilation/lint, UI rendering, and device integration remain for GitHub Actions and the phone: this Mac has no Android SDK installed.
