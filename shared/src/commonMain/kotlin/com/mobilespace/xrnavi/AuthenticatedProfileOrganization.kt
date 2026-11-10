package com.mobilespace.xrnavi

import androidx.compose.runtime.Composable

@Composable
internal fun AuthenticatedProfileOrganization(
    profile: AuthenticatedProfile,
    layout: AccountLayout,
) {
    AccountCard(
        tonal = true,
        minHeight = if (layout == AccountLayout.TabletLandscape) 164 else 0,
    ) {
        CardHeading(
            if (profile.accountType == AuthenticatedAccountType.Corporate) {
                "Корпоративний акаунт"
            } else {
                "Організація"
            },
        )
        when (profile.accountType) {
            AuthenticatedAccountType.Corporate -> {
                AccountBody("Назву організації сервер не надав.")
            }
            AuthenticatedAccountType.Personal ->
                AccountBody("Відомості про членство в організації сервером не надані.")
            AuthenticatedAccountType.Unknown ->
                AccountBody("Тип акаунта й відомості про організацію не надані сервером.")
        }
        AccountBody("Корпоративні політики та їх редагування не підключені.")
    }
}
