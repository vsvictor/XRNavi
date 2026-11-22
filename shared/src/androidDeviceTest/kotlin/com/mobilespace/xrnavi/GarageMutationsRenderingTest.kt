package com.mobilespace.xrnavi

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GarageMutationsRenderingTest {
    @Test
    fun cardUsesClickAndLongClickWithoutInterferingWithIconButtons() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        var density = 1f
        var profiles = 0
        var selections = 0
        var edits = 0
        var deletes = 0
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        density = LocalDensity.current.density
                        CompositionLocalProvider(LocalDensity provides Density(density, fontScale = 1.5f)) {
                            Column(Modifier.width(280.dp)) {
                                GarageListHeader()
                                AuthenticatedGarageVehicleCard(
                                    GarageVehicle("test", "Ford Mustang GT", "passenger", VerificationStatus.None,
                                        heightMeters = 1.4, widthMeters = 1.9, lengthMeters = 4.8, weightKg = 1700),
                                    active = false, enabled = true,
                                    onSelect = { selections++ }, onDetails = { profiles++ },
                                    onEdit = { edits++ }, onDelete = { deletes++ },
                                )
                            }
                        }
                    }
                }
            }
            val positions = mutableMapOf<String, android.graphics.Rect>()
            for (label in listOf("Редагувати", "Видалити")) {
                var button = awaitText(label, scroll = false)
                while (!button.isClickable) button = requireNotNull(button.parent)
                positions[label] = android.graphics.Rect().also { button.getBoundsInScreen(it) }
                assertEquals("Action must be icon-only", null, button.text)
                assertTrue("Action must have a 48dp touch target", positions.getValue(label).width() >= 48 * density)
            }
            val edit = positions.getValue("Редагувати")
            val delete = positions.getValue("Видалити")
            assertTrue("Edit and delete must be aligned", edit.right < delete.left && edit.centerY() == delete.centerY())
            val name = awaitText("Ford Mustang GT", scroll = false)
            var card = name
            while (!card.isClickable || !card.isLongClickable) card = requireNotNull(card.parent)
            assertTrue(card.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            assertTrue(card.performAction(AccessibilityNodeInfo.ACTION_LONG_CLICK))
            click("Редагувати", scroll = false)
            click("Видалити", scroll = false)
            assertEquals(1, profiles)
            assertEquals(1, selections)
            assertEquals(1, edits)
            assertEquals(1, deletes)
            for (window in automation.windows) {
                assertEquals(null, findText(window.root, "+"))
                assertEquals(null, findText(window.root, "Назад до мапи"))
                assertEquals(null, findText(window.root, "Висота 1.4 м"))
                assertEquals(null, findText(window.root, "Маса 1700 кг"))
            }
        } finally {
            scenario.close()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    @Test
    fun cardEditsSavedVehicleAndDeletionRequiresConfirmation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val originalFlags = automation.serviceInfo.flags
        automation.serviceInfo = automation.serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        val snapshot =
            """{"make":{"id":"1","name":"Toyota","raw_data":{}},"model":{"id":"2","name":"Camry","raw_data":{}},"year":2020,"trim":{"id":"42","name":"LE","raw_data":{}},"bodies":[{"height":58.2,"curb_weight":3460}],"engines":[{"transmission":"Automatic"}],"mileages":[],"exterior_colors":[{"name":"White","rgb":"255,255,255"},{"name":"Black","rgb":"0,0,0"}],"interior_colors":[],"selected_exterior_color":{"name":"White","rgb":"255,255,255"},"equipment_data":{},"vin":null,"vin_data":null}"""
        var record: String? =
            """{"id":"saved","name":"Toyota Camry · 2020","category":"passenger","verification":"none","vehicle":$snapshot}"""
        var updateCalls = 0
        var deleteCalls = 0
        val service = RpcGarageService { method, raw ->
            when (method) {
                "garage.vehicles.list" -> """{"vehicles":[${record.orEmpty()}],"active_vehicle_id":${if (record == null) "null" else "\"saved\""}}"""
                "car.oemmakemodels_index_get" -> """{"data":[{"id":2,"name":"Camry"}]}"""
                "car.years_index2_get" -> "[2019,2020]"
                "car.oemtrims_index_get" -> """{"data":[{"id":42,"description":"LE"}],"collection":{"pages":1}}"""
                "garage.vehicles.update" -> {
                    val params = Json.parseToJsonElement(raw).jsonObject
                    assertEquals("saved", params["vehicle_id"]?.jsonPrimitive?.content)
                    val vehicle = params["vehicle"]?.jsonObject
                    assertEquals(
                        "Black",
                        vehicle?.get("selected_exterior_color")?.jsonObject?.get("name")?.jsonPrimitive?.content
                    )
                    updateCalls++
                    record =
                        """{"id":"saved","name":"Toyota Camry · 2020","category":"passenger","verification":"none","vehicle":$vehicle}"""
                    """{"vehicle":$record}"""
                }

                "garage.vehicles.delete" -> {
                    deleteCalls++
                    record = null
                    """{"active_vehicle_id":null}"""
                }

                else -> error("Unexpected RPC: $method")
            }
        }
        val session = { AuthSession("t", "r", "Bearer", 60u) }
        val garage = GarageBloc(service, session, scope)
        val add = AddVehicleBloc(service, session, scope)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                garage.dispatch(GarageEvent.Load)
                activity.setContent {
                    XrnaviTheme {
                        val garageState = garage.state.collectAsState().value
                        val addState = add.state.collectAsState().value
                        LaunchedEffect(addState.added) {
                            if (addState.added != null) garage.dispatch(GarageEvent.Load)
                        }
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            GarageListHeader()
                            if (addState.open) AddVehicleScreen(addState, add::dispatch)
                            else GarageScreen(
                                garageState,
                                garage::dispatch,
                                {},
                                {},
                                { add.dispatch(AddVehicleEvent.Edit(it)) })
                        }
                    }
                }
            }
            scenario.onActivity { activity ->
                activity.startActivity(
                    Intent(activity, DesignCaptureActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }
            click("Редагувати")
            assertEquals("saved", add.state.value.editingVehicle?.id)
            awaitText("Camry")
            click("Комплектація")
            click("Black · 0,0,0")
            click("Зберегти зміни")
            awaitText("Зміни авто збережено")
            assertEquals(1, updateCalls)
            assertEquals("saved", add.state.value.added?.id)
            click("Готово")
            click("Видалити")
            awaitText("Видалити авто?")
            assertEquals(0, deleteCalls)
            click("Скасувати")
            assertFalse(garage.state.value.vehicles.isEmpty())
            click("Видалити")
            click("Видалити", scroll = false)
            awaitText("Гараж порожній")
            assertEquals(1, deleteCalls)
            assertTrue(garage.state.value.vehicles.isEmpty())
            assertEquals(null, garage.state.value.activeVehicleId)
        } finally {
            scenario.close()
            scope.cancel()
            automation.serviceInfo = automation.serviceInfo.apply { flags = originalFlags }
        }
    }

    private fun click(text: String, scroll: Boolean = true) {
        var node = awaitText(text, scroll)
        while (!node.isClickable) node = requireNotNull(node.parent)
        assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
    }

    private fun awaitText(text: String, scroll: Boolean = true): AccessibilityNodeInfo {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val automation = instrumentation.uiAutomation
        val deadline = SystemClock.uptimeMillis() + 12_000
        var forward = true
        while (SystemClock.uptimeMillis() < deadline) {
            if (android.os.Build.VERSION.SDK_INT >= 33) automation.clearCache()
            for (window in automation.windows.reversed()) findText(
                window.root,
                text
            )?.let { return it }
            if (scroll) {
                val roots = automation.windows.mapNotNull { it.root }
                val scrollable = roots.firstNotNullOfOrNull { findScrollable(it) }
                val moved =
                    scrollable?.performAction(if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
                if (moved != true) forward = !forward
            }
            instrumentation.waitForIdleSync()
            SystemClock.sleep(200)
        }
        error("Missing garage UI text: $text")
    }

    private fun findText(node: AccessibilityNodeInfo?, text: String): AccessibilityNodeInfo? {
        if (node == null) return null
        if (node.text?.toString() == text || node.contentDescription?.toString() == text) return node
        for (i in 0 until node.childCount) findText(node.getChild(i), text)?.let { return it }
        return null
    }

    private fun findScrollable(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        if (node.isScrollable) return node
        for (i in 0 until node.childCount) node.getChild(i)?.let { findScrollable(it) }
            ?.let { return it }
        return null
    }
}
