package com.mobilespace.xrnavi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import xrnavi.shared.generated.resources.*

enum class FeatureDestination { Back, Cancel, Decline, Accepted, Map, Garage, Settings, Profile, Task, TripPlan }
data class FeatureEffect(val destination: FeatureDestination, val taskId: String? = null)

abstract class FeatureViewModel<S>(
    initialState: S,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    protected val mutableState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = mutableState.asStateFlow()
    private val effectChannel = Channel<FeatureEffect>(Channel.BUFFERED)
    val effects = effectChannel.receiveAsFlow()
    private var running = false

    fun navigate(destination: FeatureDestination) {
        emit(FeatureEffect(destination))
    }

    protected fun emit(effect: FeatureEffect) {
        viewModelScope.launch(dispatcher) { effectChannel.send(effect) }
    }

    protected fun submit(
        busy: (Boolean) -> Unit,
        action: suspend () -> Unit,
    ) {
        if (running) return
        running = true
        busy(true)
        viewModelScope.launch(dispatcher) {
            try {
                action()
            } finally {
                running = false
                busy(false)
            }
        }
    }
}

data class AccountDeletionUiState(
    val acknowledged: Boolean = false,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
)

class AccountDeletionViewModel(
    private val requestDeletion: RequestAccountDeletion,
    private val exportData: ExportAccountData,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<AccountDeletionUiState>(AccountDeletionUiState(), dispatcher) {
    fun toggleAcknowledgement() {
        if (uiState.value.isSubmitting) return
        mutableState.update { it.copy(acknowledged = !it.acknowledged, statusMessage = null) }
    }

    fun deleteAccount() {
        val acknowledged = uiState.value.acknowledged
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            when (requestDeletion(acknowledged)) {
                AccountDeletionResult.ConsentRequired -> mutableState.update {
                    it.copy(statusMessage = Res.string.ui_pidtverdte_shcho_rozumiyete_naslidky_vydalennya)
                }
                AccountDeletionResult.NotConfigured -> deletionUnavailable()
            }
        }
    }

    fun exportAccountData() {
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            exportData()
            exportUnavailable()
        }
    }

    private fun deletionUnavailable() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_zapyt_ne_nadislano_servis_vydalennya_akaunta_ne_pidklyucheno)
        }
    }

    private fun exportUnavailable() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_eksport_ne_rozpochato_servis_keruvannya_danymy_ne_pidklyucheno)
        }
    }
}

data class OfflineMapsUiState(
    val showHelp: Boolean = false,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
)

class OfflineMapsViewModel(
    private val performAction: PerformOfflineMapsAction,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<OfflineMapsUiState>(OfflineMapsUiState(), dispatcher) {
    fun toggleHelp() { mutableState.update { it.copy(showHelp = !it.showHelp) } }
    fun perform(action: OfflineMapsAction) {
        val message = when (action) {
            OfflineMapsAction.UpdateRegion -> Res.string.ui_onovlennya_ne_zapushcheno_servis_oflayn_map_ne_pidklyucheno
            OfflineMapsAction.PauseDownload -> Res.string.ui_zavantazhennya_ne_pryzupyneno_prohres_na_ekrani_ye_demonstratsiy
            OfflineMapsAction.CancelDownload -> Res.string.ui_zavantazhennya_ne_skasovano_servis_oflayn_map_ne_pidklyucheno
            OfflineMapsAction.OpenCachedRoute -> Res.string.ui_kesh_marshrutu_navedeno_yak_pryklad_perehlyad_zberezhenoyi_mapy
            OfflineMapsAction.DownloadRegion -> Res.string.ui_rehion_ne_zavantazheno_servis_oflayn_map_ne_pidklyucheno
        }
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            performAction(action)
            mutableState.update { it.copy(statusMessage = message) }
        }
    }
}

data class OrganizationInvitationUiState(
    val consentGiven: Boolean = true,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
)

class OrganizationInvitationViewModel(
    private val acceptInvitation: AcceptOrganizationInvitation,
    private val invitationCode: String,
    private val invitationEmail: String,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<OrganizationInvitationUiState>(OrganizationInvitationUiState(), dispatcher) {
    fun toggleConsent() {
        if (uiState.value.isSubmitting) return
        mutableState.update { it.copy(consentGiven = !it.consentGiven, statusMessage = null) }
    }
    fun showPrivacy() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_zaproshennya_pryv_yazane_do_andrii_ukr_net_osobysti_poyizdky_ne)
        }
    }
    fun accept() {
        val consent = uiState.value.consentGiven
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            when (acceptInvitation(invitationCode, invitationEmail, consent)) {
                OrganizationInvitationResult.Accepted -> emit(FeatureEffect(FeatureDestination.Accepted))
                OrganizationInvitationResult.ConsentRequired -> mutableState.update {
                    it.copy(statusMessage = Res.string.ui_pidtverdte_oznayomlennya_z_pravylamy_ta_umovamy_peredachi_danykh)
                }
                OrganizationInvitationResult.NotConfigured -> unavailable()
            }
        }
    }
    private fun unavailable() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_pryynyattya_zaproshen_shche_ne_pidklyucheno_zaproshennya_ne_pryy)
        }
    }
}

data class TripHistoryUiState(
    val selectedFilter: Int = 0,
    val showHelp: Boolean = false,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
) {
    val showPersonalTrips: Boolean get() = selectedFilter != 2
    val showWorkTrips: Boolean get() = selectedFilter != 1
}

class TripHistoryViewModel(
    private val performAction: PerformTripHistoryAction,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<TripHistoryUiState>(TripHistoryUiState(), dispatcher) {
    fun selectFilter(index: Int) {
        if (index !in 0..2) return
        mutableState.update { it.copy(selectedFilter = index, statusMessage = null) }
    }
    fun toggleHelp() { mutableState.update { it.copy(showHelp = !it.showHelp) } }
    fun perform(action: TripHistoryAction) {
        val message = when (action) {
            TripHistoryAction.OpenPersonalTrip -> Res.string.ui_detali_poyizdky_ne_zavantazheno_servis_istoriyi_ne_pidklyucheno
            TripHistoryAction.OpenWorkTrip -> Res.string.ui_detali_reysu_ne_zavantazheno_servis_istoriyi_ne_pidklyucheno
            TripHistoryAction.OpenClassification -> Res.string.ui_zapyt_na_zminu_klasyfikatsiyi_ne_nadislano_servis_zhurnalu_poyiz
        }
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            performAction(action)
            mutableState.update { it.copy(statusMessage = message) }
        }
    }
}

data class WorkTasksUiState(
    val chosenTask: WorkTask?,
    val offeredTask: WorkTask?,
    val session: SessionState,
    val selectedTab: Int = 0,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
)

class WorkTasksViewModel(
    private val performAction: PerformWorkTripAction,
    private val openChosenTask: OpenChosenWorkTask,
    session: SessionRepository,
    chosenTask: WorkTask?,
    offeredTask: WorkTask?,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<WorkTasksUiState>(WorkTasksUiState(chosenTask, offeredTask, session.state.value), dispatcher) {
    init {
        viewModelScope.launch(dispatcher) {
            session.state.collect { state -> mutableState.update { it.copy(session = state) } }
        }
    }
    fun selectTab(index: Int) {
        if (index !in 0..1) return
        mutableState.update { it.copy(selectedTab = index, statusMessage = null) }
    }
    fun showHelp() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_tse_demonstratsiynyy_perelik_dani_ta_diyi_zavdan_potrebuyut_serv)
        }
    }
    fun openTask() {
        val task = uiState.value.chosenTask
        if (openChosenTask(task)) emit(FeatureEffect(FeatureDestination.Task, task?.id))
        else showHelp()
    }
    fun openCompletedTask() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_detali_zavershenoho_zavdannya_nedostupni_bez_servisu_reysiv)
        }
    }
    fun acceptTask() {
        val task = uiState.value.offeredTask
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            when (performAction(WorkTripAction.AcceptTask, task)) {
                WorkTripActionResult.NotConfigured -> unavailable()
                WorkTripActionResult.InvalidContext -> showHelp()
            }
        }
    }
    private fun unavailable() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_zavdannya_ne_pryynyato_servis_korporatyvnykh_reysiv_ne_pidklyuch)
        }
    }
}

data class WorkTripDetailsUiState(
    val chosenTask: WorkTask?,
    val session: SessionState,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
)

class WorkTripDetailsViewModel(
    private val performAction: PerformWorkTripAction,
    private val session: SessionRepository,
    chosenTask: WorkTask?,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<WorkTripDetailsUiState>(WorkTripDetailsUiState(chosenTask, session.state.value), dispatcher) {
    init {
        viewModelScope.launch(dispatcher) {
            session.state.collect { state -> mutableState.update { it.copy(session = state) } }
        }
    }
    fun showHelp() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_dani_reysu_demonstratsiyni_dlya_zmin_ta_pidtverdzhen_potriben_se)
        }
    }
    fun openTripPlan() {
        emit(FeatureEffect(FeatureDestination.TripPlan, uiState.value.chosenTask?.id))
    }
    fun planRoute() {
        val task = uiState.value.chosenTask
        if (task != null && OpenChosenWorkTask(session)(task)) {
            emit(FeatureEffect(FeatureDestination.Map, task.id))
        } else showHelp()
    }
    fun perform(action: WorkTripAction) {
        val message = when (action) {
            WorkTripAction.ContactReceiver -> Res.string.ui_dzvinok_ne_zdiysneno_intehratsiyu_telefoniyi_ne_pidklyucheno
            WorkTripAction.MessageDispatcher -> Res.string.ui_povidomlennya_ne_nadislano_servis_zv_yazku_z_dyspetcherom_ne_pid
            WorkTripAction.ReportArrival -> Res.string.ui_prybuttya_ne_pidtverdzheno_heolokatsiyu_ta_servis_reysiv_ne_pidk
            WorkTripAction.ReportDelay -> Res.string.ui_povidomlennya_pro_zatrymku_ne_nadislano_servis_reysiv_ne_pidklyu
            WorkTripAction.AcceptTask -> Res.string.ui_zavdannya_ne_pryynyato_servis_korporatyvnykh_reysiv_ne_pidklyuch
        }
        val task = uiState.value.chosenTask
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            when (performAction(action, task)) {
                WorkTripActionResult.NotConfigured -> mutableState.update { it.copy(statusMessage = message) }
                WorkTripActionResult.InvalidContext -> showHelp()
            }
        }
    }
}

data class TripPlanUiState(
    val chosenTask: WorkTask?,
    val session: SessionState,
    val showHelp: Boolean = false,
    val isSubmitting: Boolean = false,
    val statusMessage: StringResource? = null,
) {
    val personalPlan get() = chosenTask == null || session.context == DrivingContext.Personal
    val destination get() = if (personalPlan) session.destination else chosenTask?.destination.orEmpty()
}

class TripPlanViewModel(
    private val addTripPlanStop: AddTripPlanStop,
    session: SessionRepository,
    chosenTask: WorkTask?,
    dispatcher: CoroutineDispatcher,
) : FeatureViewModel<TripPlanUiState>(TripPlanUiState(chosenTask, session.state.value), dispatcher) {
    init {
        viewModelScope.launch(dispatcher) {
            session.state.collect { state -> mutableState.update { it.copy(session = state) } }
        }
    }
    fun toggleHelp() { mutableState.update { it.copy(showHelp = !it.showHelp) } }
    fun addStop() {
        val task = uiState.value.chosenTask
        submit(
            busy = { busy -> mutableState.update { it.copy(isSubmitting = busy) } },
        ) {
            when (addTripPlanStop(task)) {
                TripPlanActionResult.NotConfigured -> unavailable()
                TripPlanActionResult.InvalidContext -> {
                    mutableState.update { it.copy(showHelp = true) }
                    unavailable()
                }
            }
        }
    }
    private fun unavailable() {
        mutableState.update {
            it.copy(statusMessage = Res.string.ui_zupynku_ne_dodano_servis_planuvannya_reysu_ne_pidklyucheno)
        }
    }
}
