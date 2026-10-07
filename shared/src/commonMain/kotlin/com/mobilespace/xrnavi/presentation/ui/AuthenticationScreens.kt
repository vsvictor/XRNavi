package com.mobilespace.xrnavi.presentation.ui

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.mustang_photograph
import xrnavi.shared.generated.resources.mustang_cockpit


import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.ValidationError

@Composable
internal fun SignInScreen(
    viewModel: SignInViewModel,
    onRegister: () -> Unit,
    onAuthenticated: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val email = uiState.email
    val password = uiState.password
    val emailError = authenticationError(uiState.errors.email, false)
    val passwordError = authenticationError(uiState.errors.password, true)
    val statusMessage = uiState.statusMessage?.let { stringResource(it) }
    val isSubmitting = uiState.isSubmitting
    var passwordVisible by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthenticationEffect.Authenticated -> onAuthenticated()
                AuthenticationEffect.Register -> onRegister()
                else -> Unit
            }
        }
    }
    val scrollState = rememberScrollState()
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeInset = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .verticalScroll(scrollState)
            .padding(top = maxOf(48.dp, topInset), bottom = maxOf(bottomInset, imeInset)),
    ) {
        HeroSection()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            ApexTextField(
                label = stringResource(Res.string.ui_elektronna_poshta),
                value = email,
                onValueChange = {
viewModel.emailChanged(it)
                },
                hint = stringResource(Res.string.ui_name_example_com),
                error = emailError,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                trailingLabel = "✉",
            )
            Spacer(Modifier.height(12.dp))
            ApexTextField(
                label = stringResource(Res.string.ui_parol),
                value = password,
                onValueChange = {
viewModel.passwordChanged(it)
                },
                hint = stringResource(Res.string.ui_vvedit_parol),
                error = passwordError,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                trailingLabel = if (passwordVisible) "◉" else "◉̸",
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                onTrailingClick = { passwordVisible = !passwordVisible },
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                LabelText(
                    text = stringResource(Res.string.ui_zabuly_parol),
                    color = ApexLinkRed,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable {
                        viewModel.recoverPassword()
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                label = stringResource(Res.string.ui_uviyty),
                enabled = !isSubmitting,
            ) {
                viewModel.submit()
            }
            statusMessage?.let {
                Spacer(Modifier.height(8.dp))
                LabelText(
                    text = it,
                    color = if (emailError != null || passwordError != null) ApexLinkRed else ApexMuted,
                    fontSize = 12.sp,
                )
            }
            Spacer(Modifier.height(18.dp))
            AlternativeDivider()
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProviderButton("♧", stringResource(Res.string.ui_apple_brand)) {
                    viewModel.apple()
                }
                ProviderButton("◎", stringResource(Res.string.ui_google_brand)) {
                    viewModel.google()
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabelText(stringResource(Res.string.ui_shche_nemaye_akaunta), ApexMuted, 12.sp)
                Spacer(Modifier.width(6.dp))
                LabelText(
                    stringResource(Res.string.ui_zareyestruvatysya),
                    ApexLinkRed,
                    12.sp,
                    modifier = Modifier.clickable(onClick = { viewModel.navigate(AuthenticationEffect.Register) }),
                    weight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            LabelText(
                stringResource(Res.string.ui_vkhid_cherez_vashu_orhanizatsiyu),
                ApexMuted,
                11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.corporate()
                    },
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
internal fun RegistrationScreen(
    viewModel: RegistrationViewModel,
    onSignIn: () -> Unit,
    onJoinOrganization: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val fullName = uiState.fullName
    val email = uiState.email
    val password = uiState.password
    val personalAccount = uiState.personalAccount
    val consentGiven = uiState.consentGiven
    val nameError = uiState.errors.fullName?.let {
        stringResource(if (it == ValidationError.Required) Res.string.ui_vvedit_im_ya_ta_prizvyshche_2 else Res.string.ui_vkazhit_im_ya_ta_prizvyshche)
    }
    val emailError = authenticationError(uiState.errors.email, false)
    val passwordError = authenticationError(uiState.errors.password, true)
    val consentError = uiState.errors.consent?.let { stringResource(Res.string.ui_potribna_zhoda_z_umovamy_ta_politykoyu_konfidentsiynosti) }
    val statusMessage = uiState.statusMessage?.let { stringResource(it) }
    var passwordVisible by remember { mutableStateOf(false) }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AuthenticationEffect.SignIn -> onSignIn()
                AuthenticationEffect.Invitation -> onJoinOrganization()
                else -> Unit
            }
        }
    }
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeInset = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .verticalScroll(rememberScrollState())
            .padding(top = maxOf(48.dp, topInset), bottom = maxOf(bottomInset, imeInset)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LabelText(
                "‹",
                ApexText,
                30.sp,
                modifier = Modifier.clickable(onClick = { viewModel.navigate(AuthenticationEffect.SignIn) }),
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                LabelText(
                    stringResource(Res.string.ui_stvoryty_akaunt),
                    ApexText,
                    26.sp,
                    weight = FontWeight.Bold,
                    condensed = true,
                    lineHeight = 29.sp,
                )
                LabelText(stringResource(Res.string.ui_vash_shlyakh_pochynayetsya_tut), ApexMuted, 11.sp)
            }
            LabelText(
                "ⓘ",
                ApexMuted,
                20.sp,
                modifier = Modifier.clickable {
                    viewModel.help()
                },
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            CockpitBanner()
            AccountTypeSelector(
                personalAccount = personalAccount,
                onSelect = {
viewModel.selectAccount(it)
                },
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ApexTextField(
                    label = stringResource(Res.string.ui_im_ya_ta_prizvyshche),
                    value = fullName,
                    onValueChange = {
viewModel.nameChanged(it)
                    },
                    hint = stringResource(Res.string.ui_vvedit_im_ya_ta_prizvyshche),
                    error = nameError,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    trailingLabel = "♙",
                )
                ApexTextField(
                    label = stringResource(Res.string.ui_elektronna_poshta),
                    value = email,
                    onValueChange = {
viewModel.emailChanged(it)
                    },
                    hint = stringResource(Res.string.ui_name_example_com),
                    error = emailError,
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    trailingLabel = "✉",
                )
                ApexTextField(
                    label = stringResource(Res.string.ui_parol),
                    value = password,
                    onValueChange = {
viewModel.passwordChanged(it)
                    },
                    hint = stringResource(Res.string.ui_vvedit_parol),
                    error = passwordError,
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    trailingLabel = if (passwordVisible) "◉" else "◉̸",
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    onTrailingClick = { passwordVisible = !passwordVisible },
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                LabelText("⊙", Color(0xFF85C6AC), 14.sp)
                LabelText(
                    stringResource(Res.string.ui_nadiynyy_parol_vid_8_symvoliv),
                    if (uiState.passwordStrong) {
                        Color(0xFF85C6AC)
                    } else {
                        ApexLinkRed
                    },
                    11.sp,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
viewModel.toggleConsent()
                    },
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(19.dp)
                        .height(19.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (consentGiven) ApexRed else ApexSurface)
                        .border(
                            1.dp,
                            if (consentError != null) ApexLinkRed else ApexBorder,
                            RoundedCornerShape(5.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (consentGiven) LabelText("✓", ApexText, 13.sp, weight = FontWeight.Bold)
                }
                LabelText(
                    stringResource(Res.string.ui_pohodzhuyusya_z_umovamy_korystuvannya_ta_politykoyu_konfidentsiy),
                    ApexMuted,
                    11.sp,
                    modifier = Modifier.weight(1f),
                    lineHeight = 15.sp,
                )
            }
            consentError?.let { LabelText(it, ApexLinkRed, 11.sp) }

            PrimaryButton(stringResource(Res.string.ui_stvoryty_akaunt), enabled = !uiState.isSubmitting) {
                viewModel.submit()
            }
            statusMessage?.let { LabelText(it, ApexMuted, 12.sp, lineHeight = 16.sp) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ApexSurface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.Top,
            ) {
                LabelText("▤", ApexMuted, 17.sp)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    LabelText(
                        stringResource(Res.string.ui_dlya_korporatyvnoho_akaunta_potribne_zaproshennya_vid_administra),
                        ApexMuted,
                        10.sp,
                        lineHeight = 14.sp,
                    )
                    if (!personalAccount) {
                        LabelText(
                            stringResource(Res.string.ui_perehlyanuty_zaproshennya),
                            ApexLinkRed,
                            11.sp,
                            modifier = Modifier.clickable(onClick = { viewModel.navigate(AuthenticationEffect.Invitation) }),
                            weight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LabelText(stringResource(Res.string.ui_uzhe_mayete_akaunt), ApexMuted, 12.sp)
                Spacer(Modifier.width(5.dp))
                LabelText(
                    stringResource(Res.string.ui_uviyty),
                    ApexLinkRed,
                    12.sp,
                    modifier = Modifier.clickable(onClick = { viewModel.navigate(AuthenticationEffect.SignIn) }),
                    weight = FontWeight.SemiBold,
                )
            }
        }
    }
}


@Composable
private fun authenticationError(error: ValidationError?, password: Boolean): String? = error?.let {
    stringResource(when (it) {
        ValidationError.Required -> if (password) Res.string.ui_vvedit_parol_2 else Res.string.ui_vvedit_elektronnu_poshtu
        ValidationError.InvalidEmail -> Res.string.ui_perevirte_format_elektronnoyi_poshty
        ValidationError.ShortPassword -> Res.string.ui_parol_maye_mistyty_shchonaymenshe_8_symvoliv
        else -> Res.string.ui_perevirte_format_elektronnoyi_poshty
    })
}
