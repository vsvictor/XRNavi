# XRNavi

**EN.** XRNavi is a Kotlin Multiplatform / Compose Multiplatform navigation app for Android and iOS,
aimed at drivers of trucks and other heavy vehicles ("XR — a new interaction format"). It combines
a Mapbox route planner, a vehicle garage, stop-place search and a full account flow (sign-in,
registration, password recovery) on top of a JSON-RPC 2 backend.

**UK.** XRNavi — навігаційний застосунок на Kotlin Multiplatform / Compose Multiplatform для Android
та iOS, призначений для водіїв вантажівок і великогабаритного транспорту («XR — новий формат
взаємодії»). Він поєднує планувальник маршрутів Mapbox, гараж транспортних засобів, пошук місць
зупинки та повний потік облікового запису (вхід, реєстрація, відновлення пароля) поверх
JSON-RPC 2 бекенду.

## Features / Можливості

| EN | UK |
|----|----|
| Sign-in, registration, password recovery with a six-digit emailed code; adaptive portrait/landscape layout with a shared hero | Вхід, реєстрація, відновлення пароля шестизначним кодом з листа; адаптивний portrait/landscape-макет зі спільним hero |
| OAuth 2 session lifecycle with secure token storage (Android Keystore, iOS Keychain) | Життєвий цикл OAuth 2-сесії із захищеним сховищем токенів (Android Keystore, iOS Keychain) |
| Map (S03): Mapbox geocoding, driving directions, geolocation states | Мапа (S03): геокодування Mapbox, маршрути, стани геолокації |
| Garage and vehicle wizard; local stop-place catalog; profile and theme settings | Гараж і майстер додавання авто; локальний каталог місць зупинки; профіль і теми |
| Connection indicator (QUIC / TLS / TCP) | Індикатор з'єднання (QUIC / TLS / TCP) |

## Architecture / Архітектура

**EN.** State management is BLoC everywhere: each screen has its own `XScreen`, `XViewModel` and
`XBloc` (state/events in `XState`) in separate files; dependencies are injected via
`AppDependencies` (no singletons). The backend is the Rust server `JSON-RPC-2-OAuth2` (deployed with
the `BRNavi` Docker project); the app talks to it through the
[JSON-RPC-2-Client-KMP](https://github.com/vsvictor/JSON-RPC-2-Client-KMP) library, included as a Git
submodule. Docs are kept in English and Ukrainian.

**UK.** Керування станом — BLoC скрізь: кожен екран має окремі `XScreen`, `XViewModel` і `XBloc`
(стан/події в `XState`) в окремих файлах; залежності передаються через `AppDependencies`
(без singleton). Бекенд — Rust-сервер `JSON-RPC-2-OAuth2` (розгортається проєктом Docker `BRNavi`);
застосунок спілкується з ним через бібліотеку
[JSON-RPC-2-Client-KMP](https://github.com/vsvictor/JSON-RPC-2-Client-KMP), підключену як Git submodule.
Документація ведеться англійською та українською.

## Project layout / Структура проєкту

* [/shared](./shared/src) — shared Compose UI, BLoCs, ViewModels and services (`commonMain`); platform code in `androidMain` and `iosMain`. / спільний UI, BLoC, ViewModel і сервіси; платформний код в `androidMain` та `iosMain`.
* [/androidApp](./androidApp) — Android entry point and instrumented tests. / точка входу Android та інструментальні тести.
* [/iosApp](./iosApp/iosApp) — iOS host app (SwiftUI wrapper, Mapbox view factory) and XCUITests. / iOS-хост (SwiftUI, фабрика Mapbox) та XCUITest.
* [/external](./external) — `JSON-RPC-2-Client-KMP` submodule. / submodule клієнта JSON-RPC.
* [/docs](./docs) — design, verification and screen reports. / звіти про дизайн, перевірки й екрани.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

The iOS application deployment target is iOS 16.0 in Debug and Release, including
the shared native framework build. The UI-test target requires iOS 17.0 because
the installed Xcode XCTest libraries have that minimum; building the application
for iOS 16 does not establish device compatibility until installation and runtime
checks succeed. On iOS 16, use a compatible older Xcode for XCTest or perform
device checks without the current XCTest runner.

Мінімальна версія iOS застосунку — 16.0 у Debug і Release, включно зі збіркою
спільного нативного фреймворку. UI-тести вимагають iOS 17.0 через мінімальну версію
бібліотек XCTest установленого Xcode; успішна збірка для iOS 16 не підтверджує
сумісність пристрою без встановлення та перевірки виконання. На iOS 16 використовуйте
сумісний старіший Xcode для XCTest або перевіряйте пристрій без поточного runner.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

### Updated Figma screens / Екрани оновленого дизайну Figma

The main design page has 46 in-scope screen IDs, including native splash S00 and the
collapsed/partial/expanded S03 map. Annotations about future S05 and XRDisp S32 are not
implemented navigation screens. The shared UI adds company settings (S09), invitations (S12),
trip summaries (S13), vehicle catalog/model selection (S14–S15), unverified routes (S17),
offline maps (S21), tasks/details/history (S25–S27), privacy/deletion (S28–S29), diagnostics
(S31), route explanations (S38), and location education (S46). Existing vehicle details S11
remain available; the catalog also has a local editable vehicle-parameter draft. Stop-place
list/details S19–S20 reuse the local catalog and route-stop BLoC with adaptive layouts.

На основній сторінці дизайну є 46 ідентифікаторів екранів поточного scope, включно з нативним
Splash S00 і трьома станами панелі мапи S03. Примітки про майбутні S05 та XRDisp S32 не є
реалізованими навігаційними екранами. Додано компанію (S09), запрошення (S12), підсумок
поїздки (S13), каталог і вибір моделі (S14–S15), неперевірений маршрут (S17), офлайн-мапи
(S21), завдання, деталі рейсу та історію (S25–S27), приватність і видалення (S28–S29),
діагностику (S31), пояснення маршруту (S38) та пояснення геолокації (S46). Наявні деталі
автомобіля S11 збережено; у каталозі також є локальна редагована чернетка параметрів.
Список і деталі зупинок S19–S20 використовують наявний демокаталог і BLoC зупинок,
з адаптивним компонуванням.

Open the catalog from Garage's add button, company/diagnostics/offline maps from Settings,
and tasks/history/invitations/privacy from Profile. Map help opens a sample route explanation;
a long press opens the **sample**, not geocoded, selected-point screen S35. Diagnostics has an
explicit local state gallery for location states S48–S50, search states S51–S53, and route
building/failure S54–S55. Location education also links to the acquisition example.
`AdditionalScreensBloc` owns these selections, back-stack transitions, consent, and notices;
the application ViewModel owns an instance and passes its state and event handler to the UI.
Returning to an existing destination
unwinds the stack instead of accumulating navigation cycles.

Каталог відкривається кнопкою додавання в гаражі; компанія, діагностика й офлайн-мапи —
з налаштувань; завдання, історія, запрошення та приватність — з профілю. Довідка мапи
відкриває приклад пояснення маршруту; довге натискання — **приклад** вибраної точки S35,
а не геокодовану координату натискання. У діагностиці є явно позначена локальна галерея
станів геолокації S48–S50, пошуку S51–S53 і побудови/помилки маршруту S54–S55.
З пояснення геолокації також можна відкрити приклад визначення місця.
Вибором, переходами, згодами й повідомленнями керує `AdditionalScreensBloc`; ViewModel
володіє окремим екземпляром і передає його стан та обробник подій у UI.
Повернення до вже відкритого екрана скорочує стек,
а не накопичує цикли навігації.

These new flows are explicitly local demonstrations, not backend integrations. Invitations do
not change membership; deleting an account does not delete the session or data; vehicle drafts
are not saved to the garage; map downloads, exports, dispatch, GPS requests, and routing do not
run. Unsupported actions display an explanation. Schematics are not navigation maps, sample
dimensions and ETA are not verified, and simulated loading does not imply an active request.
The connection dot still reflects the real injected transport BLoC independently of demo UI.

Нові сценарії є явно позначеними локальними демонстраціями, а не інтеграціями з сервером.
Запрошення не змінюють членство, видалення не видаляє сесію чи дані, чернетка автомобіля
не зберігається в гараж. Завантаження мап, експорт, дії диспетчера, GPS-запити та побудова
маршрутів не виконуються. Непідключені дії показують пояснення. Схеми не є навігаційними
мапами, габарити й ETA із прикладів не підтверджені, а демонстраційний стан завантаження
не означає активний запит. Індикатор з’єднання незалежно від демо-UI показує справжній
стан переданого транспортного BLoC.

Android device rendering tests cover the 45 Compose screen IDs plus map-sheet variants and
the additional S11 catalog draft: 384 PNG captures at 402×874, 874×402, 834×1194, and
1120×768 in light/dark themes. They verify rendering, exact dimensions, and opaque backgrounds,
**not pixel equivalence with Figma, complete interaction coverage, or iOS-device fidelity**.
S00 is a native launch screen and is not included in this Compose capture matrix.
The test APK has its own capture activity and does not change OS theme, resolution, or rotation.
Gradle connected tests remove that APK and its output directory afterward; to retain images,
install `shared/build/outputs/apk/androidTest/shared-androidTest.apk` with `adb`, run the
instrumentation classes directly, pull `design-captures` and `existing-captures` from
`/sdcard/Android/data/com.mobilespace.xrnavi.shared.test/files/`, then uninstall only
`com.mobilespace.xrnavi.shared.test`.

Рендер-тести Android охоплюють 45 Compose-екранів, варіанти панелі мапи та додаткову
чернетку каталогу S11: 384 PNG у розмірах 402×874, 874×402, 834×1194 та 1120×768,
у світлій і темній темах. Вони перевіряють рендеринг, точні розміри й непрозоре тло,
**але не піксельну тотожність Figma, всі взаємодії чи відповідність на iOS-пристрої**.
S00 — нативний екран запуску, він не входить до Compose-матриці. Тестовий APK має власну
Activity й не змінює системну тему, роздільність або орієнтацію. Gradle видаляє цей APK
і його каталог результатів після connected-тесту. Щоб зберегти знімки, встановіть
`shared/build/outputs/apk/androidTest/shared-androidTest.apk` через `adb`, запустіть класи
instrumentation безпосередньо, скопіюйте `design-captures` та `existing-captures` із
`/sdcard/Android/data/com.mobilespace.xrnavi.shared.test/files/`, після чого видаліть лише
`com.mobilespace.xrnavi.shared.test`.

Physical-iPad validation uses `AdditionalScreensUITests`: two scenarios capture 164 PNG
screenshots in light/dark app themes and portrait/landscape orientations. Coverage includes
the supplemental destinations and state gallery, sign-in/registration/recovery S01–S02
and S41–S45, location education S46, and stop lists/details S18–S20. Assertions check
foreground activation, navigation/back-stack return, consent guards, unavailable-action
notices, and the connection indicator's 5-point size and 8-point top/right safe-area
insets on every capture. A bounded repeat run of the current suite on physical iPad
passed both scenarios (2 passed, 0 failed) and retained another 164 screenshots for
visual review. The tests restore the recorded app theme and
orientation, leave demo mode, and return to Home; they do not submit credentials, request
location permission, or confirm destructive actions. Screenshots must be EXIF-normalized
before visual review. This is **not the complete 45-screen iOS matrix or an iPhone check**.
Native S00 was observed during a light-system launch, but its full theme/orientation
matrix and pixel equivalence with Figma remain unverified.

The physical iPhone 17 Pro run retained 102 supplemental-screen PNG captures before
the connection to the test runner was lost. These partial captures cover both app
themes and orientations; that run did not complete either scenario. A subsequent
bounded rerun passed the authentication/location/stop-place scenario and retained
44 screenshots across both themes and orientations. The supplemental-screen rerun
lost its connection to `testmanagerd` before capturing screenshots, so the complete
iPhone matrix remains unverified. The indicator assertions use the safe drawing area's
top/right edges: on a notched iPhone in landscape, the physical-screen inset includes
the additional safe-area inset. UI automation scrolls within the preview dialog,
checks full control visibility, and distinguishes a side rail from a bottom bar.
If UI Automation was manually enabled for testing, restore its original setting
afterward; interrupted tests may require manual app-theme/orientation restoration.

На фізичному iPhone 17 Pro збережено 102 PNG додаткових екранів до втрати зв'язку
з test runner. Часткові знімки охоплюють обидві теми й орієнтації; той запуск не завершив
жодного сценарію. Наступний прогін з обмеженням часу успішно завершив сценарій
авторизації/геолокації/зупинок і зберіг 44 знімки в обох темах та орієнтаціях.
Повторний прогін додаткових екранів утратив зв'язок із `testmanagerd` до збереження
знімків, тому повна iPhone-матриця залишається неперевіреною.
Відступи індикатора перевіряються від верхнього/правого країв безпечної області:
на iPhone з вирізом у landscape фізичний відступ також включає системний safe-area
inset. UI-автоматизація прокручує вміст усередині діалогу preview, перевіряє повну
видимість елементів і відрізняє бічну навігацію від нижньої панелі. Якщо UI Automation
вмикалася вручну для перевірки, після неї відновіть початкове значення; перервані тести
можуть потребувати ручного відновлення теми застосунку й орієнтації.

Перевірка на фізичному iPad використовує `AdditionalScreensUITests`: два сценарії зберігають
164 PNG у світлій/темній темах застосунку та портретній/альбомній орієнтаціях. Охоплено
додаткові екрани й галерею станів, вхід/реєстрацію/відновлення S01–S02 і S41–S45,
пояснення геолокації S46 та списки/деталі зупинок S18–S20. Перевіряються активність
застосунку, навігація й повернення зі стека, обов’язкова згода, повідомлення про
непідключені дії та індикатор з’єднання; на екранах авторизації й зупинок також перевіряються
його розмір 5 пунктів і правий відступ 8 пунктів. Поточний набір перевіряє розмір
і верхній/правий відступи 8 пунктів від safe area на кожному знімку. Повторний прогін
з обмеженням часу на фізичному iPad успішно завершив обидва сценарії (2 успішні,
0 помилок) та зберіг ще 164 знімки для візуального огляду. Тести відновлюють записані тему й
орієнтацію, виходять із деморежиму та повертаються на домашній екран; облікові дані
не надсилаються, дозвіл геолокації не запитується, руйнівні дії не підтверджуються.
Перед візуальним оглядом знімків потрібно застосувати їхню EXIF-орієнтацію.
Це **не повна iOS-матриця 45 екранів і не перевірка iPhone**. Нативний S00 спостерігався
під час запуску зі світлою системною темою, але повна матриця його тем/орієнтацій
і піксельна тотожність Figma залишаються неперевіреними.

The physical-device capture results above are historical; `AdditionalScreensUITests`
has been retired because its scenarios depended on the removed guest/demo entry.
Current iOS UI coverage is in `RegistrationFlowUITests`, `AutoLoginUITests`, and
`RecoveryFlowUITests`. Additional registration and demo-only profile, garage, and map
scenarios must be redesigned for the supported authentication flow before they are reintroduced.

Наведені вище результати фізичних пристроїв є історичними; `AdditionalScreensUITests`
вилучено, оскільки його сценарії залежали від видаленого входу гостем/у деморежим.
Поточне UI-покриття iOS міститься в `RegistrationFlowUITests`, `AutoLoginUITests` та
`RecoveryFlowUITests`. Додаткові сценарії реєстрації й демопрофілю, гаража та мапи потрібно
переробити для підтримуваного потоку авторизації, перш ніж повертати їх.

Run on an unlocked, connected physical device; record its original system settings first.
Restore and verify any externally changed settings afterward. Use a fresh result-bundle path.

Запускайте на розблокованому підключеному фізичному пристрої; спочатку запишіть його
початкові системні налаштування. Після перевірки відновіть і перевірте всі параметри,
змінені поза тестами. Для результатів використовуйте новий шлях.

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination "platform=iOS,id=$IOS_DEVICE_UDID" \
  -only-testing:iosAppUITests/RegistrationFlowUITests \
  -parallel-testing-enabled NO -collect-test-diagnostics never \
  -resultBundlePath /tmp/xrnavi-ipad-ui.xcresult test
```

### Backend connection (JSON-RPC client) / Підключення до бекенду (клієнт JSON-RPC)

Every screen displays a 5 dp connection dot at the top-right corner of the safe drawing
area, 8 dp from its top and right edges, clear of system bars and display cutouts. Green means QUIC,
blue TLS, cyan TCP, and red disconnected or connection error. An injected `ConnectionBloc`
tracks verified pong responses, recovery, and transport changes;
the indicator does not send additional pings.

На кожному екрані є індикатор з'єднання діаметром 5 dp у правому верхньому
кутку безпечної області, за 8 dp від її верхнього та правого країв, поза системними
панелями й вирізами дисплея. Зелений означає QUIC, синій — TLS, голубий — TCP, червоний — відсутність
з'єднання або його помилку. Переданий через DI `ConnectionBloc` відстежує початкове
підключення, підтверджене pong, відновлення та зміну транспорту;
індикатор не надсилає додаткових ping.

The shared module depends on [JSON-RPC-2-Client-KMP](https://github.com/vsvictor/JSON-RPC-2-Client-KMP)
as a Git submodule and Gradle project; its source is not copied into XRNavi. Initialize it with
`git submodule update --init --recursive`. Build the native artifacts required by this client with
`external/JSON-RPC-2-Client-KMP/scripts/build-ios.sh` and/or
`external/JSON-RPC-2-Client-KMP/scripts/build-android.sh` before building the corresponding target.

The client pins upstream JSON-RPC-2 commit `e90318f1052d5b7b253c7e4c824aa53404703d23`,
which includes native lifecycle support; no separate lifecycle patch is required.

The iOS integration test performs a QUIC transport ping to the current A record for
`mobilespace.app:8443` (the simulator cannot resolve the hostname in this environment); TLS
certificate verification still uses `mobilespace.app` as the server name. Run it with
`./gradlew :shared:iosSimulatorArm64Test`. The public CA bundle is stored at
`shared/src/commonMain/composeResources/files/mobilespace_app_ca.pem`.

The app injects one `MobileSpaceRpcClient` through `AppDependencies` for the application lifetime
(Android `Application`, iOS app controller). XRNavi owns the lifecycle; the library's periodic
ping/recovery loop is disabled (`pingFrequency(0)`). Each connection must answer an immediate
transport ping before becoming active. Further pings run after `MOBILESPACE_RPC_PING_INTERVAL_MS`;
a successful pong resets the consecutive failure count. After `MOBILESPACE_RPC_MAX_MISSED_PONGS`
failures, XRNavi restores the existing transport and verifies it with another ping. If recovery
fails, it closes the connection and falls back QUIC → TLS → TCP. Exhausting TCP displays a connection
error, waits `RPC_CONNECTION_TRANSPORT_UP_MS`, and restarts at QUIC.

While TLS is active, every `RPC_CONNECTION_TRANSPORT_UP_MS` XRNavi probes QUIC. While TCP is active,
it probes QUIC, then TLS if QUIC fails. Probes must receive a pong before replacing the old connection;
the old connection continues its own ping/pong checks during probing. Failed probes leave a healthy
connection intact. A replacement closes the old connection only after being verified. Ping and restore
deadlines are enforced inside the native runtime, not by cancelling a blocking Kotlin FFI call.
If an active TCP connection cannot be restored, it is closed and reconnected over TCP; if that fails,
the exhausted-transport cycle restarts at QUIC. A verified TCP reconnect resumes upgrade probes.
Host, port, intervals, and the missed-pong threshold are defined in
`shared/src/commonMain/kotlin/com/mobilespace/xrnavi/Constants.kt`.

Спільний модуль використовує [JSON-RPC-2-Client-KMP](https://github.com/vsvictor/JSON-RPC-2-Client-KMP)
як Git submodule та Gradle-модуль; код клієнта не копіюється у XRNavi. Ініціалізуйте його
командою `git submodule update --init --recursive`. Перед збиранням відповідної платформи згенеруйте
нативні артефакти клієнта командами
`external/JSON-RPC-2-Client-KMP/scripts/build-ios.sh` та/або
`external/JSON-RPC-2-Client-KMP/scripts/build-android.sh`.

Клієнт використовує зафіксований коміт JSON-RPC-2 `e90318f1052d5b7b253c7e4c824aa53404703d23`
з нативною підтримкою життєвого циклу; окремий патч життєвого циклу не потрібен.

Інтеграційний тест iOS виконує QUIC transport ping до поточного A-запису
`mobilespace.app:8443` (у цьому середовищі симулятор не розв’язує доменне ім’я); TLS-сертифікат
перевіряється з `mobilespace.app` як server name. Запуск:
`./gradlew :shared:iosSimulatorArm64Test`. Публічний CA bundle зберігається за шляхом
`shared/src/commonMain/composeResources/files/mobilespace_app_ca.pem`.

Застосунок передає один `MobileSpaceRpcClient` через `AppDependencies` на час життя застосунку
(Android `Application`, контролер iOS). Життєвим циклом керує XRNavi; періодичний цикл ping і відновлення
бібліотеки вимкнено (`pingFrequency(0)`). Кожне підключення має відповісти pong на негайний транспортний
ping, перш ніж стати активним. Наступні ping виконуються через `MOBILESPACE_RPC_PING_INTERVAL_MS`;
успішний pong обнуляє лічильник послідовних невдач. Після `MOBILESPACE_RPC_MAX_MISSED_PONGS` невдач
XRNavi відновлює наявний транспорт і перевіряє його новим ping. Якщо відновлення не вдалося,
з'єднання закривається, а транспорт змінюється QUIC → TLS → TCP. Після невдачі TCP відображається
помилка з'єднання, через `RPC_CONNECTION_TRANSPORT_UP_MS` цикл починається з QUIC.

Під час роботи TLS кожні `RPC_CONNECTION_TRANSPORT_UP_MS` XRNavi перевіряє QUIC; під час роботи TCP —
QUIC, а за його невдачі TLS. Нове з'єднання замінює старе лише після pong; поки триває перевірка,
старе продовжує власні ping/pong. Невдала перевірка не зачіпає працездатного старого з'єднання.
Після успішного переходу старе закривається. Тайм-аути ping і відновлення виконуються всередині
нативного runtime, а не скасуванням блокувального FFI-виклику Kotlin. Якщо активний TCP не вдається
відновити, його закривають і перепідключають по TCP; за невдачі цикл починається з QUIC.
Підтверджене pong перепідключення TCP відновлює перевірки кращих транспортів. Хост, порт, інтервали
та поріг пропущених pong зібрані у файлі `shared/src/commonMain/kotlin/com/mobilespace/xrnavi/Constants.kt`.

### Verification evidence

[Map contracts and iOS launch verification, 2026-10-27 (Ukrainian)](docs/verification-2026-10-27.md)
records the tested builds, local evidence, and remaining physical-device/accessibility blockers.

### Authenticated account and explicit demo / Авторизований аккаунт і явний деморежим

All sections use the Map-style bottom navigation: equal-height tabs with icons above labels.
Landscape side navigation keeps its existing adaptive layout.

Усі розділи використовують нижнє меню як на мапі: вкладки однакової висоти з іконками над підписами.
Бокова навігація в альбомній орієнтації зберігає наявне адаптивне компонування.

Sign-in actions run through `SignInBloc`. The eye button reveals/hides the existing password
field without replacing it. The envelope opens Android's user-controlled system account chooser
(no broad account-reading permission); selecting an account only fills the email field.
iOS does not expose a public email-account list, so it displays an explanation and keeps manual entry.

Password recovery uses the public methods `auth.requestPasswordReset` (`email`) and
`auth.confirmPasswordReset` (`reset_token`, `new_password`) through `RecoveryService`, driven by
`RecoveryBloc`. A rejected or expired token (-32001) shows the invalid-link screen. The server does
not e-mail the token itself; the app receives it via `RecoveryViewModel.receiveResetToken`.

Login calls `auth.login` on the current verified RPC connection with `email`, `password`, and
`grant_type: "password"`, following the contract in
[JSON-RPC-2-OAuth2](https://github.com/vsvictor/JSON-RPC-2-OAuth2), without including that repository.
Duplicate submissions are blocked, connection waits and RPC calls are bounded, and failures are shown
on the form. Valid access/refresh tokens stay in memory only; passwords are cleared after success or
leaving the form, and leaving the session discards the tokens. There is no persistent session,
automatic token refresh, or automatic login replay. Success (including a test account) enters
authenticated mode, never demo mode. `AuthenticatedAccountBloc` receives the in-memory session
from `SignInBloc` and loads `auth.getProfile` through an injected service and ViewModel.
Loading, safe errors, retry, section navigation, local logout and expiry are BLoC events/state.
Logout cancels requests and discards profile and tokens; expiry requires a new login, not refresh.
Only “continue without login” enables the existing demo screens.
Login also uses unencrypted TCP when that is the active fallback, as explicitly requested:
**TCP exposes credentials and tokens to network observers; QUIC/TLS should be used on untrusted networks.**

Дії входу обробляє `SignInBloc`. Кнопка ока показує/приховує пароль без заміни поля.
Конверт відкриває системний вибір аккаунта Android за дією користувача (без широкого дозволу
на читання аккаунтів); вибір лише заповнює пошту. iOS не надає публічного списку поштових
аккаунтів, тому відображається пояснення й залишається ручне введення.

Відновлення пароля використовує публічні методи `auth.requestPasswordReset` (`email`) та
`auth.confirmPasswordReset` (`reset_token`, `new_password`) через `RecoveryService` під керуванням
`RecoveryBloc`. Відхилений або прострочений токен (-32001) відкриває екран недійсного посилання.
Сервер сам не надсилає токен поштою; застосунок отримує його через `RecoveryViewModel.receiveResetToken`.

Вхід викликає `auth.login` через поточне перевірене RPC-з'єднання з `email`, `password` та
`grant_type: "password"` за контрактом JSON-RPC-2-OAuth2, без включення цього репозиторію.
Повторні натискання під час запиту блокуються, очікування з'єднання та RPC мають тайм-аути,
помилки відображаються у формі. Перевірені access/refresh tokens залишаються лише в пам'яті;
пароль очищається після успіху або виходу з форми, вихід із сесії видаляє токени.
Постійного збереження сесії, автоматичного refresh чи повторного надсилання входу немає.
Успішний вхід (також тестового аккаунта) відкриває авторизований режим, ніколи не деморежим.
`AuthenticatedAccountBloc` отримує сесію в пам’яті від `SignInBloc` і завантажує `auth.getProfile`
через DI-сервіс і ViewModel. Завантаження, безпечні помилки, повторний запит, навігація,
локальний вихід і завершення сесії обробляються BLoC. Вихід скасовує запити й видаляє профіль
та токени; після завершення сесії потрібен новий вхід, без refresh.
Лише «продовжити без входу» вмикає наявні демоекрани.
За прямою вказівкою вхід також дозволений через незашифрований TCP, якщо це активний транспорт:
**TCP розкриває пароль і токени спостерігачам мережі; у недовірених мережах слід використовувати QUIC/TLS.**

The sign-in layout uses the full window size, not the keyboard-reduced content height.
Opening the keyboard keeps the same form and password input focus.
The sign-in image panel has no outer padding or rounded frame. Its portrait height is reduced
to fit the shorter content: no theme button or product caption, and the subtitle is
“XR — новий формат взаємодії”. Text retains its internal padding.
Email and password fields use the scaled text line height plus 24 dp (12 dp above and below).
Input text is vertically centered and left-aligned.
In light mode, the sign-in background transitions linearly from white at the bottom of the
image to the theme background color at the bottom of the viewport. In landscape, the form
background transitions from white at its top. Dark mode is unchanged.

Макет входу визначається повним розміром вікна, а не висотою контенту після відкриття
клавіатури. Форма залишається тією самою, а поле пароля зберігає фокус.
Плашка з картинкою не має зовнішніх відступів і заокругленої рамки. Її висоту в портретній
орієнтації зменшено під скорочений вміст: без кнопки теми та підпису продукту,
з підзаголовком «XR — новий формат взаємодії». Внутрішні відступи тексту збережено.
Висота полів пошти й пароля дорівнює масштабованій висоті рядка шрифту плюс 24 dp
(по 12 dp зверху та знизу). Текст вирівняно по центру вертикально та зліва горизонтально.
У світлій темі фон входу рівномірно переходить від білого біля нижнього краю картинки до
кольору фону теми біля нижнього краю видимої області. В альбомній орієнтації фон форми
починається білим зверху. Темну тему не змінено.

The authenticated Map/Garage/Settings/Profile sections share the same bottom navigation and
adaptive landscape rail as demo. Profile shows the backend identity, roles, creation time and
available profile fields; absent optional fields say “not specified”, never use fixtures.
Map/routing/search and account garage APIs are not integrated: their sections explicitly say
unavailable, with no synthetic map, routes or vehicles. A garage that was not loaded is not
claimed to be empty. Settings exposes the existing local theme picker, not fake server settings.
Profile editing is not implemented; the profile is explicitly read-only.
In explicit demo mode, Map still opens the local demo profile (S08) and settings (S07).

Exact backend contract verified against read-only BRNavi `src/auth.rs` and its pinned
`json-rpc-2-oauth2` dependency at `8b9d6372a80708389c6d4a314879e69bf966a67c`
(`src/rpc.rs`, `src/models.rs`, `src/mongo.rs`, `src/profile.rs`):

- `auth.getProfile` params: `{"access_token":"<in-memory access token>"}`. Identity comes
  from verified token claims, not client-supplied email, user ID or roles.
- RPC result (the native client already unwraps JSON-RPC): `id: string`, `email: string`,
  `created_at: RFC3339 string`, `roles: string[]`, `profile: object`. Missing required
  fields/wrong types are rejected. Empty roles/profile are valid.
- BRNavi profile schema: `first_name`, `last_name`, `middle_name`, `account_type` are text;
  `birth_date` is an optional RFC3339 datetime; `notifications_enabled` is an optional boolean.
- `auth.register` params: `email`, `password`, `profile` with required `first_name`,
  `last_name`, optional `middle_name` (may be empty/omitted) and required `account_type`
  (`private` or `corporate`). The server validates this for all storage backends.
  The Registration screen calls it via `RpcRegistrationService`; on success it opens Sign-in,
  on failure it shows a notice (invalid data / already exists / network).
- `auth.updateProfile` params: `access_token` plus `profile: map<string,string>`, **not**
  the whole UserResponse. Boolean updates use `"true"`/`"false"` strings; datetime updates
  use RFC3339 strings. It patches supplied fields and returns the same UserResponse.
  This contract is documented, not called by the current read-only UI.

Авторизовані розділи Мапа/Гараж/Налаштування/Профіль використовують те саме нижнє меню
й адаптивну бокову панель в альбомній орієнтації. Профіль показує ID, пошту, ролі, дату
створення та наявні поля з бекенда; відсутні необов’язкові поля — «Не вказано», без фікстур.
Реальні API мапи/маршрутів/пошуку й гаража не інтегровані: показується недоступність без
синтетичних мап, маршрутів чи автомобілів. Незавантажений гараж не оголошується порожнім.
Налаштування містять наявний локальний вибір теми, без імітації серверних перемикачів.
Профіль лише для перегляду; редагування не реалізоване.
У явному деморежимі залишаються локальні демопрофіль S08 і налаштування S07.

Контракт перевірено у BRNavi `src/auth.rs` та зафіксованій залежності OAuth2 (ревізія вище).
`auth.getProfile` приймає лише `access_token`, повертає `id`, `email`, `created_at` (RFC3339),
`roles` (масив рядків), `profile` (об’єкт). ID/ролі не беруться з вводу користувача.
`first_name`, `last_name`, `middle_name`, `account_type` — текст; `birth_date` — необов’язкова дата RFC3339;
`auth.register` приймає `email`, `password` і `profile` з обов’язковими `first_name`, `last_name`,
необов’язковим `middle_name` (може бути порожнім або відсутнім) та обов’язковим `account_type`
(`private` — особистий, `corporate` — корпоративний); сервер перевіряє це для всіх сховищ.
Екран реєстрації викликає його через `RpcRegistrationService`; успіх відкриває вхід,
помилка показує повідомлення (некоректні дані / вже існує / мережа).
`notifications_enabled` — необов’язковий boolean. `auth.updateProfile` приймає `access_token`
і `profile` як мапу **рядкових** значень (boolean — `"true"`/`"false"`, дата — RFC3339),
оновлює лише передані поля й повертає той самий UserResponse. UI його поки не викликає.

System/Light/Dark appearance is shared by every screen and stored locally using Android
SharedPreferences or iOS UserDefaults under `appearance.theme`. No credentials or session are
stored with it. Unconnected services and organization policies are informational, not working switches.
The theme row opens a System/Light/Dark picker. Profile and settings select one of four
window-size-driven layouts, with compact navigation in phone landscape; demo disclosures
remain visible instead of copying authenticated or connected-service states from the designs.

[Profile and settings verification, 2026-10-27 (Ukrainian)](docs/profile-settings-ui-2026-10-27.md)
records the checks and remaining platform limitations.

### OAuth 2 session lifecycle / Життєвий цикл OAuth 2-сесії

**English.** `SessionManager` owns the session; tokens are stored only in platform secure storage
(Android Keystore AES-GCM, iOS Keychain `AfterFirstUnlockThisDeviceOnly`) and are never logged.
- At launch `Restore` runs: no token → sign-in page; valid token → automatic sign-in; expired
  token → `auth.refresh`. A server rejection (`-32001`) clears the tokens and opens sign-in;
  a network failure keeps them and asks to sign in again.
- Every server call goes through `AuthorizedRpc`: the token is validated (30 s skew) and refreshed
  first; if the server still answers `-32001`, one refresh and one retry follow. Refreshes are
  serialized by a mutex.
- Calls sent without a token (`publicRpcMethods`): `auth.login`, `auth.register`, `auth.refresh`,
  password-recovery methods. Mapbox requests use their own key and are not backend calls.
- A background job refreshes the token shortly before it expires; a rejected refresh ends the session.
- Logout clears stored tokens; closing the app keeps them.

**Українською.** `SessionManager` керує сесією; токени зберігаються лише в захищеному сховищі
платформи (Android Keystore AES-GCM, iOS Keychain `AfterFirstUnlockThisDeviceOnly`) і не логуються.
- Під час запуску виконується `Restore`: токена немає → сторінка входу; токен чинний → автоматичний
  вхід; токен застарів → `auth.refresh`. Відмова сервера (`-32001`) очищає токени й відкриває вхід;
  помилка мережі зберігає їх і просить увійти знову.
- Кожен виклик сервера проходить через `AuthorizedRpc`: токен перевіряється (запас 30 с) і за потреби
  оновлюється; якщо сервер усе ж відповів `-32001` — одне оновлення й один повтор. Оновлення
  серіалізуються mutex-ом.
- Виклики без токена (`publicRpcMethods`): `auth.login`, `auth.register`, `auth.refresh`, методи
  відновлення пароля. Запити Mapbox мають власний ключ і не є викликами бекенду.
- Фонове завдання оновлює токен перед завершенням строку; відхилене оновлення завершує сесію.
- Вихід очищає збережені токени; закриття застосунку їх зберігає.

### Password-reset token delivery / Доставка токена відновлення пароля

**English.** A password-reset token is never returned to the requesting device. The server pushes it
as a `navi.reset_token` callback to the user's other signed-in device over the same transport as the
direct connection. `RpcCallbackService` subscribes after sign-in (`navi.callback.subscribe`, access
token required) and stops on logout; `CallbackBloc` shows `ResetTokenDialog`. Limits: a user with
no subscribed device receives nothing; the gateway keeps one connection per `client_id`, so with
several devices the last subscriber wins; push works on QUIC, TCP/TLS (line framing), SRT and
WS-SRT only. Live check: sign in on one device, then run the iPad UI test
`TEST_RUNNER_XRNAVI_TEST_EMAIL=<email> xcodebuild test ... -only-testing:iosAppUITests/RecoveryFlowUITests/testRequestTokenForConfiguredAccount`.
Test credentials live in the git-ignored `.test-account.json`.

**Українською.** Токен відновлення пароля ніколи не повертається пристрою, що його запросив. Сервер
надсилає його як callback `navi.reset_token` на інший авторизований пристрій користувача тим самим
транспортом, що й пряме з'єднання. `RpcCallbackService` підписується після входу
(`navi.callback.subscribe`, потрібен access-токен) і зупиняється при виході; `CallbackBloc` показує
`ResetTokenDialog`. Обмеження: користувач без підписаного пристрою нічого не отримає; gateway
тримає одне з'єднання на `client_id`, тож при кількох пристроях діє останній підписник; push
працює лише на QUIC, TCP/TLS (line framing), SRT і WS-SRT. Перевірка наживо: увійдіть на одному
пристрої та запустіть UI-тест iPad
`TEST_RUNNER_XRNAVI_TEST_EMAIL=<email> xcodebuild test ... -only-testing:iosAppUITests/RecoveryFlowUITests/testRequestTokenForConfiguredAccount`.
Тестові облікові дані — у `.test-account.json` (у `.gitignore`).

### Mapbox map / Мапа Mapbox

**EN.** The Map tab uses the native Mapbox Maps SDK 11.34.2 on Android (`MapView`) and iOS (Swift Package `MapboxMaps` via `MapboxViewFactory.swift`). The public `pk.` token is read from the git-ignored `tokens.dat` at build time (Android `resValue`; iOS via git-ignored `iosApp/Mapbox.local.xcconfig`, generated by `python3 tools/configure-mapbox.py`). Never commit tokens or embed secret/download tokens. Screen S03 (`AuthenticatedMapScreen`, `RouteBloc`, `RoutePlanning.kt`): Mapbox Geocoding v6 address search, Mapbox Directions v5 driving route drawn as a map line with pins, bottom sheet (time, distance, roads, vehicle class), light/dark/satellite layers. HTTP is done with Ktor; the token is passed through `AppDependencies` and never logged. S03 states: geolocation is a BLoC (`GeoBloc`, injected `LocationProvider`; Android `LocationManager`, iOS `CLLocationManager`) with phases Unknown/Denied/PermanentlyDenied/ServiceOff/Acquiring/TimedOut/Reliable/Stale shown in `GeoStatusCard`; the current place can be a route origin only with a reliable fix. Search phases Idle/Loading/Results/Empty/Error (empty ≠ error, retry keeps the query); a stale route is marked while re-planning or after failure; Back closes search then lowers the sheet; the sheet has expand/collapse buttons. Policy numbers (stale 60 s, acquire timeout 30 s) are PROVISIONAL pending QA/backend. Not implemented yet: turn-by-turn navigation ("Почати маршрут" shows a notice), long-press point (S35/S36), real vehicle data/garage, links to S04/S17/S18/S19/S34/S38, unverified-truck blocking.

**UK.** Вкладка «Мапа» використовує нативний Mapbox Maps SDK 11.34.2 на Android (`MapView`) та iOS (Swift Package `MapboxMaps` через `MapboxViewFactory.swift`). Публічний токен `pk.` читається з ігнорованого git файлу `tokens.dat` під час збірки (Android `resValue`; iOS — через ігнорований `iosApp/Mapbox.local.xcconfig`, що генерує `python3 tools/configure-mapbox.py`). Не комітьте токени й не вбудовуйте секретні токени. Екран S03 (`AuthenticatedMapScreen`, `RouteBloc`, `RoutePlanning.kt`): пошук адрес через Mapbox Geocoding v6, маршрут Mapbox Directions v5 (driving) із лінією та позначками на мапі, нижня панель (час, відстань, дороги, клас ТЗ), шари світлий/темний/супутник. HTTP — Ktor; токен передається через `AppDependencies` і не логується. Стани S03: геолокація — BLoC (`GeoBloc`, інжектований `LocationProvider`; Android `LocationManager`, iOS `CLLocationManager`) зі станами Unknown/Denied/PermanentlyDenied/ServiceOff/Acquiring/TimedOut/Reliable/Stale у `GeoStatusCard`; поточне місце може бути початком маршруту лише за надійної позиції. Фази пошуку Idle/Loading/Results/Empty/Error (порожньо ≠ помилка, повтор зберігає запит); застарілий маршрут позначається під час перебудови або після помилки; «Назад» закриває пошук, потім знижує панель; у панелі є кнопки розгорнути/згорнути. Числа політики (застаріло 60 с, тайм-аут 30 с) ПОПЕРЕДНІ до уточнення QA/бекендом. Ще немає: покрокової навігації («Почати маршрут» показує повідомлення), довгого натискання (S35/S36), реальних даних ТЗ/гаража, переходів на S04/S17/S18/S19/S34/S38, блокування неверифікованої вантажівки.

### Local stop-place catalog / Локальний каталог місць зупинки

S19–S20 provide a local, explicitly synthetic catalog with category/query filtering and place
details. Open it from Map's “Місця зупинки” action or S18's stop search. Adding a place changes
only the S18 draft; apply it with “Зберегти зупинки”, or discard it with Cancel/Back.
Unknown characteristics remain unspecified or unverified. There is no POI API, GPS,
vehicle-suitability confirmation, multipoint routing, or working navigation.

S19–S20 містять локальний, явно синтетичний каталог із категоріями, пошуком і деталями.
Входи: дія «Місця зупинки» на Map та пошук зупинки S18. Додавання змінює лише чернетку S18;
застосування — через «Зберегти зупинки», скасування — через Cancel/Back.
Невідомі характеристики лишаються незаданими або неперевіреними. POI API, GPS,
підтвердження придатності для авто, багатоточкова маршрутизація й справжня навігація відсутні.

[S19–S20 local verification / Локальна перевірка, 2026-10-31](docs/s19-s20-local-verification-2026-10-31.md)
identifies the checked artifacts and separates local Android evidence from unverified platforms.
Звіт ідентифікує перевірені артефакти й відділяє локальні Android-докази від неперевірених платформ.

---

Built with [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

**EN.** Garage (authenticated Garage tab): `GarageBloc` (list/select active/details) and `AddVehicleBloc` (class → make → model → year+trim wizard, S10/S14/S15/S17) with separate ViewModels; `GarageService` is injected via `AppDependencies` (`RpcGarageService`). The JSON-RPC method names (`garage.vehicles.list/set_active/add`, `catalog.classes.list/makes.list/models.list`) and payloads are PROVISIONAL: the backend contract is not confirmed, so on failure the UI shows an error with retry (never an empty garage). Selection changes only after server confirmation. Not yet: S11 dimensions screen, S16 configurations, S03 integration.

**UK.** Гараж (вкладка «Гараж» в автентифікованому режимі): `GarageBloc` (список/вибір активного/деталі) та `AddVehicleBloc` (майстер клас → марка → модель → рік+комплектація, S10/S14/S15/S17) з окремими ViewModel; `GarageService` інжектується через `AppDependencies` (`RpcGarageService`). Назви JSON-RPC методів (`garage.vehicles.list/set_active/add`, `catalog.classes.list/makes.list/models.list`) і формати ПОПЕРЕДНІ: контракт бекенду не підтверджено, тому при збої UI показує помилку з повтором (ніколи не порожній гараж). Вибір змінюється лише після підтвердження сервером. Ще немає: екрана габаритів S11, конфігурацій S16, інтеграції з S03.

### File structure rule / Правило структури файлів

**EN.** Every screen, its ViewModel and its BLoC (state, events, transitions) live in separate files (`XScreen.kt`, `XViewModel.kt`, `XBloc.kt`, with state/events in `XState.kt`). `DemoAccountScreen` is a thin host; settings/profile/garage/vehicle screens, `RecoveryBloc`, `RegistrationBloc` and `ThemeSettingsBloc` have own files (Recovery, Registration and Theme ViewModels are facades over their BLoCs). `StopPlacesBloc` is a stateless class (no singleton) in `StopPlacesBloc.kt`; app models moved from `App.kt` to `AppModels.kt`. Tests live in separate files.

**UK.** Кожен екран, його ViewModel і BLoC (стан, події, переходи) лежать в окремих файлах (`XScreen.kt`, `XViewModel.kt`, `XBloc.kt`, стан/події в `XState.kt`). `DemoAccountScreen` — тонкий хост; екрани налаштувань/профілю/гаража/авто, `RecoveryBloc`, `RegistrationBloc` і `ThemeSettingsBloc` мають власні файли (ViewModel Recovery, Registration і Theme — фасади над BLoC). `StopPlacesBloc` — клас без стану (не singleton) у `StopPlacesBloc.kt`; моделі застосунку винесено з `App.kt` до `AppModels.kt`. Тести — в окремих файлах.

**EN.** Demo navigation lives in `DemoAppBloc` (`DemoAppState.kt` state/events, `DemoAppBloc.kt` transitions) held by `DemoAppViewModel` (persistence of saved places and route stops); `XrnaviApplication.kt` only renders and dispatches events. Navigation state survives rotation but is no longer restored after process death.

**UK.** Демо-навігація живе в `DemoAppBloc` (`DemoAppState.kt` — стан/події, `DemoAppBloc.kt` — переходи) у `DemoAppViewModel` (збереження адрес і зупинок); `XrnaviApplication.kt` лише малює й надсилає події. Стан навігації переживає поворот екрана, але більше не відновлюється після завершення процесу.
