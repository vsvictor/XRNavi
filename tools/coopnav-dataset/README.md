# CoopNav Ukrainian Vehicle Dataset

**EN.** This directory contains a reproducible pipeline for building a YOLO
dataset for vehicle detection on Ukrainian roads. Raw images and model
artifacts are not committed: they stay under `data/`.

**UK.** Ця папка містить відтворюваний конвеєр для створення YOLO-датасету
розпізнавання транспортних засобів на українських дорогах. Сирі зображення
та артефакти моделі не комітяться: вони зберігаються в `data/`.

## Classes

| ID | Class |
|---:|-------|
| 0 | car |
| 1 | truck |
| 2 | bus |
| 3 | trolleybus |

The trolleybus class is not inferred from bus data. Only a dataset and/or
license-compatible model that explicitly distinguishes trolleybuses may use
this class.

Клас тролейбуса не виводиться з даних автобуса. Використовувати цей клас
можна лише за явної розмітки тролейбусів у датасеті або сумісної моделі.

## Pipeline

```sh
cd tools/coopnav-dataset
python3 -m venv .venv
source .venv/bin/activate
python3 -m pip install -r requirements.txt

# 1. Collect openly licensed candidate images from Wikimedia Commons.
python3 collect_wikimedia.py

# 2. Review candidates and create YOLO boxes.
# Use CVAT or Label Studio. Export labels as YOLO .txt beside each image.

# 3. Validate reviewed labels.
python3 validate_dataset.py

# 4. Split only an explicitly reviewed and approved dataset.
python3 split_dataset.py --source data/yolo --manifest data/review/approved.json --output data/yolo_split_v1
```

## Current review / Поточний перегляд

**EN.** `initial-review.json` records preliminary contact-sheet triage of
177 candidates, several possible same-scene groups and draft pixel
boxes. The draft annotations are incomplete and are **not** training labels.
None of the images is approved: individual source/license, privacy, complete
bounding-box and scene-group reviews remain necessary. `data/review/candidates.csv`
tracks those reviews; its `preliminary_reject` status is not a legal decision.
`data/review/possible-duplicates.json` contains automated suggestions, not a
complete duplicate search: visually similar scenes may not be detected.
`source-review.json` records individual Commons-page observations, not legal
approval; metadata alone does not establish copyright or privacy clearance.
Draft copies and their hashes are kept in `data/review/`; privacy and
bounding-box review statuses apply to the recorded output, not the raw image.
Scene grouping and dataset approval are still pending.
Some earlier review claims were recorded when images were not actually
displayed. Affected records have been reset to pending; all draft boxes,
masks and notes require visual verification before use.
`data/review/visual-observations-*.json` records subsequent observations
from displayed images; these do not represent completed reviews.
`train_yolo.py` uses Ultralytics: verify its code and weight licenses, including
commercial terms, before installation or use.
A four-class YOLO export with channels `[xywh, car, truck, bus, trolleybus]`
matches `YoloConfig.ukrainianVehicles`. An 80-class COCO export still uses
`YoloConfig.cocoVehicles` and does not label trolleybuses. Draft boxes in
this directory are not an approved training set.

**UK.** `initial-review.json` містить попередній огляд контактних аркушів
177 кандидатів, кілька можливих груп однакових сцен і чернеткові записи
у пікселях. Чернетки неповні й **не є** розміткою для навчання. Жоден кадр
ще не схвалено: потрібні індивідуальні перевірки джерел і ліцензій,
приватності, повноти рамок та груп сцен. Стан перевірок у
`data/review/candidates.csv`; `preliminary_reject` не є юридичним висновком.
`data/review/possible-duplicates.json` містить автоматичні підказки, а не
повний перелік дублікатів: схожі сцени можуть залишитись непоміченими.
`source-review.json` зберігає результати огляду окремих сторінок Commons, а
не юридичне схвалення: метадані самі по собі не підтверджують права чи
дотримання приватності.
Чернеткові копії та їхні хеші зберігаються у `data/review/`; статуси
перевірки приватності й рамок стосуються зазначених копій, а не оригіналів.
Групи сцен та схвалення для датасету ще не визначені.
Деякі попередні висновки були записані, коли зображення фактично не
відображалися. Відповідні статуси повернуто до pending; усі чернеткові рамки,
маски й примітки потрібно перевірити візуально перед використанням.
`data/review/visual-observations-*.json` містять подальші спостереження за
фактично відкритими зображеннями, а не результати завершених перевірок.
`train_yolo.py`
використовує Ultralytics: перед встановленням чи використанням перевірте
ліцензії коду й ваг, зокрема комерційні умови.
Експорт YOLO з каналами `[xywh, car, truck, bus, trolleybus]` відповідає
`YoloConfig.ukrainianVehicles`. Експорт COCO на 80 класів лишається на
`YoloConfig.cocoVehicles` і тролейбус не позначає. Чернеткові рамки в цій
теці не є схваленим навчальним набором.

## Reviewed split manifest / Маніфест перевіреної вибірки

**EN.** Prepare `approved.json` with `schemaVersion: 1` and an `images` array.
Each item must contain `image` (path relative to the source directory), the
SHA-256 of the final image (after any redaction), `sourceUrl`, `license`,
`licenseUrl`, `author`, `sceneGroup`, and the four fields `licenseReview`,
`privacyReview`, `annotationReview`, `sceneReview` set to `approved` only
after individual review. Every source image must have a matching YOLO `.txt`
and appear exactly once. Scene groups require manual review of related scenes;
exact duplicate content in different groups is rejected. The output must not
already exist. The script saves assignments and provenance, but cannot prove
the correctness of licenses, annotations or group choices, nor ensure class
balance.

**UK.** Створіть `approved.json` із `schemaVersion: 1` і масивом `images`.
Кожен елемент має містити `image` (відносний шлях), SHA-256 остаточного
зображення після можливого знеособлення, `sourceUrl`, `license`, `licenseUrl`,
`author`, `sceneGroup`, а також `licenseReview`, `privacyReview`,
`annotationReview`, `sceneReview` зі значенням `approved` лише після
індивідуальної перевірки. Кожне зображення має мати YOLO-файл `.txt` і
траплятися в маніфесті рівно один раз. Групи сцен перевіряються вручну;
однаковий вміст у різних групах відхиляється. Вихідний каталог не повинен
існувати. Скрипт зберігає розподіл та походження, але не може гарантувати
правильність ліцензій, розмітки, груп чи баланс класів.

## Licensing

Every source record must retain a stable URL, author, title, license and
license URL. The collector stores candidates in `data/raw/commons-v2/` with
one JSON record per image and caches searches in `.cache/commons-search/`.
It saves progress after each image and resumes on rerun. It waits at least
five seconds between requests and respects `Retry-After` on HTTP 429; repeated
rate limits stop the run with an error. Candidates are not approved: review
each image, provenance, attribution, license terms and personal data before use.
Search class is not a verified object label. Previous files in `data/raw/`
outside `commons-v2/` were collected by the old unverified process and must
not be treated as approved.

No people, number plates or faces need to be included in the final model
dataset. Personal-data candidates should be rejected or redacted before
annotation.

## Ліцензії та продовження збору

Кандидати та їхні метадані зберігаються у `data/raw/commons-v2/`, результати
пошуку — у `.cache/commons-search/`. Прогрес зберігається після кожного файла;
повторний запуск продовжує збір. Між запитами витримується щонайменше п'ять
секунд, `Retry-After` для HTTP 429 враховується; за повторних обмежень збір
завершується з помилкою. Перед використанням кожного кадру людина має перевірити
походження, атрибуцію, умови ліцензії та персональні дані. Пошуковий клас не є
підтвердженою розміткою. Раніше зібрані файли в `data/raw/` поза `commons-v2/`
не перевірені й не вважаються дозволеними до використання.
