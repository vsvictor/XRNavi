import UIKit
import Combine
import MapboxMaps
import Shared

final class MapboxViewFactory: IosMapViewFactory {
    func create(
        camera: MapCamera,
        status: @escaping (KotlinInt, MapLoadStatus) -> Void,
        cameraChanged: @escaping (MapCamera) -> Void,
        initialRecenterHandled: @escaping (KotlinInt) -> Void
    ) -> IosMapView {
        MapboxNativeView(camera: camera, status: status, cameraChanged: cameraChanged, initialRecenterHandled: initialRecenterHandled)
    }
}

private final class MapboxNativeView: IosMapView {
    let mapView: MapView
    var view: UIView { mapView }
    private var revision: Int32 = -1
    private var commandId: Int32?
    private var subscriptions: [AnyCancelable] = []
    private var active = true
    private var styleReady = false
    private var mapLoaded = false
    private var pendingInitialRecenter: (id: Int32, target: GeoPoint)?
    private var overlay = MapOverlay(route: [], origin: nil, destination: nil, user: nil, location: nil)
    private let trackerLocations = TrackerMapboxLocationProvider()
    private var fittedRoute: [GeoPoint] = []
    private let status: (KotlinInt, MapLoadStatus) -> Void
    private let initialRecenterHandled: (KotlinInt) -> Void

    init(
        camera: MapCamera,
        status: @escaping (KotlinInt, MapLoadStatus) -> Void,
        cameraChanged: @escaping (MapCamera) -> Void,
        initialRecenterHandled: @escaping (KotlinInt) -> Void
    ) {
        self.status = status
        self.initialRecenterHandled = initialRecenterHandled
        mapView = MapView(frame: .zero, mapInitOptions: MapInitOptions(
            cameraOptions: CameraOptions(
                center: CLLocationCoordinate2D(latitude: camera.latitude, longitude: camera.longitude),
                zoom: camera.zoom, bearing: camera.bearing, pitch: camera.pitch
            )
        ))
        var ornamentOptions = mapView.ornaments.options
        ornamentOptions.scaleBar.visibility = .hidden
        mapView.ornaments.options = ornamentOptions
        mapView.location.dataModel = LocationDataModel(
            location: trackerLocations.publisher,
            heading: Empty().eraseToAnyPublisher()
        )
        mapView.location.options = LocationOptions(puckBearing: .course)
        subscriptions.append(mapView.mapboxMap.onMapLoaded.observe { [weak self] _ in
            guard let self, self.active else { return }
            self.mapLoaded = true
            self.runPendingInitialRecenter()
            self.status(KotlinInt(int: self.revision), .ready)
        })
        subscriptions.append(mapView.mapboxMap.onStyleLoaded.observe { [weak self] _ in
            guard let self, self.active else { return }
            self.styleReady = true
            self.drawOverlay()
        })
        subscriptions.append(mapView.mapboxMap.onMapLoadingError.observe { [weak self] _ in
            guard let self, self.active else { return }
            self.status(KotlinInt(int: self.revision), .failed)
        })
        subscriptions.append(mapView.mapboxMap.onCameraChanged.observe { [weak self] _ in
            guard let self, self.active else { return }
            let camera = self.mapView.mapboxMap.cameraState
            cameraChanged(MapCamera(
                latitude: camera.center.latitude, longitude: camera.center.longitude,
                zoom: camera.zoom, bearing: camera.bearing, pitch: camera.pitch
            ))
        })
    }

    func update(
        dark: Bool, satellite: Bool, revision: Int32, command: MapCommand, commandId: Int32,
        initialRecenterTarget: GeoPoint?, overlay: MapOverlay
    ) {
        guard active else { return }
        if self.overlay.location != overlay.location {
            trackerLocations.update(overlay.location)
            if overlay.location != nil {
                var puck = Puck2DConfiguration.makeDefault(showBearing: overlay.location?.bearingDegrees != nil)
                puck.showsAccuracyRing = overlay.location?.accuracyMeters != nil
                mapView.location.options.puckType = .puck2D(puck)
            } else {
                mapView.location.options.puckType = nil
            }
            mapView.location.options.puckBearingEnabled = overlay.location?.bearingDegrees != nil
        }
        if self.revision != revision {
            self.revision = revision
            styleReady = false
            mapLoaded = false
            mapView.mapboxMap.loadStyle(satellite ? .satelliteStreets : (dark ? .dark : .light))
        }
        if self.overlay != overlay {
            let geometryChanged = self.overlay.route != overlay.route ||
                self.overlay.origin != overlay.origin || self.overlay.destination != overlay.destination
            self.overlay = overlay
            if geometryChanged { drawOverlay() }
        }
        if self.commandId != commandId && (self.commandId != nil || command.name == "InitialRecenter") {
            self.commandId = commandId
            pendingInitialRecenter = nil
            let zoom = mapView.mapboxMap.cameraState.zoom
            if command.name == "ZoomIn" {
                mapView.mapboxMap.setCamera(to: CameraOptions(zoom: min(22, zoom + 1)))
            } else if command.name == "ZoomOut" {
                mapView.mapboxMap.setCamera(to: CameraOptions(zoom: max(0, zoom - 1)))
            } else if command.name == "Recenter" {
                let target = overlay.user
                mapView.mapboxMap.setCamera(to: CameraOptions(
                    center: CLLocationCoordinate2D(
                        latitude: target?.latitude ?? 50.4501, longitude: target?.longitude ?? 30.5234
                    ),
                    zoom: target == nil ? 11 : 14, bearing: 0, pitch: 0
                ))
            } else if command.name == "InitialRecenter" {
                guard let target = initialRecenterTarget else {
                    preconditionFailure("Initial recenter requires a target")
                }
                pendingInitialRecenter = (commandId, target)
                runPendingInitialRecenter()
            }
        }
        self.commandId = commandId
    }

    private func runPendingInitialRecenter() {
        guard mapLoaded, let pending = pendingInitialRecenter else { return }
        pendingInitialRecenter = nil
        mapView.camera.fly(to: CameraOptions(
            center: CLLocationCoordinate2D(latitude: pending.target.latitude, longitude: pending.target.longitude),
            zoom: 14, bearing: 0, pitch: 0
        ), duration: 1.2)
        initialRecenterHandled(KotlinInt(int: pending.id))
    }

    private func drawOverlay() {
        guard let map = mapView.mapboxMap, styleReady || map.isStyleLoaded else { return }
        for id in ["xrnavi-route-casing", "xrnavi-route-line", "xrnavi-pins"] { try? map.removeLayer(withId: id) }
        for id in ["xrnavi-route-source", "xrnavi-pins-source"] { try? map.removeSource(withId: id) }
        let route = overlay.route.map { CLLocationCoordinate2D(latitude: $0.latitude, longitude: $0.longitude) }
        if route.count >= 2 {
            var source = GeoJSONSource(id: "xrnavi-route-source")
            source.data = .geometry(.lineString(LineString(route)))
            try? map.addSource(source)
            var casing = LineLayer(id: "xrnavi-route-casing", source: "xrnavi-route-source")
            casing.lineColor = .constant(StyleColor(.white))
            casing.lineWidth = .constant(9)
            casing.lineCap = .constant(.round)
            casing.lineJoin = .constant(.round)
            try? map.addLayer(casing)
            var line = LineLayer(id: "xrnavi-route-line", source: "xrnavi-route-source")
            line.lineColor = .constant(StyleColor(UIColor(red: 0.18, green: 0.48, blue: 1, alpha: 1)))
            line.lineWidth = .constant(6)
            line.lineCap = .constant(.round)
            line.lineJoin = .constant(.round)
            try? map.addLayer(line)
        }
        var features: [Feature] = []
        if let origin = overlay.origin {
            var feature = Feature(geometry: .point(Point(CLLocationCoordinate2D(latitude: origin.latitude, longitude: origin.longitude))))
            feature.properties = ["pin-color": .string("#FFFFFF")]
            features.append(feature)
        }
        if let destination = overlay.destination {
            var feature = Feature(geometry: .point(Point(CLLocationCoordinate2D(latitude: destination.latitude, longitude: destination.longitude))))
            feature.properties = ["pin-color": .string("#E5484D")]
            features.append(feature)
        }
        if !features.isEmpty {
            var source = GeoJSONSource(id: "xrnavi-pins-source")
            source.data = .featureCollection(FeatureCollection(features: features))
            try? map.addSource(source)
            var pins = CircleLayer(id: "xrnavi-pins", source: "xrnavi-pins-source")
            pins.circleRadius = .constant(8)
            pins.circleColor = .expression(Exp(.toColor) { Exp(.get) { "pin-color" } })
            pins.circleStrokeColor = .constant(StyleColor(UIColor(red: 0.09, green: 0.13, blue: 0.2, alpha: 1)))
            pins.circleStrokeWidth = .constant(3)
            try? map.addLayer(pins)
        }
        if route.count >= 2, overlay.route != fittedRoute {
            fittedRoute = overlay.route
            if let camera = try? map.camera(
                for: route, camera: CameraOptions(),
                coordinatesPadding: UIEdgeInsets(top: 180, left: 48, bottom: 280, right: 48),
                maxZoom: nil, offset: nil
            ) {
                map.setCamera(to: camera)
            }
        } else if route.isEmpty {
            fittedRoute = []
        }
    }

    func dispose() {
        active = false
        trackerLocations.update(nil)
        mapView.location.options.puckType = nil
        subscriptions.forEach { $0.cancel() }
        subscriptions.removeAll()
        mapView.removeFromSuperview()
    }
}
