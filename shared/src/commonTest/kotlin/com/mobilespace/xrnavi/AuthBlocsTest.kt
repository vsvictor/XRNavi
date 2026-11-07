package com.mobilespace.xrnavi

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthBlocsTest {
    @Test
    fun registrationSubmitWithInvalidFormTouchesAllFields() {
        val bloc = RegistrationBloc()
        bloc.dispatch(RegistrationEvent.Submitted)
        assertEquals(RegistrationField.entries.toSet(), bloc.state.value.touched)
        assertFalse(bloc.state.value.canSubmit)
    }

    @Test
    fun registrationValidSubmitClearsSecrets() {
        val bloc = RegistrationBloc()
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Name, "Андрій"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Email, "a@example.com"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Password, "abcdefgh"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, "abcdefgh"))
        bloc.dispatch(RegistrationEvent.ConsentChanged(true))
        assertTrue(bloc.state.value.canSubmit)
        bloc.dispatch(RegistrationEvent.Submitted)
        assertEquals("", bloc.state.value.password)
        assertEquals(registrationUnavailableMessage, bloc.state.value.notice)
    }

    @Test
    fun themeSelectPersistsAndFailedWriteKeepsPrevious() {
        var saved: String? = null
        var ok = true
        val store = object : ThemePreferenceStore {
            override fun read() = saved
            override fun write(theme: ThemePreference): Boolean {
                if (ok) saved = theme.name
                return ok
            }
        }
        val bloc = ThemeSettingsBloc(store)
        bloc.dispatch(ThemeSettingsEvent.Select(ThemePreference.Dark))
        assertEquals(ThemePreference.Dark, bloc.state.value.preference)
        ok = false
        bloc.dispatch(ThemeSettingsEvent.Select(ThemePreference.Light))
        assertEquals(ThemePreference.Dark, bloc.state.value.preference)
        assertTrue(bloc.state.value.notice.isNotEmpty())
    }
}
