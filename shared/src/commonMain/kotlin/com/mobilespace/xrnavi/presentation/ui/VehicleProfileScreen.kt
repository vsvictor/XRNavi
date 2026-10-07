package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.mobilespace.xrnavi.presentation.FordParameter
import com.mobilespace.xrnavi.presentation.VehicleProfileState
import com.mobilespace.xrnavi.presentation.VehicleProfileViewModel
import com.mobilespace.xrnavi.domain.VehicleConfiguration
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.garage_truck
import xrnavi.shared.generated.resources.profile_help
import xrnavi.shared.generated.resources.profile_lock

private val ProfileValueBackground = Color(0xFF20262E)
private val ProfilePolicyBackground = Color(0xFF30291F)
private val ProfileGold = Color(0xFFE9BA76)

@Composable
internal fun VehicleProfileScreen(
    viewModel: VehicleProfileViewModel,
    onBack: () -> Unit,
    onOpenConfigurations: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val statusMessage = vehicleMessage(state.message)
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val imeInset = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = maxOf(48.dp, topInset)),
    ) {
        VehicleProfileHeader(
            personal = state.isPersonal,
            onBack = onBack,
            onHelp = viewModel::toggleHelp,
            onOpenConfigurations = onOpenConfigurations,
        )
        if (state.helpVisible) {
            LabelText(
                stringResource(if (state.isPersonal) Res.string.ui_zobrazhennya_ne_pidtverdzhuye_komplektatsiyu_parametry_zvirte_z
                    else Res.string.ui_habaryty_ta_obmezhennya_avtopoyizda_vstanovleni_orhanizatsiyeyu),
                ApexMuted,
                11.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = maxOf(20.dp, imeInset)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (state.isPersonal) {
                PersonalFordProfile(state, viewModel::changeFordParameter)
            } else {
                VehicleIdentityCard()
                ProfileApprovalNotice()
                state.truckConfiguration?.let {
                    DimensionsSection(it, state.actualLoad, state.actualLoadValid, viewModel::changeActualLoad)
                }
                TrailerSection()
            }
            RoutingRestrictionsSection(
                personal = state.isPersonal,
                avoidTolls = state.avoidTolls,
                onToggleAvoidTolls = viewModel::toggleAvoidTolls,
            )
            statusMessage?.let {
                LabelText(it, ApexMuted, 11.sp, modifier = Modifier.fillMaxWidth())
            }
            PrimaryButton(
                label = stringResource(Res.string.ui_zberehty_profil),
                enabled = state.saveEnabled,
                onClick = viewModel::save,
            )
            if (!state.isPersonal) LabelText(
                stringResource(Res.string.ui_zaprosyty_zminu_v_dyspetchera),
                ApexMuted,
                11.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !state.isSubmitting, onClick = viewModel::requestDispatcherChange)
                    .padding(vertical = 3.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun PersonalFordProfile(
    state: VehicleProfileState,
    onParameterChange: (FordParameter, String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ApexSurface).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabelText(fordProfileTitle(state.ford), ApexText, 20.sp, weight = FontWeight.SemiBold, condensed = true)
        LabelText("${state.ford.year} · ${stringResource(Res.string.ui_osobystyy_rezhym)}", ApexMuted, 11.sp)
        LabelText(stringResource(Res.string.ui_zobrazhennya_ne_pidtverdzhuye_komplektatsiyu_parametry_zvirte_z),
            ApexMuted, 10.sp, lineHeight = 14.sp)
    }
    ProfileSectionHeading(stringResource(Res.string.ui_habaryty_ta_masa))
    PersonalParameterField(stringResource(Res.string.ui_vysota), state.fields.height, "м") {
        onParameterChange(FordParameter.Height, it)
    }
    PersonalParameterField(stringResource(Res.string.ui_shyryna), state.fields.width, "м") {
        onParameterChange(FordParameter.Width, it)
    }
    PersonalParameterField(stringResource(Res.string.ui_dovzhyna), state.fields.length, "м") {
        onParameterChange(FordParameter.Length, it)
    }
    PersonalParameterField(stringResource(Res.string.ui_povna_masa), state.fields.mass, stringResource(Res.string.ui_t)) {
        onParameterChange(FordParameter.Mass, it)
    }
    if (!state.personalParametersValid) {
        LabelText(stringResource(Res.string.ui_zvirte_dani_z_tekhpasportom), ApexRed, 11.sp)
    }
}

@Composable
private fun PersonalParameterField(label: String, value: String, unit: String, onChange: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(ApexSurface)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(label, ApexText, 12.sp, modifier = Modifier.weight(1f))
        BasicTextField(
            value = value, onValueChange = onChange, singleLine = true,
            modifier = Modifier.width(72.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(color = ApexText, fontSize = 12.sp, textAlign = TextAlign.End),
        )
        LabelText(unit, ApexMuted, 12.sp)
    }
}

@Composable
private fun VehicleProfileHeader(
    personal: Boolean,
    onBack: () -> Unit,
    onHelp: () -> Unit,
    onOpenConfigurations: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 44.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.CenterStart,
        ) {
            LabelText("‹", ApexText, 32.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            LabelText(
                stringResource(Res.string.ui_profil_transportu),
                ApexText,
                26.sp,
                weight = FontWeight.Bold,
                condensed = true,
                lineHeight = 30.sp,
            )
            LabelText(stringResource(if (personal) Res.string.ui_osobystyy_rezhym else Res.string.ui_avtovoz_korporatyvnyy_rezhym), ApexMuted, 11.sp)
        }
        Box(
            modifier = Modifier
                .size(28.dp)
                .clickable(onClick = onHelp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.profile_help),
                contentDescription = stringResource(Res.string.ui_dovidka),
                modifier = Modifier.size(19.dp),
                colorFilter = ColorFilter.tint(ApexMuted),
            )
        }
        LabelText("›", ApexMuted, 22.sp, modifier = Modifier.clickable(onClick = onOpenConfigurations))
    }
}

@Composable
private fun VehicleIdentityCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ApexSurface)
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ProfileValueBackground),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(Res.drawable.garage_truck),
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                colorFilter = ColorFilter.tint(ApexText),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_man_tgx_avtovoz), ApexText, 14.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_aa_4821_kkh_dnipro_logistics), ApexMuted, 10.sp)
        }
    }
}

@Composable
private fun ProfileApprovalNotice() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ProfilePolicyBackground)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        ProfileLockIcon(Modifier.padding(top = 1.dp).size(17.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LabelText(stringResource(Res.string.ui_profil_zatverdzheno_orhanizatsiyeyu), ProfileGold, 11.sp, weight = FontWeight.SemiBold)
            LabelText(stringResource(Res.string.ui_habaryty_y_limity_zminyuye_lyshe_dyspetcher), ApexMuted, 10.sp)
        }
    }
}

@Composable
private fun DimensionsSection(
    configuration: VehicleConfiguration,
    actualLoad: String,
    actualLoadValid: Boolean,
    onActualLoadChange: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileSectionHeading(stringResource(Res.string.ui_habaryty_ta_masa), stringResource(Res.string.ui_uves_avtopoyizd))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DimensionCard(stringResource(Res.string.ui_vysota), "${configuration.dimensions.height} м", Modifier.weight(1f))
            DimensionCard(stringResource(Res.string.ui_shyryna), "${configuration.dimensions.width} м", Modifier.weight(1f))
            DimensionCard(stringResource(Res.string.ui_dovzhyna), "${configuration.dimensions.length} м", Modifier.weight(1f))
        }
        InheritedProfileSetting(stringResource(Res.string.ui_maksymalna_povna_masa),
            "${configuration.maximumMass} ${stringResource(Res.string.ui_t)}")
        InheritedProfileSetting(stringResource(Res.string.ui_navantazhennya_na_vis),
            "${configuration.maximumAxleMass} ${stringResource(Res.string.ui_t)}")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurface)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelText(stringResource(Res.string.ui_faktychna_masa_z_vantazhem), ApexText, 12.sp)
                LabelText(
                    if (actualLoadValid) stringResource(Res.string.ui_vashe_znachennya_ne_bilshe_40_t) else stringResource(Res.string.ui_vvedit_znachennya_vid_0_do_40_t),
                    if (actualLoadValid) ApexMuted else ApexRed,
                    10.sp,
                )
            }
            BasicTextField(
                value = actualLoad,
                onValueChange = onActualLoadChange,
                modifier = Modifier.width(52.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = TextStyle(
                    color = if (actualLoadValid) ApexText else ApexRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                ),
            )
            LabelText(stringResource(Res.string.ui_t), ApexText, 12.sp)
            LabelText("›", ApexMuted, 22.sp)
        }
    }
}

@Composable
private fun DimensionCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ProfileValueBackground)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LabelText(label, ApexMuted, 10.sp)
            ProfileLockIcon(Modifier.size(12.dp))
        }
        LabelText(value, ApexText, 24.sp, weight = FontWeight.Bold, condensed = true)
    }
}

@Composable
private fun InheritedProfileSetting(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ApexSurface)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LabelText(label, ApexText, 12.sp, modifier = Modifier.weight(1f))
        LabelText(value, ProfileGold, 12.sp, weight = FontWeight.SemiBold)
        ProfileLockIcon(Modifier.size(14.dp))
    }
}

@Composable
private fun TrailerSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileSectionHeading(stringResource(Res.string.ui_prychip_buksyruvannya))
        InheritedProfileSetting(stringResource(Res.string.ui_typ_zcheplennya), stringResource(Res.string.ui_napivprychip))
        LabelText(
            stringResource(Res.string.ui_habaryty_y_masa_vklyuchayut_tyahach_napivprychip_i_vantazh_buksy),
            ApexMuted,
            10.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun RoutingRestrictionsSection(
    personal: Boolean,
    avoidTolls: Boolean,
    onToggleAvoidTolls: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ProfileSectionHeading(stringResource(Res.string.ui_obmezhennya_marshrutu),
            if (personal) null else stringResource(Res.string.ui_polityka_3_2))
        if (!personal) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PolicyBadge(stringResource(Res.string.ui_bez_gruntovykh_dorih))
            PolicyBadge(stringResource(Res.string.ui_do_90_km_hod))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ApexSurface)
                .clickable(onClick = onToggleAvoidTolls)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LabelText(stringResource(Res.string.ui_unykaty_platnykh_dorih), ApexText, 12.sp)
                LabelText(stringResource(Res.string.ui_osobysta_optsiya_mozhna_zminyuvaty), ApexMuted, 10.sp)
            }
            LabelText(if (avoidTolls) stringResource(Res.string.ui_tak) else stringResource(Res.string.ui_ni), ApexText, 12.sp, weight = FontWeight.SemiBold)
            LabelText("›", ApexMuted, 22.sp)
        }
        LabelText(
            stringResource(Res.string.ui_nyzhchyy_dorozhniy_limit_maye_priorytet_zavzhdy_dotrymuytesya_zn),
            ApexSubtle,
            10.sp,
            lineHeight = 14.sp,
        )
    }
}

@Composable
private fun ProfileSectionHeading(title: String, trailing: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LabelText(title, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
        trailing?.let { LabelText(it, ApexSubtle, 10.sp) }
    }
}

@Composable
private fun PolicyBadge(text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ProfilePolicyBackground)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileLockIcon(Modifier.size(12.dp))
        LabelText(text, ProfileGold, 10.sp, weight = FontWeight.SemiBold)
    }
}

@Composable
private fun ProfileLockIcon(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.profile_lock),
        contentDescription = stringResource(Res.string.ui_zablokovano_orhanizatsiyeyu),
        modifier = modifier,
        colorFilter = ColorFilter.tint(ProfileGold),
    )
}
