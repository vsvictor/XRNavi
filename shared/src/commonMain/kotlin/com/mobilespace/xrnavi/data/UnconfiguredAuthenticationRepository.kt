package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.*

class UnconfiguredAuthenticationRepository : AuthenticationRepository {
    override suspend fun signIn(credentials: Credentials) = AuthenticationResult.NotConfigured
    override suspend fun register(registration: Registration) = AuthenticationResult.NotConfigured
}
