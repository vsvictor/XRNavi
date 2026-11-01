import XCTest

final class AuthenticationGateUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10))
    }

    func testUnauthenticatedLaunchHasNoGuestOrMapEntry() {
        XCTAssertTrue(app.staticTexts["З поверненням"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.buttons["Увійти"].exists)
        XCTAssertFalse(element(label: "Продовжити без входу").exists)
        XCTAssertFalse(element(label: "Демо-профіль").exists)
        XCTAssertFalse(element(label: "Вибрати адресу призначення").exists)
    }

    func testUnavailableSignInDoesNotOpenMap() {
        XCTAssertTrue(app.buttons["Увійти"].waitForExistence(timeout: 10))
        app.buttons["Увійти"].tap()

        XCTAssertTrue(app.staticTexts["Авторизація недоступна"].waitForExistence(timeout: 5))
        XCTAssertFalse(element(label: "Вибрати адресу призначення").exists)
    }

    private func element(label: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", label))
            .firstMatch
    }
}
