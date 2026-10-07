package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryPreferencesRepository : PreferencesRepository {
    private val mutableState = MutableStateFlow(Preferences())
    override val state = mutableState.asStateFlow()
    override fun update(transform: (Preferences) -> Preferences) = mutableState.update(transform)
}
class UnconfiguredPersonalDataRepository : PersonalDataRepository {
    override suspend fun perform(action: PersonalDataAction) = UnavailableResult.NotConfigured
}
class UnconfiguredOrganizationRepository : OrganizationRepository {
    override suspend fun perform(action: OrganizationAction) = UnavailableResult.NotConfigured
}
