package com.mobilespace.xrnavi

import androidx.lifecycle.ViewModelStore
import kotlin.test.*

class RecoveryViewModelTest {
    @Test
    fun initialRequestHasNoPrematureErrors() {
        val model = RecoveryViewModel()
        assertEquals(RecoveryStage.Request, model.stage)
        assertEquals("", model.email)
        assertNull(model.emailError)
        assertFalse(model.canRequest)
        assertEquals("", model.notice)
    }

    @Test
    fun emailErrorAppearsAfterBlurOrEditing() {
        val model = RecoveryViewModel()
        model.touchEmail()
        assertNotNull(model.emailError)
        model.updateEmail("invalid")
        assertNotNull(model.emailError)
        model.updateEmail("  demo+reset@example.com  ")
        assertNull(model.emailError)
        assertTrue(model.canRequest)
    }

    @Test
    fun invalidSubmitStaysOnRequestWithoutClaimingDelivery() {
        val model = RecoveryViewModel()
        model.requestReset()
        assertNotNull(model.emailError)
        assertEquals("", model.notice)
        assertEquals(RecoveryStage.Request, model.stage)
    }

    @Test
    fun validSubmitNeverTransitionsToCheckEmailEvenInDebug() {
        listOf(false, true).forEach { debug ->
            val model = RecoveryViewModel(debug)
            model.updateEmail("demo@example.com")
            repeat(2) { model.requestReset() }
            assertEquals(recoveryUnavailableMessage, model.notice)
            assertEquals(RecoveryStage.Request, model.stage)
            assertEquals("demo@example.com", model.email)
            assertEquals(AppScreen.SignIn, initialScreenForSession(false))
        }
    }

    @Test
    fun productionCannotOpenDemoStagesOrAcceptNewSecrets() {
        val model = RecoveryViewModel()
        RecoveryStage.entries.forEach { assertFalse(model.openPreview(it)) }
        model.updatePassword("abcdefgh")
        model.updateConfirmation("abcdefgh")
        model.togglePasswordVisibility()
        model.toggleConfirmationVisibility()
        model.submitNewPassword()
        model.resend()
        assertEquals(RecoveryStage.Request, model.stage)
        assertEquals("", model.password)
        assertEquals("", model.confirmation)
        assertEquals("", model.notice)
        assertFalse(model.canReset)
        assertFalse(model.passwordVisible)
        assertFalse(model.confirmationVisible)
    }

    @Test
    fun debugStagesAreExplicitSelectionNotAutomaticFlow() {
        val model = RecoveryViewModel(true)
        RecoveryStage.entries.forEach {
            assertTrue(model.openPreview(it))
            assertEquals(it, model.stage)
        }
    }

    @Test
    fun newPasswordErrorsAreDeferred() {
        val model = newPasswordModel()
        assertNull(model.passwordError)
        assertNull(model.confirmationError)
        assertFalse(model.canReset)
        model.submitNewPassword()
        assertNotNull(model.passwordError)
        assertNotNull(model.confirmationError)
        assertEquals("", model.notice)
    }

    @Test
    fun passwordPolicyIsOnlyEightCharactersWithoutNormalization() {
        val model = newPasswordModel()
        model.updatePassword("abcdefg")
        model.updateConfirmation("abcdefg")
        assertFalse(model.canReset)
        listOf("abcdefgh", "12345678", " abcdef ", "        ").forEach {
            model.updatePassword(it)
            model.updateConfirmation(it)
            assertEquals(it, model.password)
            assertEquals(it, model.confirmation)
            assertTrue(model.canReset)
        }
    }

    @Test
    fun confirmationMustMatchExactlyAndTracksChanges() {
        val model = newPasswordModel()
        model.updatePassword("abcdefgh")
        model.updateConfirmation("ABCDEFGH")
        assertEquals("Паролі не збігаються", model.confirmationError)
        model.updateConfirmation("abcdefgh")
        assertTrue(model.canReset)
        model.updatePassword("abcdefgh ")
        assertFalse(model.canReset)
        assertEquals("Паролі не збігаються", model.confirmationError)
    }

    @Test
    fun visibilityControlsAreIndependent() {
        val model = newPasswordModel()
        model.togglePasswordVisibility()
        assertTrue(model.passwordVisible)
        assertFalse(model.confirmationVisible)
        model.toggleConfirmationVisibility()
        model.togglePasswordVisibility()
        assertFalse(model.passwordVisible)
        assertTrue(model.confirmationVisible)
    }

    @Test
    fun validResetClearsSecretsWithoutChangingPasswordOrStage() {
        val model = validNewPasswordModel()
        model.submitNewPassword()
        assertEquals(passwordResetUnavailableMessage, model.notice)
        assertEquals(RecoveryStage.NewPassword, model.stage)
        assertSecretsCleared(model)
        assertNull(model.passwordError)
        assertNull(model.confirmationError)
        assertEquals(AppScreen.SignIn, initialScreenForSession(false))
    }

    @Test
    fun leavingNewPasswordForAnyPreviewClearsSecretsAndVisibility() {
        RecoveryStage.entries.forEach { next ->
            val model = validNewPasswordModel()
            model.togglePasswordVisibility()
            model.toggleConfirmationVisibility()
            model.openPreview(next)
            assertSecretsCleared(model)
        }
    }

    @Test
    fun returningToRequestPreservesEmailButClearsSecrets() {
        val model = validNewPasswordModel()
        model.updateEmail("demo@example.com")
        model.returnToRequest()
        assertEquals(RecoveryStage.Request, model.stage)
        assertEquals("demo@example.com", model.email)
        assertSecretsCleared(model)
    }

    @Test
    fun cancelAndLifetimeEndClearSecrets() {
        val cancelled = validNewPasswordModel()
        cancelled.updateEmail("demo@example.com")
        cancelled.cancel()
        assertSecretsCleared(cancelled)
        assertEquals("", cancelled.email)
        assertNull(cancelled.emailError)
        assertEquals(RecoveryStage.Request, cancelled.stage)
        val ended = validNewPasswordModel()
        val store = ViewModelStore()
        store.put("recovery", ended)
        store.clear()
        assertSecretsCleared(ended)
        assertSecretsCleared(newPasswordModel())
    }

    @Test
    fun resendAndOpenMailDoNotPretendToSendOrChangeStage() {
        val model = RecoveryViewModel(true)
        model.openPreview(RecoveryStage.CheckEmail)
        model.resend()
        assertEquals(recoveryUnavailableMessage, model.notice)
        assertEquals(RecoveryStage.CheckEmail, model.stage)
        model.openMailPreview()
        assertTrue(model.notice.contains("Жодного листа не надсилали"))
        assertEquals(RecoveryStage.CheckEmail, model.stage)
    }

    private fun newPasswordModel() = RecoveryViewModel(true).apply {
        openPreview(RecoveryStage.NewPassword)
    }

    private fun validNewPasswordModel() = newPasswordModel().apply {
        updatePassword("abcdefgh")
        updateConfirmation("abcdefgh")
    }

    private fun assertSecretsCleared(model: RecoveryViewModel) {
        assertEquals("", model.password)
        assertEquals("", model.confirmation)
        assertFalse(model.passwordVisible)
        assertFalse(model.confirmationVisible)
        assertFalse(model.canReset)
    }
}
