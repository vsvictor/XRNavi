package com.mobilespace.xrnavi

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal enum class AdditionalScreen(
    val designId: String,
    val title: String,
    val statePreview: Boolean = false
) {
    Company("S09", "Налаштування компанії"),
    Invitation("S12", "Приєднатися до команди"),
    TripSummary("S13", "Ви на місці"),
    VehicleCatalog("S14", "Додати транспорт"),
    VehicleModel("S15", "Ваш Ford"),
    VehicleReview("S11", "Перевірити параметри"),
    RouteUnverified("S17", "Маршрут не перевірено"),
    OfflineMaps("S21", "Офлайн-мапи"),
    DriverTasks("S25", "Мої завдання"),
    TaskDetails("S26", "Рейс DL-204"),
    TripHistory("S27", "Історія поїздок"),
    Privacy("S28", "Центр приватності"),
    DeleteAccount("S29", "Видалити акаунт?"),
    Diagnostics("S31", "Пристрої та діагностика"),
    RouteExplanation("S38", "Чому обрано цей шлях"),
    LocationEducation("S46", "Навіщо геолокація"),
    SelectedPoint("S35", "Вибрана точка", true),
    LocationAcquiring("S48", "Визначаємо місце", true),
    LocationDenied("S49", "Доступ до геолокації вимкнено", true),
    LocationDisabled("S50", "Службу геолокації вимкнено", true),
    SearchLoading("S51", "Шукаємо адресу", true),
    SearchEmpty("S52", "Нічого не знайдено", true),
    SearchError("S53", "Помилка пошуку", true),
    RouteBuilding("S54", "Будуємо маршрут", true),
    RouteFailed("S55", "Маршрут не побудовано", true)
}

internal enum class HistoryFilter(val label: String) {
    All("Усі"), Personal("Особисті"), Work("Робочі")
}

internal data class AdditionalScreensState(
    val screen: AdditionalScreen? = null,
    val backStack: List<AdditionalScreen> = emptyList(),
    val sectionTarget: DemoSection? = null,
    val notice: String? = null,
    val query: String = "",
    val vehicleClass: GarageFilter = GarageFilter.Passenger,
    val model: String = "Mustang",
    val year: String = "2024",
    val trim: String = "GT · 5.0 V8 · автоматична",
    val vehicleHeight: String = "",
    val vehicleLength: String = "",
    val vehicleMass: String = "",
    val historyFilter: HistoryFilter = HistoryFilter.All,
    val completedTasks: Boolean = false,
    val invitationCode: String = "",
    val invitationConsent: Boolean = false,
    val deletionConsent: Boolean = false,
    val dispatcherNotifications: Boolean = true,
    val voicePrompts: Boolean = true
)

internal sealed interface AdditionalScreensEvent {
    data class Open(val screen: AdditionalScreen) : AdditionalScreensEvent
    data object Back : AdditionalScreensEvent
    data class Section(val section: DemoSection) : AdditionalScreensEvent
    data object SectionHandled : AdditionalScreensEvent
    data object DismissNotice : AdditionalScreensEvent
    data class Unavailable(val action: String) : AdditionalScreensEvent
    data class Query(val value: String) : AdditionalScreensEvent
    data class VehicleClass(val value: GarageFilter) : AdditionalScreensEvent
    data class Model(val value: String) : AdditionalScreensEvent
    data class Year(val value: String) : AdditionalScreensEvent
    data class Trim(val value: String) : AdditionalScreensEvent
    data class VehicleHeight(val value: String) : AdditionalScreensEvent
    data class VehicleLength(val value: String) : AdditionalScreensEvent
    data class VehicleMass(val value: String) : AdditionalScreensEvent
    data class History(val value: HistoryFilter) : AdditionalScreensEvent
    data class CompletedTasks(val value: Boolean) : AdditionalScreensEvent
    data class InvitationCode(val value: String) : AdditionalScreensEvent
    data class InvitationConsent(val value: Boolean) : AdditionalScreensEvent
    data class DeletionConsent(val value: Boolean) : AdditionalScreensEvent
    data class DispatcherNotifications(val value: Boolean) : AdditionalScreensEvent
    data class VoicePrompts(val value: Boolean) : AdditionalScreensEvent
    data object AcceptInvitation : AdditionalScreensEvent
    data object DeleteAccount : AdditionalScreensEvent
}

internal class AdditionalScreensBloc {
    private val mutableState = MutableStateFlow(AdditionalScreensState())
    val state = mutableState.asStateFlow()

    fun dispatch(event: AdditionalScreensEvent) {
        mutableState.value = transition(mutableState.value, event)
    }

    companion object {
        fun transition(
            state: AdditionalScreensState,
            event: AdditionalScreensEvent
        ): AdditionalScreensState =
            when (event) {
                is AdditionalScreensEvent.Open -> if (event.screen == state.screen) state else state.copy(
                    screen = event.screen,
                    backStack = if (event.screen in state.backStack)
                        state.backStack.take(state.backStack.indexOf(event.screen))
                    else state.backStack + listOfNotNull(state.screen),
                    notice = null,
                    deletionConsent = false,
                    invitationConsent = false
                )

                AdditionalScreensEvent.Back -> state.copy(
                    screen = state.backStack.lastOrNull(),
                    backStack = state.backStack.dropLast(1),
                    notice = null,
                    deletionConsent = false,
                    invitationConsent = false
                )

                is AdditionalScreensEvent.Section -> state.copy(
                    screen = null,
                    backStack = emptyList(),
                    sectionTarget = event.section,
                    notice = null,
                    invitationConsent = false,
                    deletionConsent = false
                )

                AdditionalScreensEvent.SectionHandled -> state.copy(sectionTarget = null)
                AdditionalScreensEvent.DismissNotice -> state.copy(notice = null)
                is AdditionalScreensEvent.Unavailable -> state.copy(
                    notice = "${event.action}: сервіс не підключено. Дію не виконано; серверні дані не змінено."
                )

                is AdditionalScreensEvent.Query -> state.copy(query = event.value)
                is AdditionalScreensEvent.VehicleClass -> state.copy(vehicleClass = event.value)
                is AdditionalScreensEvent.Model -> state.copy(model = event.value)
                is AdditionalScreensEvent.Year -> state.copy(year = event.value)
                is AdditionalScreensEvent.Trim -> state.copy(trim = event.value)
                is AdditionalScreensEvent.VehicleHeight -> state.copy(vehicleHeight = event.value)
                is AdditionalScreensEvent.VehicleLength -> state.copy(vehicleLength = event.value)
                is AdditionalScreensEvent.VehicleMass -> state.copy(vehicleMass = event.value)
                is AdditionalScreensEvent.History -> state.copy(historyFilter = event.value)
                is AdditionalScreensEvent.CompletedTasks -> state.copy(completedTasks = event.value)
                is AdditionalScreensEvent.InvitationCode -> state.copy(
                    invitationCode = event.value, invitationConsent = false
                )

                is AdditionalScreensEvent.InvitationConsent -> state.copy(invitationConsent = event.value)
                is AdditionalScreensEvent.DeletionConsent -> state.copy(deletionConsent = event.value)
                is AdditionalScreensEvent.DispatcherNotifications -> state.copy(
                    dispatcherNotifications = event.value,
                    notice = "Змінено лише локальний приклад. Сервіс сповіщень диспетчера не підключено."
                )

                is AdditionalScreensEvent.VoicePrompts -> state.copy(
                    voicePrompts = event.value,
                    notice = "Змінено лише локальний приклад. Голосовий сервіс не підключено."
                )

                AdditionalScreensEvent.AcceptInvitation -> state.copy(
                    notice = if (!state.invitationConsent || state.invitationCode.isBlank())
                        "Введіть код запрошення та підтвердьте ознайомлення з правилами."
                    else "Перевірка й прийняття запрошення не підключені. Членство в організації не змінено."
                )

                AdditionalScreensEvent.DeleteAccount -> state.copy(
                    notice = if (!state.deletionConsent) "Підтвердьте ознайомлення з наслідками видалення."
                    else "Видалення акаунта не підключено. Акаунт, сесію та локальні дані не видалено."
                )
            }
    }
}
