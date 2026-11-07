package com.mobilespace.xrnavi

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun AdditionalScreens(
    state: AdditionalScreensState,
    onEvent: (AdditionalScreensEvent) -> Unit
) {
    val screen = state.screen ?: return
    val back = { onEvent(AdditionalScreensEvent.Back) }
    AuthBackHandler(back)
    if (screen.statePreview) {
        Box(Modifier.fillMaxSize().exportTestTags().testTag("design-${screen.designId}")) {
            AdditionalStatePreview(state, onEvent)
            AdditionalScreenNotice(state, onEvent)
        }
        return
    }
    val window = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current
    val layout = AccountLayout.forWindow(
        with(density) { window.width.toDp().value },
        with(density) { window.height.toDp().value }
    )
    val scroll = rememberScrollState()
    LaunchedEffect(screen) { scroll.scrollTo(0) }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().imePadding().exportTestTags()
                .testTag("design-${screen.designId}-${layout.name}")
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (layout.tablet) ProductWordmark(Modifier.padding(horizontal = 16.dp))
                IconButton(onClick = back) {
                    AccountIcon(AccountIcon.Back, contentDescription = "Назад")
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        screen.title, fontFamily = rememberRobotoCondensedFontFamily(),
                        fontSize = if (layout.tablet) 28.sp else 26.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.semantics { heading() }
                    )
                    Text(
                        "Локальний приклад · серверні дані не отримано",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onEvent(AdditionalScreensEvent.Unavailable("Довідка")) }) {
                    AccountIcon(AccountIcon.Help, contentDescription = "Довідка")
                }
            }
            Row(Modifier.weight(1f)) {
                if (layout.landscape) BottomNavigation(
                    additionalNavigationSection(screen),
                    { onEvent(AdditionalScreensEvent.Section(it)) },
                    rail = true, compactRail = !layout.tablet, accountStyle = true
                )
                Column(
                    Modifier.weight(1f).verticalScroll(scroll)
                        .padding(horizontal = if (layout.tablet) 24.dp else 20.dp)
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdditionalContent(state, layout, onEvent)
                }
            }
            if (!layout.landscape && screen !in setOf(
                    AdditionalScreen.Invitation, AdditionalScreen.DeleteAccount,
                    AdditionalScreen.LocationEducation, AdditionalScreen.RouteUnverified
                )
            ) BottomNavigation(
                additionalNavigationSection(screen),
                { onEvent(AdditionalScreensEvent.Section(it)) },
                accountStyle = true
            )
        }
        AdditionalScreenNotice(state, onEvent)
    }
}

@Composable
private fun AdditionalScreenNotice(
    state: AdditionalScreensState,
    onEvent: (AdditionalScreensEvent) -> Unit
) {
    state.notice?.let { notice ->
        AlertDialog(
            onDismissRequest = { onEvent(AdditionalScreensEvent.DismissNotice) },
            title = { Text("Демонстраційний режим") },
            text = { Text(notice, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = { onEvent(AdditionalScreensEvent.DismissNotice) }) {
                    Text("Зрозуміло")
                }
            }
        )
    }
}

internal fun additionalNavigationSection(screen: AdditionalScreen): DemoSection =
    if (screen.statePreview) DemoSection.Map else when (screen) {
        AdditionalScreen.VehicleCatalog, AdditionalScreen.VehicleModel, AdditionalScreen.VehicleReview ->
            DemoSection.Garage

        AdditionalScreen.Company, AdditionalScreen.OfflineMaps, AdditionalScreen.Diagnostics ->
            DemoSection.Settings

        AdditionalScreen.TripSummary, AdditionalScreen.RouteUnverified,
        AdditionalScreen.RouteExplanation, AdditionalScreen.LocationEducation -> DemoSection.Map

        else -> DemoSection.Profile
    }

@Composable
private fun ColumnScope.AdditionalContent(
    state: AdditionalScreensState,
    layout: AccountLayout,
    onEvent: (AdditionalScreensEvent) -> Unit
) {
    val screen = state.screen ?: return
    val open: (AdditionalScreen) -> Unit = { onEvent(AdditionalScreensEvent.Open(it)) }
    val unavailable: (String) -> Unit = { onEvent(AdditionalScreensEvent.Unavailable(it)) }
    when (screen) {
        AdditionalScreen.Company -> AdditionalColumns(layout, primary = {
            AdditionalCard {
                AdditionalTitle("Dnipro Logistics")
                AdditionalBody("Приклад організації · політика автопарку 3.2")
                AdditionalBadge("Успадковано · лише для читання")
            }
            AdditionalHeading("Правила організації")
            AdditionalCard {
                AdditionalPolicy(
                    "Профілі техніки та габарити",
                    "MAN TGX · 4,0 × 2,55 × 16,5 м · 40 т"
                )
                HorizontalDivider()
                AdditionalPolicy("Дозволені дороги", "Без ґрунтових доріг · обхід обмежень")
                HorizontalDivider()
                AdditionalPolicy(
                    "Максимальна швидкість",
                    "90 км/год · нижчий дорожній ліміт важливіший"
                )
            }
        }, secondary = {
            AdditionalHeading("Ваші налаштування · локальний приклад")
            AdditionalCard {
                AdditionalSwitch(
                    "Сповіщення диспетчера", "Нові рейси та зміни завдань",
                    state.dispatcherNotifications
                ) {
                    onEvent(AdditionalScreensEvent.DispatcherNotifications(it))
                }
                HorizontalDivider()
                AdditionalSwitch("Голосові підказки", "Українська · Олена", state.voicePrompts) {
                    onEvent(AdditionalScreensEvent.VoicePrompts(it))
                }
            }
            AdditionalAction("Приєднатися до команди", "Ввести код запрошення") {
                open(AdditionalScreen.Invitation)
            }
            AdditionalButton("Запросити зміну параметрів", secondary = true) {
                unavailable("Запит адміністратору")
            }
            AdditionalBody("Геолокація не передається. Організацію та робочу роль не підтверджено.")
        })

        AdditionalScreen.Invitation -> AdditionalColumns(layout, primary = {
            AdditionalHeading("Код запрошення")
            OutlinedTextField(
                value = state.invitationCode,
                onValueChange = { onEvent(AdditionalScreensEvent.InvitationCode(it)) },
                placeholder = { Text("Код від адміністратора") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Код не перевірено · сервіс запрошень не підключено") }
            )
            AdditionalCard {
                AdditionalTitle("Dnipro Logistics")
                AdditionalBody("Приклад запрошення на роль: водій")
                AdditionalBadge("Політика автопарку 3.2 · не отримана із сервера")
            }
        }, secondary = {
            AdditionalHeading("Що зміниться після приєднання")
            AdditionalNotice(
                "Організація керує",
                "Габаритами робочої техніки, дозволеними дорогами та лімітом швидкості.",
                warning = true
            )
            AdditionalCard {
                AdditionalTitle("Ви налаштовуєте")
                AdditionalBody("Мову, голосові підказки, вигляд мапи та сповіщення.")
            }
            AdditionalCard {
                AdditionalTitle("Особисті маршрути — приватні")
                AdditionalBody("Передачу даних і політику організації необхідно перевірити перед прийняттям.")
            }
            AdditionalConsent(
                "Ознайомився з правилами організації та умовами передачі даних",
                state.invitationConsent
            ) { onEvent(AdditionalScreensEvent.InvitationConsent(it)) }
            AdditionalButton(
                "Прийняти запрошення",
                enabled = state.invitationCode.isNotBlank() && state.invitationConsent
            ) {
                onEvent(AdditionalScreensEvent.AcceptInvitation)
            }
            AdditionalButton("Не зараз · залишитися в особистому режимі", secondary = true) {
                onEvent(AdditionalScreensEvent.Back)
            }
        })

        AdditionalScreen.VehicleCatalog -> AdditionalColumns(layout, primary = {
            OutlinedTextField(
                state.query, { onEvent(AdditionalScreensEvent.Query(it)) },
                placeholder = { Text("Пошук марки або моделі") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            AdditionalHeading("Клас транспорту")
            AdditionalChoices(
                GarageFilter.entries.filter { it != GarageFilter.All }.map { it.label },
                state.vehicleClass.label
            ) { label ->
                onEvent(AdditionalScreensEvent.VehicleClass(GarageFilter.entries.first { it.label == label }))
            }
            AdditionalNotice(
                "Не знайшли свій транспорт?",
                "Довідник має обмежене покриття. Підключеного глобального каталогу немає."
            )
            AdditionalButton("Ввести транспорт вручну", secondary = true) {
                open(AdditionalScreen.VehicleReview)
            }
        }, secondary = {
            AdditionalHeading("Оберіть марку · приклади")
            val brands = when (state.vehicleClass) {
                GarageFilter.Passenger -> listOf("Ford", "Volkswagen", "Toyota", "BMW")
                GarageFilter.Cargo -> listOf("MAN", "Volvo", "Mercedes-Benz")
                else -> listOf("Евакуатор", "Автовоз", "Тягач")
            }
            val results = brands.filter {
                it.contains(state.query.trim(), ignoreCase = true) ||
                        (it == "Ford" && "Mustang Focus Explorer".contains(
                            state.query.trim(),
                            ignoreCase = true
                        ))
            }
            if (results.isEmpty()) AdditionalCard { AdditionalBody("За цим запитом прикладів немає.") }
            results.forEach { brand ->
                AdditionalAction(
                    brand,
                    if (brand == "Ford") "Mustang · Focus · Explorer" else "Приклад марки"
                ) {
                    if (brand == "Ford") open(AdditionalScreen.VehicleModel)
                    else unavailable("Каталог $brand")
                }
            }
        })

        AdditionalScreen.VehicleModel -> AdditionalColumns(layout, primary = {
            AdditionalVehicleImage()
            AdditionalTitle("Ford ${state.model}", large = true)
            AdditionalBody("Ілюстративний автомобіль · характеристики не підтверджені")
        }, secondary = {
            AdditionalHeading("Модель")
            AdditionalChoices(listOf("Mustang", "Focus", "Explorer"), state.model) {
                onEvent(AdditionalScreensEvent.Model(it))
            }
            AdditionalHeading("Рік випуску")
            AdditionalChoices(listOf("2022", "2023", "2024", "2025"), state.year) {
                onEvent(AdditionalScreensEvent.Year(it))
            }
            AdditionalHeading("Комплектація")
            listOf("GT · 5.0 V8 · автоматична", "EcoBoost · 2.3").forEach { trim ->
                AdditionalCard(selected = state.trim == trim) {
                    TextButton(onClick = { onEvent(AdditionalScreensEvent.Trim(trim)) }) {
                        Text(trim)
                    }
                    AdditionalBody("Приклад · параметри не підтверджено")
                }
            }
            AdditionalNotice(
                "Перевірте перед збереженням",
                "Зображення не підтверджує комплектацію. Звірте параметри з техпаспортом; це не ліцензований каталог."
            )
            AdditionalButton("Далі · перевірити параметри") { open(AdditionalScreen.VehicleReview) }
        })

        AdditionalScreen.VehicleReview -> AdditionalColumns(layout, primary = {
            AdditionalVehicleImage()
            AdditionalCard {
                AdditionalTitle("Ford ${state.model} · ${state.year}")
                AdditionalBody(state.trim)
                AdditionalBody("Приклад вибору з каталогу · не доданий до гаража")
            }
        }, secondary = {
            AdditionalHeading("Звірте параметри з техпаспортом · локальна чернетка")
            AdditionalChoices(listOf("Mustang", "Focus", "Explorer"), state.model) {
                onEvent(AdditionalScreensEvent.Model(it))
            }
            OutlinedTextField(
                state.year,
                { onEvent(AdditionalScreensEvent.Year(it)) },
                label = { Text("Рік випуску") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                state.vehicleHeight,
                { onEvent(AdditionalScreensEvent.VehicleHeight(it)) },
                label = { Text("Висота, м") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                state.vehicleLength,
                { onEvent(AdditionalScreensEvent.VehicleLength(it)) },
                label = { Text("Довжина, м") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                state.vehicleMass,
                { onEvent(AdditionalScreensEvent.VehicleMass(it)) },
                label = { Text("Повна маса, т") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            AdditionalNotice(
                "Характеристики не підтверджені",
                "Не використовуйте ілюстративний профіль для перевірки обмежень маршруту.",
                warning = true
            )
            AdditionalButton("Зберегти транспорт") { unavailable("Збереження нового транспорту") }
            AdditionalButton(
                "До каталогу",
                secondary = true
            ) { open(AdditionalScreen.VehicleCatalog) }
        })

        AdditionalScreen.OfflineMaps -> AdditionalColumns(layout, primary = {
            AdditionalCard {
                AdditionalTitle("Мапи на пристрої")
                AdditionalBody("Фактичне сховище не підключено · завантажених регіонів не перевірено")
                LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth())
            }
            AdditionalNotice(
                "Мапа ≠ офлайн-перебудова",
                "Офлайн-перегляд, нові маршрути й перебудова без інтернету не підключені.",
                warning = true
            )
        }, secondary = {
            AdditionalHeading("Регіони · приклад дизайну")
            AdditionalCard {
                AdditionalTitle("Київська область")
                AdditionalBody("248 МБ · розмір із макета, файл не завантажено")
                AdditionalBadge("Не завантажено")
            }
            AdditionalCard {
                AdditionalTitle("Коридор Київ → Львів")
                AdditionalBody("612 МБ · приклад регіону")
                TextButton(onClick = { unavailable("Оновлення регіону") }) { Text("Оновити регіон") }
            }
            AdditionalCard {
                AdditionalTitle("Львівська область")
                AdditionalBody("Завантаження не розпочато")
                LinearProgressIndicator(progress = { 0f }, modifier = Modifier.fillMaxWidth())
                AdditionalButton(
                    "Завантажити",
                    secondary = true
                ) { unavailable("Завантаження мап") }
            }
            AdditionalAction(
                "Кеш активного маршруту",
                "Кеш не створено"
            ) { unavailable("Перегляд кешу") }
            AdditionalButton("Завантажити інший регіон", secondary = true) {
                unavailable("Вибір регіону для завантаження")
            }
        })

        AdditionalScreen.DriverTasks -> AdditionalColumns(layout, primary = {
            AdditionalChoices(
                listOf("Активні · 2", "Завершені · 1"),
                if (state.completedTasks) "Завершені · 1" else "Активні · 2"
            ) {
                onEvent(AdditionalScreensEvent.CompletedTasks(it.startsWith("Завершені")))
            }
            AdditionalPolicy(
                "MAN TGX · автовоз",
                "Приклад призначеного транспорту · не підтверджено"
            )
            AdditionalNotice(
                "Робоча геолокація не передається",
                "Сервіс завдань і передача позиції не підключені. Ці приклади не є призначеними рейсами."
            )
        }, secondary = {
            if (!state.completedTasks) {
                AdditionalCard(selected = true) {
                    AdditionalBody("DL-204 · приклад рейсу")
                    AdditionalTitle("Київ → Львів", large = true)
                    AdditionalBody("6 авто · склад «Захід» · 17:00–18:00")
                    AdditionalMetrics(
                        layout,
                        listOf("ETA · приклад" to "17:30", "Затримка" to "+20 хв")
                    )
                    TextButton(onClick = { open(AdditionalScreen.TaskDetails) }) { Text("Відкрити завдання") }
                }
                AdditionalCard {
                    AdditionalBody("DL-205 · приклад нового завдання")
                    AdditionalTitle("Львів → Київ", large = true)
                    AdditionalBody("4 авто · MAN TGX · термінал «Захід»")
                    AdditionalButton("Прийняти завдання") { unavailable("Прийняття завдання") }
                }
            } else AdditionalAction("DL-198 · Київ → Житомир", "Приклад завершеного рейсу") {
                open(AdditionalScreen.TripSummary)
            }
        })

        AdditionalScreen.TaskDetails -> AdditionalColumns(layout, primary = {
            AdditionalBadge("Приклад · не активний рейс")
            AdditionalCard {
                AdditionalTitle("Київ → Львів", large = true)
                AdditionalBody("Розвантаження · склад «Захід»")
                AdditionalBody("вул. Городоцька, 359 · ворота 2")
                AdditionalBody("6 автомобілів · 36,8 т · не ADR · приклад")
            }
            AdditionalMetrics(
                layout, listOf(
                    "Прибуття, ETA · приклад" to "17:30",
                    "Вікно приймання" to "17:00–18:00"
                )
            )
            AdditionalNotice(
                "Затримка +20 хв · приклад",
                "Жодного повідомлення диспетчеру не надсилалось.", warning = true
            )
        }, secondary = {
            AdditionalAction("Контакт приймання", "Контакт із сервера не отримано") {
                unavailable("Контакт складу")
            }
            AdditionalAction("Диспетчер", "Контакт із сервера не отримано") {
                unavailable("Зв'язок із диспетчером")
            }
            AdditionalNotice(
                "Під'їзд потребує перевірки",
                "Останні 4,2 км не перевірені для автовоза.", warning = true
            )
            AdditionalButton("Я прибув", secondary = true) { unavailable("Підтвердження прибуття") }
            AdditionalButton("Завершити", enabled = false) { unavailable("Завершення рейсу") }
            AdditionalAction("Повідомити нову затримку", "Причина · новий очікуваний час") {
                unavailable("Повідомлення про затримку")
            }
        })

        AdditionalScreen.TripHistory -> {
            AdditionalChoices(HistoryFilter.entries.map { it.label }, state.historyFilter.label) {
                onEvent(AdditionalScreensEvent.History(HistoryFilter.entries.first { f -> f.label == it }))
            }
            AdditionalNotice(
                "Лише приклади поїздок",
                "Історію із сервера не отримано. Жоден приклад не є активним робочим рейсом."
            )
            AdditionalColumns(layout, primary = {
                if (state.historyFilter != HistoryFilter.Work) {
                    AdditionalHeading("Особиста поїздка · приклад")
                    AdditionalCard {
                        AdditionalTitle("Київ → Львів")
                        AdditionalBody("Ford Mustang GT")
                        AdditionalRouteIllustration()
                        AdditionalBody("09:41–15:53 · 540 км · 6 год 12 хв")
                        TextButton(onClick = { open(AdditionalScreen.TripSummary) }) { Text("Підсумок поїздки") }
                    }
                }
            }, secondary = {
                if (state.historyFilter != HistoryFilter.Personal) {
                    AdditionalHeading("Робочі поїздки · приклад")
                    AdditionalAction(
                        "DL-198 · Київ → Житомир",
                        "142 км · 2 год 25 хв · не підтверджено"
                    ) {
                        open(AdditionalScreen.TaskDetails)
                    }
                }
                AdditionalAction(
                    "Класифікація поїздки",
                    "Особистий / робочий режим обирається до старту"
                ) {
                    unavailable("Зміна класифікації")
                }
            })
        }

        AdditionalScreen.Privacy -> AdditionalColumns(layout, primary = {
            AdditionalCard {
                AdditionalBadge("Особистий простір")
                AdditionalTitle("Особисті поїздки — лише ваші", large = true)
                AdditionalBody("У цьому деморежимі історія та маршрути не передаються організації.")
            }
            AdditionalHeading("Робочий рейс · приклад політики")
            AdditionalPolicy("Передача робочої геолокації", "Лише від старту до завершення рейсу")
            AdditionalNotice(
                "Це правило організації",
                "Фактичну політику не отримано. Обов'язкове відстеження не можна удавано вимкнути перемикачем.",
                warning = true
            )
        }, secondary = {
            AdditionalHeading("Зберігання даних")
            AdditionalCard {
                AdditionalTitle("Робоча геолокація · 30 днів")
                AdditionalBody("Робочі звіти — 180 днів · приклад із дизайну, не підтверджена політика")
                AdditionalTitle("Особиста історія")
                AdditionalBody("Історію в акаунті й строки зберігання не перевірено.")
            }
            AdditionalAction(
                "Експортувати особисті дані",
                "Отримати копію профілю та власних поїздок"
            ) {
                unavailable("Експорт даних")
            }
            AdditionalAction("Видалити особисту історію", "Не видаляє робочі записи організації") {
                unavailable("Видалення історії")
            }
            AdditionalAction(
                "Видалення акаунта", "Наслідки та підтвердження на наступному кроці",
                destructive = true
            ) { open(AdditionalScreen.DeleteAccount) }
        })

        AdditionalScreen.DeleteAccount -> AdditionalColumns(layout, primary = {
            AdditionalCard(destructive = true) {
                AdditionalTitle("Ви втратите доступ до XRNavi", large = true)
                AdditionalBody("Це видалення особистого акаунта, а не всіх даних компанії.")
            }
            AdditionalHeading("Що буде видалено")
            AdditionalCard {
                AdditionalTitle("Особистий профіль та історія")
                AdditionalBody("Поїздки, збережені місця й особистий гараж більше не будуть доступні.")
                AdditionalTitle("Вхід на всіх пристроях")
                AdditionalBody("Сеанси завершаться. Локальні мапи та кеш потрібно очистити на кожному пристрої.")
            }
            AdditionalNotice(
                "Робочі записи зберігаються окремо",
                "Реальні строки та правила організації необхідно перевірити перед видаленням.",
                warning = true
            )
        }, secondary = {
            AdditionalAction("Спочатку експортувати дані", "Отримати копію особистих поїздок") {
                unavailable("Експорт даних")
            }
            AdditionalConsent(
                "Розумію наслідки та окреме зберігання робочих даних.",
                state.deletionConsent
            ) { onEvent(AdditionalScreensEvent.DeletionConsent(it)) }
            AdditionalButton("Скасувати · залишити акаунт", secondary = true) {
                onEvent(AdditionalScreensEvent.Back)
            }
            AdditionalButton(
                "Підтвердити запит на видалення", destructive = true,
                enabled = state.deletionConsent
            ) { onEvent(AdditionalScreensEvent.DeleteAccount) }
        })

        AdditionalScreen.Diagnostics -> AdditionalColumns(layout, primary = {
            AdditionalHeading("Екран автомобіля")
            AdditionalCard {
                AdditionalTitle("Android Auto")
                AdditionalBody("Заплановано · інтеграція ще не доступна")
                AdditionalTitle("Apple CarPlay")
                AdditionalBody("Заплановано · інтеграція ще не доступна")
            }
            AdditionalHeading("Навігація у фоні")
            AdditionalAction("Фонова геолокація", "Дозвіл ОС не перевірено") {
                unavailable("Налаштування фонової геолокації")
            }
            AdditionalAction("Голосові підказки", "Сервіс не підключено") {
                unavailable("Налаштування голосу")
            }
            AdditionalAction("Сповіщення", "Дозвіл ОС не перевірено") {
                unavailable("Налаштування сповіщень")
            }
            AdditionalAction("Енергозбереження", "Стан пристрою не перевірено") {
                unavailable("Налаштування енергозбереження")
            }
            AdditionalNotice(
                "Менше батареї — рідше оновлення",
                "Енергозбереження ОС може затримувати GPS і фонові підказки.", warning = true
            )
        }, secondary = {
            AdditionalHeading("Діагностика GPS")
            AdditionalCard {
                AdditionalTitle("Немає підтвердженого виміру")
                AdditionalBody("Точність, джерело й вік позиції не отримано з ОС.")
            }
            AdditionalButton("Відкрити налаштування пристрою", secondary = true) {
                unavailable("Перехід до системних налаштувань")
            }
            AdditionalHeading("Приклади станів інтерфейсу · не операції ОС або сервера")
            AdditionalScreen.entries.filter { it.statePreview }.forEach { preview ->
                AdditionalAction(preview.title, "${preview.designId} · локальний приклад") {
                    open(
                        preview
                    )
                }
            }
        })

        AdditionalScreen.TripSummary -> AdditionalColumns(layout, primary = {
            AdditionalBadge("Приклад завершеної поїздки")
            AdditionalRouteIllustration()
        }, secondary = {
            AdditionalMetrics(
                layout,
                listOf("Відстань" to "540 км", "Загальний час" to "6 год 12 хв")
            )
            AdditionalMetrics(layout, listOf("У русі" to "5 год 54 хв", "Зупинки · 1" to "18 хв"))
            AdditionalCard {
                AdditionalTitle("Ford Mustang GT")
                AdditionalBody("Київ · 09:41 → Львів · 15:53 · особистий режим")
            }
            AdditionalNotice(
                "Не збережено в історії",
                "Це приклад підсумку з макета, а не результат виконаної навігації."
            )
            AdditionalButton("Повернутися до мапи") {
                onEvent(AdditionalScreensEvent.Section(DemoSection.Map))
            }
        })

        AdditionalScreen.RouteExplanation, AdditionalScreen.RouteUnverified -> AdditionalColumns(
            layout,
            primary = {
                AdditionalRouteIllustration()
                AdditionalMetrics(
                    layout, listOf(
                        "Плановий шлях · приклад" to "552 км",
                        "Орієнтовний час" to "7 год 10 хв"
                    )
                )
            },
            secondary = {
                AdditionalNotice(
                    if (screen == AdditionalScreen.RouteUnverified) "Безпеку для автовоза не підтверджено"
                    else "Останні 4,2 км не перевірені",
                    "М-06 → склад «Захід», вул. Городоцька, 359. Не починайте рейс за неперевіреним під'їздом.",
                    warning = true
                )
                AdditionalCard(selected = screen == AdditionalScreen.RouteExplanation) {
                    AdditionalTitle("М-06 · об'їзд центру Львова", large = true)
                    AdditionalBody("Приклад пояснення: міст із лімітом 3,8 м виключено; висота MAN TGX — 4,0 м.")
                }
                AdditionalMetrics(
                    layout, listOf(
                        "Повна маса · факт / max" to "36,8 / 40 т",
                        "Вісь · факт / max" to "10,8 / 11,5 т"
                    )
                )
                AdditionalBadge("Пояснення · не сертифікація безпеки")
                AdditionalBody("Профіль і обмеження не отримані з сервера. Легковий маршрут не є безпечною альтернативою для вантажівки.")
                AdditionalButton("Переглянути альтернативи") { unavailable("Побудова альтернативного маршруту") }
                AdditionalButton("Уточнити під'їзд із диспетчером", secondary = true) {
                    unavailable("Зв'язок із диспетчером")
                }
                if (screen == AdditionalScreen.RouteExplanation)
                    AdditionalAction(
                        "Вантажна перевірка",
                        "Чому маршрут не можна використовувати"
                    ) {
                        open(AdditionalScreen.RouteUnverified)
                    }
            })

        AdditionalScreen.LocationEducation -> {
            ProductWordmark()
            AdditionalBadge("Ваш перший маршрут")
            AdditionalTitle("Почнімо з вашого місця", large = true)
            AdditionalBody("З поточною позицією XRNavi підставить початок маршруту та допоможе зорієнтуватися в дорозі.")
            AdditionalCard {
                AdditionalBody("Лише коли потрібно — для визначення початку й навігації.")
                AdditionalBody("Особисті поїздки залишаються приватними. Робочі правила діють лише під час робочого рейсу.")
                AdditionalBody("Без обов'язкового фонового доступу. Маршрут можна спланувати й вручну.")
            }
            AdditionalButton("Надати доступ до геолокації") {
                unavailable("Системний запит геолокації")
            }
            AdditionalButton("Продовжити без геолокації", secondary = true) {
                onEvent(AdditionalScreensEvent.Section(DemoSection.Map))
            }
            AdditionalAction(
                "Переглянути стан визначення місця",
                "Локальний приклад · GPS не запитується"
            ) {
                open(AdditionalScreen.LocationAcquiring)
            }
        }

        AdditionalScreen.SelectedPoint, AdditionalScreen.LocationAcquiring,
        AdditionalScreen.LocationDenied, AdditionalScreen.LocationDisabled,
        AdditionalScreen.SearchLoading, AdditionalScreen.SearchEmpty, AdditionalScreen.SearchError,
        AdditionalScreen.RouteBuilding, AdditionalScreen.RouteFailed ->
            error("State previews must render through AdditionalStatePreview")
    }
}

@Composable
private fun AdditionalStatePreview(
    state: AdditionalScreensState,
    onEvent: (AdditionalScreensEvent) -> Unit
) {
    val screen = requireNotNull(state.screen)
    val back = { onEvent(AdditionalScreensEvent.Back) }
    val open: (AdditionalScreen) -> Unit = { onEvent(AdditionalScreensEvent.Open(it)) }
    val unavailable: (String) -> Unit = { onEvent(AdditionalScreensEvent.Unavailable(it)) }
    val section: (DemoSection) -> Unit = { onEvent(AdditionalScreensEvent.Section(it)) }
    if (screen in listOf(
            AdditionalScreen.SearchLoading,
            AdditionalScreen.SearchEmpty,
            AdditionalScreen.SearchError
        )
    ) {
        SearchScreen(
            demoMode = true, origin = "Київ · приклад", destination = "",
            vehicleClass = "Легкове", selectedVehicle = demoVehicle,
            target = SearchTarget.Destination, query = state.query,
            status = when (screen) {
                AdditionalScreen.SearchLoading -> SearchStatus.Loading
                AdditionalScreen.SearchEmpty -> SearchStatus.Empty
                else -> SearchStatus.Error
            },
            vehicleMode = "Особистий · приклад стану", savedPlaceKeys = emptySet(),
            onVehicleModeChange = { unavailable("Зміна режиму") },
            onTargetChange = { unavailable("Редагування адреси у прикладі") },
            onQueryChange = { onEvent(AdditionalScreensEvent.Query(it)) },
            onSearch = {
                open(
                    if (screen == AdditionalScreen.SearchLoading) AdditionalScreen.SearchError
                    else AdditionalScreen.SearchLoading
                )
            },
            onBack = back, onPickMap = { open(AdditionalScreen.SelectedPoint) },
            onSelectPlace = { unavailable("Вибір адреси у прикладі") },
            onSavePlace = { unavailable("Збереження адреси у прикладі"); SavedPlaceAddStatus.Invalid },
            onOpenSavedPlaces = { unavailable("Обране у прикладі") },
            onSectionChange = section, onToggleTheme = { unavailable("Зміна теми у прикладі") }
        )
        return
    }
    MapScreen(
        preview = false,
        demoMode = true,
        origin = if (screen == AdditionalScreen.RouteBuilding || screen == AdditionalScreen.RouteFailed)
            "Київ · приклад" else "",
        destination = if (screen == AdditionalScreen.SelectedPoint ||
            screen == AdditionalScreen.RouteBuilding || screen == AdditionalScreen.RouteFailed
        )
            "Львів · площа Ринок, 1 · приклад" else "",
        routeStops = emptyList(),
        sheetLevel = SheetLevel.Expanded,
        geoStatus = GeoStatus.NotConfigured,
        notice = "",
        vehicleClass = "Легкове",
        selectedVehicle = demoVehicle,
        vehicleMode = "Особистий · приклад стану",
        onVehicleClassChange = { unavailable("Зміна транспорту у прикладі") },
        onOpenSearch = { open(AdditionalScreen.SearchEmpty) },
        onOpenRouteStops = { unavailable("Зупинки у прикладі") },
        onOpenStopPlaces = { unavailable("Місця зупинки у прикладі") },
        onUseLocation = { open(AdditionalScreen.LocationEducation) },
        onSheetLevelChange = {},
        onPreview = { open(AdditionalScreen.RouteBuilding) },
        onStartRoute = { unavailable("Навігація") },
        onOpenRouteSettings = { unavailable("Параметри у прикладі") },
        onBack = back,
        onSectionChange = section,
        onToggleTheme = { unavailable("Зміна теми у прикладі") },
        onOpenAdditional = open,
        onMapLayers = { unavailable("Шари мапи") },
        sheetContent = {
            AdditionalHeading("Локальний приклад стану · запит не виконується")
            when (screen) {
                AdditionalScreen.SelectedPoint -> {
                    AdditionalTitle("Львів · площа Ринок, 1", large = true)
                    AdditionalBody("Точка з макета · не визначена за координатами натискання")
                    AdditionalButton("Переглянути побудову маршруту") { open(AdditionalScreen.RouteBuilding) }
                    AdditionalButton(
                        "Змінити точку",
                        secondary = true
                    ) { open(AdditionalScreen.SearchEmpty) }
                }

                AdditionalScreen.RouteBuilding -> {
                    AdditionalTitle("Ford Mustang GT")
                    CircularProgressIndicator(
                        Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    AdditionalTitle("Будуємо маршрут")
                    AdditionalBody("Приклад індикації. Маршрутизатор не підключено; розрахунок не запущено.")
                    AdditionalButton("Переглянути стан помилки") { open(AdditionalScreen.RouteFailed) }
                    AdditionalButton("Скасувати побудову", secondary = true, onClick = back)
                }

                AdditionalScreen.RouteFailed -> {
                    AdditionalTitle("Ford Mustang GT")
                    AdditionalCard {
                        AdditionalTitle("Маршрут не побудовано")
                        AdditionalBody("Перевірте з’єднання або змініть адреси. У цьому прикладі серверний розрахунок не виконувався.")
                    }
                    AdditionalButton("Повторити · приклад") { open(AdditionalScreen.RouteBuilding) }
                    AdditionalButton(
                        "Редагувати маршрут",
                        secondary = true
                    ) { open(AdditionalScreen.SearchEmpty) }
                }

                AdditionalScreen.LocationAcquiring -> {
                    CircularProgressIndicator(
                        Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    AdditionalTitle("Визначаємо місце")
                    AdditionalBody("Очікуємо надійний сигнал GPS. Це лише приклад; позицію та дозволи ОС не запитуємо.")
                    AdditionalButton("Скасувати визначення", secondary = true, onClick = back)
                    AdditionalButton("Задати початок вручну") { section(DemoSection.Map) }
                }

                AdditionalScreen.LocationDenied, AdditionalScreen.LocationDisabled -> {
                    AdditionalCard {
                        AdditionalTitle(screen.title)
                        AdditionalBody("Позицію не отримано. Демонстраційний стан не означає, що фактичний доступ або служба ОС вимкнені.")
                    }
                    AdditionalButton("Відкрити налаштування пристрою", secondary = true) {
                        unavailable("Системні налаштування")
                    }
                    AdditionalButton("Задати початок вручну") { section(DemoSection.Map) }
                }

                else -> error("Not a map state preview: $screen")
            }
        }
    )
}

@Composable
internal fun AdditionalColumns(
    layout: AccountLayout,
    primary: @Composable ColumnScope.() -> Unit,
    secondary: @Composable ColumnScope.() -> Unit
) {
    if (layout.landscape || layout.tablet) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = primary
            )
            Column(
                Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = secondary
            )
        }
    } else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        primary()
        secondary()
    }
}

@Composable
internal fun AdditionalCard(
    selected: Boolean = false,
    destructive: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = when {
            destructive -> colors.error.copy(alpha = 0.14f)
            selected -> colors.surfaceVariant
            colors.background.red < 0.5f -> Color(0xFF080F19)
            else -> colors.surface
        },
        border = if (selected) BorderStroke(1.dp, colors.primary) else null
    ) {
        Column(
            Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

@Composable
internal fun AdditionalTitle(text: String, large: Boolean = false) {
    Text(
        text,
        fontFamily = if (large) rememberRobotoCondensedFontFamily() else rememberInterFontFamily(),
        fontSize = if (large) 24.sp else 14.sp,
        lineHeight = if (large) 28.sp else 20.sp,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.semantics { heading() })
}

@Composable
internal fun AdditionalHeading(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() })
}

@Composable
internal fun AdditionalBody(text: String) {
    Text(
        text, fontSize = 12.sp, lineHeight = 17.sp, letterSpacing = 0.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun AdditionalBadge(text: String) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp)) {
        Text(
            text, Modifier.padding(horizontal = 8.dp, vertical = 5.dp), fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
internal fun AdditionalNotice(title: String, body: String, warning: Boolean = false) {
    Surface(
        Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
        color = if (warning) accountPolicyColor().copy(alpha = 0.12f)
        else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AccountIcon(
                if (warning) AccountIcon.Lock else AccountIcon.Help,
                Modifier.size(20.dp), tint = if (warning) accountPolicyColor() else null
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                    color = if (warning) accountPolicyColor() else MaterialTheme.colorScheme.onSurface
                )
                AdditionalBody(body)
            }
        }
    }
}

@Composable
private fun AdditionalPolicy(title: String, detail: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            AdditionalBody(detail)
        }
        AccountIcon(AccountIcon.Lock, Modifier.size(18.dp), contentDescription = "Лише для читання")
    }
}

@Composable
private fun AdditionalAction(
    title: String, detail: String, destructive: Boolean = false, onClick: () -> Unit
) {
    Surface(
        onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                AdditionalBody(detail)
            }
            AccountIcon(AccountIcon.ChevronRight, Modifier.size(18.dp))
        }
    }
}

@Composable
private fun AdditionalButton(
    title: String, secondary: Boolean = false, destructive: Boolean = false,
    enabled: Boolean = true, onClick: () -> Unit
) {
    Button(
        onClick, Modifier.fillMaxWidth().heightIn(min = 52.dp), enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                destructive -> MaterialTheme.colorScheme.error
                secondary -> MaterialTheme.colorScheme.surfaceVariant
                else -> MaterialTheme.colorScheme.primary
            },
            contentColor = when {
                destructive -> MaterialTheme.colorScheme.onError
                secondary -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onPrimary
            }
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) { Text(title, fontSize = 14.sp) }
}

@Composable
private fun AdditionalChoices(values: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { value ->
            FilterChip(
                selected == value, onClick = { onSelect(value) }, label = { Text(value) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    selectedLabelColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun AdditionalSwitch(
    title: String,
    detail: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp)
            AdditionalBody(detail)
        }
        Switch(checked, onChange, modifier = Modifier.semantics { contentDescription = title })
    }
}

@Composable
private fun AdditionalConsent(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().toggleable(checked, role = Role.Checkbox, onValueChange = onChange)
        .semantics { contentDescription = title },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked, onCheckedChange = null)
        Text(title, fontSize = 12.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun AdditionalMetrics(layout: AccountLayout, values: List<Pair<String, String>>) {
    if (layout == AccountLayout.PhonePortrait && values.size > 2) {
        values.chunked(2).forEach { AdditionalMetrics(layout, it) }
    } else Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        values.forEach { (label, value) ->
            Surface(
                Modifier.weight(1f), color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdditionalBody(label)
                    Text(
                        value, fontFamily = rememberRobotoCondensedFontFamily(),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AdditionalVehicleImage() {
    val dark = MaterialTheme.colorScheme.background.red < 0.5f
    AdditionalCard {
        Image(
            painterResource(if (dark) Res.drawable.mustang_hero_dark else Res.drawable.mustang_hero_light),
            contentDescription = "Ілюстративне зображення Ford Mustang",
            modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop
        )
        AdditionalBody("Ілюстративне зображення · не підтверджує комплектацію")
    }
}

@Composable
internal fun AdditionalRouteIllustration(
    caption: String = "Київ → Львів · схема з макета, не навігаційна мапа"
) {
    Surface(
        Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column {
            MapCanvas(Modifier.fillMaxWidth().height(180.dp), preview = true, compact = true)
            Box(Modifier.padding(12.dp)) { AdditionalBody(caption) }
        }
    }
}
