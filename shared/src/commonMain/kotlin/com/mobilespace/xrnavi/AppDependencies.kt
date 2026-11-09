package com.mobilespace.xrnavi

class AppDependencies(
    val mapFactory: PlatformMapFactory = PlatformMapFactory(),
    mapboxToken: () -> String = { "" },
    val locationProvider: LocationProvider = UnavailableLocationProvider(),
    sessionStorage: SecureSessionStorage = MemorySecureSessionStorage(),
    deviceIdStorage: SecureSessionStorage = MemorySecureSessionStorage(),
) {
    internal val routePlanningService: RoutePlanningService = MapboxRoutePlanningService(mapboxToken)
    internal val connectionBloc = ConnectionBloc()
    internal val mobileSpaceRpcClient = MobileSpaceRpcClient(connectionBloc)
    private val rawCall: suspend (String, String) -> String = { method, params ->
        val client = mobileSpaceRpcClient.awaitClient()
        client.setRequestTimeout(signInTimeoutMillis.toULong())
        client.call(method, params)
    }
    private val rpcSignIn = RpcSignInAuthService(
        deviceIdProvider = StoredDeviceIdProvider(deviceIdStorage),
        call = rawCall,
    )
    internal val signInAuthService: SignInAuthService = rpcSignIn
    internal val sessionManager = SessionManager(sessionStorage, rpcSignIn)
    private val authorizedRpc = AuthorizedRpc(sessionManager, rawCall)
    internal val registrationService: RegistrationService = RpcRegistrationService(rawCall)
    internal val recoveryService: RecoveryService = RpcRecoveryService(authorizedRpc::call)
    internal val authenticatedProfileService: AuthenticatedProfileService =
        RpcAuthenticatedProfileService(authorizedRpc::call)
    internal val callbackService: CallbackService =
        RpcCallbackService(mobileSpaceRpcClient.clients, authorizedRpc::call)
    internal val garageService: GarageService = RpcGarageService(authorizedRpc::call)
}
