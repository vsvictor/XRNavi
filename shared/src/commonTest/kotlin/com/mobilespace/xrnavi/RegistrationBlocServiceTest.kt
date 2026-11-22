package com.mobilespace.xrnavi

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.Json
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class RegistrationBlocServiceTest {
    private class FakeService(var failure: Exception? = null) : RegistrationService {
        val sent = mutableListOf<RegistrationState>()
        override suspend fun register(state: RegistrationState) {
            failure?.let { throw it }
            sent += state
        }
    }

    private fun filled(service: FakeService): RegistrationBloc {
        val bloc = RegistrationBloc(service, TestScope(UnconfinedTestDispatcher()))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.FirstName, "Ада"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.LastName, "Лавлейс"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Email, "ada@example.com"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Password, "abcdefgh"))
        bloc.dispatch(RegistrationEvent.FieldChanged(RegistrationField.Confirmation, "abcdefgh"))
        bloc.dispatch(RegistrationEvent.ConsentChanged(true))
        return bloc
    }

    @Test
    fun successSendsRequestAndClearsSecrets() {
        val service = FakeService()
        val bloc = filled(service)
        bloc.dispatch(RegistrationEvent.Submitted)
        assertEquals(1, service.sent.size)
        assertEquals("abcdefgh", service.sent.single().password)
        assertTrue(bloc.state.value.registered)
        assertFalse(bloc.state.value.busy)
        assertEquals("", bloc.state.value.password)
    }

    @Test
    fun failureKeepsFormAndShowsMessage() {
        val bloc = filled(FakeService(RuntimeException("-32602")))
        bloc.dispatch(RegistrationEvent.Submitted)
        assertFalse(bloc.state.value.registered)
        assertFalse(bloc.state.value.busy)
        assertEquals(registrationRejectedMessage, bloc.state.value.notice)
        assertEquals("abcdefgh", bloc.state.value.password)
    }

    @Test
    fun handledResetsRegisteredFlag() {
        val bloc = filled(FakeService())
        bloc.dispatch(RegistrationEvent.Submitted)
        bloc.dispatch(RegistrationEvent.RegisteredHandled)
        assertFalse(bloc.state.value.registered)
    }

    @Test
    fun requestJsonHasProfileAndAccountType() {
        val bloc = filled(FakeService())
        val json = Json.parseToJsonElement(bloc.state.value.registerJson().toString()).jsonObject
        val profile = json.getValue("profile").jsonObject
        assertEquals("Ада", profile.getValue("first_name").jsonPrimitive.content)
        assertEquals("private", profile.getValue("account_type").jsonPrimitive.content)
        assertFalse("middle_name" in profile)
    }
}
