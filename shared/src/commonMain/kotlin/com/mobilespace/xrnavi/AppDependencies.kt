package com.mobilespace.xrnavi

class AppDependencies {
    internal val connectionBloc = ConnectionBloc()
    internal val mobileSpaceRpcClient = MobileSpaceRpcClient(connectionBloc)
    internal val signInAuthService: SignInAuthService = RpcSignInAuthService { method, params ->
        val client = mobileSpaceRpcClient.awaitClient()
        client.setRequestTimeout(signInTimeoutMillis.toULong())
        client.call(method, params)
    }
}
