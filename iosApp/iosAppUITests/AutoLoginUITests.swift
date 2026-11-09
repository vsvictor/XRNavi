import XCTest

final class AutoLoginUITests: XCTestCase {
    func testStoredSessionSignsInAutomaticallyAfterRelaunch() throws {
        let env = ProcessInfo.processInfo.environment
        guard let email = env["XRNAVI_TEST_EMAIL"], let password = env["XRNAVI_TEST_PASSWORD"] else {
            throw XCTSkip("Set TEST_RUNNER_XRNAVI_TEST_EMAIL and TEST_RUNNER_XRNAVI_TEST_PASSWORD.")
        }
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launch()
        let emailField = app.textViews.element(boundBy: 0)
        if emailField.waitForExistence(timeout: 10) {
            emailField.tap()
            emailField.typeText(email)
            let passwordField = app.textViews.element(boundBy: 1)
            passwordField.tap()
            passwordField.typeText(password)
            app.buttons["Увійти"].firstMatch.tap()
        }
        XCTAssertTrue(map(app).waitForExistence(timeout: 20), "login did not reach the map")
        app.terminate()
        app.launch()
        XCTAssertTrue(map(app).waitForExistence(timeout: 20), "stored session did not sign in automatically")
        let shot = XCTAttachment(screenshot: app.screenshot())
        shot.lifetime = .keepAlways
        add(shot)
    }

    private func map(_ app: XCUIApplication) -> XCUIElement {
        app.descendants(matching: .any).matching(NSPredicate(format: "label CONTAINS %@", "Центр мапи")).firstMatch
    }
}
