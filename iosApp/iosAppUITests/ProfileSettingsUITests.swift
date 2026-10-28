import XCTest
import UIKit

final class ProfileSettingsUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        guard !UIAccessibility.isVoiceOverRunning else {
            throw XCTSkip("This is an ordinary touch check, not a VoiceOver check.")
        }
        XCUIDevice.shared.orientation = .portrait
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 10), app.debugDescription)
        rotate(.portrait)
        attachText(
            "VoiceOver: \(UIAccessibility.isVoiceOverRunning)\n" +
            "Content size: \(UIApplication.shared.preferredContentSizeCategory.rawValue)\n" +
            "Device: \(UIDevice.current.model), \(UIDevice.current.systemVersion)",
            name: "runtime-conditions"
        )
    }

    func testProfileSettingsThemesRotationAndPersistence() {
        tap("Продовжити без входу · деморежим")
        tap("Профіль")
        XCTAssertTrue(find("Демо-профіль").exists)
        XCTAssertFalse(find("Пошту підтверджено").exists)
        tap("Редагувати профіль · не підключено")
        XCTAssertTrue(find("Можливість не підключено").waitForExistence(timeout: 3))
        tap("Зрозуміло")
        tap("Налаштування")

        for (mode, label) in [("light", "Світла"), ("dark", "Темна"), ("system", "Як у системі")] {
            tap(label)
            assertSelected(label)
            app.terminate()
            app.launch()
            XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 10))
            screenshot("signin-restored-\(mode)")
            tap("Продовжити без входу · деморежим")
            tap("Налаштування")
            reveal(label)
            assertSelected(label)
            screenshot("theme-restored-\(mode)")
            if mode != "system" {
                for orientation in [UIDeviceOrientation.portrait, .landscapeLeft] {
                    rotate(orientation)
                    let suffix = orientation == .portrait ? "portrait" : "landscape"
                    scrollTop()
                    screenshot("settings-\(suffix)-\(mode)")
                    tap("Профіль")
                    XCTAssertTrue(find("Демо-профіль").exists)
                    screenshot("profile-\(suffix)-\(mode)")
                    tap("Керувати гаражем · не підключено")
                    screenshot("after-garage-tap-\(suffix)-\(mode)")
                    assertGarageDialog()
                    screenshot("garage-unavailable-\(suffix)-\(mode)")
                    attachText(app.debugDescription, name: "garage-dialog-tree-\(suffix)-\(mode)")
                    tap("Зрозуміло")
                    tap("Налаштування")
                }

                rotate(.portrait)
            }
        }
        tap("XRNavi 1.0 (1) · Mobile space")
        screenshot("settings-bottom-unavailable")
        tap("Профіль")
        tap("Історія поїздок і статистика відсутні; серверні дані не отримувалися.")
        screenshot("profile-empty-statistics")
        tap("Вийти з деморежиму")
        XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 5))
    }

    func testLandscapeProfileGarageScrolling() {
        tap("Продовжити без входу · деморежим")
        for (mode, label) in [("light", "Світла"), ("dark", "Темна")] {
            tap("Налаштування")
            tap(label)
            for orientation in [UIDeviceOrientation.landscapeLeft, .landscapeRight] {
                rotate(orientation)
                tap("Профіль")
                let suffix = "\(mode)-\(orientation == .landscapeLeft ? "left" : "right")"
                screenshot("garage-scroll-before-\(suffix)")
                tap("Керувати гаражем · не підключено")
                assertGarageDialog()
                screenshot("garage-scroll-dialog-\(suffix)")
                tap("Зрозуміло")
                let content = app.descendants(matching: .any)["demo-account-content"]
                let garage = find("Керувати гаражем · не підключено")
                XCTAssertTrue(garage.isHittable)
                XCTAssertTrue(content.frame.contains(garage.frame), app.debugDescription)
                screenshot("garage-scroll-revealed-\(suffix)")
                attachText(app.debugDescription, name: "garage-scroll-revealed-tree-\(suffix)")
                tap("Налаштування")
            }
            rotate(.portrait)
        }
        tap("Як у системі")
        tap("Мапа")
    }

    func testFigmaVariantMatrix() {
        tap("Продовжити без входу · деморежим")
        for (mode, label) in [("light", "Світла"), ("dark", "Темна")] {
            tap("Налаштування")
            tap(label)
            for orientation in [UIDeviceOrientation.portrait, .landscapeLeft] {
                rotate(orientation)
                tap("Налаштування")
                scrollTop()
                let frame = app.windows.firstMatch.frame
                let tablet = min(frame.width, frame.height) >= 600
                let landscape = orientation.isLandscape
                let layout = "\(tablet ? "Tablet" : "Phone")\(landscape ? "Landscape" : "Portrait")"
                XCTAssertTrue(app.descendants(matching: .any)["account-layout-\(layout)"].exists)
                if tablet && landscape {
                    assertTabletLandscapeGeometry()
                } else if !tablet && !landscape {
                    let themeRow = app.descendants(matching: .any)["settings-row-Тема застосунку"]
                    XCTAssertGreaterThanOrEqual(themeRow.frame.height, 65)
                }
                if tablet || !landscape {
                    let headerAction = find(tablet ? "Довідка про деморежим" : "Пошук у налаштуваннях · не підключено")
                    XCTAssertGreaterThan(headerAction.frame.minX, find("Налаштування").frame.minX)
                    XCTAssertLessThan(headerAction.frame.maxY, frame.midY)
                }
                if !landscape {
                    XCTAssertGreaterThan(find("Мапа").frame.minY, frame.midY)
                    XCTAssertLessThan(find("Назад до попереднього розділу").frame.maxY, frame.midY)
                }
                let suffix = "\(layout)-\(mode)"
                screenshot("figma-S07-\(suffix)")
                attachText(app.debugDescription, name: "figma-S07-tree-\(suffix)")
                tap("Профіль")
                scrollTop()
                XCTAssertTrue(find("Демо-профіль").exists)
                XCTAssertFalse(find("Пошту підтверджено").exists)
                let identity = app.descendants(matching: .any)["profile-identity"]
                let garage = app.descendants(matching: .any)["profile-garage-heading"]
                if tablet && landscape {
                    assertTabletLandscapeGeometry()
                }
                if landscape {
                    if tablet {
                        XCTAssertLessThan(identity.frame.midX, garage.frame.midX)
                    } else {
                        XCTAssertGreaterThan(identity.frame.midX, garage.frame.midX)
                    }
                } else {
                    XCTAssertLessThan(identity.frame.minY, garage.frame.minY)
                }
                screenshot("figma-S08-\(suffix)")
                attachText(app.debugDescription, name: "figma-S08-tree-\(suffix)")
                tap("Деталі демоавто · не підключено")
                XCTAssertTrue(find("Можливість не підключено").waitForExistence(timeout: 3))
                XCTAssertTrue(find("Деталі авто й редагування не підключено. Ford Mustang GT: Легковий · 1,4 м · 1,8 т. Це демонстраційний профіль, не підтверджене володіння.").exists)
                tap("Зрозуміло")
                reveal("Історія поїздок і статистика відсутні; серверні дані не отримувалися.")
                XCTAssertTrue(find("Ім’я та пошту не отримано. Акаунт і підтвердження пошти відсутні.").exists)
                tap("Налаштування")
            }
            rotate(.portrait)
        }
        tap("Як у системі")
        tap("Мапа")
    }

    private func assertTabletLandscapeGeometry() {
        let header = app.descendants(matching: .any)["account-header"].frame
        let content = app.descendants(matching: .any)["demo-account-content"].frame
        let first = app.descendants(matching: .any)["account-sections-first"].frame
        let second = app.descendants(matching: .any)["account-sections-second"].frame
        XCTAssertEqual(header.height, 72, accuracy: 1)
        XCTAssertEqual(first.minX - content.minX, 24, accuracy: 1)
        XCTAssertEqual(second.minX - first.maxX, 24, accuracy: 1)
        XCTAssertEqual(content.maxX - second.maxX, 24, accuracy: 1)
    }

    func testGarageDialogRotationAndDismissal() {
        tap("Продовжити без входу · деморежим")
        tap("Профіль")
        tap("Керувати гаражем · не підключено")
        assertGarageDialog()
        find("Повний гараж і редагування автомобілів не підключено. Наявний лише легковий демопрофіль Ford Mustang GT.")
            .coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
        assertGarageDialog()
        for orientation in [UIDeviceOrientation.landscapeLeft, .portrait, .landscapeRight] {
            rotate(orientation)
            assertGarageDialog()
            screenshot("open-dialog-rotation-\(orientation.rawValue)")
            attachText(app.debugDescription, name: "open-dialog-rotation-tree-\(orientation.rawValue)")
        }
        app.windows.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.1, dy: 0.5)).tap()
        let dismissed = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == false"),
            object: app.buttons["Зрозуміло"]
        )
        XCTAssertEqual(XCTWaiter.wait(for: [dismissed], timeout: 3), .completed, app.debugDescription)
        tap("Керувати гаражем · не підключено")
        assertGarageDialog()
        tap("Зрозуміло")
        rotate(.portrait)
    }

    func testMapPreviewAndPanelSurviveProfileSettings() {
        tap("Продовжити без входу · деморежим")
        tap("Вибрати початкову точку")
        result("площа Ринок, 1").tap()
        tap("Знайти адресу")
        result("площа Ринок, 10").tap()
        tap("Переглянути маршрут")
        let panel = find("Панель маршруту")
        for state in ["Згорнута", "Частково розгорнута", "Розгорнута"] {
            XCTAssertEqual(panel.value as? String, state, app.debugDescription)
            tap("Профіль")
            tap("Відкрити налаштування")
            tap("Темна")
            tap("Назад до попереднього розділу")
            XCTAssertTrue(find("Демо-профіль").exists)
            tap("Назад до попереднього розділу")
            XCTAssertTrue(find("площа Ринок, 1").exists)
            XCTAssertTrue(find("площа Ринок, 10").exists)
            XCTAssertTrue(find("Почати маршрут").isHittable)
            XCTAssertTrue(find("Демо · Особистий").exists)
            XCTAssertEqual(panel.value as? String, state)
            screenshot("map-retained-\(state)")
            if state != "Розгорнута" { panel.tap() }
        }
        tap("Вантажне")
        XCTAssertFalse(find("Почати маршрут").exists)
        tap("Профіль")
        let warning = find("Активна категорія на мапі: Вантажне.")
        reveal("Активна категорія на мапі: Вантажне.")
        XCTAssertTrue(warning.exists)
        screenshot("profile-truck-unavailable")
        tap("Мапа")
        XCTAssertTrue(find("Профіль недоступний").exists)
        tap("Легкове")
        XCTAssertTrue(find("Переглянути маршрут").exists)
        tap("Налаштування")
        tap("Як у системі")
    }

    func testSearchKeyboardAndAuthReturnPaths() {
        tap("Продовжити без входу · деморежим")
        tap("Вибрати початкову точку")
        let field = app.textViews.firstMatch
        XCTAssertTrue(field.waitForExistence(timeout: 5))
        field.tap()
        field.typeText("LocalDraft")
        XCTAssertTrue(app.keyboards.firstMatch.exists)
        tap("Профіль")
        XCTAssertTrue(find("Демо-профіль").exists)
        rotate(.landscapeLeft)
        screenshot("profile-after-search-keyboard")
        tap("Назад до попереднього розділу")
        XCTAssertEqual(app.textViews.firstMatch.value as? String, "LocalDraft")
        tap("Налаштування")
        tap("Назад до попереднього розділу")
        XCTAssertEqual(app.textViews.firstMatch.value as? String, "LocalDraft")
        tap("Мапа")
        XCTAssertTrue(find("Вибрати початкову точку").exists)
        rotate(.portrait)
        tap("Профіль")
        tap("Відкрити реєстрацію")
        XCTAssertTrue(find("Ваш шлях починається тут").waitForExistence(timeout: 5))
        tap("Повернутися до входу")
        tap("Продовжити без входу · деморежим")
        tap("Профіль")
        tap("Відновлення пароля")
        XCTAssertTrue(find("Відновити пароль").waitForExistence(timeout: 5))
        tap("Повернутися до входу")
        XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 5))
    }

    private func assertGarageDialog() {
        let message = "Повний гараж і редагування автомобілів не підключено. Наявний лише легковий демопрофіль Ford Mustang GT."
        let ready = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let body = self.find(message)
            let button = self.app.buttons["Зрозуміло"]
            return body.exists && body.frame.height > 0 &&
                self.app.windows.firstMatch.frame.contains(body.frame) &&
                button.exists && button.isHittable && button.frame.height > 0
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [ready], timeout: 5), .completed, app.debugDescription)
    }

    private func find(_ text: String) -> XCUIElement {
        let all = app.descendants(matching: .any)
        let exact = all.matching(NSPredicate(format: "label == %@", text)).firstMatch
        return exact.exists ? exact : all.matching(NSPredicate(format: "label CONTAINS %@", text)).firstMatch
    }

    private func result(_ address: String) -> XCUIElement {
        let row = app.buttons.matching(NSPredicate(
            format: "label CONTAINS %@ AND label CONTAINS %@", address, "Вибрати адресу"
        )).firstMatch
        XCTAssertTrue(row.waitForExistence(timeout: 5), app.debugDescription)
        return row
    }

    private func reveal(_ text: String) {
        let frame = app.windows.firstMatch.frame
        let landscape = frame.width > frame.height
        let top = frame.minY + (landscape ? 90 : 150)
        let bottom = frame.maxY - (landscape ? 25 : 105)
        for direction in [true, false] {
            for _ in 0..<14 {
                let target = find(text)
                if target.exists && target.isHittable {
                    let label = target.label
                    let navigation = ["Мапа", "Профіль", "Налаштування"].contains(label) ||
                        label.contains("Назад до попереднього розділу")
                    if navigation || (target.frame.minY >= top && target.frame.maxY <= bottom) {
                        return
                    }
                }
                let upper = app.coordinate(withNormalizedOffset: .zero).withOffset(
                    CGVector(dx: frame.width * 0.75, dy: top + (bottom - top) * 0.2)
                )
                let lower = app.coordinate(withNormalizedOffset: .zero).withOffset(
                    CGVector(dx: frame.width * 0.75, dy: top + (bottom - top) * 0.75)
                )
                if landscape {
                    let content = app.descendants(matching: .any)["demo-account-content"]
                    XCTAssertTrue(content.exists, app.debugDescription)
                    let start = content.coordinate(withNormalizedOffset: CGVector(dx: 0.6, dy: direction ? 0.8 : 0.25))
                    let end = content.coordinate(withNormalizedOffset: CGVector(dx: 0.6, dy: direction ? 0.25 : 0.8))
                    start.press(forDuration: 0.1, thenDragTo: end, withVelocity: .slow, thenHoldForDuration: 0.4)
                } else if direction { lower.press(forDuration: 0.15, thenDragTo: upper) }
                else { upper.press(forDuration: 0.15, thenDragTo: lower) }
            }
        }
        XCTAssertTrue(find(text).isHittable, app.debugDescription)
    }

    private func tap(_ text: String) {
        if ["Як у системі", "Світла", "Темна"].contains(text) {
            tap("Тема застосунку")
            XCTAssertTrue(find("Зрозуміло").waitForExistence(timeout: 5))
            find(text).tap()
            tap("Зрозуміло")
            return
        }
        if text == "Зрозуміло" {
            XCTAssertTrue(find(text).waitForExistence(timeout: 5), app.debugDescription)
        } else {
            reveal(text)
        }
        let target = find(text)
        let hittable = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "isHittable == true"),
            object: target
        )
        XCTAssertEqual(XCTWaiter.wait(for: [hittable], timeout: 3), .completed, app.debugDescription)
        var previousFrame: CGRect?
        var stableSince: Date?
        let stable = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let frame = self.find(text).frame
            if frame != previousFrame {
                previousFrame = frame
                stableSince = Date()
                return false
            }
            return Date().timeIntervalSince(stableSince ?? Date()) >= 0.7
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [stable], timeout: 5), .completed)
        find(text).coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
    }

    private func scrollTop() {
        let frame = app.windows.firstMatch.frame
        let top: CGFloat = frame.width > frame.height ? 100 : 180
        for _ in 0..<4 {
            if frame.width > frame.height {
                app.descendants(matching: .any)["demo-account-content"].swipeDown(velocity: .slow)
                continue
            }
            let start = app.coordinate(withNormalizedOffset: .zero).withOffset(
                CGVector(dx: frame.width * 0.75, dy: top)
            )
            let end = app.coordinate(withNormalizedOffset: .zero).withOffset(
                CGVector(dx: frame.width * 0.75, dy: frame.height - 120)
            )
            start.press(forDuration: 0.15, thenDragTo: end)
        }
    }

    private func assertSelected(_ label: String) {
        tap("Тема застосунку")
        XCTAssertTrue(find("Зрозуміло").waitForExistence(timeout: 5))
        let target = find(label)
        attachText(app.debugDescription, name: "theme-tree-\(label)")
        XCTAssertTrue(target.isSelected || target.value as? String == "1", app.debugDescription)
        tap("Зрозуміло")
    }

    private func rotate(_ orientation: UIDeviceOrientation) {
        XCUIDevice.shared.orientation = orientation
        let landscape = orientation.isLandscape
        let settled = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let frame = self.app.windows.firstMatch.frame
            return landscape ? frame.width > frame.height : frame.height > frame.width
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [settled], timeout: 5), .completed)
    }

    private func screenshot(_ name: String) {
        if name.hasPrefix("figma-") {
            let tag = name.hasPrefix("figma-S07") ? "settings-summary" : "profile-identity"
            var previous: CGRect?
            var stableSince: Date?
            let settled = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
                let anchor = self.app.descendants(matching: .any)[tag]
                guard anchor.exists else { return false }
                let frame = anchor.frame
                if frame != previous {
                    previous = frame
                    stableSince = Date()
                    return false
                }
                return Date().timeIntervalSince(stableSince ?? Date()) >= 0.7
            }, object: nil)
            XCTAssertEqual(XCTWaiter.wait(for: [settled], timeout: 5), .completed)
        }
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    private func attachText(_ text: String, name: String) {
        let attachment = XCTAttachment(string: text)
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
