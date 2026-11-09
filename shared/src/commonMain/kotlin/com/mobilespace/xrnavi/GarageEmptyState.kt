package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
internal fun GarageEmptyState(filter: GarageFilter) {
    AccountCard(tonal = true) {
        CardHeading("Немає сумісних демопрофілів")
        AccountBody(
            "Для категорії «${filter.label}» локальних демопрофілів немає. " +
                "Профіль іншої категорії не відображається як сумісний транспорт."
        )
    }
}
