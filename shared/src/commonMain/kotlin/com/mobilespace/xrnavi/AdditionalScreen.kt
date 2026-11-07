package com.mobilespace.xrnavi

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
