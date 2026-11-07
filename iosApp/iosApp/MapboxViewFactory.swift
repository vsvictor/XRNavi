import UIKit
import MapboxMaps
import Shared

final class MapboxViewFactory: IosMapViewFactory {
    func create(
        camera: MapCamera,
        status: @escaping (KotlinInt, MapLoadStatus) -> Void,
        cameraChanged: @escaping (MapCamera) -> Void
    ) -> IosMapView {
        MapboxNativeView(camera: camera, status: status, cameraChanged: cameraChanged)
    }
}

private final class MapboxNativeView: IosMapView {
    let mapView: MapView
    var view: UIView { mapView }
    private var revision: Int32 = -1
    private var commandId: Int32?
    private var subscriptions: [AnyCancelable] = []
    private var active = true
    private let status: (KotlinInt, MapLoadStatus) -> Void

    init(
        camera: MapCamera,
        status: @escaping (KotlinInt, MapLoadStatus) -> Void,
        cameraChanged: @escaping (MapCamera) -> Void
    ) {
        self.status = status
        mapView = MapView(frame: .zero, mapInitOptions: MapInitOptions(
            cameraOptions: CameraOptions(
                center: CLLocationCoordinate2D(latitude: camera.latitude, longitude: camera.longitude),
                zoom: camera.zoom, bearing: camera.bearing, pitch: camera.pitch
            )
        ))
        subscriptions.append(mapView.mapboxMap.onMapLoaded.observe { [weak self] _ in
            guard let self, self.active else { return }
            self.status(KotlinInt(int: self.revision), .ready)
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

    func update(dark: Bool, revision: Int32, command: MapCommand, commandId: Int32) {
        guard active else { return }
        if self.revision != revision {
            self.revision = revision
            mapView.mapboxMap.loadStyle(dark ? .dark : .light)
        }
        if let previous = self.commandId, previous != commandId {
            let zoom = mapView.mapboxMap.cameraState.zoom
            if command.name == "ZoomIn" {
                mapView.mapboxMap.setCamera(to: CameraOptions(zoom: min(22, zoom + 1)))
            } else if command.name == "ZoomOut" {
                mapView.mapboxMap.setCamera(to: CameraOptions(zoom: max(0, zoom - 1)))
            } else if command.name == "Recenter" {
                mapView.mapboxMap.setCamera(to: CameraOptions(
                    center: CLLocationCoordinate2D(latitude: 50.4501, longitude: 30.5234),
                    zoom: 11, bearing: 0, pitch: 0
                ))
            }
        }
        self.commandId = commandId
    }

    func dispose() {
        active = false
        subscriptions.forEach { $0.cancel() }
        subscriptions.removeAll()
        mapView.removeFromSuperview()
    }
}
