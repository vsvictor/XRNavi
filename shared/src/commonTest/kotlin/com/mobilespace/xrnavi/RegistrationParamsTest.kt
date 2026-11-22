package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RegistrationParamsTest {
    private val filled = RegistrationState(
        name = " Ада ",
        lastName = "Лавлейс",
        email = " ada@example.com ",
        password = "abcdefgh",
        confirmation = "abcdefgh",
        consent = true
    )

    @Test
    fun middleNameIsOptionalAndOmittedWhenEmpty() {
        assertTrue(filled.canSubmit)
        val profile = filled.registerParams()["profile"] as Map<*, *>
        assertEquals(
            mapOf("first_name" to "Ада", "last_name" to "Лавлейс", "account_type" to "private"),
            profile
        )
    }

    @Test
    fun corporateAccountWithMiddleName() {
        val state = filled.copy(
            middleName = "Августа",
            membership = RegistrationMembership.Corporate
        )
        val params = state.registerParams()
        assertEquals("ada@example.com", params["email"])
        val profile = params["profile"] as Map<*, *>
        assertEquals("Августа", profile["middle_name"])
        assertEquals("corporate", profile["account_type"])
    }

    @Test
    fun firstAndLastNameAreRequired() {
        assertFalse(filled.copy(name = " ").canSubmit)
        assertFalse(filled.copy(lastName = "").canSubmit)
    }
}
