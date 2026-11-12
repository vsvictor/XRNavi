package com.mobilespace.xrnavi

import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class ProfileRedesignRenderingTest {
    @Test
    fun securityShowsTwoSectionsWithIconValueContacts() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val profile = AuthenticatedProfile(
            "12345678-1234-1234-1234-123456789012", "login@example.invalid", "2026-01-01",
            listOf("driver", "admin", "company_manager"),
            buildJsonObject { put("first_name", "Ім’я") },
            contacts = listOf(
                ProfileContact("email", "contact@example.invalid"),
                ProfileContact("phone", "+12025550123"),
            ),
        )
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            AuthenticatedProfileDetails(profile)
                        }
                    }
                }
            }
            awaitNode { it.text?.toString() == "Дані акаунта" }
            val root = requireNotNull(instrumentation.uiAutomation.rootInActiveWindow)
            listOf("Особисті дані", "Дані акаунта", "ПІБ", "Ім’я",
                "contact@example.invalid", "+12025550123").forEach { label ->
                assertNotNull(label, find(root) { it.text?.toString() == label })
            }
            listOf("Електронна пошта", "Телефон").forEach { label ->
                assertNotNull(label, find(root) { it.contentDescription?.toString() == label })
            }
            listOf("Контакти", "Підтвердження пошти", "Статус невідомий",
                "login@example.invalid", "Вийти з акаунта").forEach { label ->
                assertFalse(label, find(root) { it.text?.toString() == label } != null)
            }
            listOf("driver", "admin", "company_manager").forEach { role ->
                assertNotNull(role, find(root) { it.text?.toString() == role })
            }
            assertEquals(3, count(root) { it.contentDescription?.toString()?.startsWith("Роль: ") == true })
            listOf("driver", "admin", "company_manager").forEach { role ->
                assertNotNull(find(root) { it.contentDescription?.toString() == "Роль: $role" })
            }
            val idNode = requireNotNull(find(root) {
                it.text?.toString() == profile.id
            })
            val idBounds = android.graphics.Rect().also(idNode::getBoundsInScreen)
            val idLabel = requireNotNull(find(root) { it.text?.toString() == "ID" })
            val idLabelBounds = android.graphics.Rect().also(idLabel::getBoundsInScreen)
            assertTrue(
                "ID should occupy a single line",
                idBounds.height() < 30 * instrumentation.targetContext.resources.displayMetrics.density,
            )
            assertTrue(
                "ID label and value should share a row",
                kotlin.math.abs(idBounds.centerY() - idLabelBounds.centerY()) <
                    8 * instrumentation.targetContext.resources.displayMetrics.density,
            )
        } finally {
            scenario.close()
        }
    }

    @Test
    fun personalStatisticsMatchFigmaMetricsWithoutDemoValues() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        AuthenticatedProfileStatistics(AccountLayout.PhonePortrait)
                    }
                }
            }
            awaitNode { it.text?.toString() == "год у дорозі" }
            val root = requireNotNull(instrumentation.uiAutomation.rootInActiveWindow)
            listOf("поїздки", "км", "год у дорозі", "—").forEach { label ->
                assertNotNull(find(root) { it.text?.toString() == label })
            }
            listOf("Авто в гаражі", "Місць на пристрої", "32", "4 820", "126").forEach { label ->
                assertFalse(find(root) { it.text?.toString() == label } != null)
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun profileTabsSwitchOnlyTheirContentThroughBloc() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val profile = AuthenticatedProfile(
            "fixture", "driver@example.invalid", "2026-01-01", emptyList(),
            buildJsonObject { put("first_name", "Ім’я") },
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        val bloc = AuthenticatedAccountBloc({ profile }, scope)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                bloc.dispatch(
                    AuthenticatedAccountEvent.LoginCompleted(
                        AuthSession("fixture-access", "fixture-refresh", "Bearer", 3600uL),
                    )
                )
                activity.setContent {
                    val state by bloc.state.collectAsState()
                    XrnaviTheme {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                            AuthenticatedProfileScreen(
                                profile, AccountLayout.PhonePortrait, GarageState(loaded = true),
                                {}, {}, {}, {}, state.profileTab,
                                { bloc.dispatch(AuthenticatedAccountEvent.SelectProfileTab(it)) },
                                onLogout = {},
                            )
                        }
                    }
                }
            }
            fun root(): AccessibilityNodeInfo {
                instrumentation.waitForIdleSync()
                return requireNotNull(instrumentation.uiAutomation.rootInActiveWindow)
            }

            fun clickTab(label: String) {
                val tab = find(root()) { node ->
                    node.isClickable && find(node) { it.text?.toString() == label } != null
                }
                assertNotNull(tab)
                assertTrue(tab!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                instrumentation.waitForIdleSync()
            }
            assertFalse(find(root()) { it.text?.toString() == "Зареєстрований" } != null)
            assertFalse(find(root()) { it.text?.toString() == "АКАУНТ XRNavi" } != null)
            clickTab("Корпоративний")
            awaitNode { it.text?.toString() == "Корпоративний акаунт" }
            assertEquals(ProfileAccountTab.Corporate, bloc.state.value.profileTab)
            assertNotNull(find(root()) { it.text?.toString() == "Корпоративний акаунт" })
            assertFalse(find(root()) { it.text?.toString() == "Особисті показники" } != null)
            assertFalse(find(root()) { it.text?.toString() == profile.email } != null)
            assertFalse(find(root()) { it.text?.toString() == "Вийти з акаунта" } != null)
            clickTab("Особистий")
            awaitNode { it.text?.toString() == profile.email }
            assertEquals(ProfileAccountTab.Personal, bloc.state.value.profileTab)
            assertNotNull(find(root()) { it.text?.toString() == profile.email })
        } finally {
            scenario.close()
            scope.cancel()
        }
    }

    @Test
    fun logoutActionWorksAndSecurityHasNoLogout() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val logout = AtomicBoolean(false)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent { XrnaviTheme { ProfileLogoutButton { logout.set(true) } } }
            }
            instrumentation.waitForIdleSync()
            val button = awaitNode { node ->
                node.isClickable && find(node) { it.text?.toString() == "Вийти з акаунта" } != null
            }
            assertTrue(button.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            instrumentation.waitForIdleSync()
            assertTrue(logout.get())
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        Column(
                            Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState(Int.MAX_VALUE))
                        ) {
                            AuthenticatedAccountSecurityScreen(
                                AuthenticatedProfile(
                                    "fixture",
                                    "driver@example.invalid",
                                    "2026-01-01",
                                    emptyList(),
                                    buildJsonObject {}),
                                {},
                            )
                        }
                    }
                }
            }
            instrumentation.waitForIdleSync()
            awaitNode { it.text?.toString() == "Ролі" }
            val security = requireNotNull(instrumentation.uiAutomation.rootInActiveWindow)
            assertFalse(find(security) { it.text?.toString() == "Вийти з акаунта" } != null)
            assertNotNull(find(security) { it.text?.toString() == "Ролі" })
        } finally {
            scenario.close()
        }
    }

    @Test
    fun appHeaderOnlyShowsBackForChildScreens() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val back = AtomicBoolean(false)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            for (child in listOf(false, true)) {
                scenario.onActivity { activity ->
                    activity.setContent {
                        XrnaviTheme {
                            AccountPageHero(
                                "Профіль",
                                onBack = if (child) ({ back.set(true); Unit }) else null
                            )
                        }
                    }
                }
                val deadline = SystemClock.uptimeMillis() + 5_000
                var root: AccessibilityNodeInfo? = null
                do {
                    instrumentation.waitForIdleSync()
                    root = instrumentation.uiAutomation.rootInActiveWindow
                    if (root != null &&
                        find(root) { it.text?.toString() == "XRNavi" } != null &&
                        (find(root) { it.contentDescription?.toString() == "Назад" } != null) == child
                    ) break
                    SystemClock.sleep(50)
                } while (SystemClock.uptimeMillis() < deadline)
                val window = requireNotNull(root)
                assertNotNull(find(window) { it.text?.toString() == "XRNavi" })
                assertNotNull(find(window) { it.contentDescription?.toString() == "Знак Mobile Space 01 Orbit" })
                val arrow = find(window) { it.contentDescription?.toString() == "Назад" }
                assertTrue((arrow != null) == child)
                if (child) {
                    val button = find(window) {
                        it.isClickable && find(it) { node ->
                            node.contentDescription?.toString() == "Назад"
                        } != null
                    }
                    assertNotNull(button)
                    assertTrue(button!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                    instrumentation.waitForIdleSync()
                    assertTrue(back.get())
                }
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun rendersProfileInLightDarkPhoneAndTabletLayouts() {
        val profile = AuthenticatedProfile(
            id = "design-fixture",
            email = "driver@example.invalid",
            createdAt = "2026-01-01",
            roles = emptyList(),
            fields = buildJsonObject {
                put("first_name", "Ім’я")
                put("last_name", "Прізвище")
                put("account_type", "private")
            },
        )
        val vehicle =
            GarageVehicle("fixture-car", "Мій автомобіль", "Легкове", VerificationStatus.None)
        val active =
            GarageState(loaded = true, vehicles = listOf(vehicle), activeVehicleId = vehicle.id)
        val error = GarageState(
            loadFailed = true,
            error = "Не вдалося завантажити гараж. Перевірте з'єднання та повторіть.",
        )
        val corporate = profile.copy(fields = buildJsonObject {
            put("first_name", "Дуже довге ім’я для перевірки")
            put("last_name", "Довге прізвище користувача")
            put("account_type", "corporate")
        })
        captureDesigns(
            listOf(
                DesignCase("Profile-Active") { ProfileDesignPreview(profile, active) },
                DesignCase("Profile-PersonalActions") {
                    ProfileDesignPreview(
                        profile,
                        active,
                        scrollToBottom = true
                    )
                },
                DesignCase("Profile-Empty") {
                    ProfileDesignPreview(
                        profile,
                        GarageState(loaded = true)
                    )
                },
                DesignCase("Profile-Error") { ProfileDesignPreview(profile, error) },
                DesignCase("Profile-Loading") {
                    ProfileDesignPreview(
                        profile,
                        GarageState(loading = true)
                    )
                },
                DesignCase("Profile-CorporateLong") { ProfileDesignPreview(corporate, active) },
                DesignCase("Profile-CorporateTab") {
                    ProfileDesignPreview(
                        profile,
                        active,
                        ProfileAccountTab.Corporate
                    )
                },
                DesignCase("Account-Security") {
                    Column(
                        Modifier.fillMaxSize().accountPageBackground()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        AuthenticatedAccountSecurityScreen(
                            profile.copy(contacts = listOf(
                                ProfileContact("email", profile.email),
                                ProfileContact("phone", "+12025550123"),
                                ProfileContact("telegram", "@example_driver"),
                                ProfileContact("viber", "+12025550123"),
                                ProfileContact("facebook", "example.driver"),
                                ProfileContact("x", "@example_driver"),
                                ProfileContact("instagram", "@example_driver"),
                                ProfileContact("tiktok", "@example_driver"),
                                ProfileContact("whatsapp", "+12025550123"),
                                ProfileContact("signal", "example.123"),
                                ProfileContact("linkedin", "example-driver"),
                                ProfileContact("website", "https://example.invalid"),
                            )),
                            {},
                        )
                    }
                },
                DesignCase("Account-Favorites") {
                    SavedPlacesScreen(
                        places = emptyList(), initialEditKey = null,
                        allowAddressSelection = false, allowDestinationUse = true,
                        notice = "", onBack = {}, onSelectAddress = {},
                        onUseAsDestination = {}, onRename = { _, _ -> },
                        onDelete = {},
                    )
                },
                DesignCase("Account-FavoritesFilled") {
                    SavedPlacesScreen(
                        places = listOf(SavedPlace("Київ, Хрещатик, 1", "Збережена адреса", "Дім")),
                        initialEditKey = null,
                        allowAddressSelection = false, allowDestinationUse = true,
                        notice = "", onBack = {}, onSelectAddress = {},
                        onUseAsDestination = {}, onRename = { _, _ -> },
                        onDelete = {},
                    )
                },
            ),
            "profile-redesign-captures",
        )
    }

    @Test
    fun profileActionsAndVehicleDetailsRemainReachableInDarkTheme() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val securityOpened = AtomicBoolean(false)
        val favoritesOpened = AtomicBoolean(false)
        val detailsOpened = AtomicBoolean(false)
        val vehicle =
            GarageVehicle("fixture-car", "Мій автомобіль", "Легкове", VerificationStatus.None)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(instrumentation.context, DesignCaptureActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme(darkTheme = true) {
                        Column {
                            AuthenticatedProfileActions(
                                AccountLayout.PhonePortrait,
                                onOpenSecurity = { securityOpened.set(true) },
                                onOpenFavorites = { favoritesOpened.set(true) },
                            )
                            AuthenticatedProfileVehicle(
                                GarageState(
                                    loaded = true, vehicles = listOf(vehicle),
                                    activeVehicleId = vehicle.id,
                                ),
                                onOpenGarage = {},
                                onOpenVehicleDetails = { detailsOpened.set(it == vehicle.id) },
                            )
                        }
                    }
                }
            }
            listOf(
                "Безпека та особисті дані" to securityOpened,
                "Обране" to favoritesOpened,
                "Деталі авто" to detailsOpened,
            ).forEach { (label, opened) ->
                val deadline = SystemClock.uptimeMillis() + 5_000
                var button: AccessibilityNodeInfo? = null
                while (button == null && SystemClock.uptimeMillis() < deadline) {
                    instrumentation.uiAutomation.rootInActiveWindow?.let { root ->
                        button = find(root) {
                            it.isClickable && find(it) { child -> child.text?.toString() == label } != null
                        }
                    }
                    if (button == null) SystemClock.sleep(50)
                }
                assertNotNull("Missing profile action: $label", button)
                assertTrue(button!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                instrumentation.waitForIdleSync()
                assertTrue("Profile action did not invoke callback: $label", opened.get())
            }
        } finally {
            scenario.close()
        }
    }

    @Test
    fun garageFailureIsVisibleAndGarageActionStillWorks() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.context
        val opened = AtomicBoolean(false)
        val scenario = ActivityScenario.launch<DesignCaptureActivity>(
            Intent(
                context,
                DesignCaptureActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        try {
            scenario.onActivity { activity ->
                activity.setContent {
                    XrnaviTheme {
                        Column {
                            val garage = GarageState(loadFailed = true, error = "Гараж недоступний")
                            AuthenticatedProfileVehicle(
                                garage,
                                onOpenGarage = { opened.set(true) },
                                onOpenVehicleDetails = {},
                            )
                        }
                    }
                }
            }
            instrumentation.waitForIdleSync()
            val deadline = SystemClock.uptimeMillis() + 5_000
            var root = instrumentation.uiAutomation.rootInActiveWindow
            while (
                (root == null || find(root) { it.text?.toString() == "Перейти до гаража" } == null) &&
                SystemClock.uptimeMillis() < deadline
            ) {
                SystemClock.sleep(50)
                root = instrumentation.uiAutomation.rootInActiveWindow
            }
            root = requireNotNull(root) { "Profile preview must be in the foreground" }
            assertFalse(find(root) { it.text?.toString() == "Гараж недоступний" } != null)
            assertFalse(find(root) { it.text?.toString() == "Ваш гараж ще порожній" } != null)
            val button = find(root) {
                it.isClickable && find(it) { child -> child.text?.toString() == "Перейти до гаража" } != null
            }
            assertNotNull(button)
            assertTrue(button!!.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            instrumentation.waitForIdleSync()
            assertTrue(opened.get())
        } finally {
            scenario.close()
        }
    }

    private fun awaitNode(predicate: (AccessibilityNodeInfo) -> Boolean): AccessibilityNodeInfo {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val deadline = SystemClock.uptimeMillis() + 5_000
        do {
            instrumentation.waitForIdleSync()
            instrumentation.uiAutomation.rootInActiveWindow?.let { root ->
                find(root, predicate)?.let { return it }
            }
            SystemClock.sleep(50)
        } while (SystemClock.uptimeMillis() < deadline)
        throw AssertionError("Expected accessibility node did not appear")
    }

    private fun find(
        node: AccessibilityNodeInfo,
        matches: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        if (matches(node)) return node
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { find(it, matches)?.let { found -> return found } }
        }
        return null
    }

    private fun count(
        node: AccessibilityNodeInfo,
        matches: (AccessibilityNodeInfo) -> Boolean,
    ): Int {
        var result = if (matches(node)) 1 else 0
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { result += count(it, matches) }
        }
        return result
    }
}
