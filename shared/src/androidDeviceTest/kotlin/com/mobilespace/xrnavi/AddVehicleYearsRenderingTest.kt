package com.mobilespace.xrnavi

import android.content.Intent
import android.graphics.Rect
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AddVehicleYearsRenderingTest {
    @Test
    fun vinDialogFillsVehicleAndYearWithoutLeavingGeneral() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val service = RpcGarageService { method, _ ->
            when (method) {
                "catalog.classes.list" -> """{"classes":[]}"""
                "car.vindecoder_index_get" -> """{"make":"Toyota","model":"Camry","year":2018,"trim":"LE","specs":{"curb_weight_pounds":"3460"},"trims":[]}"""
                "car.makes_index2_get" -> """{"data":[{"id":1,"name":"Toyota"}]}"""
                "car.oemmakemodels_index_get" -> """{"data":[{"id":2,"name":"Camry"}]}"""
                "car.years_index2_get" -> "[2018,2019,2020]"
                "car.oemtrims_index_get" -> """{"data":[{"id":42,"trim":"LE","description":"LE Sedan"}],"collection":{"pages":1}}"""
                "car.oemtrims_view_get" -> """{"bodies":[],"engines":[],"mileages":[],"exterior_colors":[],"interior_colors":[]}"""
                "car.makemodeltrimbodies_index2_get" -> """{"data":[{"type":"Sedan","height":58.2,"length":191.7,"width":72.9}],"collection":{"pages":1}}"""
                "car.makemodeltrimengines_index2_get" -> """{"data":[{"size":2.5,"transmission":"Automatic","drive_type":"Front wheel drive"}],"collection":{"pages":1}}"""
                "car.makemodeltrimmileages_index2_get" -> """{"data":[{"combined_mpg":30}],"collection":{"pages":1}}"""
                else -> error("Unexpected RPC: $method")
            }
        }
        val bloc = AddVehicleBloc(service, { AuthSession("t", "r", "Bearer", 60u) }, scope)
        val scroll = ScrollState(0)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                bloc.dispatch(AddVehicleEvent.Open)
                activity.setContent {
                    XrnaviTheme {
                        val state = bloc.state.collectAsState().value
                        Column(Modifier.verticalScroll(scroll)) { AddVehicleScreen(state, bloc::dispatch) }
                    }
                }
            }
            bringTestToFront()
            awaitFormText("VIN", scroll, scope, scenario)
            assertFalse(automation.windows.any { findText(it.root, "Введіть VIN авто") != null })
            val vinBounds = android.graphics.Rect().also { awaitText("VIN").getBoundsInScreen(it) }
            val scanBounds = android.graphics.Rect().also { awaitText("Scan").getBoundsInScreen(it) }
            val smartCarBounds = android.graphics.Rect().also { awaitText("SmartCar").getBoundsInScreen(it) }
            assertEquals(vinBounds.centerY(), scanBounds.centerY())
            assertEquals(vinBounds.centerY(), smartCarBounds.centerY())
            assertTrue(vinBounds.right < scanBounds.left)
            assertTrue(scanBounds.right < smartCarBounds.left)
            val buttonBounds = listOf("VIN", "Scan", "SmartCar").map { label ->
                var button = awaitText(label)
                while (!button.isClickable) button = requireNotNull(button.parent)
                android.graphics.Rect().also { button.getBoundsInScreen(it) }
            }
            for (bounds in buttonBounds.drop(1)) {
                assertTrue(kotlin.math.abs(buttonBounds.first().width() - bounds.width()) <= 1)
                assertEquals(buttonBounds.first().height(), bounds.height())
                assertEquals(buttonBounds.first().top, bounds.top)
            }
            for (label in listOf("Scan", "SmartCar")) {
                var button = awaitText(label)
                while (!button.isClickable) button = requireNotNull(button.parent)
                assertTrue(button.isEnabled)
                val before = bloc.state.value
                clickText(label)
                assertEquals(before, bloc.state.value)
            }
            clickText("VIN")
            awaitText("Введіть VIN авто")
            assertTrue(bloc.state.value.vinDialogOpen)
            var input = awaitText("Введіть VIN авто")
            while (!input.isEditable) input = requireNotNull(input.parent)
            assertTrue(input.performAction(
                AccessibilityNodeInfo.ACTION_SET_TEXT,
                android.os.Bundle().apply {
                    putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "1GTG6CEN0L1139305")
                },
            ))
            instrumentation.waitForIdleSync()
            clickText("Пошук")
            awaitFormText("Camry", scroll, scope, scenario)
            assertEquals("Toyota", bloc.state.value.selectedMake?.name)
            assertEquals("Camry", bloc.state.value.selectedModel?.name)
            assertEquals(2018, bloc.state.value.year)
            assertEquals(AddVehicleTab.General, bloc.state.value.selectedTab)
            assertFalse(bloc.state.value.vinDialogOpen)
            val year = awaitFormText("2018", scroll, scope, scenario)
            var selected = year
            while (!selected.isSelected && !selected.isChecked && selected.parent != null) {
                selected = requireNotNull(selected.parent)
            }
            assertTrue(selected.isSelected || selected.isChecked)
            clickText("Комплектація")
            awaitFormText("LE Sedan", scroll, scope, scenario)
            assertEquals("42", bloc.state.value.equipment.selectedTrim?.id)
            awaitFormText("Споряджена маса, кг: 1569.4", scroll, scope, scenario)
            awaitFormText("Трансмісія: Automatic", scroll, scope, scenario)
            awaitFormText("Змішана витрата, л/100 км: 7.8", scroll, scope, scenario)
            assertTrue(bloc.state.value.canSubmit)
        } finally {
            scenario.close()
            scope.cancel()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    @Test
    fun makeSelectionKeepsMakeFocusAndModelSelectionHidesKeyboardWithCursorAtEnd() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val service = RpcGarageService { method, _ ->
            when (method) {
                "catalog.classes.list" -> """{"classes":[]}"""
                "car.makes_index2_get" -> """{"data":[{"id":1,"name":"Toyota"}]}"""
                "car.oemmakemodels_index_get" -> """{"data":[{"id":2,"name":"Camry"}]}"""
                "car.years_index2_get" -> "[2015,2016]"
                else -> error("Unexpected RPC: $method")
            }
        }
        val bloc = AddVehicleBloc(service, { AuthSession("t", "r", "Bearer", 60u) }, scope)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                bloc.dispatch(AddVehicleEvent.Open)
                activity.setContent {
                    XrnaviTheme {
                        val state = bloc.state.collectAsState().value
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            AddVehicleScreen(state, bloc::dispatch)
                        }
                    }
                }
            }
            bringTestToFront()
            var makeField = awaitScrollableText("Введіть марку авто")
            while (!makeField.isEditable) makeField = requireNotNull(makeField.parent)
            assertTrue(makeField.performAction(AccessibilityNodeInfo.ACTION_FOCUS))
            clickText("Toyota")
            var modelField = awaitScrollableText("Введіть модель авто")
            while (!modelField.isEditable) modelField = requireNotNull(modelField.parent)
            assertFalse(modelField.isFocused)
            val make = awaitText("Toyota")
            assertTrue(make.isFocused)
            assertEquals(6, make.textSelectionStart)
            assertEquals(6, make.textSelectionEnd)
            awaitIme(scenario, true)
            assertTrue(modelField.performAction(AccessibilityNodeInfo.ACTION_FOCUS))
            instrumentation.waitForIdleSync()
            clickText("Camry")
            awaitFieldSelection("Camry", 5)
            awaitIme(scenario, false)
            assertEquals("Camry", bloc.state.value.selectedModel?.name)
            assertEquals(null, bloc.state.value.inputEffect)
        } finally {
            scenario.close()
            scope.cancel()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    @Test
    fun selectedModelStaysInFieldAndYearsAreSelectableBelowIt() {
        val scroll = ScrollState(0)
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        var savedVehicle: kotlinx.serialization.json.JsonObject? = null
        val service = RpcGarageService { method, params ->
            when (method) {
                "catalog.classes.list" -> """{"classes":[]}"""
                "car.makes_index2_get" -> """{"data":[{"id":1,"name":"Toyota"}]}"""
                "car.oemmakemodels_index_get" -> """{"data":[{"id":2,"name":"Camry"}]}"""
                "car.years_index2_get" -> """{"jsonrpc":"2.0","id":1,"result":[2015,2016,2017,2018,2019,2020]}"""
                "car.oemtrims_index_get" -> """{"data":[{"id":42,"trim":"SE","description":"SE"},{"id":43,"trim":"SUV","description":"SUV"}],"collection":{"pages":1}}"""
                "car.makemodeltrimbodies_index2_get" -> {
                    assertEquals("42", Json.parseToJsonElement(params).jsonObject["trim_id"]?.jsonPrimitive?.content)
                    """{"data":[{"type":"Sedan","doors":4,"length":191.7,"width":72.9,"height":58.2,"curb_weight":3460,"seats":5}],"collection":{"pages":1}}"""
                }
                "car.makemodeltrimengines_index2_get" -> {
                    assertEquals("42", Json.parseToJsonElement(params).jsonObject["trim_id"]?.jsonPrimitive?.content)
                    """{"data":[{"fuel_type":"Gasoline","size":"2.5","horsepower_hp":200,"transmission":"Automatic","drive_type":"Front wheel drive"}],"collection":{"pages":1}}"""
                }
                "car.makemodeltrimmileages_index2_get" -> {
                    assertEquals("42", Json.parseToJsonElement(params).jsonObject["trim_id"]?.jsonPrimitive?.content)
                    """{"data":[{"combined_mpg":"30","epa_city_mpg":"25","epa_highway_mpg":"35"}],"collection":{"pages":1}}"""
                }
                "garage.vehicles.add" -> {
                    val snapshot = Json.parseToJsonElement(params).jsonObject["vehicle"]?.jsonObject
                    assertEquals("42", snapshot?.get("trim")?.jsonObject?.get("id")?.jsonPrimitive?.content)
                    savedVehicle = Json.parseToJsonElement(
                        """{"id":"saved-42","name":"Toyota Camry 2018","category":"passenger","verification":"none","vehicle":$snapshot}""",
                    ).jsonObject
                    """{"vehicle":$savedVehicle}"""
                }
                "car.oemtrims_view_get" -> when (
                    Json.parseToJsonElement(params).jsonObject["id"]?.jsonPrimitive?.content
                ) {
                    "42" -> """{"bodies":[{"type":"Sedan","doors":4}],"engines":[{"size":2.5,"fuel_type":"Gasoline","horsepower_hp":200,"transmission":"Automatic"}],"mileages":[{"combined_mpg":30,"range_electric":200}],"exterior_colors":[{"name":"White","rgb":"255,255,255"}],"interior_colors":[{"name":"Black","rgb":"0,0,0"}]}"""
                    "43" -> """{"bodies":[{"type":"SUV","doors":5}],"engines":[{"size":4.0,"transmission":"Manual"}],"mileages":[],"exterior_colors":[{"name":"Red","rgb":"255,0,0"}],"interior_colors":[{"name":"Beige","rgb":"245,245,220"}]}"""
                    else -> error("Unexpected trim")
                }
                else -> error("Unexpected RPC: $method")
            }
        }
        val bloc = AddVehicleBloc(service, { AuthSession("t", "r", "Bearer", 60u) }, scope)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                bloc.dispatch(AddVehicleEvent.Open)
                bloc.dispatch(AddVehicleEvent.MakeFocusChanged(true))
                bloc.dispatch(AddVehicleEvent.SelectMake("1"))
                bloc.dispatch(AddVehicleEvent.SelectModel("2"))
                assertEquals("Camry", bloc.state.value.searchQuery)
                assertEquals((2015..2020).toList(), bloc.state.value.selectedModel?.years)
                activity.setContent {
                    XrnaviTheme {
                        val state = bloc.state.collectAsState().value
                        Column(Modifier.verticalScroll(scroll)) {
                            AddVehicleScreen(state, bloc::dispatch)
                        }
                    }
                }
            }
            bringTestToFront()
            awaitFormText("Camry", scroll, scope, scenario)
            for (year in 2015..2020) {
                awaitFormText("$year", scroll, scope, scenario)
            }
            var year = awaitFormText("2018", scroll, scope, scenario)
            while (!year.isClickable) year = requireNotNull(year.parent)
            assertTrue(year.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            instrumentation.waitForIdleSync()
            assertEquals(2018, bloc.state.value.year)
            assertEquals(AddVehicleTab.Equipment, bloc.state.value.selectedTab)
            awaitFormText("Оберіть комплектацію", scroll, scope, scenario)
            for (window in automation.windows) {
                for (text in listOf(
                    "Кузов", "Двигун", "Трансмісія", "Витрата палива та запас ходу",
                    "Колір кузова", "Колір салону", "Немає даних",
                    "Оберіть комплектацію для перегляду даних кузова.",
                    "Оберіть комплектацію для перегляду даних двигуна.",
                )) {
                    assertEquals("Unexpected details before trim selection: $text", null, findText(window.root, text))
                }
            }
            clickText("Оберіть комплектацію")
            clickText("SE")
            assertEquals("42", bloc.state.value.equipment.selectedTrim?.id)
            assertEquals("SE", bloc.state.value.equipment.trimQuery)
            awaitFormText("Кузов", scroll, scope, scenario)
            awaitFormText("Тип кузова: Sedan", scroll, scope, scenario)
            awaitFormText("Довжина, мм: 4869.2", scroll, scope, scenario)
            awaitFormText("Двигун", scroll, scope, scenario)
            awaitFormText("Об'єм двигуна, л: 2.5", scroll, scope, scenario)
            val fuelRow = awaitText("Пальне: Gasoline")
            val sizeRow = awaitText("Об'єм двигуна, л: 2.5")
            val fuelBounds = Rect().also { fuelRow.getBoundsInScreen(it) }
            val sizeBounds = Rect().also { sizeRow.getBoundsInScreen(it) }
            assertTrue(fuelBounds.bottom <= sizeBounds.top)
            awaitFormText("Потужність, hp: 200", scroll, scope, scenario)
            awaitFormText("Трансмісія", scroll, scope, scenario)
            awaitFormText("Привід: Front wheel drive", scroll, scope, scenario)
            val transmissionRow = awaitText("Трансмісія: Automatic")
            val driveRow = awaitText("Привід: Front wheel drive")
            val transmissionBounds = Rect().also { transmissionRow.getBoundsInScreen(it) }
            val driveBounds = Rect().also { driveRow.getBoundsInScreen(it) }
            assertTrue(transmissionBounds.bottom <= driveBounds.top)
            assertEquals("Camry", bloc.state.value.searchQuery)
            assertEquals((2015..2020).toList(), bloc.state.value.selectedModel?.years)
            for (field in VehicleConfigurationField.entries.filter {
                it != VehicleConfigurationField.Body && it != VehicleConfigurationField.Engine
                    && it != VehicleConfigurationField.Transmission
            }) {
                awaitFormText(field.label, scroll, scope, scenario)
                val options = bloc.state.value.equipment.options(field)
                val color = if (field == VehicleConfigurationField.ExteriorColor) "White" else "Black"
                val option = options.single { it.rawData["name"]?.jsonPrimitive?.content == color }
                awaitFormText(option.name, scroll, scope, scenario)
                val diameter = kotlin.math.round(50 * instrumentation.targetContext.resources.displayMetrics.density).toInt()
                scenario.onActivity { scope.launch {
                    scroll.scrollTo((scroll.value + diameter).coerceAtMost(scroll.maxValue))
                } }
                instrumentation.waitForIdleSync()
                if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
                val swatch = awaitText(option.name)
                val bounds = Rect().also { swatch.getBoundsInScreen(it) }
                assertEquals(diameter, bounds.width())
                assertEquals(diameter, bounds.height())
                assertEquals(null, swatch.text)
                clickText(option.name)
                assertEquals(option, bloc.state.value.equipment.selections[field])
                awaitFormText(option.name, scroll, scope, scenario)
            }
            awaitFormText("Витрата палива та запас ходу", scroll, scope, scenario)
            awaitFormText("Змішана витрата, л/100 км: 7.8", scroll, scope, scenario)
            awaitFormText("Витрата на трасі, л/100 км: 6.7", scroll, scope, scenario)
            val cityRow = awaitText("Витрата в місті, л/100 км: 9.4")
            val highwayRow = awaitText("Витрата на трасі, л/100 км: 6.7")
            val cityBounds = Rect().also { cityRow.getBoundsInScreen(it) }
            val highwayBounds = Rect().also { highwayRow.getBoundsInScreen(it) }
            assertTrue(cityBounds.bottom <= highwayBounds.top)
            scenario.onActivity { bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.General)) }
            instrumentation.waitForIdleSync()
            awaitFormText("Camry", scroll, scope, scenario)
            assertEquals(2, bloc.state.value.equipment.selections.size)
            assertEquals("2.5", bloc.state.value.equipment.engines.single()["size"]?.jsonPrimitive?.content)
            scenario.onActivity { bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.Equipment)) }
            instrumentation.waitForIdleSync()
            scenario.onActivity { scope.launch { scroll.scrollTo(0) } }
            instrumentation.waitForIdleSync()
            for ((label, tab) in listOf(
                "Загальне" to AddVehicleTab.General,
                "Комплектація" to AddVehicleTab.Equipment,
                "Загальне" to AddVehicleTab.General,
            )) {
                var node = awaitFormText(label, scroll, scope, scenario)
                while (!node.isClickable) node = requireNotNull(node.parent)
                assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                instrumentation.waitForIdleSync()
                assertEquals(tab, bloc.state.value.selectedTab)
                assertEquals(2018, bloc.state.value.year)
                if (tab == AddVehicleTab.Equipment) awaitFormText("Кузов", scroll, scope, scenario)
            }
            awaitFormText("Camry", scroll, scope, scenario)
            awaitFormText("2018", scroll, scope, scenario)
            scenario.onActivity { bloc.dispatch(AddVehicleEvent.SelectTab(AddVehicleTab.Equipment)) }
            awaitFormText("Зберегти авто", scroll, scope, scenario)
            clickText("Зберегти авто")
            awaitFormText("Транспорт додано до гаража", scroll, scope, scenario)
            assertEquals("saved-42", bloc.state.value.added?.id)
            assertEquals(savedVehicle?.get("vehicle"), bloc.state.value.added?.vehicleData)
            assertEquals(191.7 * 0.0254, bloc.state.value.added?.lengthMeters)
            assertEquals(58.2 * 0.0254, bloc.state.value.added?.heightMeters)
            assertEquals(72.9 * 0.0254, bloc.state.value.added?.widthMeters)
            assertEquals(1569, bloc.state.value.added?.weightKg)
            assertEquals("1569", savedVehicle?.get("vehicle")?.jsonObject?.get("weight_kg")?.jsonPrimitive?.content)
            awaitFormText("Маса: 1569 кг", scroll, scope, scenario)
            awaitFormText("Тип кузова: Sedan", scroll, scope, scenario)
            awaitFormText("Об'єм двигуна, л: 2.5", scroll, scope, scenario)
            awaitFormText("Трансмісія: Automatic", scroll, scope, scenario)
            awaitFormText("Змішана витрата, л/100 км: 7.8", scroll, scope, scenario)
            awaitFormText("Готово", scroll, scope, scenario)
            clickText("Готово")
            assertFalse(bloc.state.value.open)
        } finally {
            scenario.close()
            scope.cancel()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    private fun awaitFormText(
        text: String,
        scroll: ScrollState,
        scope: CoroutineScope,
        scenario: ActivityScenario<DesignCaptureActivity>,
    ): AccessibilityNodeInfo {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        var offset = 0
        val deadline = SystemClock.uptimeMillis() + 8_000
        while (SystemClock.uptimeMillis() < deadline) {
            scenario.onActivity { scope.launch { scroll.scrollTo(offset) } }
            instrumentation.waitForIdleSync()
            SystemClock.sleep(250)
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            for (window in automation.windows) {
                findText(window.root, text)?.let { return it }
            }
            offset += 120
            if (offset > scroll.maxValue + 120) offset = 0
        }
        val visible = automation.windows.mapNotNull { it.root }
            .filter { it.packageName?.toString() == "com.mobilespace.xrnavi.shared.test" }
            .flatMap { visibleText(it) }
        error("Form content did not become visible: $text; scroll=${scroll.value}/${scroll.maxValue}; rendered=$visible")
    }

    private fun visibleText(node: AccessibilityNodeInfo): List<String> =
        listOfNotNull(node.text?.toString()) +
            (0 until node.childCount).flatMap { index ->
                node.getChild(index)?.let(::visibleText).orEmpty()
            }

    private fun bringTestToFront() {
        val context = InstrumentationRegistry.getInstrumentation().context
        context.startActivity(Intent(context, DesignCaptureActivity::class.java).addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP,
        ))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun awaitText(text: String): AccessibilityNodeInfo {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            for (window in automation.windows) {
                findText(window.root, text)?.let { return it }
            }

            SystemClock.sleep(50)
        }
        error("Text did not become visible: $text; windows=${automation.windows.map { it.root?.packageName }}")
    }

    private fun awaitFieldSelection(text: String, cursor: Int) {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            for (window in automation.windows) {
                val node = findText(window.root, text)
                if (node?.isEditable == true &&
                    node.textSelectionStart == cursor && node.textSelectionEnd == cursor) return
            }

            SystemClock.sleep(50)
        }
        error("Field $text did not place its cursor at $cursor")
    }

    private fun awaitScrollableText(text: String, forward: Boolean = true): AccessibilityNodeInfo {
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            for (window in automation.windows) {
                findText(window.root, text)?.let { return it }
                findScrollable(window.root)?.performAction(
                    if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
                    else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD,
                )
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            SystemClock.sleep(50)
        }
        error("Content did not become visible: $text")
    }

    private fun findScrollable(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.isScrollable) return node
        for (index in 0 until node.childCount) {
            findScrollable(node.getChild(index))?.let { return it }
        }
        return null
    }

    private fun clickText(text: String) {
        var node = awaitText(text)
        while (!node.isClickable) node = requireNotNull(node.parent)
        assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun awaitIme(scenario: ActivityScenario<DesignCaptureActivity>, visible: Boolean) {
        val deadline = SystemClock.uptimeMillis() + 5_000
        while (SystemClock.uptimeMillis() < deadline) {
            var matches = false
            scenario.onActivity {
                matches = ViewCompat.getRootWindowInsets(it.window.decorView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == visible
            }
            if (matches) return
            SystemClock.sleep(50)
        }
        error("Keyboard visibility did not become $visible")
    }

    private fun findText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.contentDescription?.toString() == text || node.text?.toString() == text || node.text?.toString()?.lineSequence()?.any { it == text } == true) return node
        for (index in 0 until node.childCount) {
            findText(node.getChild(index), text)?.let { return it }
        }
        return null
    }
}
