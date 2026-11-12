package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileContactPhoneTest {
    @Test
    fun formatsTenDigitsUsingRequestedGrouping() {
        assertEquals("+(123) 456 78 90", formatProfilePhone("+1 (234) 567-890"))
    }

    @Test
    fun formatsLongInternationalNumbersWithoutDroppingDigits() {
        val phone = "+380989420913"
        val formatted = formatProfilePhone(phone)

        assertEquals("+(380) 989 42 09 13", formatted)
        assertEquals(phone.filter { it in '0'..'9' }, formatted.filter { it in '0'..'9' })
    }

    @Test
    fun keepsShortOrNonNumericValuesExplicit() {
        assertEquals("+(123)", formatProfilePhone("123"))
        assertEquals("ext", formatProfilePhone("ext"))
    }
}
