# XRNavi: адреси, транспорт і діагностика iOS — 27.10.2026

> **Оновлено після повторного запиту 27.10.2026.** Причину чорного кадру
> початкових Simulator-записів встановлено: SplashBoard відхиляв неповністю
> підписаний launch-ресурс. Чотири нові записи штатної ad-hoc збірки не
> відтворюють цей fallback. X9 залишається заблокованою. Реальний звук TalkBack
> захоплено, але надійність оголошень не підтверджено. Актуальний висновок —
> у [доповненні повторної діагностики](#повторна-діагностика-27102026);
> нижче збережено первинний звіт із його тодішніми обмеженнями.

**Результат:** контракт адрес закріплено перевірками стану та UI; категорія
демо-профілю більше не залишає актуальний легковий Preview для вантажного
транспорту. Фізичний X9 iPad, реальне озвучення читачів і остаточна атрибуція
чорного launch-кадру залишаються відкритими. Це не підтвердження готовності етапу
чи релізу.

Джерело: [Remaining Gaps, 26.10.2026](https://github.com/vsvictor/XRNavi/wiki/iOS-Map-Verification-Remaining-Gaps-2026-10-26).
У цьому звіті «цей цикл» означає лише перевірки 27.10.2026.

## Межі та збереження робочого дерева

Базовий HEAD — `65c92efe2b726d204eae66d69a2d3ed3978a6a6b`. Дерево було
**брудним до початку**: `App.kt`, `XrnaviScreens.kt`, `SharedCommonTest.kt`,
`MapScenarioUITests.swift`; також були `.wiki-publish/` і `tokens.dat`.
Наявні зміни інтегровані, не скинуті. Початковий diff окремо збережено локально
як `preexisting-working-diff.txt`. `tokens.dat` не читався й не включений у докази.

Зміни цього циклу не слід ототожнювати з усім diff від HEAD. Зокрема, перед
початком уже існували часткові виправлення категорій, явної цілі пошуку та
скидання Preview, але UI-тести ще очікували попередню поведінку.

Commit, push і публікація не виконувалися. Нових SDK, продуктових екранів,
реальної авторизації, маршрутизації чи геолокації немає. Висоти карток і
алгоритм sheet anchors не змінювалися.

## Контракти та зміни

### Адреси

Поточний контракт: CTA «Вибрати початкову точку» передає `SearchTarget.Origin`
**до** переходу в Search; результат записується лише в активну ціль.
Підтверджені адреси не є пошуковою чернеткою.

`AddressSearchState` об'єднує підтверджені адреси, активну ціль і запит; той самий
код переходів використовується застосунком і common tests. `open`, `activate`,
`select` і `cancel` не очищають підтверджену адресу іншого поля. `listSaver`
зберігає ціль, обидві адреси та запит. Додано назви активних полів, очищення
запиту й скасування пошуку в accessibility semantics.

Важливе обмеження висновку: спостереження зі старого запису про заповнення
Destination замість Origin **не відтворилося на поточному checkout**.
Навіть базовий HEAD уже передає Origin у цій CTA. Причину саме історичної
невідповідності не встановлено; старий артефакт не підмінено припущенням про
типову ціль. Нова фізична та Simulator перевірки підтверджують поточний контракт.

Після підтвердження нової адреси повернення відбувається в Map без актуального
Preview. Скасування чернетки повертає попередній екран: незмінений Preview можна
залишити, оскільки підтверджені параметри не змінилися.

### Транспорт

У дослідженій реалізації немає списку гаражних автомобілів чи реального профілю
маршрутизатора. `vehicleClass` керував категорією та попередженням запуску,
а картка Mustang і числовий Preview були незалежними константами. Тому
«Вантажне» могло співіснувати з легковими характеристиками: перемикач не обирав
іншу машину й нічого не фільтрував.

Збережено цю демо-семантику, явно підписану «Категорія демо-профілю».
Наявний лише `demoVehicle` — Ford Mustang GT, «Легкове», 1,4 м / 1,8 т.
Назва, категорія та характеристики мають спільне джерело. Для «Вантажне» й
«Спецтехніка» сумісного профілю немає: показано недоступність, а не нове авто.
Селектор доступний і після виходу з Preview, тому можна явно повернутися до
наявного легкового профілю без пастки в недоступній категорії.

Зміна категорії скидає Preview; повернення до «Легкове» не відновлює старий
Preview автоматично. CTA «Профіль недоступний» пояснює блокування, а не
запускає маршрут. Легкова CTA також лише показує чесне попередження
«Маршрут і навігацію не підключено».

## A — незалежно виконано в цьому циклі

Локальний корінь доказів **E**:

`/Users/victor/.copilot/session-state/d741b6ef-fcb2-4da3-9c7f-f01c910ab68e/files`

Назви нижче відносні до E, якщо не зазначено інше. Це локальні артефакти,
**не публічні URL**. `artifact-sha256.json` містить хеші бінарників, медіа,
джерел і завершених xcresult; manifests вкладень пов'язують PNG із тестами,
часом та пристроєм.

| Перевірка | Пристрій / ОС / умови | Кроки й очікування | Фактичний результат і підтвердження |
|---|---|---|---|
| Common tests + Android Debug | Host macOS; UI-тема, орієнтація, текст і панель — N/A; app 1.0, versionCode 1 | `:shared:testAndroidHostTest :androidApp:assembleDebug`; перевірити переходи адрес, відновлення стану та доступність профілів | 12/12 тестів, 0 помилок; APK зібрано. `android-tests.log`, XML у `shared/build/test-results/testAndroidHostTest/` |
| Common tests iOS | iOS Simulator / Kotlin Native; UI-умови — N/A | `:shared:iosSimulatorArm64Test`; той самий common-контракт на iOS | 12/12 тестів, 0 помилок. `ios-common-tests.log`, XML у `shared/build/test-results/iosSimulatorArm64Test/` |
| Origin, перемикання, скасування, зміна адрес | iPhone 16 Pro Simulator, iOS 18.6 / 22G86; Light; Large; Portrait/Landscape; Search/клавіатура, Collapsed Preview/Map; app 1.0 (1) | Від початкової Map натиснути Origin CTA, ввести адресу, повернути під час пошуку, вибрати результат; Destination, перемикання полів, скасування чернетки з Preview, підтвердження іншої адреси | Активний Origin і запит збереглись; Destination спочатку порожній; інша підтверджена адреса не зникла; скасування зберегло старий Preview, новий вибір скинув його. `testOriginCTASelectionSwitchCancelAndRotation`, `iphone-light-final.xcresult` |
| Preview, транспорт, клавіатура, повернення | Той самий iPhone; системна Light/Large; перемикання теми всередині app двічі; обидві орієнтації; Collapsed/Partial/Expanded; клавіатура в обох орієнтаціях | Обидві адреси → явний Preview → стани панелі/drag → «Вантажне» → поворот/тема/фон → «Легкове» → новий Preview → CTA | Старі тривалість/маршрут і легкова картка не лишилися актуальними для вантажного класу; повернення до легкового потребує Preview; навігація не стартує. Разом з окремим keyboard test: **3/3**, 0 skipped. `iphone-light-final-summary.json`, 29 PNG у `iphone-light-attachments/` |
| Адресний контракт на iPad | iPad Pro 13-inch (M4) Simulator, iOS 18.6 / 22G86; Light; Large; Portrait/Landscape; Search/клавіатура та Collapsed Preview/Map; app 1.0 (1) | Той самий Origin/switch/cancel/rotation/address-change сценарій | **1/1**, 0 skipped. `ipad-light-ui.xcresult`, `ipad-light-ui-summary.json`, 6 PNG у `ipad-light-attachments/` |
| Окрема Dark/max конфігурація | iPhone 16 Pro Simulator, iOS 18.6 / 22G86; **Dark**, **accessibility-extra-extra-extra-large**; обидві орієнтації, клавіатура; усі рівні панелі; app 1.0 (1) | Перед запуском зчитати `simctl ui appearance/content_size`; повний Preview/transport test та Origin/switch/cancel test | **2/2**, 0 skipped. `iphone-dark-max-conditions.json`, `iphone-dark-max-ui.xcresult`, `iphone-dark-max-ui-summary.json`, 24 PNG у `iphone-dark-max-attachments/`. На Portrait PNG видно фактично збільшені шрифти, а не лише змінену системну настройку |
| Фізичний touch Android | Samsung SM-X135F, Android 16 / API 36; `font_scale=1.0`; спочатку Light, потім внутрішня Dark; Portrait/Landscape; Search/IME; Collapsed/Expanded, перехід через Partial; app 1.0 / code 1 | Після погодженого вимкнення TalkBack: чистий процес → демовхід → початкова CTA → Origin → Destination через запит `22` → Preview → вантажний клас → поворот/тема/фон → легковий клас → новий Preview → CTA | 11 checkpoints пройдено: потрібні адреси/CTA наявні, за вантажного класу немає старої тривалості, легкових характеристик і запуску маршруту. `android-physical-manifest.json`, `android-map-check.py`, 11 пар `android-*.png/.xml`. Початкові reader/rotation settings відновлено й повторно зчитано; TalkBack знову bound |
| Чотири cold-process записи | iPhone 16 Pro та iPad Pro 13-inch (M4) Simulator, iOS 18.6 / 22G86; окремо Light і Dark; Portrait; Large; SpringBoard → SignIn, панель N/A; cold-build 1.0 (1) | Завершити процес; почати запис і дочекатися першого записаного кадру; лише тоді запустити app | У всіх чотирьох є темний проміжок, далі SignIn. `cold-launch-manifest.json`, чотири `*-cold.mp4` і `*-contact.png`; фазу не оголошено остаточно встановленою |

Звичайні XCTest перевірки тепер пропускаються, якщо
`UIAccessibility.isVoiceOverRunning == true`; VoiceOver-тести залишені окремими
методами. У наведених успішних Simulator-серіях skipped = 0. Це не перевірка
реального мовлення.

### Візуальні межі успішних XCTest

У Dark/max Portrait збільшений текст підтверджено переглядом PNG. Водночас
видно тісний верхній branding/theme рядок і скорочені підписи нижніх вкладок.
Повну візуальну доступність за максимального тексту не оголошено.
Один Landscape/keyboard attachment містить кадр переходу з повернутим вмістом
і чорною ділянкою; він не використовується як доказ усталеного landscape
layout. Успішна взаємодія XCTest підтверджує адресний стан і доступність
перевірених дій, але не закриває ці візуальні питання.

Початковий unsigned Simulator XCTest запуск не завершив result bundle
коректно й був зупинений. Окремий ранній адресний test впав через неоднозначний
substring-локатор: замість результату було натиснуто вже підтверджену Origin
адресу з тим самим текстом. Локатор результатів звужено до кнопок вибору адрес;
наступні завершені серії наведені вище. Ці невдалі спроби не зараховані як pass
і не приписані VoiceOver.
Перша Android automation спроба після background натиснула текст «Легкове»
в поясненні замість однойменної кнопки. Після exact-first локатора повний
11-checkpoint прохід повторено успішно; це не зараховано як регресію продукту.

## Діагностика launch-кадру

Фактичний cold bundle: Xcode `27A9275`, SDK `iphonesimulator27.1`, app `1.0 (1)`,
bundle ID `com.mobilespace.xrnavi.XRNaviX9VB8434PD`. Runtime пристроїв — **18.6**,
не версія SDK. Cold процес не означає очищений системний launch snapshot.

У зібраному `Info.plist` є `UILaunchStoryboardName=LaunchScreen` і
`UILaunchScreen={}`. У bundle є `LaunchScreen.storyboardc` та `Assets.car`.
`assetutil` підтвердив Light/Dark варіанти `LaunchOrbit`,
`LaunchBackgroundColor`, `LaunchTextColor`, `LaunchSecondaryTextColor`.
Повторно зчитані конфігурація та інвентар збережені в
`bundle-launch-config.json` і `ios-launch-assets.json`; compiled storyboard
files окремо захешовані в маніфесті.

Через LLDB на `ContentView.swift:9`, після присвоєння тла Compose controller,
отримано:

| Об'єкт | Light RGBA | Dark RGBA |
|---|---|---|
| `viewController.view.backgroundColor`, resolved | 0.965 / 0.973 / 0.988 / 1 | 0.043 / 0.071 / 0.125 / 1 |
| Фактично завантажений `LaunchScreen` controller, resolved | 0.965 / 0.973 / 0.988 / 1 | 0.043 / 0.071 / 0.125 / 1 |

У цей момент `UIWindow.backgroundColor == nil`, а SwiftUI root view має
динамічний `systemBackgroundColor`. Стек містить
`UIApplication._firstCommitBlock` → SwiftUI layout →
`ComposeView.makeUIViewController`; controller ще не повернуто в SwiftUI.
На контрольному screenshot у цей момент тло чорне.
Докази: `launch-lldb.log`, `launch-colors-lldb.log`,
`launch-resolved-colors-lldb.log`, `ipad-before-compose-frame.png`.
Це окремий **інструментований** запуск, не вимірювання нормальної тривалості
launch. Breakpoints видалені, debugger від'єднаний; діагностичний процес завершено.

Нові неінструментовані відео дають таку послідовність:

| Відео | PTS центральних майже чорних кадрів, с | Тривалість контейнера |
|---|---|---|
| iPhone Light | 0.497–0.807 | 1.615 с |
| iPhone Dark | 0.382–0.533 | 1.643 с |
| iPad Light | 0.408–0.668 | 1.557 с |
| iPad Dark | 0.445–0.555 | 1.888 с |

Це PTS відео, не час від створення процесу. Центральний ROI 100×100 визнано
майже чорним, коли понад 98% його пікселів мають усі RGB-компоненти ≤4;
метрика описує зображення, **не визначає фазу compositor-а**.
`launch-frame-timeline.json` зберігає всі відповідні PTS. Contact sheets — 16
кадрів із кроком 0.1 с. Статичні інтервали Simulator recording не слід
ототожнювати з wall-clock тривалістю записувального процесу.

Висновок обмежений: чорний стан спостерігається до завершення першого
SwiftUI/CA commit; відсутність asset або неправильний Light/Dark колір
controller-а не підтвердились. Відокремити системний snapshot від системної
launch presentation за цими даними не вдалося. Пробна збірка на окремому ID
без `UILaunchScreen={}` не була валідною парною cold-from-SpringBoard серією
і не використовується для причинного висновку. Діагностичний control app
видалено. Launch source/config не змінювалися; «Splash виправлено» не заявлено.
Таймера, другої Compose-заставки чи примусового утримання LaunchScreen немає.

## X9: перевірений блокер і потрібна дія

На початку **всі чотири** target-level `DEVELOPMENT_TEAM` були YP6FLB38QN,
усупереч тексту попереднього звіту; `TEAM_ID` у xcconfig залишався X9VB8434PD.
Обидва targets, Debug/Release, переведено на `$(TEAM_ID)`.
`xcodebuild -showBuildSettings` незалежно підтвердив
`DEVELOPMENT_TEAM = X9VB8434PD` для app і UI tests.

Наявний Xcode account cache показує лише Personal Team YP6FLB38QN.
У двох стандартних локальних каталогах не виявлено профілів X9;
наявні XRNavi development profiles мають TeamIdentifier YP6FLB38QN,
включно з application identifiers:

- `YP6FLB38QN.com.mobilespace.xrnavi.XRNaviX9VB8434PD`;
- `YP6FLB38QN.com.mobilespace.xrnavi.iosAppUITests.xctrunner`.

Виконано **одну** цільову `build-for-testing -allowProvisioningUpdates`
спробу з явним `DEVELOPMENT_TEAM=X9VB8434PD` для фізичного iPad mini (A17 Pro),
iOS 27.0.1 / 24A446. Результат — `No Account for Team "X9VB8434PD"` в обох
targets і відсутність development profiles для
`com.mobilespace.xrnavi.XRNaviX9VB8434PD` та
`com.mobilespace.xrnavi.iosAppUITests.xctrunner`.
`x9-provisioning.log` зберігає помилки. Нових X9 app/runner артефактів немає,
отже перевірити їхні embedded TeamIdentifier/application-identifier та підпис
неможливо. Старий YP app не зарахований як X9.

Власник X9 має перевірити членство **наявного** Apple Account у команді,
прийняте запрошення та право на development provisioning; забезпечити профілі
для app і runner із зареєстрованим iPad. Після цього оновити teams/profiles
наявного Account у Xcode й перевірити готові app та runner окремо.
Повторно додавати той самий Account, передавати ключі чи перемикатися на
Personal Team не потрібно. Доступ до порталу X9 лише з локального cache
довести не можна; точний факт цього циклу — Xcode не зміг використати X9.

## B — повідомлено користувачем

Нових результатів фактичного проходження чи озвучення користувачем у цьому
циклі не отримано. Погодження тимчасово вимкнути TalkBack — дозвіл на перевірку,
не користувацький результат тесту.

## C — історичні результати

Спостереження 26.10 про Origin/Destination, Mustang у вантажній категорії,
старі cold videos, попередні max-text проходи та фізичний YP запуск із
VoiceOver залишаються історичними. Вони не зараховані як нові A.
Успішні старі виправлення висот карток не відкривалися повторно.

Публічний raw-текст джерела отримано з HTTP 200. **Усі 22 унікальні**
`images/...` посилання цієї сторінки окремо перевірено HEAD-запитами:
22 × HTTP 200. Результат — `historical-public-links.json`.
Це перевірка доступності історичних файлів, не нова візуальна чи поведінкова
перевірка їхнього вмісту. Нові докази цього циклу не завантажені в wiki.

## D — не перевірено, гіпотези або заблоковано

| Пункт | Точний статус |
|---|---|
| Причина історичного неправильного поля з Origin CTA | Не встановлена; поточний source уже передавав Origin, новий сценарій не відтворив помилку |
| Чорний launch-кадр | Відтворено та локалізовано до завершення першого commit; snapshot проти launch presentation остаточно не розділено, виправлення немає |
| Фізичний iPad за X9: Map, поворот, тема, клавіатура, background | Заблоковано account/profiles X9; не запускалося під YP заради успіху |
| Фізичний холодний iOS запис | Не виконано в цьому циклі |
| Реальні VoiceOver оголошення, порядок фокусу, traversal, прихований контент | Не виконано. На iPad фактично зчитано VoiceOver enabled; налаштування не змінювалися. X9 блокер не дозволяє чесно зарахувати фізичний Map прохід |
| Реальні TalkBack оголошення, фокус і touch exploration | Не виконано. До й після touch-проходу TalkBack enabled/bound; touch перевірка виконувалася лише з погодженим вимкненням і не є TalkBack-проходом |
| Max-text візуальні межі | Перевірені адресні/transport дії пройшли; верхній рядок/підписи вкладок і перехідний landscape кадр не дають підстав заявити повну візуальну доступність |

Ручний reader-прохід потрібен окремо на обох платформах: фактичні оголошення
Origin/Destination і результатів, порядок фокусу, транспорт, розгортання та
згортання панелі без drag, CTA/демо-попередження/повернення та відсутність фокусу
на прихованому вмісті. Accessibility tree, screenshots, active reader і XCTest
не замінюють слухової перевірки. Якщо її виконає користувач, результат слід
додати до B з пристроєм/ОС, збіркою, темою, орієнтацією, текстом і фактичними
оголошеннями, а не переносити до A.

## Ідентифікація перевірених артефактів

Усі збірки нижче — **локальні, з брудного дерева**. HEAD сам по собі
не ідентифікує їх. Source hashes у `artifact-sha256.json` стосуються коду
перевіреної збірки; цей Markdown до бінарників не входить.

| Артефакт | SHA-256 |
|---|---|
| Android Debug APK | `4df0b88c330d30cd7681c86ed83484560da09598c5c173cc59435feacf42ab8c` |
| Фінальний Simulator `XRNavi` executable | `0d0fae9837cdde5840b626c27a374ebfd3e3e12d4c8c9fd360cfe69f93bda8f5` |
| Фінальний `XRNavi.debug.dylib` — код app, не лише executable trampoline | `5676f669bb1e3438836425a9088da140ef3d353f95f97c8c1e624d13407929ab` |
| Фінальний UI-test binary | `47627666ea6904d21d801e448a89ecae11e37282f79cea2df2b05c72c36a38b4` |
| Cold-build `XRNavi` executable, до ad-hoc Simulator rebuild | `d039b6ecf777fd8d3d1e92a11b4c2821d927ba56783dfb1502ce36a97a7e1346` |
| Cold-build `XRNavi.debug.dylib` | `f1869dad313e752cbf21d7b204edf1a496675498d593d025167ad826cf5b9cf2` |
| `Assets.car` | `09e9dd940b87a3454e83102540dab4929a6dd9bbe370bbf32516e89b3b10359f` |
| iPhone Light cold video | `5f0aae6dd5094ae92c3de8c3d3c5008531fae2389d1639b477afcc1cbe3910eb` |
| iPhone Dark cold video | `3215294c682763c05220e84f514be5f79abe3d40f6b303210267d7576b3d9e65` |
| iPad Light cold video | `088864e769d9f9e96c62fc99f95b5fd36bdfe7d10857e5e32b8dd9193c1689d9` |
| iPad Dark cold video | `7bc6ed90501c58da71c07acd30cfac0b8e798b4499586473295f0e9e30e0436c` |
| Aggregate перевірених source-файлів | `9af6d1c2f3608ea33ca3f0233cc8d119e1f08cd95e2469b87e3d09618f7d0388` |

Фінальні Simulator артефакти мають звичайне ad-hoc signing; це **не доказ**
Team X9 на фізичному пристрої. Cold-записи зроблено до наступних
Simulator signing rebuilds; Kotlin/Swift код app між цими запусками не змінено.
Хеші двох варіантів не змішані.

Aggregate для source і завершених xcresult обчислено як SHA-256 від відсортованих
рядків `relative POSIX path + NUL + file SHA-256 + LF`. Маніфест містить також
індивідуальні SHA-256 PNG та manifests. Профілі, приватні ключі, credentials і
матеріали підписування до звіту не включені.

SHA-256 самого `artifact-sha256.json`:
`a139255f035b543186652132211b827342881b07b8db7cd9ec8213dc1197dc18`.
Після підготовки звіту дерево залишається брудним: п'ять файлів коду/проєкту,
README і новий звіт; початкові `.wiki-publish/` та `tokens.dat` залишені на місці.

## Повторна діагностика 27.10.2026

Цей розділ замінює попередній відкритий висновок щодо причини чорного кадру.
Діагностика не змінювала код застосунку, кольори, LaunchScreen, таймери або
Compose UI. Нові докази залишаються локальними в тому самому E; commit, push
і публікація не виконувалися.

### A — встановлена причина чорного кадру

**Це системний чорний fallback замість launch snapshot після відмови
SplashBoard перевірити ресурс збірки, створеної з `CODE_SIGNING_ALLOWED=NO`.**
Такий Simulator bundle не є буквально «без будь-якого підпису»: Mach-O має
автоматичний linker ad-hoc підпис, але bundle не має resource seal.
Ця відмінність від повного ad-hoc підписування істотна для LaunchScreen.

Незалежно зчитано системні журнали iPhone та iPad за час початкових чотирьох
записів. У них є послідовність:

1. `Resource validation error: Security error -67056` при читанні
   `LaunchScreen.storyboardc/launch-vc-view-launch-view.nib`.
2. `Snapshot generation resulted in an error with code: 11`.
3. App позначено як `bad launch image until it is updated`.
4. Під час запусків запит snapshot відхилено через `denylisted`.

На iPhone відмова сталася о 07:36:32, запуски — 07:36:34 і 07:36:41
(локальний час журналу); на iPad — 07:36:49, запуски — 07:36:51 і 07:36:58.
Це збігається з UTC часом у початковому `cold-launch-manifest.json`.
Докази: `retry-historical-splashboard.log`,
`retry-ipad-historical-splashboard.log`. Це нова діагностична перевірка
збережених системних подій, а не новий запуск того старого бінарника.

Контроль без зміни вихідного коду підтвердив причинний зв'язок:

| Контроль | Фактичний результат | Доказ |
|---|---|---|
| Новий bundle ID, linker-signed copy | Чорний launch на першому й повторному cold запуску; та сама помилка -67056 і denylist | `retry-paired-linkersigned-first.mp4`, `retry-paired-linkersigned-second.mp4`, `retry-paired-linkersigned-system.log` |
| Інша copy тих самих ресурсів із повним ad-hoc resource seal | Видимий штатний LaunchScreen, без чорного центру; обидва cold запуски | `retry-paired-sealed-first.mp4`, `retry-paired-sealed-second.mp4` |
| Linker-signed bundle зупинено до коду app | Чорний екран; LLDB підтвердив `_dyld_start` | `retry-linkersigned-precode.png`, `retry-linkersigned-precode-stack.log` |
| Той самий bundle ID оновлено лише повним ad-hoc підписом, без uninstall між цими двома вимірами | На `_dyld_start` уже видно правильну заставку; `Info.plist`, dylib, assets і storyboard не змінено | `retry-same-id-sealed-precode.png`, `retry-same-id-sealed-precode-stack.log`, `retry-signature-comparison.json` |

У всіх цих контролях: iPhone 16 Pro Simulator, iOS 18.6 / 22G86, Light,
Portrait, Large; панель N/A, SpringBoard → LaunchScreen → SignIn.
Свідоме призупинення процесу було лише діагностичним прийомом; у застосунок
не додано утримання заставки.

`codesign --verify --deep --strict` дає для linker-signed copy exit 1:
`code has no resources but signature indicates they must be present`.
Повністю ad-hoc copies проходять із exit 0.
Для same-ID пари хеші `Info.plist`, `XRNavi.debug.dylib`, `Assets.car` та
трьох compiled storyboard files тотожні; змінено підпис executable та
додано bundle resource seal. Усе зафіксовано в
`retry-signature-comparison.json`.

Отже, це не помилка встановлених Light/Dark кольорів і не перший чорний
Compose-кадр: чорний стан контрольного bundle наявний ще на `_dyld_start`.
Статус `denylisted` може зберігати проблему до оновлення app, але причина
його появи тепер відома — відмова перевірки ресурсу, а не просто невідомий
«старий кеш».

### A — чотири нові штатні cold записи

Перевірено саме встановлену фінальну Simulator-збірку попереднього UI циклу:
app 1.0 (1), Xcode 27A9275, SDK iphonesimulator27.1. Хеші executable
`0d0fae98…`, code dylib `5676f669…`, assets `09e9dd94…` збігаються з таблицею
первинного звіту. Ресурси й код не перебудовувалися для отримання кращого
кольору. `retry-installed-builds.json` містить повні хеші та конфігурацію.

| Запис | Умови | Кадрів / майже чорний центр | Очікування та факт |
|---|---|---|---|
| `retry-iphone-light-cold.mp4` | iPhone 16 Pro, iOS 18.6 / 22G86; Light; Portrait; Large; панель N/A | 85 / 0 | Штатна заставка перед SignIn; чорний fallback не відтворено |
| `retry-iphone-dark-cold.mp4` | Той самий iPhone; Dark; Portrait; Large; панель N/A | 49 / 0 | Темний ідентифікований LaunchScreen із логотипом, не суцільний чорний кадр |
| `retry-ipad-light-cold.mp4` | iPad Pro 13-inch M4 Simulator, iOS 18.6 / 22G86; Light; Portrait; Large; панель N/A | 70 / 0 | Штатна заставка → SignIn, без попереднього fallback |
| `retry-ipad-dark-cold.mp4` | Той самий iPad; Dark; Portrait; Large; панель N/A | 74 / 0 | Темна заставка → SignIn, без попереднього fallback |

Кожний процес завершено перед записом; запис почався на SpringBoard і був
готовий до `simctl launch`. Кеши не видалялися. Збережено before/after PNG,
відео, recorder logs, contact sheets і `retry-cold-manifest.json`.
У `retry-frame-analysis.json` перевірено **всі декодовані кадри**, а не лише
contact sheet, за попереднім критерієм чорного центру; contact sheets окремо
переглянуто. Ця метрика не доводить відсутність кожного можливого іншого
візуального артефакту.

Перша спроба control із повністю видаленими Mach-O підписами взагалі не
запустилася; її коротке відео **не є успішним запуском** і виключене з
порівняння. Коректний контроль використовує справжній linker-signed bundle.
Інтеграція IDE також знову не створила підтвердженої debug session; стеки
отримано окремим LLDB attach. Діагностичні apps видалено із Simulator,
призупинені процеси завершено, breakpoints очищено, початкові Light теми
обох Simulator відновлено.

**Рекомендація:** для наступних launch-доказів використовувати стандартне
Simulator ad-hoc підписування Xcode, не вимикати signing через
`CODE_SIGNING_ALLOWED=NO`; перед install перевіряти resource seal.
Не змінювати `ContentView`, LaunchScreen або кольори заради компенсації
неправильно зібраного перевірочного артефакту. Це не підтвердження поведінки
фізичного X9-підписаного iPad — він усе ще заблокований.

### A/D — повторна X9 спроба

Фізичний iPad mini A17 Pro підключений, paired, Developer Mode enabled,
iOS 27.0.1 / 24A446. Наявний Xcode account знову показує лише Personal Team.
Виконано одну нову цільову `build-for-testing -allowProvisioningUpdates`
спробу з явним `DEVELOPMENT_TEAM=X9VB8434PD`. Для **обох** targets знову:
`No Account for Team "X9VB8434PD"` та відсутність потрібних development
profiles. Доказ — `retry-x9-provisioning.log`. Нових X9 app/runner немає;
їхні підписи перевірити й фізичний Map прохід виконати неможливо.

На початку цього повтору `project.pbxproj` уже не мав попереднього локального
diff; актуальні build settings знову показують YP6FLB38QN. Зміна відбулася
між циклами, її не перезаписували. Цей повтор використовував X9 лише явним
CLI override й **не змінював** project signing.
Рекомендація для власника: надати наявному Account доступ/профілі X9;
для подальшого штатного запуску перевірити також target-level
`DEVELOPMENT_TEAM` у `project.pbxproj`, щоб вони не перекривали `TEAM_ID`.
Повторне додавання Account або заміна X9 на YP не виконувались.

### A/D — автономна спроба реального TalkBack audio

На фізичному Samsung SM-X135F / Android 16 API36 встановлений APK має
SHA-256 `4df0b88c…`, тобто це попередньо перевірений Debug artifact.
Умови: Light, Portrait, font_scale 1.0; Search без Map-панелі. TalkBack
enabled/bound, touch exploration enabled до й після спроб; налаштування
читача не змінювалися.

Після прохання користувача перевіряти самостійно виконано не лише XML-знімки:

- `scrcpy` playback/dup захоплення дало тишу; це не висновок про відсутність
  мовлення на пристрої.
- Прямий output capture захопив фактичний цифровий аудіосигнал пристрою разом
  із відео: `retry-talkback-output-probe.mkv/.wav`. Мікрофон, навколишні звуки
  та зовнішні speech API не використовувалися.
- Додатковий control із тимчасовою UHID клавіатурою та Alt/Alt+Shift навігацією
  збережений як `retry-talkback-hid.mkv/.wav`, action log і focus PNG.
  Надійного наскрізного порядку TalkBack-фокусу цей probe не встановив.
- Наявним `whisper-cli` із локальною multilingual small model виконано
  offline ASR. Автовизначення й явні uk/en спроби не дали надійної
  транскрипції оголошень. Машинні фрази на кшталт «foreign language» не є
  фактичним текстом TalkBack і не прийняті за результат.

Samsung TTS engine — `com.samsung.SMT`, системний default locale —
`com.samsung.SMT:en_US`. Це контекст можливої мовної невідповідності, **не
доведена причина** ненадійного ASR й не доказ конкретного голосу кожної
фрази. Голос, швидкість і мову не перемикали без погодження.
Тимчасовий UHID пристрій, control server, adb forward і redirects прибрані;
TalkBack залишився активним.

Точний підсумок: **реальний звук захоплено незалежно**, але його мовна
зрозумілість, точні назви Origin/Destination, повний focus order, transport,
non-drag sheet actions і відсутність прихованого фокусу залишаються D.
Наявність сигналу та ненадійна ASR-транскрипція не замінюють перевірки
оголошень. Від користувача не отримано результатів прослуховування, тому
нової категорії B немає й прохід йому не приписано.

На iPad VoiceOver повторно зчитано як enabled, стан не змінювали.
Озвучення XRNavi за X9 залишається D через provisioning blocker; успіхом
наявного старого YP app це не підмінено.

### Цілісність повторних доказів

Новий `retry-artifact-sha256.json` зберігає SHA-256 126 локальних файлів
повтору, installed binary fingerprints, поточні source hashes, dirty status
і виключені невдалі probes. Первинний маніфест не перезаписано.

| Новий cold video | SHA-256 |
|---|---|
| iPhone Light | `a6d1f37f1809bddf3deb9d3bdd98ce0c68752ddb092517d8c6e53bab9b271af9` |
| iPhone Dark | `31dd8b103b68f388906b36cff0710f087553eda785c3c7fb26a014e184cabd5a` |
| iPad Light | `3c9b75a7374c3277192089486d38f884b6bc5033a3b72a811e107b7f42749435` |
| iPad Dark | `60f77b92b41bead8e0b4c9dbe0b174670c5357da818a4f06788e11964fa85e7e` |

SHA-256 нового маніфесту:
`aecf56f91fcd1c79c554c76a0aab786ef87ba911327792c1c2a570275f52c443`.
HEAD не змінився; дерево залишається брудним. Програмні зміни попереднього
циклу та сторонні файли збережені. Усі нові медіа лише локальні —
публічних посилань на них немає.
