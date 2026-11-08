import XCTest

/// Live password-recovery flow. The server is not required: failures are shown as a notice.
final class RecoveryFlowUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(label("З поверненням").waitForExistence(timeout: 20))
    }

    override func tearDownWithError() throws {
        XCUIDevice.shared.press(.home)
    }

    func testRecoveryRequestTokenAndNewPasswordForms() {
        button("Забули пароль?").tap()
        XCTAssertTrue(label("Відновити пароль").waitForExistence(timeout: 5))
        XCTAssertTrue(label("Вкажіть електронну пошту акаунта").waitForExistence(timeout: 5))

        let email = app.textViews.element(boundBy: 0)
        email.tap()
        email.typeText("recovery@example.com")
        button("Надіслати токен").tap()
        let outcome = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            self.label("Не вдалося зв’язатися із сервером").exists || self.label("Перевірте пошту").exists
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [outcome], timeout: 20), .completed, app.debugDescription)

        if !label("Токен відновлення").exists {
            button("Внутрішній preview S41–S45 · debug").tap()
            button("S42 · Перевірте пошту").tap()
        }
        XCTAssertTrue(label("Токен відновлення").waitForExistence(timeout: 5))
        let token = app.textViews.element(boundBy: 0)
        token.tap()
        token.typeText("v4.local.dummy")
        button("Продовжити").tap()

        XCTAssertTrue(label("Введіть новий пароль для акаунта").waitForExistence(timeout: 5))
        let password = app.textViews.element(boundBy: 0)
        password.tap()
        password.typeText("Str0ngPass99")
        let confirmation = app.textViews.element(boundBy: 1)
        confirmation.tap()
        confirmation.typeText("Str0ngPass99")
        button("Зберегти новий пароль").tap()
        XCTAssertTrue(
            label("Не вдалося зв’язатися із сервером").waitForExistence(timeout: 20) ||
                label("Пароль змінено").exists || label("Токен недійсний").exists,
            app.debugDescription
        )
    }

    func testRequestTokenForConfiguredAccount() throws {
        guard let address = ProcessInfo.processInfo.environment["XRNAVI_TEST_EMAIL"] else {
            throw XCTSkip("Set TEST_RUNNER_XRNAVI_TEST_EMAIL to request a real reset token.")
        }
        button("Забули пароль?").tap()
        XCTAssertTrue(label("Відновити пароль").waitForExistence(timeout: 5))
        let email = app.textViews.element(boundBy: 0)
        email.tap()
        email.typeText(address)
        button("Надіслати токен").tap()
        XCTAssertTrue(label("Токен відновлення").waitForExistence(timeout: 30), app.debugDescription)
        sleep(20)
    }

    private func button(_ text: String) -> XCUIElement {
        let target = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", text)).firstMatch
        XCTAssertTrue(target.waitForExistence(timeout: 10), app.debugDescription)
        return target
    }

    private func label(_ text: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", text)).firstMatch
    }
}
