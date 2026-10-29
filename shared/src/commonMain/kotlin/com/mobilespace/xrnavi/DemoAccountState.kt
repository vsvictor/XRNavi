package com.mobilespace.xrnavi

import androidx.compose.runtime.saveable.listSaver

internal enum class DemoSection(val label: String) {
    Map("Мапа"),
    Garage("Гараж"),
    VehicleDetails("Профіль автомобіля"),
    VehicleConfigurations("Конфігурації"),
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
    demoVehicles.filter { vehicle ->
        filter == GarageFilter.All || vehicle.category == filter.label
    }

internal data class GarageVehicleSelection(
    val vehicleId: String,
    val vehicleClass: String,
    val changed: Boolean
)

internal fun selectGarageVehicle(
    currentVehicleId: String,
    vehicle: DemoVehicleProfile
): GarageVehicleSelection = GarageVehicleSelection(
    vehicleId = vehicle.id,
    vehicleClass = vehicle.category,
    changed = currentVehicleId != vehicle.id
)

internal const val garageAddUnavailableExplanation =
    "Додавання реального транспорту не підключено. У гаражі показані лише локальні демонстраційні профілі; онлайн-каталог, VIN-перевірка й реєстрація права власності відсутні."

internal const val garageEditUnavailableExplanation =
    "Редагування й перевірка характеристик не підключені. Параметри локальних демопрофілів не підтверджені; право власності не перевірялося."

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
        val parentIndex = backStack.lastIndexOf(next)
        if (parentIndex >= 0) {
            return copy(section = next, backStack = backStack.take(parentIndex))
        }
        if (
            section in setOf(DemoSection.VehicleDetails, DemoSection.VehicleConfigurations) &&
            next == back().section
        ) {
            val parent = back()
            return parent
        }
        if (section == DemoSection.VehicleDetails && next == DemoSection.Garage) {
            return back().open(next)
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

internal data class DemoProfileState(
    val vehicleClass: String,
    val vehicleMode: String,
    val vehicleId: String = demoVehicleForClass(vehicleClass)?.id ?: demoVehicle.id
) {
    val title = "Демо-профіль"
    val identity = "Без авторизації"
    val accountDetails = "Ім’я та пошту не отримано. Акаунт і підтвердження пошти відсутні."
    val organization = "Корпоративне членство не підключено. Організацію й роль не підтверджено."
    val statisticsExplanation =
        "Історія поїздок і статистика відсутні; серверні дані не отримувалися."
    val selectedVehicle: DemoVehicleProfile? get() =
        demoVehicleById(vehicleId)?.takeIf { it.category == vehicleClass }
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
