package com.mobilespace.xrnavi.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import xrnavi.shared.generated.resources.*

data class MessageState(val message: StringResource? = null, val isSubmitting: Boolean = false)
enum class ProfileEffect { Invitation }
data class ProfileUiState(val context: DrivingContext, val organizationMember: Boolean, val message: StringResource? = null)
class ProfileViewModel(private val session: SessionRepository) : ViewModel() {
    private val message = MutableStateFlow<StringResource?>(null)
    private val channel = Channel<ProfileEffect>(Channel.BUFFERED)
    val effects = channel.receiveAsFlow()
    val state = combine(session.state, message) { shared, notice ->
        ProfileUiState(shared.context, shared.organizationMember, notice)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        ProfileUiState(session.state.value.context, session.state.value.organizationMember))
    fun selectContext(index: Int) {
        val context = if (index == 1) DrivingContext.Work else DrivingContext.Personal
        if (!SelectDrivingContext(session)(context)) channel.trySend(ProfileEffect.Invitation)
        else message.value = null
    }
    fun edit() { message.value = Res.string.ui_redahuvannya_profilyu_shche_ne_pidklyucheno }
}

data class PreferencesUiState(val preferences: Preferences, val searchExpanded: Boolean = false, val message: StringResource? = null)
class PreferencesViewModel(private val preferences: PreferencesRepository) : ViewModel() {
    private val local = MutableStateFlow(false to (null as StringResource?))
    val state = combine(preferences.state, local) { settings, ui -> PreferencesUiState(settings, ui.first, ui.second) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PreferencesUiState(preferences.state.value))
    fun search() { local.update { !it.first to if (it.first) Res.string.ui_poshuk_nalashtuvan_shche_ne_pidklyucheno else null } }
    fun language() = notice(Res.string.ui_zmina_movy_stane_dostupnoyu_pislya_pidklyuchennya_lokalizatsiyi)
    fun theme() = notice(Res.string.ui_peremykannya_temy_mapy_shche_ne_pidklyucheno)
    fun toggleVoice() {
        preferences.update { it.copy(voiceEnabled = !it.voiceEnabled) }
        local.update { it.first to null }
    }
    fun toggleXr() {
        preferences.update { it.copy(xrEnabled = !it.xrEnabled) }
        notice(if (preferences.state.value.xrEnabled) Res.string.ui_xr_rezhym_uvimkneno_lokalno_kameru_ta_xr_servis_ne_pidklyucheno
            else Res.string.ui_xr_rezhym_vymkneno)
    }
    private fun notice(resource: StringResource) { local.update { it.first to resource } }
}

class PrivacyViewModel(private val manageData: ManagePersonalData) : ViewModel() {
    private val mutableState = MutableStateFlow(MessageState())
    val state = mutableState.asStateFlow()
    fun help() { mutableState.value = MessageState(Res.string.ui_pravyla_pryvatnosti_zalezhat_vid_kontekstu_poyizdky_ta_polityky) }
    fun perform(action: PersonalDataAction) {
        if (state.value.isSubmitting) return
        val notice = if (action == PersonalDataAction.Export)
            Res.string.ui_eksport_ne_rozpochato_servis_keruvannya_danymy_ne_pidklyucheno
        else Res.string.ui_istoriyu_ne_vydaleno_servis_keruvannya_danymy_ne_pidklyucheno
        mutableState.value = MessageState(isSubmitting = true)
        viewModelScope.launch {
            try { manageData(action) }
            finally { mutableState.value = MessageState(notice) }
        }
    }
}

data class OrganizationSettingsUiState(
    val preferences: Preferences,
    val workContext: Boolean,
    val message: StringResource? = null,
    val isSubmitting: Boolean = false,
)
class OrganizationSettingsViewModel(
    private val preferences: PreferencesRepository,
    session: SessionRepository,
    private val contact: ContactOrganization,
) : ViewModel() {
    private val local = MutableStateFlow(MessageState())
    val state = combine(preferences.state, session.state, local) { settings, shared, ui ->
        OrganizationSettingsUiState(settings, shared.context == DrivingContext.Work && shared.organizationMember, ui.message, ui.isSubmitting)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000),
        OrganizationSettingsUiState(preferences.state.value, session.state.value.context == DrivingContext.Work && session.state.value.organizationMember))
    fun dimensions() = notice(Res.string.ui_parametry_tekhniky_zminyuye_administrator_avtoparku)
    fun roads() = notice(Res.string.ui_dozvoleni_dorohy_vyznachayutsya_politykoyu_orhanizatsiyi)
    fun speed() = notice(Res.string.ui_limit_shvydkosti_zadaye_administrator_avtoparku)
    fun toggleNotifications() {
        preferences.update { it.copy(dispatchNotifications = !it.dispatchNotifications) }
        local.value = MessageState()
    }
    fun toggleVoice() {
        preferences.update { it.copy(voiceEnabled = !it.voiceEnabled) }
        notice(if (preferences.state.value.voiceEnabled) Res.string.ui_holosovi_pidkazky_vvimkneno_lokalno
            else Res.string.ui_holosovi_pidkazky_vymkneno)
    }
    fun perform(action: OrganizationAction) {
        if (local.value.isSubmitting) return
        val notice = if (action == OrganizationAction.MessageDispatcher)
            Res.string.ui_napysaty_dyspetcheru_ne_vdalosya_kanal_zv_yazku_shche_ne_pidklyu
        else Res.string.ui_zapyt_ne_nadislano_servis_orhanizatsiyi_shche_ne_pidklyucheno
        local.value = MessageState(isSubmitting = true)
        viewModelScope.launch {
            try { contact(action) }
            finally { local.value = MessageState(notice) }
        }
    }
    private fun notice(resource: StringResource) { local.value = MessageState(resource) }
}

enum class DiagnosticsEffect { OpenDeviceSettings }
data class DiagnosticsUiState(val message: StringResource? = null, val showHelp: Boolean = false)
class DeviceDiagnosticsViewModel : ViewModel() {
    private val mutableState = MutableStateFlow(DiagnosticsUiState())
    val state = mutableState.asStateFlow()
    private val channel = Channel<DiagnosticsEffect>(Channel.BUFFERED)
    val effects = channel.receiveAsFlow()
    fun openSettings() { channel.trySend(DiagnosticsEffect.OpenDeviceSettings) }
    fun settingsResult(opened: Boolean) { mutableState.update { it.copy(message = if (opened) null
        else Res.string.ui_ne_vdalosya_vidkryty_nalashtuvannya_prystroyu_vidkryyte_nalashtu, showHelp = false) } }
    fun help() { mutableState.value = DiagnosticsUiState(showHelp = true) }
    fun voice() { mutableState.value = DiagnosticsUiState(Res.string.ui_holosovi_pidkazky_ne_vidtvoryuyutsya_servis_holosovoyi_navihatsi) }
    override fun onCleared() { channel.close() }
}
