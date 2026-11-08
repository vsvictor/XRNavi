import XCTest
import UIKit

final class AdditionalScreensUITests: XCTestCase {
    private var app: XCUIApplication!
    private var originalTheme: String?
    private var originalOrientation: UIDeviceOrientation = .unknown
    private var originalInterfaceOrientation: UIDeviceOrientation = .portrait

    override func setUpWithError() throws {
        continueAfterFailure = false
        guard !UIAccessibility.isVoiceOverRunning else {
            throw XCTSkip("Run ordinary touch checks without changing VoiceOver.")
        }
        originalOrientation = XCUIDevice.shared.orientation
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 15))
        XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 15))
        let frame = app.windows.firstMatch.frame
        originalInterfaceOrientation = frame.width > frame.height ? .landscapeLeft : .portrait
        tap("Продовжити без входу · деморежим")
        tap("Налаштування")
        tap("Тема застосунку")
        XCTAssertTrue(find("Зрозуміло").waitForExistence(timeout: 5))
        originalTheme = ["Як у системі", "Світла", "Темна"].first {
            let option = find($0)
            return option.isSelected || option.value as? String == "1"
        }
        XCTAssertNotNil(originalTheme, app.debugDescription)
        tap("Зрозуміло")
    }

    override func tearDownWithError() throws {
        guard app != nil else { return }
        if let originalTheme {
            app.terminate()
            app.launch()
            if find("З поверненням").waitForExistence(timeout: 15) {
                tap("Продовжити без входу · деморежим")
                tap("Налаштування")
                setTheme(originalTheme)
                tap("Профіль")
                tap("Вийти з деморежиму")
                XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 5))
            } else {
                XCTFail("Could not restore the original app theme.")
            }
        }
        XCUIDevice.shared.orientation = originalInterfaceOrientation
        XCUIDevice.shared.orientation = originalOrientation
        XCUIDevice.shared.press(.home)
    }

    func testAdditionalScreensThemesOrientationsAndConsent() {
        for theme in ["Світла", "Темна"] {
            tap("Налаштування")
            setTheme(theme)
            for orientation in [UIDeviceOrientation.portrait, .landscapeLeft] {
                rotate(orientation)
                let variant = "\(orientation.isLandscape ? "landscape" : "portrait")-\(theme)"
                tap("Профіль")
                capture("S08", variant)
                tap("Мої завдання")
                capture("S25", variant)
                tap("Відкрити завдання")
                capture("S26", variant)
                tap("Назад")
                tap("Назад")
                tap("Історія поїздок")
                capture("S27", variant)
                tap("Підсумок поїздки")
                capture("S13", variant)
                tap("Назад")
                tap("Назад")
                tap("Безпека та особисті дані")
                capture("S28", variant)
                tap("Видалення акаунта")
                reveal("Підтвердити запит на видалення")
                XCTAssertFalse(find("Підтвердити запит на видалення").isEnabled)
                capture("S29", variant)
                tap("Розумію наслідки та окреме зберігання робочих даних.")
                XCTAssertTrue(find("Підтвердити запит на видалення").isEnabled)
                tap("Скасувати · залишити акаунт")
                capture("S28-after-cancel", variant)
                tap("Назад")

                tap("Налаштування")
                capture("S07", variant)
                tap("Налаштування компанії")
                capture("S09", variant)
                tap("Приєднатися до команди")
                reveal("Прийняти запрошення")
                XCTAssertFalse(find("Прийняти запрошення").isEnabled)
                capture("S12", variant)
                tap("Не зараз · залишитися в особистому режимі")
                tap("Назад")
                tap("Офлайн-мапи")
                capture("S21", variant)
                tap("Назад")

                tap("Гараж")
                capture("S10", variant)
                tap("Додати транспорт")
                capture("S14", variant)
                tap("Ford")
                capture("S15", variant)
                tap("Далі · перевірити параметри")
                capture("S11", variant)
                tap("До каталогу")
                capture("S14-after-return", variant)
                tap("Назад")

                tap("Мапа")
                capture("S47", variant, tagged: false)
                tap("Чому обрано цей шлях")
                capture("S38", variant)
                tap("Вантажна перевірка")
                capture("S17", variant)
                tap("Назад")
                tap("Назад")

                let previews = [
                    ("Вибрана точка", "S35"),
                    ("Визначаємо місце", "S48"),
                    ("Доступ до геолокації вимкнено", "S49"),
                    ("Службу геолокації вимкнено", "S50"),
                    ("Шукаємо адресу", "S51"),
                    ("Нічого не знайдено", "S52"),
                    ("Помилка пошуку", "S53"),
                    ("Будуємо маршрут", "S54"),
                    ("Маршрут не побудовано", "S55")
                ]
                for (title, designID) in previews {
                    tap("Налаштування")
                    tap("Пристрої та діагностика")
                    if designID == "S35" { capture("S31", variant) }
                    tap(title)
                    capture(designID, variant)
                }
                tap("Налаштування")
            }
        }
    }

    func testAuthenticationLocationAndStopPlacesVariants() {
        for theme in ["Світла", "Темна"] {
            tap("Налаштування")
            setTheme(theme)
            for orientation in [UIDeviceOrientation.portrait, .landscapeLeft] {
                rotate(orientation)
                let variant = "\(orientation.isLandscape ? "landscape" : "portrait")-\(theme)"
                app.terminate()
                app.launch()
                XCTAssertTrue(find("З поверненням").waitForExistence(timeout: 15))
                capture("S01", variant, tagged: false)
                tap("Не маєте акаунта? Зареєструватися")
                reveal("Створити акаунт")
                XCTAssertTrue(find("Створити акаунт").waitForExistence(timeout: 5))
                capture("S02", variant, tagged: false)
                tap("Повернутися до входу")
                tap("Забули пароль?")
                XCTAssertTrue(find("Відновити пароль").waitForExistence(timeout: 5))
                capture("S41", variant, tagged: false)
                for (designID, title) in [
                    ("S42", "Перевірте пошту"),
                    ("S43", "Новий пароль"),
                    ("S44", "Пароль змінено"),
                    ("S45", "Посилання недійсне")
                ] {
                    tap("Внутрішній preview S41–S45 · debug")
                    XCTAssertTrue(find("Закрити").waitForExistence(timeout: 5))
                    tap("\(designID) · \(title)")
                    XCTAssertTrue(find(title).waitForExistence(timeout: 5))
                    capture(designID, variant, tagged: false)
                }
                tap("Повернутися до входу")
                tap("Продовжити без входу · деморежим")
                tap("Використати місцезнаходження")
                capture("S46", variant)
                tap("Назад")
                tap("Місця зупинки")
                XCTAssertTrue(find("Зупинки на маршруті").waitForExistence(timeout: 5))
                capture("S19", variant, tagged: false)
                tap("Демонстраційна стоянка 01")
                XCTAssertTrue(find("Характеристики та послуги").waitForExistence(timeout: 5))
                capture("S20", variant, tagged: false)
                tap("Уточнити в’їзд у персоналу")
                XCTAssertTrue(find("Дію не виконано").waitForExistence(timeout: 5))
                tap("Назад")
                tap("Назад")
                tap("Зупинки маршруту")
                XCTAssertTrue(find("Зупинки маршруту").waitForExistence(timeout: 5))
                capture("S18", variant, tagged: false)
                tap("Скасувати")
                tap("Налаштування")
            }
        }
    }

    private func find(_ label: String) -> XCUIElement {
        let exactButton = app.buttons.matching(NSPredicate(format: "label == %@", label)).firstMatch
        if exactButton.exists { return exactButton }
        let button = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", label)).firstMatch
        if button.exists { return button }
        let all = app.descendants(matching: .any)
        let exact = all.matching(NSPredicate(format: "label == %@", label)).firstMatch
        return exact.exists ? exact : all.matching(NSPredicate(format: "label CONTAINS %@", label)).firstMatch
    }

    private func ensureForeground() {
        if app.state != .runningForeground { app.activate() }
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 5))
    }

    private func reveal(_ label: String) {
        ensureForeground()
        for upward in [true, false] {
            for _ in 0..<9 {
                let target = find(label)
                if isVisible(target, label: label) { return }
                let menu = app.descendants(matching: .any)["recovery_preview_menu"]
                let frame = menu.exists ? menu.frame : app.windows.firstMatch.frame
                let x = target.exists && target.frame.width > 0
                    ? min(max(target.frame.midX, frame.minX + frame.width * 0.2),
                          frame.minX + frame.width * 0.85)
                    : frame.minX + frame.width * 0.75
                let upper = app.coordinate(withNormalizedOffset: .zero).withOffset(
                    CGVector(dx: x, dy: frame.minY + frame.height * 0.25)
                )
                let lower = app.coordinate(withNormalizedOffset: .zero).withOffset(
                    CGVector(dx: x, dy: frame.minY + frame.height * 0.78)
                )
                (upward ? lower : upper).press(
                    forDuration: 0.1, thenDragTo: upward ? upper : lower,
                    withVelocity: .slow, thenHoldForDuration: 0.2
                )
            }
        }
        XCTFail("Control not reachable: \(label)\n\(app.debugDescription)")
    }

    private func tap(_ label: String) {
        reveal(label)
        let target = find(label)
        XCTAssertTrue(isVisible(target, label: label), app.debugDescription)
        app.coordinate(withNormalizedOffset: .zero).withOffset(
            CGVector(dx: target.frame.midX, dy: target.frame.midY)
        ).tap()
        let deadline = Date().addingTimeInterval(0.3)
        let settled = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            Date() >= deadline
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [settled], timeout: 3), .completed)
    }

    private func isVisible(_ target: XCUIElement, label: String) -> Bool {
        guard target.exists && target.isHittable else { return false }
        let safeArea = app.descendants(matching: .any)["connection_indicator_safe_area"]
        var visible = safeArea.exists ? safeArea.frame : app.windows.firstMatch.frame
        let menu = app.descendants(matching: .any)["recovery_preview_menu"]
        if menu.exists && label.hasPrefix("S") {
            visible = visible.intersection(menu.frame)
        }
        let profile = app.buttons.matching(NSPredicate(format: "label == %@", "Профіль")).firstMatch
        if !["Мапа", "Гараж", "Налаштування", "Профіль"].contains(label),
           profile.exists, profile.frame.minX > visible.midX,
           visible.intersects(profile.frame) {
            visible.size.height = max(0, profile.frame.minY - visible.minY)
        }
        return visible.contains(target.frame)
    }

    private func setTheme(_ label: String) {
        tap("Тема застосунку")
        XCTAssertTrue(find("Зрозуміло").waitForExistence(timeout: 5))
        tap(label)
        XCTAssertTrue(find(label).isSelected || find(label).value as? String == "1")
        tap("Зрозуміло")
    }

    private func rotate(_ orientation: UIDeviceOrientation) {
        XCUIDevice.shared.orientation = orientation
        let settled = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            let frame = self.app.windows.firstMatch.frame
            return orientation.isLandscape ? frame.width > frame.height : frame.height > frame.width
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [settled], timeout: 8), .completed)
    }

    private func capture(_ designID: String, _ variant: String, tagged: Bool = true) {
        ensureForeground()
        let captureAfter = Date().addingTimeInterval(0.5)
        let settled = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
            Date() >= captureAfter
        }, object: nil)
        XCTAssertEqual(XCTWaiter.wait(for: [settled], timeout: 3), .completed)
        if tagged && !["S07", "S08", "S10"].contains(designID) {
            let baseID = designID.components(separatedBy: "-").first!
            let screen = app.descendants(matching: .any).matching(
                NSPredicate(format: "identifier BEGINSWITH %@", "design-\(baseID)")
            ).firstMatch
            XCTAssertTrue(screen.waitForExistence(timeout: 5), app.debugDescription)
        }
        let indicator = app.descendants(matching: .any).matching(NSPredicate(
            format: "label BEGINSWITH %@ OR label == %@",
            "З'єднання:", "Немає з'єднання або помилка з'єднання"
        )).firstMatch
        XCTAssertTrue(indicator.waitForExistence(timeout: 5), app.debugDescription)
        let frame = app.windows.firstMatch.frame
        XCTAssertEqual(indicator.frame.width, 5, accuracy: 0.5)
        XCTAssertEqual(indicator.frame.height, 5, accuracy: 0.5)
        let safeArea = app.descendants(matching: .any)["connection_indicator_safe_area"]
        XCTAssertTrue(safeArea.waitForExistence(timeout: 5))
        XCTAssertEqual(safeArea.frame.maxX - indicator.frame.maxX, 8, accuracy: 0.5)
        XCTAssertEqual(indicator.frame.minY - safeArea.frame.minY, 8, accuracy: 0.5)
        XCTAssertTrue(frame.contains(indicator.frame))
        let attachment = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        attachment.name = "\(designID)-\(variant)"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
