package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.DemoAddress
import com.mobilespace.xrnavi.domain.DemoStop
import com.mobilespace.xrnavi.domain.NavigationDemoRepository
import com.mobilespace.xrnavi.domain.StopCategory

/** Fixtures only: no geocoding, live location, opening hours or suitability verification. */
class DemoNavigationRepository : NavigationDemoRepository {
    override val addresses = listOf(
        DemoAddress("lviv-market-1", "Площа Ринок, 1", "Львів", true),
        DemoAddress("lviv-market-10", "Площа Ринок, 10", "Львів", false),
        DemoAddress("drohobych-market-1", "Площа Ринок, 1", "Дрогобич", false),
        DemoAddress("kyiv-khreshchatyk-22", "Вулиця Хрещатик, 22", "Київ", true),
    )
    override val stops = listOf(DemoStop("m06-parking", StopCategory.Rest, 2.4, true))
}
