import XCTest

final class MapHeaderLayoutUITests: XCTestCase {
    func testMapHeaderIsBelowStatusBarAndAboveAddresses() throws {
        let app = XCUIApplication()
        app.launch()

        let logo = app.staticTexts["XRNavi"].firstMatch
        let origin = app.buttons["route-origin"].firstMatch
        XCTAssertTrue(origin.waitForExistence(timeout: 30), "The authenticated map must be open.")
        XCTAssertTrue(logo.waitForExistence(timeout: 5))

        let safeArea = app.descendants(matching: .any)["connection_indicator_safe_area"].firstMatch
        let symbol = app.images["Знак Mobile Space 01 Orbit"].firstMatch
        XCTAssertTrue(safeArea.exists)
        XCTAssertTrue(symbol.exists)
        XCTAssertGreaterThan(safeArea.frame.minY, 0)
        XCTAssertGreaterThanOrEqual(logo.frame.minY, safeArea.frame.minY)
        XCTAssertEqual(symbol.frame.minY - safeArea.frame.minY, 0, accuracy: 1)
        XCTAssertGreaterThanOrEqual(origin.frame.minY, logo.frame.maxY)
        XCTAssertGreaterThanOrEqual(origin.frame.minY, symbol.frame.maxY)

        defer { app.staticTexts["Мапа"].firstMatch.tap() }
        app.staticTexts["Гараж"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["authenticated-garage"].firstMatch.waitForExistence(timeout: 10))
        XCTAssertEqual(symbol.frame.minY - safeArea.frame.minY, 0, accuracy: 1)

        app.staticTexts["Налаштування"].firstMatch.tap()
        XCTAssertTrue(app.staticTexts["Тема застосунку · на цьому пристрої"].firstMatch.waitForExistence(timeout: 10))
        XCTAssertEqual(symbol.frame.minY - safeArea.frame.minY, 0, accuracy: 1)

        app.staticTexts["Профіль"].firstMatch.tap()
        XCTAssertTrue(app.descendants(matching: .any)["authenticated-profile"].firstMatch.waitForExistence(timeout: 20))
        XCTAssertEqual(symbol.frame.minY - safeArea.frame.minY, 0, accuracy: 1)
    }
}
