package com.mobilespace.xrnavi.presentation.ui

import androidx.compose.runtime.Composable
import com.mobilespace.xrnavi.domain.FordModel
import com.mobilespace.xrnavi.domain.FordSpecification
import com.mobilespace.xrnavi.domain.FordTrim
import com.mobilespace.xrnavi.domain.MakeId
import com.mobilespace.xrnavi.domain.VehicleOperation
import com.mobilespace.xrnavi.presentation.VehicleMessage
import org.jetbrains.compose.resources.stringResource
import xrnavi.shared.generated.resources.*

@Composable
internal fun vehicleMakeName(id: MakeId): String = stringResource(when (id) {
    MakeId.Ford -> Res.string.ui_ford
    MakeId.Volkswagen -> Res.string.ui_volkswagen
    MakeId.Toyota -> Res.string.ui_toyota
    MakeId.Bmw -> Res.string.ui_bmw
})

@Composable
internal fun vehicleMakeModels(id: MakeId): String = stringResource(when (id) {
    MakeId.Ford -> Res.string.ui_mustang_focus_explorer
    MakeId.Volkswagen -> Res.string.ui_golf_passat_tiguan
    MakeId.Toyota -> Res.string.ui_corolla_camry_rav4
    MakeId.Bmw -> Res.string.ui_3_series_5_series_x5
})

@Composable
internal fun vehicleMessage(message: VehicleMessage?): String? = when (message) {
    null -> null
    VehicleMessage.MembershipRequired -> stringResource(Res.string.ui_pryyednatysya_do_komandy)
    VehicleMessage.ManualEntryUnavailable -> stringResource(Res.string.ui_forma_ruchnoho_vvedennya_shche_ne_pidklyuchena_transport_ne_doda)
    is VehicleMessage.MakeUnavailable -> stringResource(
        Res.string.ui_marku_1_s_obrano_konfihuratsiya_tsoho_vyrobnyka_shche_ne_pidklyu, vehicleMakeName(message.make))
    VehicleMessage.Illustration -> stringResource(Res.string.ui_zobrazhennya_lyshe_ilyustratyvne_zvirte_rik_i_komplektatsiyu_z_t)
    VehicleMessage.CatalogUnavailable -> stringResource(Res.string.ui_dani_modeli_ne_pidtverdzheni_katalohom_perevirte_rik_i_komplekta)
    VehicleMessage.InvalidParameters -> stringResource(Res.string.ui_zvirte_dani_z_tekhpasportom)
    is VehicleMessage.OperationUnavailable -> stringResource(when (message.operation) {
        VehicleOperation.SaveProfile -> Res.string.ui_servis_zberezhennya_ne_pidklyucheno_dani_zalyshylysya_lyshe_na_e
        VehicleOperation.ApplyConfiguration -> Res.string.ui_konfihuratsiyu_ne_zastosovano_servis_transportu_ne_pidklyucheno
        VehicleOperation.RequestDispatcherChange -> Res.string.ui_zapyt_dyspetcheru_ne_nadislano_servis_orhanizatsiyi_ne_pidklyuch
    })
    VehicleMessage.OperationFailed -> stringResource(Res.string.ui_operation_failed)
}

@Composable
internal fun fordProfileTitle(specification: FordSpecification): String =
    stringResource(Res.string.ui_ford_model, stringResource(when (specification.model) {
        FordModel.Mustang -> Res.string.ui_mustang
        FordModel.Focus -> Res.string.ui_focus
        FordModel.Explorer -> Res.string.ui_explorer
    })) + if (specification.trim == FordTrim.GT) " GT" else " EcoBoost"
