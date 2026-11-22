package com.mobilespace.xrnavi

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val googleClientIdMeta = "com.mobilespace.xrnavi.GOOGLE_WEB_CLIENT_ID"

@Composable
internal actual fun rememberGoogleIdTokenProvider(
    onToken: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnToken = rememberUpdatedState(onToken)
    val currentOnFailure = rememberUpdatedState(onFailure)
    return {
        val clientId = googleWebClientId(context)
        if (clientId.isBlank()) {
            currentOnFailure.value("Вхід через Google не налаштовано (googleWebClientId).")
        } else {
            scope.launch {
                try {
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(
                            GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(clientId)
                                .build()
                        )
                        .build()
                    val credential = CredentialManager.create(context)
                        .getCredential(context, request).credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        currentOnToken.value(GoogleIdTokenCredential.createFrom(credential.data).idToken)
                    } else {
                        currentOnFailure.value("Google повернув непідтримуваний тип облікових даних.")
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: GetCredentialCancellationException) {
                    // User dismissed the account sheet.
                    currentOnFailure.value("")
                } catch (_: GetCredentialException) {
                    currentOnFailure.value("Не вдалося отримати обліковий запис Google.")
                } catch (_: GoogleIdTokenParsingException) {
                    currentOnFailure.value("Google повернув некоректний токен.")
                }
            }
        }
    }
}

private fun googleWebClientId(context: Context): String = try {
    context.packageManager
        .getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        .metaData?.getString(googleClientIdMeta).orEmpty()
} catch (_: PackageManager.NameNotFoundException) {
    ""
}
