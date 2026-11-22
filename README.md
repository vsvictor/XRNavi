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

### Profile visual refresh / Візуальне оновлення профілю

**EN.** The signed-in profile follows Figma S08 and the shared settings/garage visual language:
Roboto Condensed headings, Inter body text, a circular monogram, flat theme surfaces,
and the app's blue action accent. A 16 dp identity card groups the name, email
and actual registration date; a decorative vector car/road illustration gives the garage
a visual anchor without pretending to be a photo of the user's vehicle. Personal-data
and saved-place actions form an icon-led list. A BLoC-controlled Personal / Corporate
tab bar replaces the account caption and type badge. All existing profile content
lives under Personal; Corporate is a placeholder, not a change to backend account type.
Statistics follow Figma: three tiles for trips, kilometres and hours on the road,
placed below the garage. Unavailable metrics show `—`, not fabricated demo values
or a reporting period. Decorative road slogans and
organization warnings are omitted; operational errors remain actionable.
The full-width logout button follows the Figma primary action (52 dp, 26 dp radius).
It appears only on the personal profile, not security.
The garage and personal-data/saved-place actions use the existing
BLoC navigation. The screens use the shared authentication palette and background.
Profile, account security and saved places reuse the sign-in/registration/recovery
photo-hero layout, gradient transition, uppercase Roboto Condensed title and Orbit + XRNavi wordmark.
These three pages use a separate grey Mustang GT image (`profile_mustang_hero_light/dark`)
with the same bottom fade into the page background; authentication keeps its original photo.
The photo is shared brand artwork, not a representation of the user's vehicle.
All child pages and authentication flows use the same accessible `‹` back control.
Security details and saved places use the same rounded section cards as the profile.
All three pages share `AccountPageLayout` (hero, 1120 dp maximum width, 24 dp
horizontal padding, 16 dp section spacing). Security omits the identity card
and uses read-only label/value rows with dividers, not outlined form inputs; its detail
cards use two columns when space permits. No logout action is exposed by security.
Security has two quiet sections: Personal data (full name and an icon/value contact
list) and Account data (registration date, ID and roles). Contacts come from
`profile.contacts`, an array of `{ "type": "email", "value": "driver@example.invalid" }`.
Supported types are phone, email, telegram, viber, facebook, x, instagram, tiktok,
whatsapp, signal, linkedin and website; each has its own accessible icon.
Login email remains independent from profile contacts. Legacy responses without
contacts display the login email; an explicit empty array displays no contacts.
Malformed contacts cause a profile-loading error, not a fabricated empty list.
Profile email and the account-details heading fit on one line by adapting font size.
The profile displays garage-loading errors with retry and keeps the garage action;
the garage screen also provides error details and retry.
Only security and saved places show a back arrow; profile is a root destination.
Phone portrait uses a single column; landscape uses two
columns, with bounded content on tablets. Both app themes are supported. Unavailable
trip counters and the nonfunctional edit button are omitted rather than presented as
working features. No vehicle photo, verification status, membership, or statistics is invented;
the garage explicitly distinguishes loading, errors, empty, unselected, and active states.

**UK.** Авторизований профіль відповідає Figma S08 і спільній стилістиці налаштувань та
гаража: заголовки Roboto Condensed, основний текст Inter, кругла монограма, пласкі
поверхні теми та синій акцент дій застосунку. Картка з радіусом 16 dp об'єднує ім'я,
електронну пошту й фактичну дату реєстрації з підписом «Зареєстрований»; декоративна векторна ілюстрація авто та дороги
створює візуальний акцент гаража, не імітуючи фото автомобіля користувача. Дії особистих
даних і збережених місць згруповано у список з іконками.
Замість підпису акаунта й позначки типу додано вкладки «Особистий / Корпоративний»
з керуванням через BLoC. Увесь поточний вміст — в особистій вкладці;
корпоративна поки містить заглушку й не змінює тип акаунта на сервері.
Статистика відповідає Figma: три плитки поїздок, кілометрів і годин у дорозі
під гаражем. Недоступні показники позначено «—», без вигаданих демочисел
чи звітного періоду. Декоративні гасла й
попередження про організації прибрано; робочі помилки залишаються доступними для повтору.
Кнопка виходу на всю ширину відповідає основній дії Figma (52 dp, радіус 26 dp)
й доступна лише в особистому профілі, не на екрані безпеки.
Гараж і дії особистих даних та
збережених місць використовують наявну BLoC-навігацію. Екрани використовують спільну
палітру та фон авторизації.
Профіль, безпека акаунта й обране повторно використовують композицію фотоблоку входу, реєстрації
та відновлення пароля, градієнтний перехід, заголовок Roboto Condensed великими літерами
та логотип Orbit + XRNavi. Ці три сторінки використовують окремий кадр із сірим Mustang GT
(`profile_mustang_hero_light/dark`) з таким самим нижнім переходом у фон сторінки;
фото авторизації залишається незмінним.
Фото — спільна брендова ілюстрація, а не автомобіль користувача.
Особисті дані та обране використовують такі самі округлені картки розділів, як профіль.
Дочірні сторінки та екрани авторизації використовують одну доступну кнопку «‹».
Усі три сторінки використовують `AccountPageLayout`: фотоблок, максимальну ширину
1120 dp, горизонтальні відступи 24 dp та інтервали між розділами 16 dp.
Безпека не показує картку особи й використовує рядки підпис/значення
з роздільниками замість обведених полів форми; за достатньої ширини картки
даних розташовані у дві колонки. Дії виходу на екрані безпеки немає.
Екран безпеки має два стримані розділи: особисті дані (ПІБ і список контактів
«іконка — значення») та дані акаунта (дата реєстрації, ID, ролі).
Контакти надходять у `profile.contacts` як масив об'єктів `type`/`value`.
Типи: phone, email, telegram, viber, facebook, x, instagram, tiktok,
whatsapp, signal, linkedin, website; кожен має власну іконку з доступним підписом.
Email для входу не залежить від контактів профілю. Для старих відповідей без
contacts показується email для входу; явний порожній масив означає відсутність
контактів. Некоректні контакти спричиняють помилку завантаження профілю.
Email профілю та заголовок даних акаунта вміщуються в один рядок завдяки
адаптивному розміру шрифту. Профіль показує помилку завантаження гаража та
кнопку повтору, а також дію переходу до гаража.
Стрілка назад є лише в безпеці та обраному; профіль — кореневий екран.
У portrait телефона застосовано одну колонку, у landscape — дві, на планшетах ширину контенту обмежено.
Підтримано обидві теми застосунку. Недоступні лічильники поїздок і неробочу кнопку
редагування прибрано, а не подано як реалізовані функції. Фото авто, підтвердження,
членство й статистика не вигадуються; гараж розрізняє завантаження, помилку, порожній
стан, відсутність основного авто та вибраний автомобіль.

| EN | UK |
|----|----|
| Sign-in, registration, password recovery with a six-digit emailed code; adaptive portrait/landscape layout with a shared hero | Вхід, реєстрація, відновлення пароля шестизначним кодом з листа; адаптивний portrait/landscape-макет зі спільним hero |
| OAuth 2 session lifecycle with secure token storage (Android Keystore, iOS Keychain) | Життєвий цикл OAuth 2-сесії із захищеним сховищем токенів (Android Keystore, iOS Keychain) |
| Map (S03): Mapbox geocoding, driving directions, geolocation states | Мапа (S03): геокодування Mapbox, маршрути, стани геолокації |
| Garage and vehicle wizard; local stop-place catalog; profile and theme settings | Гараж і майстер додавання авто; локальний каталог місць зупинки; профіль і теми |
| Connection indicator (QUIC / TLS / TCP) | Індикатор з'єднання (QUIC / TLS / TCP) |

## Architecture / Архітектура

### Automatic vehicle tracker / Автоматичний автомобільний трекер

**EN.** ExKMPTracker starts through an application-owned BLoC after password/Google login
or restoration of a valid session, without waiting for the profile. Its permanent installation
`trackerId` is the existing secure `deviceId` already registered during login, not a second UUID.
The authenticated QUIC call `tracking.open({deviceId})` persists the user/device tracking grant
in BRNavi's MongoDB and returns `{trackerId, sessionId, driverId, brandId}`. Only that confirmed
identity starts native collection. The map consumes the same filtered estimates, retaining
their observation times. Registration failures are visible and retried; they do not undo login.
BRNavi grants only `tracking.ingest` and `tracking.server_time` through its dedicated tracking
role. When `tracking.open` returns `refreshRequired: true`, the client refreshes and securely
persists both tokens before starting collection, without changing the tracking generation.
Logout, explicit demo mode, or rejected authentication stops tracking but retains the device ID.

Android uses a location Foreground Service and a persistent notification. First start waits
for a visible Activity and separately granted precise location, background location and
notification permissions. iOS requests only When In Use authorization and tracks in the
foreground; it does not request Always. If Always was already granted in system settings,
the existing `location` background mode may be used. OS restrictions and permission denial
still apply. XRNavi disables unverified Android sticky restoration: after process death, reopening
the app restores authentication and confirms registration before restarting collection.
Defaults remain `section(1, true)`, `section(2, false)`, `section(3, false)`.
The library is pinned as the Git submodule `external/ExKMPTracker` from
[vsvictor/ExKMPTracker](https://github.com/vsvictor/ExKMPTracker) and included as a Gradle
composite build. Initialize it with `git submodule update --init --recursive`
(or clone XRNavi with `--recurse-submodules`). No sibling checkout or local path override is needed.
Set `ANDROID_HOME` to your Android SDK, or configure `sdk.dir` in the submodule's ignored
`local.properties` as well as the root project's file.
The corresponding BRNavi integration must be deployed separately; changing these sources
does not update the running backend.

**EN. Mapbox location rule (project-wide).** Every Mapbox map must use the shared
`NativeMap` entry point and the application-injected `PlatformMapFactory`.
`TrackerLocationBloc` is the only position source: the Android location plugin receives
`TrackerMapboxLocationProvider`, and iOS receives its tracker-backed location signal
with no native heading provider. Neither adapter requests GPS or compass updates.
The map tab observes automatically; no Locate press is needed. The native location puck,
accuracy ring, recenter action and current-location route origin use the same filtered fix.
Estimate time, accuracy, course and speed come from ExKMPTracker. Logout, tracker stop,
missing estimates, revoked permissions or disabled location services clear the position;
an estimate older than 60 seconds is no longer displayed as current. Tracker v1.1.5
refreshes the timestamp of a held stop, so it does not expose the age of the
underlying measurement.
There is no fallback to another GPS provider, including while registration is pending.
The first reliable fix after opening the map animates the camera to that position once.
Later fixes do not recenter the camera or rebuild route layers; camera movement remains
controlled by gestures, explicit controls and route fitting.
Tracker v1.1.5 keeps a precise stop fixed: after the anchor is set, its latitude and
longitude do not move. A network anchor is still replaced once by the first GNSS or
fused fix. Modelled uncertainty growth stays capped at 15 m. Neither that cap nor the
calculated accuracy guarantees actual accuracy; a 5 m result depends on device signal
conditions.

**UK. Правило геолокації Mapbox (для всього проєкту).** Кожна мапа Mapbox має
використовувати спільну точку входу `NativeMap` та передану застосунком `PlatformMapFactory`.
Єдине джерело позиції — `TrackerLocationBloc`: Android-плагін геолокації отримує
`TrackerMapboxLocationProvider`, iOS — сигнал позиції трекера без нативного постачальника
напрямку компаса. Адаптери не запитують окремих оновлень GPS або компаса.
Вкладка мапи підписується автоматично, без натискання кнопки позиціювання.
Нативний індикатор місцезнаходження, коло точності, центрування та початок маршруту
«Моє місце» використовують одну відфільтровану позицію.
Час оцінки, точність, курс та швидкість надходять із ExKMPTracker.
Вихід, зупинка трекера, відсутність оцінки, відкликання дозволу чи вимкнення геолокації
прибирають позицію; оцінка віком понад 60 секунд більше не показується як поточна.
Трекер v1.1.5 оновлює час утримуваної зупинки, тому вік вихідного вимірювання недоступний.
Перемикання на інше GPS-джерело немає, зокрема під час очікування реєстрації.
Перша достовірна позиція після відкриття мапи один раз анімовано центрує камеру.
Подальші координати не центрують камеру й не перебудовують шари маршруту:
камерою керують жести, явні команди та вписування маршруту.
Трекер v1.1.5 тримає точну зупинку нерухомо: після встановлення якоря широта й
довгота не змінюються. Мережевий якір і далі один раз замінюється першим GNSS-
або fused-фіксом. Модельне зростання похибки лишається обмеженим 15 м. Ані це
обмеження, ані розрахована похибка не гарантують фактичну точність; досягнення 5 м
залежить від умов сигналу.

**UK.** ExKMPTracker запускається через BLoC рівня застосунку після входу паролем/Google
або відновлення чинної сесії, не очікуючи профілю. Постійний `trackerId` інсталяції —
наявний захищений `deviceId`, який уже реєструється під час входу, а не другий UUID.
Автентифікований QUIC-виклик `tracking.open({deviceId})` зберігає прив'язку користувача
та пристрою в MongoDB BRNavi й повертає `{trackerId, sessionId, driverId, brandId}`.
Лише підтверджена сервером ідентичність запускає нативний збір. Мапа використовує ті самі
відфільтровані оцінки зі справжнім часом вимірювання. Помилки реєстрації показуються
користувачу; повторні спроби не скасовують вхід. Вихід, явний деморежим або відхилення
автентифікації зупиняють трекер, але не видаляють ID пристрою.
BRNavi надає лише `tracking.ingest` і `tracking.server_time` через окрему роль трекера.
Якщо `tracking.open` повертає `refreshRequired: true`, клієнт оновлює й безпечно зберігає
обидва токени до запуску збору, не змінюючи покоління трекінгу.

Android використовує location Foreground Service із постійним сповіщенням. Перший запуск
очікує видимої Activity й окремого надання дозволів точної та фонової геолокації і сповіщень.
iOS запитує лише дозвіл When In Use і відстежує позицію на передньому плані, не запитуючи
Always. Якщо Always уже надано в системних налаштуваннях, може використовуватися наявний
фоновий режим `location`. Системні обмеження та відмова в дозволах залишаються чинними.
XRNavi вимикає неперевірене sticky-відновлення Android: після завершення процесу треба
знову відкрити застосунок, щоб відновити авторизацію й підтвердити реєстрацію до збору даних.
Типово: `section(1, true)`, `section(2, false)`, `section(3, false)`.
Бібліотека зафіксована як Git submodule `external/ExKMPTracker` із
[vsvictor/ExKMPTracker](https://github.com/vsvictor/ExKMPTracker) і підключена як Gradle
composite build. Ініціалізація: `git submodule update --init --recursive`
(або клонування XRNavi з `--recurse-submodules`). Сусідній checkout і локальне перевизначення шляху не потрібні.
Задайте `ANDROID_HOME` зі шляхом до Android SDK або вкажіть `sdk.dir` також в ігнорованому
`local.properties` submodule, окрім файлу кореневого проєкту.
Відповідну інтеграцію BRNavi потрібно розгорнути окремо: зміни вихідного коду
не оновлюють запущений сервер.

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
* [/external](./external) — pinned `JSON-RPC-2-Client-KMP` and `ExKMPTracker` library submodules. / зафіксовані submodule бібліотек клієнта JSON-RPC та `ExKMPTracker`.
* [/docs](./docs) — design, verification and screen reports. / звіти про дизайн, перевірки й екрани.

### Running the apps

**EN.** S00 uses a single 2.4-second Compose splash on Android and iOS, controlled by
`SplashBloc`/`SplashViewModel`. The Orbit mark is drawn as vectors at the display density,
not enlarged from a bitmap; the wordmark uses the bundled Space Grotesk Bold font.
Figma's appearance keyframes (0/450/480/720 ms) and one satellite revolution
(200–2200 ms) run once, without waiting for the network or restarting on rotation.
Connection setup and session restoration run in parallel with the splash.
The native launch screens show only the matching system-theme background, avoiding a
second, differently sized raster logo before the animation.

**UK.** S00 використовує єдиний Compose splash тривалістю 2,4 с на Android та iOS,
яким керують `SplashBloc`/`SplashViewModel`. Знак Orbit малюється векторно з густиною
пікселів дисплея, а не збільшується з bitmap; напис використовує вбудований Space Grotesk Bold.
Ключові кадри появи з Figma (0/450/480/720 мс) та один оберт супутника
(200–2200 мс) виконуються один раз, без очікування мережі й повтору при повороті.
Підключення та відновлення сесії виконуються паралельно зі splash.
Нативні екрани запуску показують лише відповідне системній темі тло, щоб перед
анімацією не з'являвся другий растровий логотип іншого розміру.

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

The main design page has 46 in-scope screen IDs, including animated splash S00 and the
collapsed/partial/expanded S03 map. Annotations about future S05 and XRDisp S32 are not
implemented navigation screens. The shared UI adds company settings (S09), invitations (S12),
trip summaries (S13), vehicle catalog/model selection (S14–S15), unverified routes (S17),
offline maps (S21), tasks/details/history (S25–S27), privacy/deletion (S28–S29), diagnostics
(S31), route explanations (S38), and location education (S46). Existing vehicle details S11
remain available; the catalog also has a local editable vehicle-parameter draft. Stop-place
list/details S19–S20 reuse the local catalog and route-stop BLoC with adaptive layouts.

На основній сторінці дизайну є 46 ідентифікаторів екранів поточного scope, включно з
анімованим Splash S00 і трьома станами панелі мапи S03. Примітки про майбутні S05 та XRDisp S32 не є
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

The add-vehicle form has General and Equipment tabs below its hero image.
Vehicle identification is in General; all technical data is in Equipment.
BLoC controls the selected tab and preserves form data across tab changes.
After make, model and year are selected, the form and vehicle details show up to six
Wikimedia Commons search results. Only CC0 and CC BY images with usable attribution
and license links are included; matching is approximate, and results may be unavailable.
The photo slider is the hero image on both screens, behind the title and back button.
Searches go directly to Wikimedia, and results are cached in memory during the session.
The first slide is always the default Mustang image for the current light or dark theme, including while searching in the background,
when no photos are found, or if the search fails. Search errors and loading indicators
are not shown.
The photo selected in the slider is saved locally per account and vehicle, including
selections made before adding the vehicle. It is restored in the slider and shown as
the garage card's thumbnail and faint background. The slider provides attribution and license
links. Photo selection is not synchronized across devices.

On the garage's add-vehicle screen, focusing the make field immediately sends
`car.makes_index2_get` without business parameters over
the authorized RPC connection. The BLoC caches the returned `data` list for the
open wizard and filters names by case-insensitive substring using the latest input.
Typing remains available during loading; further edits reuse the list without
duplicate requests. Errors are displayed, and editing retries a failed request.
Selecting a make keeps its name in the make field and reveals a separate model
input directly below it on the same form. Editing the make returns to the cached
make suggestions and clears the previous model selection.
Both fields are editable dropdowns controlled by BLoC. Selecting a make requests
`car.oemmakemodels_index_get` (`/api/models/v2`) with its name in `make`; the returned `data` populates
the model dropdown. This endpoint provides model IDs/names, not years or trims.
Selecting a make or model retains its entire original `data` object in the BLoC
selection's `rawData`, including additional fields; dropdowns display only `name`.
Focusing the model field opens the loaded suggestions without typing; losing
focus closes the dropdown. These focus transitions are handled by BLoC.
Selecting a make places its cursor at the end and keeps focus on the make
field with both dropdowns closed; the model field is focused only by the user.
Selecting a model places its cursor at the end and
hides the keyboard. BLoC stores cursor selections and pending, acknowledged UI effects.
Selecting a model keeps its name in the field and requests `car.years_index2_get`
with the selected `make` and `model` names. The integer result array is stored in
`selectedModel.years` and displayed as year-selection buttons below the model
field. Year loading, errors, retry and selection are handled by BLoC; changing
the make/model clears the year and ignores outdated responses.
With both make and model selected, choosing an available year saves it and
automatically switches to the Equipment tab, preserving the selected vehicle.
Equipment loads all pages of `car.oemtrims_index_get` with make/model/year.
Requests use `limit: 100`. Below the make/model/year header, the trim dropdown
displays `data[*].description` with separators and retains the complete selected record
in BLoC. The field is read-only; descriptions wrap fully, and the popup grows to
the available screen height with scrolling when needed.
Selecting a trim loads its body records through BRNavi using
`car.makemodeltrimbodies_index2_get` with `trim_id` from the selected v2 trim record.
The request uses the standard BRNavi session authorization and omits `verbose`.
RPC failures retain their original cause for diagnostics.
Body cards replace the body selector and show available dimensions/capacities in
metric units. BLoC handles loading, retry and rejection of outdated responses.
Selecting a trim also loads engine records via `car.makemodeltrimengines_index2_get`
with the same `trim_id`. Engine cards replace the engine selector and have independent
loading/error/retry state in BLoC. Every characteristic in every equipment card
occupies a separate row; long values wrap without truncation.
Transmission and drive type are shown in a separate Transmission card from the same
selected-trim engine response, without another request or a transmission selector.
They share BLoC loading/error/retry state with the engine and are not duplicated in its card.
Fuel consumption/range loads separately using `car.makemodeltrimmileages_index2_get`
with the selected `trim_id`, including all pages. Consumption is shown in l/100 km,
one characteristic per row, with independent BLoC loading/error/retry and stale-response
protection. Before a trim is selected, no technical cards, detail hints, or color
selectors are rendered; only vehicle identification and the trim picker remain.
The General tab starts with icon-labelled VIN, Scan and SmartCar buttons in one row
(Scan and SmartCar are enabled placeholders with empty click handlers).
VIN opens a dialog containing the VIN input and Search action. Search immediately
closes the dialog and shows an on-screen loader while resolving the vehicle.
Successful decoding
through BRNavi resolves the catalog make/model and years, fills both fields and
selects the returned year, closes the dialog and stays on General. Failures remain
in the reopened dialog without overwriting existing selections; dismissing ignores late responses.
Each trim is resolved through `car.oemtrims_view_get` with its ID. Equipment
shows exterior/interior color grids of 50 dp circular RGB swatches instead of color
name inputs. Color names remain accessibility labels only; selection is managed by
BLoC and indicated by a border. Missing/invalid RGB is explicitly marked unavailable,
not replaced with an invented color. Options are deduplicated and mutually filtered
by shared trims. Characteristic labels in every information card are bold, with
smaller regular-weight values on the same row. Only compatible mileage/range
records are shown for the selected trim. All calls go
through the authorized BRNavi RPC connection, never directly to CarAPI. Original
trim and detail objects are preserved. US dimensions, weights, fuel economy and
range are converted to metric units; missing data is explicitly marked.
Loading/errors/retry and request isolation are handled by BLoC. Changing the
vehicle/year clears the previous configuration and ignores outdated responses.
The **Save vehicle** action appears outside the tabs and is enabled after selecting
make, model and year. Trim is optional; available details are saved through
`garage.vehicles.add`. The snapshot includes original make/model/trim records when available,
body/engine/consumption records, available and selected colors, and a successfully
decoded VIN response when it still matches the vehicle. Technical requests and their
errors do not block saving; save failures keep the form for retry.
Success opens the review and reloads `garage.vehicles.list`. The returned complete
snapshot is retained with each garage vehicle. Local garage/form state is cleared
when leaving the authenticated account to avoid leaking a previous user's cars.
Snapshots also store metric `dimensions` (`height_m`, `width_m`, `length_m`) and
`weight_kg` derived from the selected body's inches and curb weight in pounds.
The saved-vehicle review, details, and garage cards read these body measurements,
including older snapshots without metric fields. Existing top-level server
measurements take precedence. Missing measurements remain unknown; gross weight
is not substituted for curb weight. Saved details also show the trim, body, engine,
transmission, and fuel consumption directly from the persisted snapshot, without
reloading the catalog.

Знімок авто також зберігає метричні `dimensions` (`height_m`, `width_m`, `length_m`)
та `weight_kg`, обчислені з дюймів і спорядженої маси кузова у фунтах.
Екран після додавання, деталі та картки гаража використовують ці характеристики
кузова, зокрема зі старих знімків без метричних полів. Попередні значення верхнього
рівня від сервера мають пріоритет. Відсутні значення залишаються невідомими;
повна маса не підміняє споряджену. Деталі також показують збережені комплектацію,
кузов, двигун, трансмісію та витрату палива без повторного завантаження каталогу.

**Garage RPC / RPC гаража**

The garage list uses its own grayscale Ford Mustang GT photo with the sign-in
gradient and product wordmark; its header has no back or add icon.
Garage operations show a loader centered on the full screen.
Tap a vehicle card to open its profile; long-press it to select the active vehicle.
The add/edit and vehicle profile screens use the account profile's page layout
and hero header with a back button; main navigation remains available.
Edit and delete remain separate icon buttons. Dimensions and weight are shown only
in the vehicle profile/review, not on list cards.
Each vehicle card offers **Edit** (the complete prefilled vehicle form) and
**Delete** (confirmation required). Updates keep the vehicle ID and active
selection and use `garage.vehicles.update` with the add payload plus `vehicle_id`.
Deletion uses `garage.vehicles.delete` with `vehicle_id`; its
`active_vehicle_id` response selects the first remaining car if the active car
was removed, or null for an empty garage. BLoC waits for server confirmation;
failures preserve the existing car and allow retry. Editing retains the saved
technical data, VIN, colors, and unknown snapshot fields and loads catalog options
for changing make/model/year/trim. Cancelling editing does not write anything.

Список гаража використовує окреме фото Ford Mustang GT у сірих тонах із градієнтом
екрана входу та логотипом; у шапці немає стрілки назад та плюса.
Індикатор операцій гаража розташовано в центрі всього екрана.
Натискання картки відкриває профіль авто, а довге натискання обирає його основним.
Екрани додавання, редагування та профілю транспорту використовують компонування
профілю акаунта й шапку з кнопкою повернення; головна навігація залишається доступною.
Редагування та видалення залишаються окремими кнопками з іконками.
Габарити й маса показуються лише у профілі/перевірці авто, не на картках списку.
Картка авто має **Редагувати** (повна заповнена форма) та **Видалити**
(із підтвердженням). `garage.vehicles.update` приймає поля додавання разом
із `vehicle_id`, зберігає ID та активний вибір. `garage.vehicles.delete`
приймає `vehicle_id` і повертає `active_vehicle_id`: перше авто, що залишилося,
якщо видалено активне, або null для порожнього гаража. BLoC чекає підтвердження
сервера; при помилці авто залишається й дію можна повторити. Форма зберігає
попередні характеристики, VIN, кольори та додаткові поля знімка й завантажує
каталог для зміни марки, моделі, року та комплектації. Скасування не змінює сервер.

**Garage hero attribution / Авторство фото гаража:** “2018 Ford Mustang GT 5.0 Front”
by / автор Vauxford, [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:2018_Ford_Mustang_GT_5.0_Front.jpg),
[CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/).
`garage_mustang_gt_hero.jpg` is a resized copy distributed under the same license;
the UI crops the image and overlays a gradient.
`garage_mustang_gt_hero.jpg` — зменшена копія під тією самою ліцензією;
інтерфейс кадрує фото та накладає градієнт.

VIN lookup also fills Equipment when the VIN identifies a unique catalog trim.
Legacy VIN trim IDs are never sent to v2 endpoints: matching uses trim names and
descriptions (and available displacement/door/seat constraints). VIN body, engine,
transmission and mileage values are reused; missing sections/essential fields
are fetched by the matched v2 trim ID. VIN-specific known values are retained.
Ambiguous VINs show a manual trim-selection notice instead of guessing.
VIN-пошук також заповнює комплектацію, якщо VIN однозначно визначає запис каталогу.
Попередні ID з VIN не передаються у v2: зіставляються назви й описи, а за наявності
також об'єм двигуна, двері та місця. Відомі дані кузова, двигуна, трансмісії й витрати
використовуються повторно; відсутні розділи/основні поля завантажуються за v2 ID.
Неоднозначний VIN показує повідомлення про ручний вибір комплектації, без здогадок.

BRNavi stores structured vehicle snapshots in MongoDB collection `garage`.
Ownership is taken only from the authenticated session, not from request parameters.
`garage.vehicles.add` returns `{ "vehicle": { "id": "...", "name": "...",
"category": "...", "verification": "none", "vehicle": { ... } } }`.
`garage.vehicles.list` takes only session authorization and returns
`{ "vehicles": [ ... ], "active_vehicle_id": null }` (or a saved vehicle ID).
`garage.vehicles.set_active` takes `vehicle_id` and returns `active_vehicle_id`;
only a vehicle owned by the authenticated user can be selected.

BRNavi зберігає структуровані дані авто в колекції MongoDB `garage`.
Власник визначається лише за авторизованою сесією, не за параметрами запиту.
`garage.vehicles.add` повертає `{ "vehicle": { "id": "...", "name": "...",
"category": "...", "verification": "none", "vehicle": { ... } } }`.
`garage.vehicles.list` приймає лише авторизацію сесії й повертає
`{ "vehicles": [ ... ], "active_vehicle_id": null }` або ID вибраного авто.
`garage.vehicles.set_active` приймає `vehicle_id` і повертає `active_vehicle_id`;
вибрати можна лише власне авто авторизованого користувача.

Example requests / Приклади запитів:

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "garage.vehicles.add",
  "params": {
    "access_token": "<session token>",
    "class_id": "passenger",
    "make_id": "22",
    "model_id": "4779",
    "year": 2020,
    "trim_id": "8873",
    "vehicle": {
      "make": { "id": "22", "name": "Toyota", "raw_data": {} },
      "model": { "id": "4779", "name": "Camry", "raw_data": {} },
      "year": 2020,
      "trim": { "id": "8873", "name": "LE", "raw_data": {} },
      "bodies": [],
      "engines": [],
      "mileages": [],
      "exterior_colors": [],
      "interior_colors": [],
      "selected_exterior_color": null,
      "selected_interior_color": null,
      "vin": null,
      "vin_data": null,
      "equipment_data": {}
    }
  }
}
```

```json
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "garage.vehicles.list",
  "params": { "access_token": "<session token>" }
}
```

The example's empty objects/arrays stand for original catalogue records and
technical data; the app sends actual loaded data. Tokens are never part of the
stored vehicle snapshot. These code changes do not deploy BRNavi automatically.

Порожні об'єкти/масиви прикладу позначають оригінальні записи каталогу та
характеристики; застосунок надсилає фактично завантажені дані. Токени не входять
до збереженого запису авто. Ці зміни коду не розгортають BRNavi автоматично.
Dropdowns use a non-focusable standard popup with a scrollable height capped at
240 dp, avoiding exposed-menu positioning failures during keyboard resizing.

Під зображенням форми додавання авто розміщені вкладки «Загальне» та «Комплектація».
Ідентифікація авто знаходиться в «Загальне», усі технічні дані — у «Комплектація».
BLoC керує вибраною вкладкою та зберігає дані при перемиканні.
Після вибору марки, моделі й року форма та профіль авто показують до шести результатів
пошуку Wikimedia Commons. Використовуються лише зображення CC0 і CC BY із доступними
авторством та посиланням на ліцензію; відповідність приблизна, фото може не знайтися.
На обох екранах слайдер фото є зображенням шапки під заголовком і кнопкою повернення.
Пошук виконується безпосередньо у Wikimedia, результати кешуються в пам'яті сесії.
Перший слайд завжди містить початкове зображення Mustang для поточної світлої або темної теми, зокрема під час фонового пошуку,
коли фото не знайдено або пошук завершився помилкою. Помилки та індикатор пошуку
не показуються.
Вибране у слайдері фото зберігається локально для кожного акаунта й авто, зокрема
коли його вибрано до додавання авто. Воно відновлюється у слайдері та показується
мініатюрою і напівпрозорим фоном картки гаража. Авторство та посилання на ліцензію
доступні у слайдері. Вибір фото не синхронізується між пристроями.

На екрані додавання транспорту при отриманні фокуса полем марки без очікування вводу
надсилається `car.makes_index2_get` без бізнес-параметрів через
авторизоване RPC-з'єднання. BLoC зберігає отриманий список `data` на час відкритого
майстра й фільтрує назви за входженням поточного тексту без урахування регістру.
Під час завантаження можна продовжувати введення; подальші зміни використовують
отриманий список без повторних запитів. Помилки показуються, зміна тексту повторює
невдалий запит.
Після вибору марки її назва залишається в полі, а безпосередньо під ним на тій самій
формі з'являється окреме поле моделі. Зміна марки повертає підказки зі збереженого
списку та скидає попередній вибір моделі.
Обидва поля є редагованими випадаючими списками зі станами в BLoC. Вибір марки
викликає `car.oemmakemodels_index_get` (`/api/models/v2`) з її назвою в `make`; отриманий `data` заповнює
список моделей. Цей метод повертає ID та назви моделей, але не роки й комплектації.
При виборі марки чи моделі BLoC зберігає весь початковий об'єкт із `data` у
`rawData` вибраного елемента, включно з додатковими полями; список показує лише `name`.
Фокус поля моделі відкриває завантажені підказки без введення тексту; втрата
фокуса закриває список. Цими переходами фокуса керує BLoC.
Вибір марки ставить курсор у кінець назви та залишає фокус на полі марки
із закритими списками; фокус у поле моделі переводить лише користувач.
Вибір моделі ставить курсор у кінець її назви
та ховає клавіатуру. BLoC зберігає позиції курсорів і UI-ефекти з підтвердженням виконання.
Після вибору моделі її назва залишається в полі та викликається
`car.years_index2_get` із назвами вибраних `make` та `model`. Масив цілих чисел
результату зберігається в `selectedModel.years` і показується кнопками вибору року
під полем моделі. Завантаженням, помилками, повтором і вибором керує BLoC; зміна
марки/моделі скидає рік та ігнорує застарілі відповіді.
Якщо вибрані марка й модель, натискання доступного року зберігає його та
автоматично відкриває вкладку «Комплектація», зберігаючи вибране авто.
Комплектація завантажує всі сторінки `car.oemtrims_index_get` із маркою/моделлю/роком.
Запити використовують `limit: 100`. Під заголовком марки/моделі/року список
комплектацій показує `data[*].description` із роздільниками; BLoC зберігає повний
вибраний запис. Поле не редагується, описи переносяться повністю, popup зростає
до доступної висоти екрана з прокруткою за потреби.
Вибір комплектації завантажує кузов через BRNavi методом
`car.makemodeltrimbodies_index2_get` із `trim_id` вибраного запису комплектації v2.
Запит використовує стандартну авторизацію сесії BRNavi без параметра `verbose`.
RPC-помилки зберігають вихідну причину для діагностики.
Замість вибору кузова показуються плашки з доступними характеристиками,
габаритами та місткістю в метричних одиницях. BLoC керує завантаженням, повтором
та відкидає застарілі відповіді.
Вибір комплектації також завантажує двигуни через
`car.makemodeltrimengines_index2_get` із тим самим `trim_id`. Плашки двигунів
замінюють список вибору двигуна; BLoC окремо керує їх завантаженням, помилками
та повтором. У всіх плашках комплектації кожна характеристика має окремий рядок,
довгі значення переносяться без обрізання.
Трансмісія та привід показуються в окремій плашці «Трансмісія» з тієї самої відповіді
про двигун вибраної комплектації, без додаткового запиту й списку вибору трансмісії.
Вони мають спільний із двигуном стан завантаження/помилки/повтору в BLoC
та не дублюються в плашці двигуна.
Витрата палива та запас ходу завантажуються окремо методом
`car.makemodeltrimmileages_index2_get` із вибраним `trim_id`, з усіх сторінок.
Витрата показується в л/100 км, кожна характеристика окремим рядком; BLoC окремо
керує завантаженням, помилками, повтором та відкидає застарілі відповіді.
До вибору комплектації технічні плашки, підказки характеристик і списки кольорів
не показуються: залишаються лише ідентифікація авто та вибір комплектації.
Зверху вкладки «Загальне» в одному рядку розташовані кнопки VIN, Scan та SmartCar
з іконками (Scan та SmartCar активні, поки з порожніми обробниками натискання).
VIN відкриває діалог із полем VIN та кнопкою «Пошук». Натискання «Пошук» одразу
закриває діалог і показує лоадер на екрані до завершення визначення авто.
Успішне декодування через
BRNavi знаходить марку/модель і роки в каталозі, заповнює обидва поля й відмічає
отриманий рік, закриває діалог та залишає вкладку «Загальне». Помилки лишаються
у повторно відкритому діалозі без перезапису вибраного авто; закриття ігнорує запізнілі відповіді.
Для кожної комплектації викликається `car.oemtrims_view_get` із її ID. Вкладка «Комплектація»
показує таблиці кольору кузова та салону кругами RGB діаметром 50 dp замість
текстових полів назв. Назви залишаються лише мітками доступності; вибіром керує
BLoC, вибраний круг позначається рамкою. Відсутній/некоректний RGB явно позначається
як недоступний, без вигаданого кольору. Варіанти без дублікатів взаємно фільтруються
за спільними комплектаціями. Назви характеристик у всіх інформаційних плашках
жирні, значення — звичайним зменшеним шрифтом у тому самому рядку.
Кнопка «Зберегти авто» розташована поза вкладками та активується після вибору
марки, моделі й року. Комплектація необов'язкова; наявні дані надсилаються через
`garage.vehicles.add`: оригінальні записи марки/моделі/комплектації за наявності,
характеристики кузова/двигуна/витрати, доступні та вибрані кольори й успішну
VIN-відповідь, якщо вона досі відповідає авто. Технічні запити та їхні помилки
не блокують збереження; невдача збереження залишає форму для повтору.
Успіх відкриває перевірку й оновлює `garage.vehicles.list`. Повний запис із сервера
зберігається в моделі авто. При виході з авторизованого акаунта локальні стани
гаража та форми очищуються, щоб не показувати авто попереднього користувача.
Показуються лише сумісні записи витрати/запасу ходу. Усі запити
проходять через авторизоване RPC-з'єднання BRNavi, не напряму до CarAPI. Початкові
об'єкти комплектації та характеристик зберігаються. Американські одиниці габаритів,
маси, витрати та відстаней переводяться в метричні; відсутність даних позначається явно.
BLoC керує завантаженням, помилками, повтором та ізоляцією запитів. Зміна авто/року
очищує попередню конфігурацію та ігнорує застарілі відповіді.
Списки використовують стандартний popup без перехоплення фокуса з прокруткою та
висотою до 240 dp, без проблемного позиціонування exposed-menu при зміні розміру
вікна клавіатурою.

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
This historical Compose capture matrix predates the animated S00 and does not include it.
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
Ця історична Compose-матриця створена до анімованого S00 й не охоплює його. Тестовий APK має власну
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
`git submodule update --init --recursive`. Gradle automatically builds the iOS native bridge before
its Kotlin/Native cinterop tasks. Build Android native artifacts with
`external/JSON-RPC-2-Client-KMP/scripts/build-android.sh` before building the Android target.
The iOS artifacts can also be prepared manually with
`external/JSON-RPC-2-Client-KMP/scripts/build-ios.sh`.

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
командою `git submodule update --init --recursive`. Gradle автоматично збирає нативний iOS-мост
перед cinterop-задачами Kotlin/Native. Перед збиранням Android згенеруйте нативні артефакти командою
`external/JSON-RPC-2-Client-KMP/scripts/build-android.sh`. iOS-артефакти також можна зібрати
вручну командою `external/JSON-RPC-2-Client-KMP/scripts/build-ios.sh`.

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
adaptive landscape rail as demo. The S08 profile follows the Figma portrait hierarchy and adapts
to landscape/tablet layouts. It displays backend identity and account type (`private` or
`corporate`), highlighting the server-reported type; missing or unrecognized types do not
silently select one. Detailed personal and account data are shown only after opening “Security
and personal data,” not on the main profile screen. The profile identity summary shows the account
name and email; an unconfirmed email is labeled accordingly. Company geolocation policy is not
inferred. The type selector is informational. Personal accounts do not display the organization
section. Its garage preview uses the authenticated garage service and opens
active-vehicle details. The first visit loads `garage.vehicles.list`; subsequent visits reuse
the session cache, updated after the server confirms a new active vehicle. Garage state is cleared
on logout, and changes made on another device appear after a new session or explicit reload.
Failures are shown in the preview with a retry action.
The full garage also reports load errors and supports retry. The security and
personal-data action opens personal/contact and account details without logout; favorites opens locally stored
places. Organization pages are not routed without verified membership data from the backend;
the corporate account type alone does not establish membership. The current API has no active-trip
contract; planned-route state remains in memory when visiting the profile, but no active trip is
fabricated. No Figma sample identities, memberships, vehicles, photos, or metrics are used as
account data. Profile editing and corporate policy controls are not connected.
Map/routing/search APIs remain unavailable, with no synthetic map or routes. Settings exposes
the existing local theme picker, not fake server settings.
In explicit demo mode, Map still opens the local demo profile (S08) and settings (S07).

The original backend contract was verified against BRNavi `src/auth.rs` and its pinned
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
- `auth.updateProfile` params: `access_token` plus `profile`, **not** the whole
  UserResponse. Scalar fields retain string values (booleans use `"true"`/`"false"`,
  datetime uses RFC3339); the local contacts extension accepts a native `contacts`
  array of `{type,value}` objects. It patches supplied fields and returns the same UserResponse.
  This contract is documented, not called by the current read-only UI.
- The contacts extension was deployed to both BRNavi instances on 2026-11-11
  using a locked source-snapshot image. BRNavi's Git dependency pins still need
  an authorized library publication and revision update before a standard rebuild.
  The deployed snapshot and rollback procedure are documented in BRNavi's README.

Авторизовані розділи Мапа/Гараж/Налаштування/Профіль використовують те саме нижнє меню
й адаптивну бокову панель в альбомній орієнтації. Профіль S08 відповідає ієрархії портретного
макета Figma та адаптується до планшета й альбомної орієнтації. Він показує тип акаунта з бекенда
(`private` чи `corporate`) з виділенням отриманого типу; невідомий тип не підмінюється іншим.
Детальні особисті дані та дані акаунта відображаються лише після відкриття блока «Безпека та
особисті дані», а не на основному екрані профілю. У зведенні профілю залишаються ім’я акаунта й
пошта; непідтверджена пошта позначається як «e-mail не підтверджено». Політика доступу компанії
до геолокації не виводиться без даних сервера. Селектор не змінює серверний акаунт.
Для особистого типу блок організації приховано. Прев’ю гаража використовує авторизований сервіс
і відкриває деталі активного авто. При першому відвідуванні завантажується
`garage.vehicles.list`; надалі використовується кеш сесії, який оновлюється після
підтвердження сервером зміни основного авто. При виході кеш очищується; зміни з
іншого пристрою з'являться після нової сесії або явного оновлення.
У разі помилки профіль показує повідомлення й кнопку повтору. Повний гараж також
показує помилку завантаження та дозволяє повторити запит.
Безпека та особисті дані відкривають особисті/контактні дані та дані акаунта без виходу; «Обране» відкриває
збережені локально місця. Екрани організації не відкриваються без підтверджених сервером даних
про членство: корпоративний тип акаунта сам по собі не підтверджує членство. Поточний API не
має контракту активного рейсу; стан планування маршруту зберігається під час перегляду профілю,
але активний рейс не вигадується. Демоімена, членства, авто, фото й показники не
використовуються як дані акаунта.
Редагування профілю й корпоративних політик не підключені. API мапи/маршрутів/пошуку
залишаються недоступними, без синтетичних мап і маршрутів. Налаштування містять наявний
локальний вибір теми, без імітації серверних перемикачів.
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
і `profile`: скалярні значення залишаються рядковими (boolean — `"true"`/`"false"`,
дата — RFC3339), а локальне розширення contacts приймає справжній масив об'єктів
`type`/`value`. Оновлюються лише передані поля, повертається той самий UserResponse.
UI його поки не викликає. Розширення розгорнуто на обох екземплярах BRNavi 2026-11-11
образом із зафіксованого знімка коду. Для стандартної перезбірки ще потрібні
окремо дозволена публікація бібліотеки й оновлення Git-ревізій залежностей BRNavi.
Знімок розгортання та порядок відкату описані в README BRNavi.

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

**EN.** Garage (authenticated Garage tab): `GarageBloc` (list/select active/details) and `AddVehicleBloc` (class → make → model → year+trim wizard, S10/S14/S15/S17) with separate ViewModels; `GarageService` is injected via `AppDependencies` (`RpcGarageService`). The JSON-RPC method names (`garage.vehicles.list/set_active/add`, `catalog.classes.list/makes.list/models.list`) and payloads are PROVISIONAL: the backend contract is not confirmed, so on failure the UI shows an error with retry (never an empty garage). Selection changes only after server confirmation. Configuration now displays CarAPI trim specifications through BRNavi as described above. Not yet: S11 dimensions screen, configuration persistence, S03 integration.

**UK.** Гараж (вкладка «Гараж» в автентифікованому режимі): `GarageBloc` (список/вибір активного/деталі) та `AddVehicleBloc` (майстер клас → марка → модель → рік+комплектація, S10/S14/S15/S17) з окремими ViewModel; `GarageService` інжектується через `AppDependencies` (`RpcGarageService`). Назви JSON-RPC методів (`garage.vehicles.list/set_active/add`, `catalog.classes.list/makes.list/models.list`) і формати ПОПЕРЕДНІ: контракт бекенду не підтверджено, тому при збої UI показує помилку з повтором (ніколи не порожній гараж). Вибір змінюється лише після підтвердження сервером. Конфігурація тепер показує характеристики комплектацій CarAPI через BRNavi, як описано вище. Ще немає: екрана габаритів S11, збереження конфігурації, інтеграції з S03.

### File structure rule / Правило структури файлів

**EN.** Every screen, its ViewModel and its BLoC (state, events, transitions) live in separate files (`XScreen.kt`, `XViewModel.kt`, `XBloc.kt`, with state/events in `XState.kt`). `DemoAccountScreen` is a thin host; settings/profile/garage/vehicle screens, `RecoveryBloc`, `RegistrationBloc` and `ThemeSettingsBloc` have own files (Recovery, Registration and Theme ViewModels are facades over their BLoCs). `StopPlacesBloc` is a stateless class (no singleton) in `StopPlacesBloc.kt`; app models moved from `App.kt` to `AppModels.kt`. Tests live in separate files.

**UK.** Кожен екран, його ViewModel і BLoC (стан, події, переходи) лежать в окремих файлах (`XScreen.kt`, `XViewModel.kt`, `XBloc.kt`, стан/події в `XState.kt`). `DemoAccountScreen` — тонкий хост; екрани налаштувань/профілю/гаража/авто, `RecoveryBloc`, `RegistrationBloc` і `ThemeSettingsBloc` мають власні файли (ViewModel Recovery, Registration і Theme — фасади над BLoC). `StopPlacesBloc` — клас без стану (не singleton) у `StopPlacesBloc.kt`; моделі застосунку винесено з `App.kt` до `AppModels.kt`. Тести — в окремих файлах.

**EN.** Demo navigation lives in `DemoAppBloc` (`DemoAppState.kt` state/events, `DemoAppBloc.kt` transitions) held by `DemoAppViewModel` (persistence of saved places and route stops); `XrnaviApplication.kt` only renders and dispatches events. Navigation state survives rotation but is no longer restored after process death.

**UK.** Демо-навігація живе в `DemoAppBloc` (`DemoAppState.kt` — стан/події, `DemoAppBloc.kt` — переходи) у `DemoAppViewModel` (збереження адрес і зупинок); `XrnaviApplication.kt` лише малює й надсилає події. Стан навігації переживає поворот екрана, але більше не відновлюється після завершення процесу.
