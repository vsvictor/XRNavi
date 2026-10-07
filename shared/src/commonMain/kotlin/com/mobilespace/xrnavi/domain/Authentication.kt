package com.mobilespace.xrnavi.domain

enum class AccountType { Personal, Corporate }
enum class ValidationError { Required, InvalidEmail, ShortPassword, FullNameRequired, ConsentRequired }
data class Credentials(val email: String, val password: String)
data class Registration(val fullName: String, val credentials: Credentials, val accountType: AccountType, val consent: Boolean)
data class AuthenticationErrors(
    val email: ValidationError? = null,
    val password: ValidationError? = null,
    val fullName: ValidationError? = null,
    val consent: ValidationError? = null,
) {
    val valid get() = email == null && password == null && fullName == null && consent == null
}

class ValidateCredentials {
    operator fun invoke(credentials: Credentials) = AuthenticationErrors(
        email = when {
            credentials.email.trim().isEmpty() -> ValidationError.Required
            !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(credentials.email.trim()) -> ValidationError.InvalidEmail
            else -> null
        },
        password = when {
            credentials.password.isBlank() -> ValidationError.Required
            credentials.password.length < 8 -> ValidationError.ShortPassword
            else -> null
        },
    )
}

class ValidateRegistration {
    operator fun invoke(registration: Registration): AuthenticationErrors =
        ValidateCredentials()(registration.credentials).copy(
            fullName = when {
                registration.fullName.isBlank() -> ValidationError.Required
                registration.fullName.trim().split(Regex("\\s+")).size < 2 -> ValidationError.FullNameRequired
                else -> null
            },
            consent = if (registration.consent) null else ValidationError.ConsentRequired,
        )
}

sealed interface AuthenticationResult {
    data object NotConfigured : AuthenticationResult
    data object Authenticated : AuthenticationResult
    data class Invalid(val errors: AuthenticationErrors) : AuthenticationResult
}

interface AuthenticationRepository {
    suspend fun signIn(credentials: Credentials): AuthenticationResult
    suspend fun register(registration: Registration): AuthenticationResult
}

class SignIn(private val repository: AuthenticationRepository) {
    suspend operator fun invoke(credentials: Credentials): AuthenticationResult {
        val errors = ValidateCredentials()(credentials)
        return if (!errors.valid) AuthenticationResult.Invalid(errors)
        else repository.signIn(credentials.copy(email = credentials.email.trim()))
    }
}

class Register(private val repository: AuthenticationRepository) {
    suspend operator fun invoke(registration: Registration): AuthenticationResult {
        val errors = ValidateRegistration()(registration)
        return if (!errors.valid) AuthenticationResult.Invalid(errors)
        else repository.register(registration.copy(
            fullName = registration.fullName.trim(),
            credentials = registration.credentials.copy(email = registration.credentials.email.trim()),
        ))
    }
}
