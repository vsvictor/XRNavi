package com.mobilespace.xrnavi

import com.exkmptracker.ports.TrackingRpcTransport
import com.exkmptracker.transport.RpcTrackingTransport
import kotlinx.coroutines.*
import kotlinx.serialization.json.*

class AppDependencies(
    val mapFactory: PlatformMapFactory = PlatformMapFactory(),
    mapboxToken: () -> String = { "" },
    locationProvider: LocationProvider = UnavailableLocationProvider(),
    sessionStorage: SecureSessionStorage = MemorySecureSessionStorage(),
    deviceIdStorage: SecureSessionStorage = MemorySecureSessionStorage(),
    vehiclePhotoStorage: SecureSessionStorage = MemorySecureSessionStorage(),
    trackingRuntimeFactory: (RpcTrackingTransport) -> AppTrackingRuntime = { UnavailableTrackingRuntime() },
) {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val installationId = StoredDeviceIdProvider(deviceIdStorage)
    internal val routePlanningService: RoutePlanningService = MapboxRoutePlanningService(mapboxToken)
    internal val vehiclePhotoService: VehiclePhotoService = WikimediaVehiclePhotoService()
    internal val vehiclePhotoSelections = VehiclePhotoSelections(vehiclePhotoStorage)
    internal val connectionBloc = ConnectionBloc()
    internal val mobileSpaceRpcClient = MobileSpaceRpcClient(connectionBloc)
    private val rawCall: suspend (String, String) -> String = { method, params ->
        val client = mobileSpaceRpcClient.awaitClient()
        client.setRequestTimeout(signInTimeoutMillis.toULong())
        client.call(method, params)
    }
    private val rpcSignIn = RpcSignInAuthService(
        deviceIdProvider = installationId,
        call = rawCall,
    )
    internal val signInAuthService: SignInAuthService = rpcSignIn
    internal val sessionManager = SessionManager(sessionStorage, rpcSignIn)
    private val authorizedRpc = AuthorizedRpc(sessionManager, rawCall)
    private val authorizedTrackingRpc = AuthorizedRpc(sessionManager) { method, params ->
        val client = mobileSpaceRpcClient.awaitQuicClient()
        client.call(method, params)
    }
    val trackingTransport = RpcTrackingTransport(object : TrackingRpcTransport {
        override suspend fun call(method: String, params: JsonObject): JsonObject =
            Json.parseToJsonElement(authorizedTrackingRpc.call(method, params.toString())) as? JsonObject
                ?: error("Tracking RPC result must be an object")

        override suspend fun notify(method: String, params: JsonObject): Unit =
            error("Tracking requires acknowledged RPC calls")
    })
    private val trackingRuntime = trackingRuntimeFactory(trackingTransport)
    internal val trackerBloc = TrackerIntegrationBloc(applicationScope, sessionManager.trackingGeneration,
        installationId, RpcTrackerRegistrationService(
            refreshSession = { sessionManager.refreshNow() },
            call = authorizedTrackingRpc::call,
        ), trackingRuntime)
    private val trackerLocationBloc = TrackerLocationBloc(applicationScope, trackerBloc.state, locationProvider)
    val locationProvider: LocationProvider = TrackingLocationProvider(locationProvider, trackerLocationBloc)
    init {
        mapFactory.locations = trackerLocationBloc.state
    }
    internal val registrationService: RegistrationService = RpcRegistrationService(rawCall)
    internal val recoveryService: RecoveryService = RpcRecoveryService(authorizedRpc::call)
    internal val authenticatedProfileService: AuthenticatedProfileService =
        RpcAuthenticatedProfileService(authorizedRpc::call)
    internal val callbackService: CallbackService =
        RpcCallbackService(mobileSpaceRpcClient.clients, authorizedRpc::call)
    internal val garageService: GarageService = RpcGarageService(authorizedRpc::call)
}
