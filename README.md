This is a Kotlin Multiplatform project targeting Android, iOS.

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

### Verification evidence

[Map contracts and iOS launch verification, 2026-10-27 (Ukrainian)](docs/verification-2026-10-27.md)
records the tested builds, local evidence, and remaining physical-device/accessibility blockers.

### Local demo profile and settings

The Map navigation opens a demo-only profile (S08) and settings (S07), not an authenticated account.
System/Light/Dark appearance is shared by every screen and stored locally using Android
SharedPreferences or iOS UserDefaults under `appearance.theme`. No credentials or session are
stored with it. Unconnected services and organization policies are informational, not working switches.
The theme row opens a System/Light/Dark picker. Profile and settings select one of four
window-size-driven layouts, with compact navigation in phone landscape; demo disclosures
remain visible instead of copying authenticated or connected-service states from the designs.

[Profile and settings verification, 2026-10-27 (Ukrainian)](docs/profile-settings-ui-2026-10-27.md)
records the checks and remaining platform limitations.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…