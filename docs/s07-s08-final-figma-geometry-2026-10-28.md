# S07/S08 — завершальний цикл узгодження геометрії з Figma

**Дата перевірки:** 28 жовтня 2026 року
**Стан:** точкові візуальні корекції внесені й перевірені; повна pixel-perfect відповідність не заявляється.

## Результат

Після останнього уточнення Figma внесено невеликі корекції геометрії S07/S08 без зміни продуктових контрактів і демосемантики:

- Tablet Landscape: зовнішні відступи контенту `24dp`, проміжок між колонками `24dp`, мінімальна висота заголовка екрана `72dp`.
- Згруповані рядки налаштувань: мінімум `65dp`; зони натискання не зменшувалися, довгі пояснення й далі можуть збільшувати висоту рядка.
- S07 Phone Portrait: heading секцій налаштувань `11sp / 13sp`.
- S08: контрольований проміжок між порожнім значенням статистики й підписом: `2dp` у Phone Portrait, `4dp` у Tablet Landscape.
- У iOS UI-регресію додані runtime-перевірки tablet-landscape header/insets/column gap і мінімальної висоти рядка теми в Phone Portrait.

Figma-дані для порівняння отримано через `get_metadata`, `get_screenshot` і variables. `get_design_context` для релевантних вузлів завершувався timeout, тому структурний design context не отриманий. Цільові вузли включали S07 Tablet Landscape Light `179:9983` і S07 Phone Landscape Light `179:4234`. Вимірювання UI підтверджувались окремими runtime assertions; це не числовий pixel-diff.

## Перевірки цього циклу — категорія A

| Перевірка | Пристрій/ОС і умови | Очікуване | Фактичне |
|---|---|---|---|
| Shared tests, Android Debug APK, iOS Simulator compile | macOS; робоче дерево без commit | Спільні тести й цільові збірки успішні | Успішно; shared `52 tests, 0 failures, 0 errors, 0 skipped` |
| Android profile/settings runtime | Samsung SM-X135F, Android 16 / API 36; Portrait-only; font scale 1.0; accessibility services вимкнені; системну орієнтацію не змінювали | S07/S08, Light/Dark, локальні налаштування, dialogs та повернення працюють; попередня тема відновлюється | Instrumentation `OK (1 test)` |
| iOS profile matrix + прокручування до гаража | iPhone 16 Pro Simulator, iOS 18.6 / 22G86, 402×874 pt; Light system appearance; Dynamic Type Large; VoiceOver вимкнений; Portrait/Landscape Left; клавіатура закрита | S07/S08, runtime-геометрія, гараж і діалоги доступні після прокручування/повороту | `testFigmaVariantMatrix` і `testLandscapeProfileGarageScrolling`: 2 passed, 0 failed |
| iOS profile matrix | iPad Pro 13-inch (M4) Simulator, iOS 18.6 / 22G86, 1032×1376 pt; ті самі theme/text/accessibility умови | S07/S08 адаптивні, планшетні відступи й колонки відповідають перевіреним assertions | `testFigmaVariantMatrix`: 1 passed, 0 failed |

Точні assertions для Tablet Landscape: header `72pt`, лівий і правий content inset `24pt`, проміжок між колонками `24pt`. Для Phone Portrait перевірено, що рядок «Тема застосунку» має не менше `65pt`. Теми застосунку в iOS матриці — Light і Dark; системна тема лишалася Light. Android прогін був окремим instrumentation-проходом; у його звіті зафіксовано portrait-only режим.

## Runtime-попередження

У iPhone UI-проході був один QoS/priority-inversion runtime warning із `dispatch_semaphore_wait`; тести пройшли, але причина попередження не встановлена. На iPad у цьому проході runtime warnings не зафіксовано. Попередження не приховується й не прирівнюється до нульових warnings.

## Докази, збірка та походження

Робоче дерево було брудним і містило сторонні незавершені зміни. HEAD під час перевірки: `65c92efe2b726d204eae66d69a2d3ed3978a6a6b`. Цей commit SHA не ідентифікує сам по собі перевірений вміст: правки цього циклу локальні й незакомічені. Контрольні SHA-256:

| Артефакт | SHA-256 |
|---|---|
| `DemoAccountScreen.kt` | `04db1b928cb1a5929ce4a13855ae694c6f050a43953c606c340409edfa796a9a` |
| `ProfileSettingsUITests.swift` | `1ca4262867085b47472996ddaefba5343a9444174fb9329743eb92d0fd83dd7e` |
| `ProfileSettingsRuntimeTest.kt` | `03cf49bd5e44696aae6d73a2076b083b8627e5d21761f09f87c5dc6d8c3a67b9` |
| Android Debug APK | `7c444cd1666c5d47a9bbbbd3d59c05a9657f57c60e4d435c953ac32e4b853aee` |
| iOS Simulator `build-for-testing` log | `afcad24681e028bd3097c4ba99e94d26c11b9b8b9fa3d973c1feea51ec1fe02e` |
| iPhone UI test log | `c6b82b5a65ffbc90c46314d9495d1842f8bd3d785e862f8fbedb9a64f0294478` |
| iPad UI test log | `12dc41c57612ab425eab994aab00ae232073316c94c5b7263fef6b16197c2b85` |
| iPhone result summary | `2cf40978db46334a342df5171c5962cd90f6db88cf92a109b5e99c458a3b05ee` |
| iPad result summary | `310e0a9f7aaf5906265e36592192a6ae6986aa3ad92bb1541d2815fdd4dd6c11` |
| Android runtime log | `f6f666702b534365b186a50936d50ff942c4c5d953d5ff6988d63d667c9efbd5` |

Логи, `.xcresult`, скриншоти й APK лишаються в локальному каталозі доказів середовища; вони не опубліковані як wiki media і не мають публічних URL. Ця сторінка публікує підсумок і контрольні хеші, а не самі бінарні артефакти. Секрети та матеріали підписування не включалися.

## Категорії A/B/C/D та межі висновку

- **A — незалежно виконано в цьому циклі:** наведені корекції, збірки й runtime-перевірки на iPhone/iPad Simulator та Android Samsung.
- **B — повідомлено користувачем:** нових користувацьких runtime-результатів для цього циклу немає.
- **C — історичні результати:** усі попередні Figma/runtime докази не підміняють перевірки цього циклу.
- **D — не перевірено або не закрито:** точний hero-фото ресурс і SVG-іконки з Figma, числовий pixel-diff, тотожність viewport для всіх варіантів, фізична перевірка X9, VoiceOver/TalkBack та відкладені P0–P4.

Отже, нішеві геометричні розбіжності, покриті новими assertions, перевірені. Загальна вимога «повна відповідність усім Figma-варіантам» залишається частковою: точні assets і pixel-diff не отримані, а різниця viewport та чесний демоконтент зберігають візуальні відмінності. Це не є заявою про завершення етапу або готовність релізу.

**Commit/push коду не виконували.** Wiki-публікація цього звіту виконується окремо від незавершених змін у робочому дереві.
