package com.mobilespace.xrnavi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.recovery_back_accessibility
import xrnavi.shared.generated.resources.recovery_back
import xrnavi.shared.generated.resources.recovery_description
import xrnavi.shared.generated.resources.recovery_email
import xrnavi.shared.generated.resources.recovery_send
import xrnavi.shared.generated.resources.recovery_title
import xrnavi.shared.generated.resources.recovery_unavailable
import xrnavi.shared.generated.resources.recovery_unavailable_title
import xrnavi.shared.generated.resources.search_theme
import com.mobilespace.xrnavi.presentation.auth.RecoveryBloc
import com.mobilespace.xrnavi.presentation.auth.RecoveryEvent
import com.mobilespace.xrnavi.presentation.auth.localizedMessage

@Composable
internal fun RecoveryScreen(
    bloc: RecoveryBloc,
    onBack: () -> Unit,
    onToggleTheme: () -> Unit
) {
    val state by bloc.state.collectAsState()
    AuthBackHandler(onBack)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Constants.DP_24),
            verticalArrangement = Arrangement.spacedBy(Constants.DP_20)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val backDescription = stringResource(Res.string.recovery_back_accessibility)
                TextButton(
                    onClick = onBack,
                    modifier = Modifier.semantics { contentDescription = backDescription }
                ) {
                    Text(stringResource(Res.string.recovery_back))
                }
                TextButton(onClick = onToggleTheme) {
                    Text(stringResource(Res.string.search_theme))
                }
            }
            ProductWordmark()
            Text(
                stringResource(Res.string.recovery_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { heading() }
            )
            Text(
                stringResource(Res.string.recovery_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AuthFormInput(
                label = stringResource(Res.string.recovery_email),
                value = state.email,
                onValueChange = { bloc.dispatch(RecoveryEvent.EmailChanged(it)) },
                error = state.emailError?.localizedMessage(),
                onBlur = { bloc.dispatch(RecoveryEvent.EmailTouched) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { bloc.dispatch(RecoveryEvent.RequestReset) }
                )
            )
            Button(
                onClick = { bloc.dispatch(RecoveryEvent.RequestReset) },
                enabled = state.canRequest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.recovery_send))
            }
            AuthInfo(
                "${stringResource(Res.string.recovery_unavailable_title)}. " +
                    stringResource(Res.string.recovery_unavailable)
            )
            if (state.requestUnavailable) {
                Text(
                    stringResource(Res.string.recovery_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(Res.string.recovery_back))
            }
        }
    }
}
