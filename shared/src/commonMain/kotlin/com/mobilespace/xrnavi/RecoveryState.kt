package com.mobilespace.xrnavi

internal enum class RecoveryStage(val code: String, val title: String) {
    Request("S41", "Відновлення пароля"),
    CheckEmail("S42", "Перевірте пошту"),
    NewPassword("S43", "Новий пароль"),
    Changed("S44", "Пароль змінено"),
    InvalidLink("S45", "Посилання недійсне")
}

internal const val recoveryUnavailableMessage =
    "Відновлення пароля ще не підключено. Дані не надіслано"
internal const val passwordResetUnavailableMessage =
    "Зміну пароля ще не підключено. Пароль не змінено. Дані не надіслано"
internal const val recoveryNetworkMessage =
    "Не вдалося зв’язатися із сервером. Спробуйте ще раз"

internal const val recoveryUnsupportedMessage =
    "Сервер ще не підтримує відновлення пароля. Спробуйте пізніше"
internal const val recoveryWeakPasswordMessage =
    "Сервер відхилив пароль: він занадто простий. Оберіть надійніший"

internal fun Throwable.recoveryFailureMessage(): String {
    val text = message.orEmpty()
    return when {
        text.contains("-32601") -> recoveryUnsupportedMessage
        text.contains("-32602") -> recoveryWeakPasswordMessage
        else -> recoveryNetworkMessage
    }
}

internal const val recoveryPreviewMessage =
    "Внутрішній preview · демо. Листи не надсилалися, токен не перевірявся, пароль не змінювався."
internal const val recoveryMailUnavailableMessage =
    "Відкриття пошти не підключено. Жодного листа не надсилали; це лише preview."

internal data class RecoveryState(
    val previewEnabled: Boolean = false,
    val stage: RecoveryStage = RecoveryStage.Request,
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val passwordVisible: Boolean = false,
    val confirmationVisible: Boolean = false,
    val notice: String = "",
    val emailTouched: Boolean = false,
    val passwordTouched: Boolean = false,
    val confirmationTouched: Boolean = false,
    val serviceAvailable: Boolean = false,
    val resetToken: String = "",
    val tokenInput: String = "",
    val busy: Boolean = false
) {
    val emailError: String?
        get() = emailValidationError(email).takeIf { emailTouched }
    val passwordError: String?
        get() = passwordValidationError(password).takeIf { passwordTouched }
    val confirmationError: String?
        get() = passwordConfirmationError(password, confirmation).takeIf { confirmationTouched }
    val canSubmitToken: Boolean
        get() = serviceAvailable && stage == RecoveryStage.CheckEmail && !busy && tokenInput.isNotBlank()
    val canRequest: Boolean
        get() = stage == RecoveryStage.Request && !busy && emailValidationError(email) == null
    val canReset: Boolean
        get() = (previewEnabled || resetToken.isNotEmpty()) && !busy &&
                stage == RecoveryStage.NewPassword &&
                passwordValidationError(password) == null &&
                passwordConfirmationError(password, confirmation) == null
}

internal sealed interface RecoveryEvent {
    data class EmailChanged(val value: String) : RecoveryEvent
    data object EmailTouched : RecoveryEvent
    data object PasswordTouched : RecoveryEvent
    data object ConfirmationTouched : RecoveryEvent
    data object ResetRequested : RecoveryEvent
    data class PreviewOpened(val stage: RecoveryStage) : RecoveryEvent
    data object ReturnedToRequest : RecoveryEvent
    data object Resent : RecoveryEvent
    data object MailOpened : RecoveryEvent
    data class PasswordChanged(val value: String) : RecoveryEvent
    data class ConfirmationChanged(val value: String) : RecoveryEvent
    data object PasswordVisibilityToggled : RecoveryEvent
    data object ConfirmationVisibilityToggled : RecoveryEvent
    data object NewPasswordSubmitted : RecoveryEvent
    data object Cancelled : RecoveryEvent
    data object Cleared : RecoveryEvent
    data class TokenInputChanged(val value: String) : RecoveryEvent
    data object TokenSubmitted : RecoveryEvent
    data class TokenReceived(val token: String) : RecoveryEvent
    data class Restored(val email: String) : RecoveryEvent
    data object RequestAccepted : RecoveryEvent
    data class RequestFailed(val message: String) : RecoveryEvent
    data object ResetCompleted : RecoveryEvent
    data class ResetFailed(val invalidToken: Boolean, val message: String) : RecoveryEvent
}
