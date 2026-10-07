package com.mobilespace.xrnavi.presentation.ui

import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.mobilespace.xrnavi.presentation.*
import com.mobilespace.xrnavi.domain.DemoAddress

import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.Res
import xrnavi.shared.generated.resources.*

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import xrnavi.shared.generated.resources.address_chevron
import xrnavi.shared.generated.resources.address_choose_map
import xrnavi.shared.generated.resources.address_clear
import xrnavi.shared.generated.resources.address_history
import xrnavi.shared.generated.resources.address_logo
import xrnavi.shared.generated.resources.address_nav_garage
import xrnavi.shared.generated.resources.address_nav_map
import xrnavi.shared.generated.resources.address_nav_settings
import xrnavi.shared.generated.resources.address_origin
import xrnavi.shared.generated.resources.address_pin
import xrnavi.shared.generated.resources.address_search
import xrnavi.shared.generated.resources.address_swap
import xrnavi.shared.generated.resources.address_vehicle_expand
import xrnavi.shared.generated.resources.apex_navigation
import xrnavi.shared.generated.resources.apex_user
import xrnavi.shared.generated.resources.mustang_thumbnail
import xrnavi.shared.generated.resources.nav_profile

@Composable
fun AddressSearchScreen(
    viewModel: AddressSearchViewModel,
    onBack: () -> Unit,
    onSelectAddress: (String) -> Unit,
    onChooseOnMap: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val notice = navigationNotice(state.notice)
    NavigationEffects(viewModel) {
        when (it) {
            NavigationAction.Back -> onBack()
            NavigationAction.AddressSelected -> viewModel.state.value.selectedAddress?.let(onSelectAddress)
            NavigationAction.ChooseOnMap -> onChooseOnMap()
            NavigationAction.Map -> onOpenMap()
            NavigationAction.Garage -> onOpenGarage()
            NavigationAction.Preferences -> onOpenSettings()
            NavigationAction.Profile -> onOpenProfile()
            else -> Unit
        }
    }
    var isVehicleExpanded by remember { mutableStateOf(true) }

    Column(
        Modifier
            .fillMaxSize()
            .background(ApexBackground)
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            .imePadding(),
    ) {
        AddressSearchHeader { viewModel.navigate(NavigationAction.Back) }
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            AddressRouteEntry(
                query = state.query,
                onQueryChange = viewModel::queryChanged,
                onSwap = viewModel::swap,
                onClear = { viewModel.queryChanged("") },
            )

            if (state.query.isNotBlank()) {
                AddressSuggestionSection(
                    title = stringResource(Res.string.ui_znaydeni_adresy),
                    trailing = state.results.size.toString(),
                    rows = state.results.map { addressRow(it, false) },
                    onSelectAddress = viewModel::selectAddress,
                )
            }

            AddressSuggestionSection(
                title = stringResource(Res.string.ui_neshchodavni),
                rows = state.recent.map { addressRow(it, true) },
                onSelectAddress = viewModel::selectAddress,
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ApexSurface)
                    .border(1.dp, ApexBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = viewModel::chooseOnMap)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AddressIcon(Res.drawable.address_choose_map, stringResource(Res.string.ui_vybraty_tochku_na_mapi), Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                LabelText(stringResource(Res.string.ui_vybraty_tochku_na_mapi), ApexText, 13.sp, Modifier.weight(1f), FontWeight.SemiBold)
                AddressIcon(Res.drawable.address_chevron, null, Modifier.size(16.dp))
            }

            Column {
                Row(
                    Modifier.fillMaxWidth().height(68.dp).clickable { isVehicleExpanded = !isVehicleExpanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(Res.drawable.mustang_thumbnail),
                        contentDescription = stringResource(Res.string.ui_ford_mustang_gt),
                        modifier = Modifier.size(width = 94.dp, height = 64.dp).clip(RoundedCornerShape(8.dp)),
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        LabelText(stringResource(Res.string.ui_ford_mustang_gt), ApexText, 23.sp, weight = FontWeight.Bold, condensed = true)
                        Spacer(Modifier.height(4.dp))
                        LabelText(stringResource(Res.string.ui_lehkovyy_1_4_m_1_8_t), ApexMuted, 10.sp)
                    }
                    AddressIcon(Res.drawable.address_vehicle_expand, stringResource(Res.string.ui_zhornuty_vybir_avto), Modifier.size(18.dp))
                }
                if (isVehicleExpanded) {
                    LabelText(
                        stringResource(Res.string.ui_obrane_avto_dlya_planuvannya_marshrutu),
                        ApexMuted,
                        11.sp,
                        modifier = Modifier.padding(start = 106.dp, top = 4.dp),
                    )
                }
            }
            notice?.let {
                LabelText(it, ApexLinkRed, 11.sp, modifier = Modifier.fillMaxWidth())
            }
            LabelText(
                stringResource(Res.string.ui_demonstratsiyni_adresy_poshuk_i_perevirka_adres_ne_pidklyucheni),
                ApexSubtle,
                10.sp,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        AddressBottomNavigation(
            onOpenMap = { viewModel.navigate(NavigationAction.Map) },
            onOpenGarage = { viewModel.navigate(NavigationAction.Garage) },
            onOpenSettings = { viewModel.navigate(NavigationAction.Preferences) },
            onOpenProfile = { viewModel.navigate(NavigationAction.Profile) },
        )
    }
}

@Composable
private fun AddressSearchHeader(onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LabelText("‹", ApexText, 28.sp, modifier = Modifier.clickable(onClick = onBack).padding(end = 10.dp))
            Box(
                Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(ApexRed),
                contentAlignment = Alignment.Center,
            ) {
                AddressIcon(Res.drawable.address_logo, null, Modifier.size(19.dp))
            }
            Spacer(Modifier.width(8.dp))
            LabelText(stringResource(Res.string.ui_apex), ApexText, 21.sp, weight = FontWeight.Bold, condensed = true)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            AddressIcon(Res.drawable.apex_user, null, Modifier.size(15.dp))
            Spacer(Modifier.width(7.dp))
            LabelText(stringResource(Res.string.ui_osobystyy_rezhym), ApexMuted, 11.sp)
        }
    }
}

@Composable
private fun AddressRouteEntry(
    query: String,
    onQueryChange: (String) -> Unit,
    onSwap: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(ApexSurface)
            .border(1.dp, ApexRed, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            AddressIcon(Res.drawable.address_origin, stringResource(Res.string.ui_potochne_mistseznakhodzhennya), Modifier.size(10.dp))
            Spacer(Modifier.width(10.dp))
            LabelText(stringResource(Res.string.ui_moye_mistseznakhodzhennya), ApexText, 13.sp, Modifier.weight(1f))
            AddressIcon(Res.drawable.address_swap, stringResource(Res.string.ui_pominyaty_mistsyamy_pochatok_i_pryznachennya), Modifier.size(18.dp), onSwap)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(ApexBorder))
        Row(Modifier.fillMaxWidth().height(40.dp), verticalAlignment = Alignment.CenterVertically) {
            AddressIcon(Res.drawable.address_search, stringResource(Res.string.ui_poshuk_adresy), Modifier.size(14.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Search,
                ),
                textStyle = TextStyle(
                    color = ApexText,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                ),
                cursorBrush = SolidColor(ApexLinkRed),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) LabelText(stringResource(Res.string.ui_vvedit_adresu), ApexMuted, 13.sp)
                        inner()
                    }
                },
            )
            AddressIcon(Res.drawable.address_clear, stringResource(Res.string.ui_ochystyty_poshuk), Modifier.size(16.dp), onClear)
        }
    }
}

private data class AddressRow(val title: String, val subtitle: String, val isRecent: Boolean, val destination: String)

@Composable
private fun addressRow(address: DemoAddress, recent: Boolean): AddressRow {
    val title = stringResource(when (address.id) {
        "lviv-market-10" -> Res.string.ui_ploshcha_rynok_10
        "kyiv-khreshchatyk-22" -> Res.string.ui_vulytsya_khreshchatyk_22
        else -> Res.string.ui_ploshcha_rynok_1
    })
    val subtitle = stringResource(when (address.id) {
        "drohobych-market-1" -> Res.string.ui_drohobych_lvivska_oblast
        "kyiv-khreshchatyk-22" -> Res.string.ui_kyyiv_kyyivska_miska_rada
        "lviv-market-1" -> if (recent) Res.string.ui_lviv_lvivska_oblast else Res.string.ui_lviv_lvivska_oblast_ratusha
        else -> Res.string.ui_lviv_lvivska_oblast
    })
    return AddressRow(title, subtitle, recent, address.destination)
}

@Composable
private fun AddressSuggestionSection(
    title: String,
    trailing: String? = null,
    rows: List<AddressRow>,
    onSelectAddress: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            LabelText(title, ApexMuted, 11.sp, weight = FontWeight.SemiBold)
            trailing?.let { LabelText(it, ApexSubtle, 11.sp) }
        }
        Column(Modifier.fillMaxWidth()) {
            rows.forEachIndexed { index, item ->
                AddressSuggestionRow(
                    item,
                    Modifier.clickable {
                        onSelectAddress(item.destination)
                    },
                )
                if (index < rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(ApexBorder))
            }
        }
    }
}

@Composable
private fun AddressSuggestionRow(item: AddressRow, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().height(if (item.isRecent) 56.dp else 60.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF20262E)),
            contentAlignment = Alignment.Center,
        ) {
            AddressIcon(
                if (item.isRecent) Res.drawable.address_history else Res.drawable.address_pin,
                if (item.isRecent) stringResource(Res.string.ui_neshchodavnya_adresa) else stringResource(Res.string.ui_rezultat_poshuku),
                Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            LabelText(item.title, ApexText, 13.sp, weight = FontWeight.SemiBold)
            LabelText(item.subtitle, ApexMuted, 11.sp)
        }
        Spacer(Modifier.width(8.dp))
        AddressIcon(Res.drawable.address_chevron, null, Modifier.size(16.dp))
    }
}

@Composable
private fun AddressBottomNavigation(
    onOpenMap: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    Row(
        Modifier.fillMaxWidth().background(ApexSurface)
            .border(1.dp, ApexBorder, RoundedCornerShape(0.dp))
            .padding(bottom = bottomInset)
            .height(61.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AddressNavItem(Res.drawable.address_nav_map, stringResource(Res.string.ui_mapa), true, onOpenMap, Modifier.weight(1f))
        AddressNavItem(Res.drawable.address_nav_garage, stringResource(Res.string.ui_harazh), false, onOpenGarage, Modifier.weight(1f))
        AddressNavItem(Res.drawable.address_nav_settings, stringResource(Res.string.ui_nalashtuvannya), false, onOpenSettings, Modifier.weight(1f))
        AddressNavItem(Res.drawable.nav_profile, stringResource(Res.string.ui_profil), false, onOpenProfile, Modifier.weight(1f))
    }
}

@Composable
private fun AddressNavItem(
    icon: DrawableResource,
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxSize().clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(21.dp))
        Spacer(Modifier.height(5.dp))
        LabelText(title, if (selected) ApexText else ApexMuted, 9.sp)
    }
}

@Composable
private fun AddressIcon(
    resource: DrawableResource,
    description: String?,
    modifier: Modifier,
    onClick: (() -> Unit)? = null,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = description,
        modifier = modifier.then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)),
    )
}
