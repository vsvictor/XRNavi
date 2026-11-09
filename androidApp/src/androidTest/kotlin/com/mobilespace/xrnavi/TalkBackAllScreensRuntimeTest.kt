package com.mobilespace.xrnavi

import android.app.UiAutomation
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class TalkBackAllScreensRuntimeTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context get() = instrumentation.targetContext
    private val output
        get() = File(context.getExternalFilesDir(null), "talkback-all-screens").apply { mkdirs() }

    @Test
    fun talkBackCanFocusContentAndActionsAcrossEveryAppScreen() {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ).orEmpty()
        assumeTrue(
            "The all-screens accessibility pass requires TalkBack to be enabled",
            enabledServices.contains("com.google.android.marvin.talkback")
        )

        val ui = instrumentation.getUiAutomation(
            UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES
        )
        val scenario = ActivityScenario.launch<MainActivity>(
            Intent(context, MainActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        try {
            waitFor("XRNavi sign-in window") { appRoot(ui) != null }

            audit(
                ui,
                "sign-in",
                "XR - НОВИЙ ФОРМАТ ВЗАЄМОДІЇ",
                "Тема",
                "Електронна пошта",
                "Пароль",
                "Забули пароль?",
                "Увійти",
                "Продовжити без входу",
                "Зареєструватися"
            )

            click(ui, "Не маєте аккаунта? Зареєструватися")
            waitFor("registration screen") { find(ui, "Реєстрація") != null }
            audit(
                ui,
                "registration",
                "Реєстрація",
                "Тема",
                "Особистий",
                "Корпоративний",
                "Ім’я та прізвище",
                "Електронна пошта",
                "Пароль",
                "Підтвердіть пароль",
                "Згода з Умовами користування",
                "Реєстрація",
                "Уже маєте аккаунт? Увійти"
            )
            click(ui, "Уже маєте аккаунт? Увійти")
            waitFor("sign-in after registration") { find(ui, "Забули пароль?") != null }

            click(ui, "Забули пароль?")
            waitFor("password recovery request screen") { find(ui, "Відновлення пароля") != null }
            audit(
                ui,
                "recovery-request",
                "Відновлення пароля",
                "Електронна пошта",
                "Надіслати посилання",
                "Внутрішній preview S41–S45"
            )
            val recoveryStages = listOf(
                "S41" to listOf("Відновлення пароля", "Електронна пошта", "Надіслати посилання"),
                "S42" to listOf(
                    "Перевірте пошту", "Відкрити пошту", "Змінити адресу",
                    "Запросити лист повторно"
                ),
                "S43" to listOf(
                    "Новий пароль", "Підтвердіть пароль", "Зберегти новий пароль"
                ),
                "S44" to listOf("Пароль змінено", "Перейти до входу"),
                "S45" to listOf("Посилання недійсне", "Запросити нове посилання")
            )
            recoveryStages.forEach { (stageCode, labels) ->
                click(ui, "Внутрішній preview S41–S45")
                waitFor("recovery preview stage menu") { find(ui, "$stageCode ·") != null }
                click(ui, "$stageCode · ${labels.first()}")
                waitFor("recovery stage $stageCode") { find(ui, labels.first()) != null }
                audit(ui, "recovery-$stageCode", *labels.toTypedArray())
            }
            click(ui, "Повернутися до входу")
            waitFor("sign-in after recovery preview") { find(ui, "Забули пароль?") != null }

            click(ui, "Продовжити без входу")
            waitFor("demo map") { find(ui, "Панель маршруту") != null }
            audit(
                ui,
                "map",
                "Панель маршруту",
                "Моє місцезнаходження",
                "Вибрати початкову точку",
                "Зупинки маршруту",
                "Місця зупинки",
                "Шари мапи",
                "Мапа",
                "Гараж",
                "Налаштування",
                "Профіль"
            )

            click(ui, "Вибрати початкову точку")
            waitFor("origin address search") { find(ui, "Початкова адреса") != null }
            audit(
                ui,
                "search-origin",
                "Початкова адреса",
                "Введіть адресу",
                "площа Ринок, 1",
                "Обране · збережене локально",
                "Вибрати адресу"
            )
            click(ui, "Обране · збережене локально")
            waitFor("saved places from search") { find(ui, "Збережені місця") != null }
            audit(ui, "saved-places-search", "Збережені місця", "Назад")
            click(ui, "Назад")
            waitFor("search after saved places") { find(ui, "Початкова адреса") != null }
            click(ui, "площа Ринок, 1")
            waitFor("map after choosing origin") { find(ui, "Знайти адресу") != null }

            click(ui, "Знайти адресу")
            waitFor("destination address search") { find(ui, "Адреса призначення") != null }
            audit(
                ui,
                "search-destination",
                "Адреса призначення",
                "Введіть адресу",
                "площа Ринок, 10",
                "Обране · збережене локально",
                "Мапа"
            )
            click(ui, "площа Ринок, 10")
            waitFor("map with both route endpoints") {
                find(ui, "Переглянути маршрут") != null
            }

            click(ui, "Панель маршруту")
            click(ui, "Панель маршруту")
            waitFor("expanded route panel") {
                find(ui, "Параметри маршруту та альтернативи") != null
            }
            audit(
                ui,
                "map-expanded",
                "Категорія демо-профілю",
                "Легкове",
                "Вантажне",
                "Спецтехніка",
                "Параметри маршруту та альтернативи",
                "Переглянути маршрут"
            )
            click(ui, "Параметри маршруту та альтернативи")
            waitFor("route settings") { find(ui, "Параметри маршруту") != null }
            audit(
                ui,
                "route-settings",
                "Параметри маршруту",
                "Уникати платних доріг",
                "Уникати ґрунтових доріг",
                "Уникати поромів",
                "Застосувати параметри",
                "Скасувати"
            )
            click(ui, "Скасувати")
            waitFor("map after route settings") { find(ui, "Переглянути маршрут") != null }

            click(ui, "Переглянути маршрут")
            waitFor("route preview") { find(ui, "Почати маршрут") != null }
            audit(
                ui,
                "route-preview",
                "Панель маршруту",
                "Кінцева точка демо-маршруту",
                "Почати маршрут"
            )
            instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK)
            waitFor("map after leaving preview") { find(ui, "Зупинки маршруту · 0") != null }

            click(ui, "Зупинки маршруту · 0")
            waitFor("route stops editor") { find(ui, "Проміжних зупинок немає") != null }
            audit(
                ui,
                "route-stops",
                "Зупинки маршруту",
                "Початок · лише для перегляду",
                "Призначення · лише для перегляду",
                "Проміжних зупинок немає",
                "Додати зупинку",
                "Скасувати",
                "Зберегти зупинки"
            )
            click(ui, "Додати зупинку")
            waitFor("route stop address search") { find(ui, "Адреса для зупинки") != null }
            audit(
                ui,
                "route-stop-search",
                "Адреса для зупинки",
                "Пошук серед демоадрес",
                "Місця зупинки",
                "площа Ринок, 1",
                "Обране"
            )
            click(ui, "Місця зупинки")
            waitFor("stop places catalog") { find(ui, "Локальний демокаталог") != null }
            audit(
                ui,
                "stop-places-catalog",
                "Місця зупинки",
                "Пошук у демокаталозі",
                "Стоянки",
                "Відпочинок",
                "Пальне",
                "Зарядки",
                "Сервіс",
                "Вагові",
                "Кордон",
                "Демонстраційна стоянка 01"
            )
            click(ui, "Відпочинок")
            waitFor("rest category results") { find(ui, "Демонстраційне місце відпочинку 01") != null }
            click(ui, "Демонстраційне місце відпочинку 01")
            waitFor("stop place details") { find(ui, "Додати зупинку") != null }
            audit(
                ui,
                "stop-place-details",
                "Демонстраційне місце відпочинку 01",
                "Години роботи",
                "Місця та тарифи",
                "Вільні місця",
                "Габарити, маса та придатність",
                "Додати зупинку",
                "Назад"
            )
            click(ui, "Назад")
            waitFor("stop places catalog after details") { find(ui, "Демонстраційне місце відпочинку 01") != null }
            click(ui, "Назад")
            waitFor("route stop search after catalog") { find(ui, "Адреса для зупинки") != null }
            click(ui, "площа Ринок, 10")
            waitFor("route stop draft") { find(ui, "Зупинок маршруту") != null }
            audit(ui, "route-stops-draft", "Проміжні зупинки: 1", "Видалити", "Скасувати")
            click(ui, "Скасувати")
            waitFor("map after cancelling stop draft") { find(ui, "Панель маршруту") != null }

            click(ui, "Гараж")
            waitFor("garage section") { find(ui, "Мій гараж") != null }
            audit(
                ui,
                "garage",
                "Мій гараж",
                "Додати транспорт",
                "Усі",
                "Легкове",
                "Вантажне",
                "Спецтехніка",
                "Переглянути профіль Ford Mustang GT"
            )
            click(ui, "Переглянути профіль Ford Mustang GT")
            waitFor("vehicle profile") { find(ui, "Профіль автомобіля") != null }
            audit(
                ui,
                "vehicle-profile",
                "Профіль автомобіля",
                "Ford Mustang GT",
                "Категорія: Легкове",
                "Переглянути конфігурації",
                "Повернутися до гаража"
            )
            click(ui, "Переглянути конфігурації")
            waitFor("vehicle configurations") { find(ui, "Конфігурації") != null }
            audit(
                ui,
                "vehicle-configurations",
                "Конфігурації",
                "Застосувати конфігурацію",
                "Скасувати",
                "Повернутися до гаража"
            )
            click(ui, "Скасувати")
            waitFor("vehicle profile after cancel") { find(ui, "Профіль автомобіля") != null }
            click(ui, "Назад до попереднього розділу")
            waitFor("garage after vehicle profile") { find(ui, "Мій гараж") != null }

            click(ui, "Профіль")
            waitFor("demo profile section") { find(ui, "Мій профіль") != null }
            audit(
                ui,
                "profile",
                "Мій профіль",
                "Демо-профіль",
                "Без авторизації",
                "Збережені місця",
                "Відкрити налаштування",
                "Вийти з деморежиму",
                "Відкрити реєстрацію",
                "Відновлення пароля"
            )
            click(ui, "Збережені місця")
            waitFor("saved places from profile") { find(ui, "Збережені місця") != null }
            audit(ui, "saved-places-profile", "Збережені місця", "Назад")
            click(ui, "Назад")
            waitFor("profile after saved places") { find(ui, "Мій профіль") != null }

            click(ui, "Налаштування")
            waitFor("demo settings section") { find(ui, "Налаштування") != null }
            audit(
                ui,
                "settings",
                "Налаштування",
                "Деморежим",
                "Мапа",
                "Гараж",
                "Профіль"
            )
        } catch (failure: Throwable) {
            capture(ui, "failure")
            throw failure
        } finally {
            scenario.close()
        }
    }

    private fun audit(ui: UiAutomation, screen: String, vararg labels: String) {
        labels.forEach { label ->
            val target = reveal(ui, label)
            val bounds = Rect().also(target::getBoundsInScreen)
            assertTrue("Accessible label is visible: $label ($bounds)", target.isVisibleToUser)
            assertTrue("Accessible label has visible bounds: $label ($bounds)", !bounds.isEmpty)
            Log.i(
                "XrnaviTalkBackFullPass",
                "label=$label class=${target.className} clickable=${target.isClickable} " +
                    "focusable=${target.isFocusable} editable=${target.isEditable} bounds=$bounds"
            )
        }
        capture(ui, screen)
    }

    private fun click(ui: UiAutomation, label: String) {
        val textNode = reveal(ui, label)
        var target: AccessibilityNodeInfo? = textNode
        while (target != null && !target.isClickable) target = target.parent
        if (target == null) {
            for (attempt in 0 until 10) {
                val snapshot = nodes(ui)
                val currentText = snapshot.firstOrNull { matches(it, label) }
                if (currentText != null) {
                    val textBounds = Rect().also(currentText::getBoundsInScreen)
                    if (!textBounds.isEmpty) {
                        target = snapshot.asSequence()
                            .filter { node ->
                                if (!node.isClickable || !node.isVisibleToUser || !node.isEnabled) {
                                    return@filter false
                                }
                                val bounds = Rect().also(node::getBoundsInScreen)
                                bounds.contains(textBounds.centerX(), textBounds.centerY())
                            }
                            .minByOrNull {
                                val bounds = Rect().also(it::getBoundsInScreen)
                                bounds.width().toLong() * bounds.height()
                            }
                    }
                }
                if (target != null) break
                SystemClock.sleep(100)
            }
        }
        val action = requireNotNull(target) {
            "Expected a clickable action for '$label'\n${describe(ui)}"
        }
        assertTrue(
            "Control is enabled before activation: $label",
            action.isEnabled && action.isVisibleToUser
        )
        assertTrue("Accessibility click succeeds: $label", action.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        instrumentation.waitForIdleSync()
        SystemClock.sleep(150)
    }

    private fun reveal(ui: UiAutomation, label: String): AccessibilityNodeInfo {
        find(ui, label)?.let { return it }
        val directions = listOf(
            AccessibilityNodeInfo.ACTION_SCROLL_FORWARD,
            AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD
        )
        for (direction in directions) {
            repeat(10) {
                find(ui, label)?.let { return it }
                val scrollable = nodes(ui).asSequence()
                    .filter { it.packageName?.toString() == context.packageName && it.isScrollable && it.isVisibleToUser }
                    .map { it to Rect().also(it::getBoundsInScreen) }
                    .filter { (_, bounds) -> !bounds.isEmpty }
                    .maxByOrNull { (_, bounds) -> bounds.width().toLong() * bounds.height() }
                    ?.first ?: return@repeat
                if (!scrollable.performAction(direction)) return@repeat
                instrumentation.waitForIdleSync()
                SystemClock.sleep(100)
            }
        }
        return find(ui, label) ?: throw AssertionError(
            "Visible TalkBack target not found: '$label'\n${describe(ui)}"
        )
    }

    private fun find(ui: UiAutomation, label: String): AccessibilityNodeInfo? {
        val window = rootBounds(ui)
        return nodes(ui).asSequence()
            .filter { node ->
                val bounds = Rect().also(node::getBoundsInScreen)
                node.packageName?.toString() == context.packageName &&
                    node.isVisibleToUser && !bounds.isEmpty && Rect.intersects(bounds, window) &&
                    matches(node, label)
            }
            .sortedByDescending { it.isClickable || it.isFocusable || it.isEditable }
            .firstOrNull()
    }

    private fun matches(node: AccessibilityNodeInfo, label: String): Boolean {
        val query = normalize(label)
        return listOf(
            node.text?.toString().orEmpty(),
            node.contentDescription?.toString().orEmpty(),
            node.stateDescription?.toString().orEmpty()
        ).any { normalize(it).contains(query) }
    }

    private fun nodes(ui: UiAutomation): List<AccessibilityNodeInfo> {
        val root = appRoot(ui) ?: return emptyList()
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo) {
            result += node
            for (index in 0 until node.childCount) node.getChild(index)?.let(::visit)
        }
        visit(root)
        return result
    }

    private fun appRoot(ui: UiAutomation): AccessibilityNodeInfo? =
        ui.rootInActiveWindow?.takeIf {
            it.packageName?.toString() == context.packageName
        }

    private fun rootBounds(ui: UiAutomation): Rect = Rect().also {
        appRoot(ui)?.getBoundsInScreen(it)
    }

    private fun normalize(value: String): String =
        value.lowercase().filter(Char::isLetterOrDigit)

    private fun describe(ui: UiAutomation): String = nodes(ui).joinToString("\n") { node ->
        val bounds = Rect().also(node::getBoundsInScreen)
        "text=${node.text} desc=${node.contentDescription} state=${node.stateDescription} " +
            "visible=${node.isVisibleToUser} focused=${node.isAccessibilityFocused} " +
            "clickable=${node.isClickable} enabled=${node.isEnabled} bounds=$bounds"
    }

    private fun waitFor(description: String, predicate: () -> Boolean) {
        val deadline = SystemClock.uptimeMillis() + 10_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (predicate()) return
            SystemClock.sleep(75)
        }
        throw AssertionError("Timed out waiting for $description")
    }

    private fun capture(ui: UiAutomation, screen: String) {
        val name = "${screen}-${SystemClock.uptimeMillis()}"
        val bitmap: Bitmap = requireNotNull(ui.takeScreenshot()) {
            "Screenshot unavailable while recording $screen"
        }
        File(output, "$name.png").outputStream().use { stream ->
            assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream))
        }
        bitmap.recycle()
        File(output, "$name.txt").writeText(describe(ui))
    }
}
