package com.mobilespace.xrnavi.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import com.mobilespace.xrnavi.presentation.NavigationAction
import com.mobilespace.xrnavi.presentation.NavigationNotice
import com.mobilespace.xrnavi.presentation.NavigationViewModel
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun NavigationEffects(viewModel: NavigationViewModel, onAction: (NavigationAction) -> Unit) {
    val current = rememberUpdatedState(onAction)
    LaunchedEffect(viewModel) { viewModel.effects.collect { current.value(it) } }
}

@Composable
internal fun navigationNotice(notice: NavigationNotice?): String? = notice?.let {
    when (it) {
        NavigationNotice.LocationUnavailable -> stringResource(Res.string.ui_heolokatsiyu_ne_pidklyucheno_pokazano_demonstratsiyne_mistseznak)
        NavigationNotice.TruckValidationUnavailable -> stringResource(Res.string.ui_perevirka_marshrutu_dlya_vantazhnoho_transportu_nedostupna_perev)
        NavigationNotice.MissingDestination -> stringResource(Res.string.ui_vvedit_adresu)
        NavigationNotice.ParkingDemo -> stringResource(Res.string.ui_dani_pro_stoyanku_demonstratsiyni_pered_zayizdom_utochnit_umovy)
        NavigationNotice.PhoneUnavailable -> stringResource(Res.string.ui_telefon_stoyanky_navedeno_v_demonstratsiynomu_maketi_dzvinok_ne)
        NavigationNotice.AddStopUnavailable -> stringResource(Res.string.ui_dodavannya_zupynky_do_marshrutu_shche_ne_pidklyucheno_zupynku_ne)
        NavigationNotice.SoundEnabled -> stringResource(Res.string.ui_zvuk_navihatsiyi_uvimkneno)
        NavigationNotice.SoundDisabled -> stringResource(Res.string.ui_zvuk_navihatsiyi_vymkneno)
        NavigationNotice.ReportUnavailable -> stringResource(Res.string.ui_nadsylannya_povidomlen_pro_podiyi_shche_ne_pidklyucheno)
        NavigationNotice.ShareUnavailable -> stringResource(Res.string.ui_poshyrennya_marshrutu_shche_ne_pidklyucheno)
        NavigationNotice.MapUnavailable -> stringResource(Res.string.ui_vybir_tochky_na_mapi_bude_dostupnyy_pislya_pidklyuchennya_kartoh)
        NavigationNotice.MembershipRequired -> stringResource(Res.string.ui_dlya_korporatyvnoho_akaunta_potribne_zaproshennya_vid_administra)
    }
}
