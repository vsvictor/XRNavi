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

### Authentication gate and removed demo flows / Авторизація й вилучені демосценарії

The app starts at Sign In. No authentication provider is configured, so submitting the form reports
that authentication is unavailable; it does not create a session or open the Map. Guest access,
the demo profile, garage, and demo vehicle-selection flows have been removed. Registration and
recovery screens do not establish an authenticated session either. Map-dependent features remain
unavailable until a real authentication provider is configured.

Застосунок запускається на екрані входу. Постачальник авторизації не налаштований, тому
надсилання форми повідомляє про недоступність авторизації — сесію не створено, мапу не відкрито.
Гостьовий доступ, демопрофіль, гараж і сценарії вибору демонстраційного автомобіля вилучено.
Екрани реєстрації та відновлення також не створюють автентифікованої сесії. Функції, що залежать
від мапи, залишаються недоступними до підключення справжнього постачальника авторизації.

### Stop-place catalog / Каталог місць зупинки

The S19/S20 catalog uses local synthetic data and retains category filters, search, place details,
and draft-only S18 stop integration. It is not a live POI service and cannot calculate or start a
route. The catalog is currently behind the authentication gate. External POI services, GPS,
vehicle-suitability confirmation, multipoint routing, and live navigation are not connected.

Каталог S19/S20 використовує локальні синтетичні дані та зберігає фільтри категорій, пошук,
деталі місця й інтеграцію зупинок S18 лише в чернетці. Це не сервіс актуальних POI; він не
обчислює маршрут і не запускає навігацію. Наразі каталог заблокований екраном авторизації.
Зовнішні POI-сервіси, GPS, перевірка придатності для автомобіля, багатоточкова маршрутизація
та навігація наживо не підключені.

[Profile and settings verification, 2026-10-27 (Ukrainian)](docs/profile-settings-ui-2026-10-27.md)
and [S19–S20 local verification / Локальна перевірка, 2026-10-31](docs/s19-s20-local-verification-2026-10-31.md)
are historical evidence; they do not imply those flows are currently reachable from app startup.
Ці звіти є історичними доказами й не означають, що відповідні сценарії зараз доступні зі стартового екрана.

[S19–S20 local verification / Локальна перевірка, 2026-10-31](docs/s19-s20-local-verification-2026-10-31.md)
identifies the checked artifacts and separates local Android evidence from unverified platforms.
Звіт ідентифікує перевірені артефакти й відділяє локальні Android-докази від неперевірених платформ.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…