import XCTest

/// Live password-recovery flow. The server is not required: failures are shown as a notice.
final class RecoveryFlowUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(
            app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Забули пароль?")).firstMatch
                .waitForExistence(timeout: 20) || label("Код із листа").exists
        )
    }

    override func tearDownWithError() throws {
        XCUIDevice.shared.press(.home)
    }

    func testRecoveryRequestTokenAndNewPasswordForms() {
        button("Забули пароль?").tap()
        XCTAssertTrue(label("Відновлення пароля").waitForExistence(timeout: 5))
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

    func testRotationScenarioA() { runRotation(returnOrientation: .portrait) }

    func testRotationScenarioB() { runRotation(returnOrientation: .landscapeLeft) }

    func testNewPasswordRotationScenarioA() { runRotation(returnOrientation: .portrait, newPassword: true) }

    func testNewPasswordRotationScenarioB() { runRotation(returnOrientation: .landscapeLeft, newPassword: true) }

    private func runRotation(returnOrientation: UIDeviceOrientation, newPassword: Bool = false) {
        let device = XCUIDevice.shared
        let initial = device.orientation
        defer { device.orientation = initial }
        device.orientation = .landscapeLeft
        sleep(2)
        if !label("Код із листа").exists {
            button("Забули пароль?").tap()
            let email = app.textViews.element(boundBy: 0)
            email.tap()
            email.typeText("rotation@example.com")
            dismissKeyboard()
            if !label("Код із листа").waitForExistence(timeout: 8) { button("Надіслати код").tap() }
        }
        XCTAssertTrue(label("Код із листа").waitForExistence(timeout: 20), app.debugDescription)
        if newPassword {
            let code = app.textViews.matching(NSPredicate(format: "label CONTAINS[c] %@", "Код із листа")).firstMatch
            code.tap()
            code.typeText("123456")
            dismissKeyboard()
            if !label("Зберегти новий пароль").waitForExistence(timeout: 5) { button("Продовжити").tap() }
            XCTAssertTrue(button("Зберегти новий пароль").waitForExistence(timeout: 10), app.debugDescription)
        }
        func check(_ name: String, landscape: Bool) {
            sleep(3)
            let window = app.windows.firstMatch.frame
            let field = newPassword
                ? app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Зберегти новий пароль")).firstMatch
                : app.textViews.matching(NSPredicate(format: "label CONTAINS[c] %@", "Код із листа")).firstMatch
            let attachment = XCTAttachment(string: "\(name) window=\(window) field=\(field.frame) landscape=\(landscape)")
            attachment.name = name; attachment.lifetime = .keepAlways; add(attachment)
            let shot = XCTAttachment(screenshot: app.screenshot())
            shot.name = "\(name) shot"; shot.lifetime = .keepAlways; add(shot)
            XCTAssertTrue(field.exists, name)
            if landscape { XCTAssertGreaterThan(field.frame.minX, window.width / 3, name) }
            else { XCTAssertLessThan(field.frame.minX, window.width / 3, name) }
        }
        check("1-landscape", landscape: true)
        device.press(.home)
        device.orientation = .portrait
        sleep(2)
        if returnOrientation == .landscapeLeft { device.orientation = .landscapeLeft; sleep(1) }
        app.activate()
        let backLandscape = returnOrientation == .landscapeLeft
        check("2-back", landscape: backLandscape)
        device.orientation = backLandscape ? .portrait : .landscapeLeft
        check("3-rotated", landscape: !backLandscape)
        device.orientation = .landscapeLeft
        check("4-landscape-final", landscape: true)
    }

    func testRequestTokenForConfiguredAccount() throws {
        guard let address = ProcessInfo.processInfo.environment["XRNAVI_TEST_EMAIL"] else {
            throw XCTSkip("Set TEST_RUNNER_XRNAVI_TEST_EMAIL to request a real reset token.")
        }
        button("Забули пароль?").tap()
        XCTAssertTrue(label("Відновлення пароля").waitForExistence(timeout: 5))
        let email = app.textViews.element(boundBy: 0)
        email.tap()
        email.typeText(address)
        button("Надіслати токен").tap()
        XCTAssertTrue(label("Токен відновлення").waitForExistence(timeout: 30), app.debugDescription)
        sleep(20)
    }

    private func dismissKeyboard() {
        let done = app.keyboards.buttons.matching(NSPredicate(format: "identifier ==[c] 'Done' OR label ==[c] 'done'")).firstMatch
        if done.waitForExistence(timeout: 3) {
            done.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        }
        sleep(2)
    }

    private func button(_ text: String) -> XCUIElement {
        let target = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", text)).firstMatch
        XCTAssertTrue(target.waitForExistence(timeout: 10), "no button: \(text)")
        return target
    }

    private func label(_ text: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS[c] %@", text)).firstMatch
    }
}
