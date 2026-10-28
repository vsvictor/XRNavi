package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class DemoSection(val label: String) {
    Map("Мапа"),
    Garage("Гараж"),
    VehicleDetails("Профіль автомобіля"),
    Settings("Налаштування"),
    Profile("Профіль")
}

internal enum class GarageFilter(val label: String) {
    All("Усі"),
    Passenger("Легкове"),
    Cargo("Вантажне"),
    Special("Спецтехніка")
}

internal fun garageVehicles(filter: GarageFilter): List<DemoVehicleProfile> =
    listOf(demoVehicle).filter { vehicle ->
        filter == GarageFilter.All || vehicle.category == filter.label
    }

internal data class GarageVehicleSelection(
    val vehicleClass: String,
    val changed: Boolean
)

internal fun selectGarageVehicle(
    currentVehicleClass: String,
    vehicle: DemoVehicleProfile
): GarageVehicleSelection = GarageVehicleSelection(
    vehicleClass = vehicle.category,
    changed = currentVehicleClass != vehicle.category
)

internal const val garageAddUnavailableExplanation =
    "Додавання транспорту не підключено. Доступний лише наявний демопрофіль Ford Mustang GT; онлайн-каталог, VIN-перевірка й реєстрація права власності відсутні."

internal const val garageEditUnavailableExplanation =
    "Редагування й перевірка характеристик не підключені. Ford Mustang GT — наявний демопрофіль; право власності не підтверджувалося."

internal const val garageCorporateUnavailableExplanation =
    "Корпоративний транспорт не підключено. Автопарк або членство в організації не перевірялися."

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
    val backStack: List<DemoSection> = emptyList()
) {
    fun open(next: DemoSection): DemoSectionNavigation {
        if (next == section) return this
        if (next == DemoSection.Map) return DemoSectionNavigation()
        if (section == DemoSection.VehicleDetails) {
            val parent = back()
            return if (next == parent.section) parent else parent.open(next)
        }
        return copy(section = next, backStack = backStack + section)
    }

    fun back(): DemoSectionNavigation = if (backStack.isEmpty()) this else copy(
        section = backStack.last(),
        backStack = backStack.dropLast(1)
    )

    companion object {
        val Saver = listSaver<DemoSectionNavigation, String>(
            save = { state -> listOf(state.section.name) + state.backStack.map { it.name } },
            restore = {
                DemoSectionNavigation(
                    section = DemoSection.valueOf(it.first()),
                    backStack = it.drop(1).map(DemoSection::valueOf)
                )
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
