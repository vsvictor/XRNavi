package com.mobilespace.xrnavi.domain

import kotlinx.coroutines.flow.StateFlow

data class Preferences(
    val voiceEnabled: Boolean = true,
    val xrEnabled: Boolean = false,
    val dispatchNotifications: Boolean = true,
)
interface PreferencesRepository {
    val state: StateFlow<Preferences>
    fun update(transform: (Preferences) -> Preferences)
}
enum class PersonalDataAction { Export, DeleteHistory }
enum class OrganizationAction { MessageDispatcher, RequestPolicyChange }
enum class UnavailableResult { NotConfigured }
interface PersonalDataRepository {
    suspend fun perform(action: PersonalDataAction): UnavailableResult
}
interface OrganizationRepository {
    suspend fun perform(action: OrganizationAction): UnavailableResult
}
class ManagePersonalData(private val repository: PersonalDataRepository) {
    suspend operator fun invoke(action: PersonalDataAction) = repository.perform(action)
}
class ContactOrganization(private val repository: OrganizationRepository) {
    suspend operator fun invoke(action: OrganizationAction) = repository.perform(action)
}
