package com.mobilespace.xrnavi

import com.mobilespace.xrnavi.presentation.auth.SignInBloc
import com.mobilespace.xrnavi.presentation.auth.SignInEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class SignInBlocTest {
    @Test
    fun emailAndPasswordChangesAreRepresentedInState() {
        val bloc = SignInBloc()
        bloc.dispatch(SignInEvent.EmailChanged("driver@example.com"))
        bloc.dispatch(SignInEvent.PasswordChanged("secret123"))

        assertEquals("driver@example.com", bloc.state.value.email)
        assertEquals("secret123", bloc.state.value.password)
    }

    @Test
    fun clearingPasswordPreservesEmailAcrossAuthenticationTransitions() {
        val bloc = SignInBloc()
        bloc.dispatch(SignInEvent.EmailChanged("driver@example.com"))
        bloc.dispatch(SignInEvent.PasswordChanged("secret123"))
        bloc.dispatch(SignInEvent.ClearPassword)

        assertEquals("driver@example.com", bloc.state.value.email)
        assertEquals("", bloc.state.value.password)
    }
}
