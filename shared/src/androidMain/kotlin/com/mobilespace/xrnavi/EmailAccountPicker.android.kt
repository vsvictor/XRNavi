package com.mobilespace.xrnavi

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

@Composable
internal actual fun rememberEmailAccountPicker(
    onSelected: (String) -> Unit,
    onFailure: (String) -> Unit,
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (name.isNullOrBlank()) {
                onFailure("Системний вибір акаунта не повернув електронної адреси.")
            } else {
                onSelected(name)
            }
        } else if (result.resultCode != Activity.RESULT_CANCELED) {
            onFailure("Не вдалося отримати акаунт із системного списку.")
        }
    }
    return {
        try {
            launcher.launch(
                AccountManager.newChooseAccountIntent(
                    null, null, null, null, null, null, null
                )
            )
        } catch (_: ActivityNotFoundException) {
            onFailure("На цьому пристрої немає системного вибору акаунтів. Введіть пошту вручну.")
        } catch (_: SecurityException) {
            onFailure("Система заборонила вибір акаунта. Введіть пошту вручну.")
        }
    }
}
