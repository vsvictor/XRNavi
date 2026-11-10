package com.mobilespace.xrnavi

import kotlinx.serialization.json.JsonPrimitive

internal enum class AuthenticatedAccountType(val label: String) {
    Personal("Особистий"),
    Corporate("Корпоративний"),
    Unknown("Тип не вказано");

    companion object {
        fun from(value: String?): AuthenticatedAccountType = when (value) {
            "private" -> Personal
            "corporate" -> Corporate
            else -> Unknown
        }
    }
}

internal val AuthenticatedProfile.accountType: AuthenticatedAccountType
    get() = AuthenticatedAccountType.from(
        (fields["account_type"] as? JsonPrimitive)?.takeIf { it.isString }?.content
    )
