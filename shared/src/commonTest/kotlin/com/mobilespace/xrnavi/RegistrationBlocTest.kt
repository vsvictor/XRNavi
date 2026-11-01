package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModelStore
import com.mobilespace.xrnavi.domain.auth.AuthValidationError
import com.mobilespace.xrnavi.presentation.auth.RegistrationBloc
import com.mobilespace.xrnavi.presentation.auth.RegistrationEvent
import com.mobilespace.xrnavi.presentation.auth.RegistrationField
import com.mobilespace.xrnavi.presentation.auth.RegistrationMembership
import com.mobilespace.xrnavi.presentation.auth.RegistrationNotice
import kotlin.test.*

class RegistrationBlocTest {
    @Test
    fun emptyFormIsDisabledWithoutPrematureErrors() {
        val bloc = RegistrationBloc()
        val state = bloc.state.value
        assertFalse(state.canSubmit)
        RegistrationField.entries.forEach { field ->
            assertNull(state.visibleError(field))
            assertNotNull(state.validationError(field))
            bloc.dispatch(RegistrationEvent.FieldTouched(field))
            assertNotNull(bloc.state.value.visibleError(field))
        }
    }

    @Test
    fun nameAndEmailHaveBasicLocalValidation() {
        val bloc = validBloc()
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Name, "   "))
        assertFalse(bloc.state.value.canSubmit)
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Name, "  Андрій Коваль  "))
        listOf("", "a", "a@", "@example.com", "a@b", "a b@example.com", "a@b..com").forEach {
            bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Email, it))
            assertNotNull(bloc.state.value.visibleError(RegistrationField.Email))
            assertFalse(bloc.state.value.canSubmit)
        }
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Email, "  andrii+test@example.invalid  "))
        assertTrue(bloc.state.value.canSubmit)
    }

    @Test
    fun passwordRequiresOnlyEightCharactersAndIsNeverTrimmed() {
        val bloc = validBloc()
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Password, "1234567"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, "1234567"))
        assertFalse(bloc.state.value.canSubmit)
        listOf("abcdefgh", "12345678", " abcdef ", "        ").forEach { password ->
            bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Password, password))
            bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, password))
            assertEquals(password, bloc.state.value.password)
            assertEquals(password, bloc.state.value.confirmation)
            assertTrue(bloc.state.value.canSubmit)
        }
    }

    @Test
    fun confirmationMustMatchExactlyAndTracksPasswordEdits() {
        val bloc = validBloc()
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, "ABCDEFGH"))
        assertEquals(
            AuthValidationError.PasswordMismatch,
            bloc.state.value.visibleError(RegistrationField.Confirmation)
        )
        assertFalse(bloc.state.value.canSubmit)
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, "abcdefgh"))
        assertTrue(bloc.state.value.canSubmit)
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Password, "abcdefgh "))
        assertFalse(bloc.state.value.canSubmit)
        assertEquals(
            AuthValidationError.PasswordMismatch,
            bloc.state.value.visibleError(RegistrationField.Confirmation)
        )
    }

    @Test
    fun consentIsRequiredInBothMembershipModes() {
        val bloc = validBloc()
        RegistrationMembership.entries.forEach { membership ->
            bloc.dispatch(RegistrationEvent.MembershipSelected(membership))
            bloc.dispatch(RegistrationEvent.ConsentChanged(false))
            assertFalse(bloc.state.value.canSubmit)
            bloc.dispatch(RegistrationEvent.ConsentChanged(true))
            assertTrue(bloc.state.value.canSubmit)
        }
    }

    @Test
    fun passwordVisibilityIsIndependent() {
        val bloc = RegistrationBloc()
        bloc.dispatch(RegistrationEvent.TogglePasswordVisibility)
        assertTrue(bloc.state.value.passwordVisible)
        assertFalse(bloc.state.value.confirmationVisible)
        bloc.dispatch(RegistrationEvent.ToggleConfirmationVisibility)
        bloc.dispatch(RegistrationEvent.TogglePasswordVisibility)
        assertFalse(bloc.state.value.passwordVisible)
        assertTrue(bloc.state.value.confirmationVisible)
    }

    @Test
    fun invalidSubmitDoesNotClaimSuccess() {
        val bloc = RegistrationBloc()
        bloc.dispatch(RegistrationEvent.Submit)
        assertNull(bloc.state.value.notice)
        assertFalse(bloc.state.value.canSubmit)
        assertTrue(bloc.state.value.touched.containsAll(RegistrationField.entries))
    }

    @Test
    fun localSubmitOnlyReportsUnavailableAndClearsSecrets() {
        val bloc = validBloc()
        bloc.dispatch(RegistrationEvent.TogglePasswordVisibility)
        bloc.dispatch(RegistrationEvent.ToggleConfirmationVisibility)
        bloc.dispatch(RegistrationEvent.Submit)
        val state = bloc.state.value
        assertEquals(RegistrationNotice.SubmissionUnavailable, state.notice)
        assertEquals("", state.password)
        assertEquals("", state.confirmation)
        assertFalse(state.passwordVisible)
        assertFalse(state.confirmationVisible)
        assertFalse(state.canSubmit)
        assertEquals("Андрій", state.name)
        assertEquals("andrii@example.com", state.email)
        assertNull(state.visibleError(RegistrationField.Password))
        assertNull(state.visibleError(RegistrationField.Confirmation))
    }

    @Test
    fun cancellingClearsTheScenarioAndNewInstanceCannotRestoreSecrets() {
        val bloc = validBloc()
        bloc.dispatch(RegistrationEvent.MembershipSelected(RegistrationMembership.Corporate))
        bloc.dispatch(RegistrationEvent.Cancel)
        assertEquals("", bloc.state.value.password)
        assertEquals("", bloc.state.value.confirmation)
        assertEquals("", bloc.state.value.name)
        assertEquals("", bloc.state.value.email)
        assertFalse(bloc.state.value.consent)
        assertEquals(RegistrationMembership.Personal, bloc.state.value.membership)
        val fresh = RegistrationBloc()
        assertEquals("", fresh.state.value.password)
        assertEquals("", fresh.state.value.confirmation)
    }

    @Test
    fun endingTheBlocLifetimeClearsSecrets() {
        val bloc = validBloc()
        val store = ViewModelStore()
        store.put("registration", bloc)
        store.clear()
        assertEquals("", bloc.state.value.password)
        assertEquals("", bloc.state.value.confirmation)
        assertFalse(bloc.state.value.passwordVisible)
        assertFalse(bloc.state.value.confirmationVisible)
    }

    private fun validBloc() = RegistrationBloc().apply {
        dispatch(RegistrationEvent.FieldChanged(RegistrationField.Name, "Андрій"))
        dispatch(RegistrationEvent.FieldChanged(RegistrationField.Email, "andrii@example.com"))
        dispatch(RegistrationEvent.FieldChanged(RegistrationField.Password, "abcdefgh"))
        dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, "abcdefgh"))
        dispatch(RegistrationEvent.ConsentChanged(true))
    }
}
