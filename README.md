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

Run on an unlocked, connected physical device; record its original system settings first.
Restore and verify any externally changed settings afterward. Use a fresh result-bundle path.

Запускайте на розблокованому підключеному фізичному пристрої; спочатку запишіть його
початкові системні налаштування. Після перевірки відновіть і перевірте всі параметри,
змінені поза тестами. Для результатів використовуйте новий шлях.

```sh
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug \
  -destination "platform=iOS,id=$IOS_DEVICE_UDID" \
  -only-testing:iosAppUITests/AdditionalScreensUITests \
  -parallel-testing-enabled NO -collect-test-diagnostics never \
  -resultBundlePath /tmp/xrnavi-ipad-ui.xcresult test
```

### JSON-RPC client / Клієнт JSON-RPC

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

### Local demo profile and settings

Sign-in actions run through `SignInBloc`. The eye button reveals/hides the existing password
field without replacing it. The envelope opens Android's user-controlled system account chooser
(no broad account-reading permission); selecting an account only fills the email field.
iOS does not expose a public email-account list, so it displays an explanation and keeps manual entry.

Login calls `auth.login` on the current verified RPC connection with `email`, `password`, and
`grant_type: "password"`, following the contract in
[JSON-RPC-2-OAuth2](https://github.com/vsvictor/JSON-RPC-2-OAuth2), without including that repository.
Duplicate submissions are blocked, connection waits and RPC calls are bounded, and failures are shown
on the form. Valid access/refresh tokens stay in memory only; passwords are cleared after success or
leaving the form, and leaving the session discards the tokens. There is no persistent session,
automatic token refresh, or automatic login replay. Success opens the map with an explicit demo-data
disclosure; map/profile data are not fetched from the authenticated account.
Login also uses unencrypted TCP when that is the active fallback, as explicitly requested:
**TCP exposes credentials and tokens to network observers; QUIC/TLS should be used on untrusted networks.**

Дії входу обробляє `SignInBloc`. Кнопка ока показує/приховує пароль без заміни поля.
Конверт відкриває системний вибір акаунта Android за дією користувача (без широкого дозволу
на читання акаунтів); вибір лише заповнює пошту. iOS не надає публічного списку поштових
акаунтів, тому відображається пояснення й залишається ручне введення.

Вхід викликає `auth.login` через поточне перевірене RPC-з'єднання з `email`, `password` та
`grant_type: "password"` за контрактом JSON-RPC-2-OAuth2, без включення цього репозиторію.
Повторні натискання під час запиту блокуються, очікування з'єднання та RPC мають тайм-аути,
помилки відображаються у формі. Перевірені access/refresh tokens залишаються лише в пам'яті;
пароль очищається після успіху або виходу з форми, вихід із сесії видаляє токени.
Постійного збереження сесії, автоматичного refresh чи повторного надсилання входу немає.
Успіх відкриває мапу з явним позначенням демоданих; мапа/профіль не завантажуються з акаунта.
За прямою вказівкою вхід також дозволений через незашифрований TCP, якщо це активний транспорт:
**TCP розкриває пароль і токени спостерігачам мережі; у недовірених мережах слід використовувати QUIC/TLS.**

The sign-in layout uses the full window size, not the keyboard-reduced content height.
Opening the keyboard keeps the same form and password input focus.
The sign-in image panel has no outer padding or rounded frame. Its portrait height is reduced
to fit the shorter content: no theme button or product caption, and the subtitle is
“Навігація для кожного”. Text retains its internal padding.
Email and password fields use the scaled text line height plus 24 dp (12 dp above and below).
Input text is vertically centered and left-aligned.
In light mode, the sign-in background transitions linearly from white at the bottom of the
image to the theme background color at the bottom of the viewport. In landscape, the form
background transitions from white at its top. Dark mode is unchanged.

Макет входу визначається повним розміром вікна, а не висотою контенту після відкриття
клавіатури. Форма залишається тією самою, а поле пароля зберігає фокус.
Плашка з картинкою не має зовнішніх відступів і заокругленої рамки. Її висоту в портретній
орієнтації зменшено під скорочений вміст: без кнопки теми та підпису продукту,
з підзаголовком «Навігація для кожного». Внутрішні відступи тексту збережено.
Висота полів пошти й пароля дорівнює масштабованій висоті рядка шрифту плюс 24 dp
(по 12 dp зверху та знизу). Текст вирівняно по центру вертикально та зліва горизонтально.
У світлій темі фон входу рівномірно переходить від білого біля нижнього краю картинки до
кольору фону теми біля нижнього краю видимої області. В альбомній орієнтації фон форми
починається білим зверху. Темну тему не змінено.

The Map navigation opens a demo-only profile (S08) and settings (S07), not an authenticated account.
System/Light/Dark appearance is shared by every screen and stored locally using Android
SharedPreferences or iOS UserDefaults under `appearance.theme`. No credentials or session are
stored with it. Unconnected services and organization policies are informational, not working switches.
The theme row opens a System/Light/Dark picker. Profile and settings select one of four
window-size-driven layouts, with compact navigation in phone landscape; demo disclosures
remain visible instead of copying authenticated or connected-service states from the designs.

[Profile and settings verification, 2026-10-27 (Ukrainian)](docs/profile-settings-ui-2026-10-27.md)
records the checks and remaining platform limitations.

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

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…