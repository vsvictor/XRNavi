package com.mobilespace.xrnavi

class AppDependencies(
    val mapFactory: PlatformMapFactory = PlatformMapFactory(),
    mapboxToken: () -> String = { "" },
) {
    internal val routePlanningService: RoutePlanningService = MapboxRoutePlanningService(mapboxToken)
    internal val connectionBloc = ConnectionBloc()
    internal val mobileSpaceRpcClient = MobileSpaceRpcClient(connectionBloc)
    internal val signInAuthService: SignInAuthService = RpcSignInAuthService { method, params ->
        val client = mobileSpaceRpcClient.awaitClient()
        client.setRequestTimeout(signInTimeoutMillis.toULong())
        client.call(method, params)
    }
    internal val authenticatedProfileService: AuthenticatedProfileService =
        RpcAuthenticatedProfileService { method, params ->
            val client = mobileSpaceRpcClient.awaitClient()
            client.setRequestTimeout(signInTimeoutMillis.toULong())
            client.call(method, params)
        }
}
