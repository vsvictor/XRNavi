import XCTest

/// Live registration flow against the real server. Requires TEST_RUNNER_XRNAVI_REG_EMAIL and
/// TEST_RUNNER_XRNAVI_REG_PASSWORD; it creates a real account, so use a throwaway address.
final class RegistrationFlowUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    override func tearDownWithError() throws {
        XCUIDevice.shared.press(.home)
    }

    func testRegisterThenReturnToSignIn() throws {
        let env = ProcessInfo.processInfo.environment
        guard let email = env["XRNAVI_REG_EMAIL"], let password = env["XRNAVI_REG_PASSWORD"] else {
            throw XCTSkip("XRNAVI_REG_EMAIL / XRNAVI_REG_PASSWORD are not set")
        }
        let link = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Зареєструватися")).firstMatch
        guard link.waitForExistence(timeout: 25) else { throw XCTSkip("app is signed in") }
        link.tap()
        XCTAssertTrue(label("Реєстрація").waitForExistence(timeout: 5))

        let values = ["Test", "Check", "Mid", email, password, password]
        let first = app.textViews.element(boundBy: 0)
        XCTAssertTrue(first.waitForExistence(timeout: 5))
        first.coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0.5)).tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), "keyboard")
        // Return moves focus to the next field (Done on the last one), as the IME actions define.
        for value in values {
            app.typeText(value + "\n")
            Thread.sleep(forTimeInterval: 0.7)
        }
        Thread.sleep(forTimeInterval: 1)
        let consent = app.buttons.matching(NSPredicate(format: "label BEGINSWITH %@", "Згода з Умовами")).firstMatch
        if !consent.isHittable { app.swipeUp() }
        consent.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        let submit = app.buttons.matching(NSPredicate(format: "label == %@", "Реєстрація")).firstMatch
        if !submit.isHittable { app.swipeUp() }
        Thread.sleep(forTimeInterval: 1)
        submit.tap()

        let forgot = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Забули пароль?")).firstMatch
        XCTAssertTrue(forgot.waitForExistence(timeout: 25), app.debugDescription)

        // Sign in with the new account; Done on the password field submits the form.
        app.textViews.element(boundBy: 0).coordinate(withNormalizedOffset: CGVector(dx: 0.3, dy: 0.5)).tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), "keyboard for sign-in")
        app.typeText(email + "\n")
        Thread.sleep(forTimeInterval: 0.7)
        app.typeText(password + "\n")
        let gone = NSPredicate(format: "exists == false")
        expectation(for: gone, evaluatedWith: forgot)
        waitForExpectations(timeout: 30)
    }

    private func label(_ text: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS[c] %@", text)).firstMatch
    }
}
