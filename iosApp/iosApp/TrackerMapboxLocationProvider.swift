import Foundation
import Combine
import MapboxMaps
import MapboxCommon
import Shared

final class TrackerMapboxLocationProvider {
    private let locations = CurrentValueSubject<[Location], Never>([])
    private var fix: DeviceFix?

    var publisher: AnyPublisher<[Location], Never> {
        locations.eraseToAnyPublisher()
    }

    func update(_ fix: DeviceFix?) {
        guard self.fix != fix else { return }
        self.fix = fix
        locations.send(fix.map {
            [Location(
                coordinate: CLLocationCoordinate2D(latitude: $0.point.latitude, longitude: $0.point.longitude),
                timestamp: Date(timeIntervalSince1970: Double($0.timestampMillis) / 1000),
                horizontalAccuracy: $0.accuracyMeters?.doubleValue,
                speed: $0.speedMps?.doubleValue,
                bearing: $0.bearingDegrees?.doubleValue,
                source: "ExKMPTracker"
            )]
        } ?? [])
    }
}
