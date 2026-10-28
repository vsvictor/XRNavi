package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModelStore
import kotlin.test.*

class RegistrationViewModelTest {
    @Test
    fun emptyFormIsDisabledWithoutPrematureErrors() {
        val model = RegistrationViewModel()
        assertFalse(model.canSubmit)
        RegistrationField.entries.forEach {
            assertNull(model.visibleError(it))
            assertNotNull(model.validationError(it))
            model.touch(it)
            assertNotNull(model.visibleError(it))
        }
    }

    @Test
    fun nameAndEmailHaveBasicLocalValidation() {
        val model = validForm()
        model.update(RegistrationField.Name, "   ")
        assertFalse(model.canSubmit)
        model.update(RegistrationField.Name, "  Андрій Коваль  ")
        listOf("", "a", "a@", "@example.com", "a@b", "a b@example.com", "a@b..com").forEach {
            model.update(RegistrationField.Email, it)
            assertNotNull(model.visibleError(RegistrationField.Email))
            assertFalse(model.canSubmit)
        }
        model.update(RegistrationField.Email, "  andrii+demo@example.com  ")
        assertTrue(model.canSubmit)
    }

    @Test
    fun passwordRequiresOnlyEightCharactersAndIsNeverTrimmed() {
        val model = validForm()
        model.update(RegistrationField.Password, "1234567")
        model.update(RegistrationField.Confirmation, "1234567")
        assertFalse(model.canSubmit)
        listOf("abcdefgh", "12345678", " abcdef ", "        ").forEach {
            model.update(RegistrationField.Password, it)
            model.update(RegistrationField.Confirmation, it)
            assertEquals(it, model.password)
            assertEquals(it, model.confirmation)
            assertTrue(model.canSubmit)
        }
    }

    @Test
    fun confirmationMustMatchExactlyAndTracksPasswordEdits() {
        val model = validForm()
        model.update(RegistrationField.Confirmation, "ABCDEFGH")
        assertEquals("Паролі не збігаються", model.visibleError(RegistrationField.Confirmation))
        assertFalse(model.canSubmit)
        model.update(RegistrationField.Confirmation, "abcdefgh")
        assertTrue(model.canSubmit)
        model.update(RegistrationField.Password, "abcdefgh ")
        assertFalse(model.canSubmit)
        assertEquals("Паролі не збігаються", model.visibleError(RegistrationField.Confirmation))
    }

    @Test
    fun consentIsRequiredInBothMembershipModes() {
        val model = validForm()
        RegistrationMembership.entries.forEach {
            model.selectMembership(it)
            model.updateConsent(false)
            assertFalse(model.canSubmit)
            model.updateConsent(true)
            assertTrue(model.canSubmit)
        }
    }

    @Test
    fun passwordVisibilityIsIndependent() {
        val model = RegistrationViewModel()
        model.togglePasswordVisibility()
        assertTrue(model.passwordVisible)
        assertFalse(model.confirmationVisible)
        model.toggleConfirmationVisibility()
        model.togglePasswordVisibility()
        assertFalse(model.passwordVisible)
        assertTrue(model.confirmationVisible)
    }

    @Test
    fun invalidSubmitDoesNotClaimSuccess() {
        val model = RegistrationViewModel()
        model.submit()
        assertEquals("", model.notice)
        assertFalse(model.canSubmit)
    }

    @Test
    fun localSubmitOnlyReportsUnavailableAndClearsSecrets() {
        val model = validForm()
        model.togglePasswordVisibility()
        model.toggleConfirmationVisibility()
        model.submit()
        assertEquals(registrationUnavailableMessage, model.notice)
        assertEquals("", model.password)
        assertEquals("", model.confirmation)
        assertFalse(model.passwordVisible)
        assertFalse(model.confirmationVisible)
        assertFalse(model.canSubmit)
        assertEquals("Андрій", model.name)
        assertEquals("andrii@example.com", model.email)
        assertNull(model.visibleError(RegistrationField.Password))
        assertNull(model.visibleError(RegistrationField.Confirmation))
        assertEquals(AppScreen.SignIn, initialScreenForSession(false))
    }

    @Test
    fun cancellingClearsTheScenarioAndNewInstanceCannotRestoreSecrets() {
        val model = validForm()
        model.selectMembership(RegistrationMembership.Corporate)
        model.cancel()
        assertEquals("", model.password)
        assertEquals("", model.confirmation)
        assertEquals("", model.name)
        assertEquals("", model.email)
        assertFalse(model.consent)
        assertEquals(RegistrationMembership.Personal, model.membership)
        val fresh = RegistrationViewModel()
        assertEquals("", fresh.password)
        assertEquals("", fresh.confirmation)
    }

    @Test
    fun endingTheViewModelLifetimeClearsSecrets() {
        val model = validForm()
        val store = ViewModelStore()
        store.put("registration", model)
        store.clear()
        assertEquals("", model.password)
        assertEquals("", model.confirmation)
        assertFalse(model.passwordVisible)
        assertFalse(model.confirmationVisible)
    }

    private fun validForm() = RegistrationViewModel().apply {
        update(RegistrationField.Name, "Андрій")
        update(RegistrationField.Email, "andrii@example.com")
        update(RegistrationField.Password, "abcdefgh")
        update(RegistrationField.Confirmation, "abcdefgh")
        updateConsent(true)
    }
}
