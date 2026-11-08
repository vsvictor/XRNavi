package com.mobilespace.xrnavi

internal fun geoStatusMessage(status: GeoStatus): String = when (status) {
    GeoStatus.NotConfigured -> "служба та дозвіл не перевірені — native adapter не підключено."
    GeoStatus.PermissionDenied -> "дозвіл відхилено; можна ввести початкову адресу вручну."
    GeoStatus.ServiceDisabled -> "службу місцезнаходження вимкнено; можна ввести адресу вручну."
    GeoStatus.Acquiring -> "очікуємо на актуальну позицію; остання відома не вважається поточною."
    GeoStatus.FreshFix -> "позицію отримано від підключеного native provider."
    GeoStatus.StaleFix -> "позиція застаріла; підтвердьте початкову адресу вручну."
}
