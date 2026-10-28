package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class DemoSection(val label: String) {
    Map("Мапа"), Settings("Налаштування"), Profile("Профіль")
}

internal enum class AccountLayout {
    PhonePortrait, PhoneLandscape, TabletPortrait, TabletLandscape;

    val landscape get() = this == PhoneLandscape || this == TabletLandscape
    val tablet get() = this == TabletPortrait || this == TabletLandscape

    companion object {
        fun forWindow(width: Float, height: Float): AccountLayout {
            val tablet = minOf(width, height) >= 600f
            return when {
                width > height && tablet -> TabletLandscape
                width > height -> PhoneLandscape
                tablet -> TabletPortrait
                else -> PhonePortrait
            }
        }
    }
}

internal data class DemoSectionNavigation(
    val section: DemoSection = DemoSection.Map,
    val backTarget: DemoSection = DemoSection.Map
) {
    fun open(next: DemoSection) = if (next == section) this else DemoSectionNavigation(
        section = next,
        backTarget = if (section == DemoSection.Profile && next == DemoSection.Settings)
            DemoSection.Profile else DemoSection.Map
    )

    fun back() = DemoSectionNavigation(section = backTarget)

    companion object {
        val Saver = listSaver<DemoSectionNavigation, String>(
            save = { listOf(it.section.name, it.backTarget.name) },
            restore = {
                DemoSectionNavigation(DemoSection.valueOf(it[0]), DemoSection.valueOf(it[1]))
            }
        )
    }
}

internal data class DemoProfileState(val vehicleClass: String, val vehicleMode: String) {
    val title = "Демо-профіль"
    val identity = "Без авторизації"
    val accountDetails = "Ім’я та пошту не отримано. Акаунт і підтвердження пошти відсутні."
    val organization = "Корпоративне членство не підключено. Організацію й роль не підтверджено."
    val statisticsExplanation =
        "Історія поїздок і статистика відсутні; серверні дані не отримувалися."
    val selectedVehicle: DemoVehicleProfile? get() = demoVehicleForClass(vehicleClass)
}

internal data class UnavailableSetting(val title: String, val explanation: String) {
    val status = "Не підключено"
}

internal val unavailablePersonalSettings = listOf(
    UnavailableSetting("Голосові підказки", "Голосовий сервіс і підказки маршруту не підключено."),
    UnavailableSetting(
        "XR-навігація",
        "XR-сценарій не реалізовано. Справжня навігація недоступна."
    ),
    UnavailableSetting("Сповіщення", "Сервіс не підключено. Системний дозвіл не перевірявся.")
)

internal val unavailableOrganizationSettings = listOf(
    UnavailableSetting(
        "Уникати ґрунтових доріг",
        "Політики автопарку не отримано; редагування недоступне."
    ),
    UnavailableSetting(
        "Ліміт швидкості автопарку",
        "Ліміт не отримано. Організацію й роль не підтверджено."
    )
)
