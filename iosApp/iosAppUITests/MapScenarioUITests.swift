import XCTest
import UIKit

final class MapScenarioUITests: XCTestCase {
    private var app: XCUIApplication!
    private var originalOrientation: UIDeviceOrientation = .unknown

    override func setUpWithError() throws {
        continueAfterFailure = false
        if !name.contains("VoiceOver") && UIAccessibility.isVoiceOverRunning {
            throw XCTSkip("Ordinary touch tests must run separately with VoiceOver disabled by consent.")
        }
        let currentOrientation = XCUIDevice.shared.orientation
        originalOrientation = currentOrientation == .unknown ? .landscapeRight : currentOrientation
        XCUIDevice.shared.orientation = .portrait
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10))
        XCTAssertTrue(element(containing: "З поверненням").waitForExistence(timeout: 10))
        XCUIDevice.shared.orientation = .portrait
        waitForOrientation(.portrait)
    }

    override func tearDownWithError() throws {
        guard app != nil else { return }
        if app.state != .notRunning {
            app.terminate()
        }
        XCUIDevice.shared.orientation = originalOrientation
        XCUIDevice.shared.press(.home)
    }

    func testSignInButtonRespondsToTap() {
        app.buttons["Увійти"].tap()
        XCTAssertTrue(element(containing: "Вкажіть коректну електронну пошту").waitForExistence(timeout: 5))
    }

    func testRealLoginShowsNativeMapWithoutLogout() throws {
        let env = ProcessInfo.processInfo.environment
        guard let email = env["XRNAVI_TEST_EMAIL"], let password = env["XRNAVI_TEST_PASSWORD"] else {
            throw XCTSkip("Set XRNAVI_TEST_EMAIL and XRNAVI_TEST_PASSWORD to run the real-login test.")
        }
        let emailField = app.textViews.element(boundBy: 0)
        XCTAssertTrue(emailField.waitForExistence(timeout: 5))
        emailField.tap()
        emailField.typeText(email)
        let passwordField = app.textViews.element(boundBy: 1)
        passwordField.tap()
        passwordField.typeText(password)
        app.buttons["Увійти"].firstMatch.tap()
        XCTAssertTrue(element(containing: "Центр мапи").waitForExistence(timeout: 20))
        XCTAssertFalse(element(containing: "Вийти з акаунта").exists)
        XCTAssertFalse(element(containing: "Мапа недоступна").exists)
        sleep(5)
        let shot = XCTAttachment(screenshot: app.screenshot()); shot.lifetime = .keepAlways; add(shot)
        tapElement(containing: "Вибрати адресу початку")
        app.typeText("Київ")
        sleep(3)
        attachScreenshot(named: "s03-origin-suggestions")
        tapElement(containing: "Київ, ")
        tapElement(containing: "Вибрати адресу призначення")
        app.typeText("Львів")
        sleep(3)
        tapElement(containing: "Львів, ")
        XCTAssertTrue(element(containing: "Почати маршрут").waitForExistence(timeout: 20))
        sleep(3)
        attachScreenshot(named: "s03-route-ready")
        tapElement(containing: "Супутник")
        sleep(4)
        let finalShot = XCTAttachment(screenshot: app.screenshot()); finalShot.lifetime = .keepAlways; add(finalShot)
    }

    func testDemoEntryNavigatesToMap() {
        tapElement(containing: "Продовжити без входу")
        XCTAssertTrue(element(containing: "Вибрати адресу призначення").waitForExistence(timeout: 5))
    }

    func testVoiceOverDoubleTapActivatesDemoEntry() throws {
        try requireVoiceOver()
        let demoEntry = element(containing: "Продовжити без входу")
        XCTAssertTrue(demoEntry.waitForExistence(timeout: 5), app.debugDescription)
        activateWithVoiceOver(demoEntry)
        XCTAssertTrue(
            element(containing: "Вибрати адресу призначення").waitForExistence(timeout: 5),
            app.debugDescription
        )
    }

    func testVoiceOverFocusTraversalOnSignIn() throws {
        try requireVoiceOver()
        for step in 0..<14 {
            app.swipeRight()
            attachScreenshot(named: "voiceover-signin-focus-\(step + 1)")
        }
    }

    func testVoiceOverCompletesMapPreviewAndSheetActions() throws {
        try requireVoiceOver()
        activateWithVoiceOver(element(containing: "Продовжити без входу"))
        activateWithVoiceOver(element(containing: "Вибрати адресу призначення"))
        enterVoiceOverQuery("площа Ринок", resultAddress: "площа Ринок, 1")
        activateWithVoiceOver(element(containing: "площа Ринок, 1"))

        activateWithVoiceOver(element(containing: "Моє місцезнаходження"))
        enterVoiceOverQuery("Хрещатик", resultAddress: "Хрещатик, 22")
        activateWithVoiceOver(element(containing: "Хрещатик, 22"))
        activateWithVoiceOver(element(containing: "Переглянути маршрут"))

        let sheet = element(containing: "Панель маршруту")
        XCTAssertTrue(sheet.waitForExistence(timeout: 5), app.debugDescription)
        for state in ["Частково розгорнута", "Розгорнута", "Згорнута"] {
            activateWithVoiceOver(sheet)
            waitForSheetState(sheet, state: state)
            XCTAssertTrue(element(containing: "Почати маршрут").isHittable, app.debugDescription)
            XCTAssertTrue(element(containing: "Mapbox").exists, app.debugDescription)
            attachScreenshot(named: "voiceover-map-sheet-\(state)")
        }

        activateWithVoiceOver(element(containing: "Почати маршрут"))
        XCTAssertTrue(
            element(containing: "Маршрут і навігацію не підключено").waitForExistence(timeout: 5),
            app.debugDescription
        )
        attachScreenshot(named: "voiceover-map-demo-warning")
    }

    func testLargeTextMapAttributionDoesNotOverlapDurationChip() {
        tapElement(containing: "Продовжити без входу")
        tapElement(containing: "Вибрати адресу призначення")
        enterSearchQuery(
            "площа Ринок",
            resultAddress: "площа Ринок, 1",
            screenshotName: "large-text-destination-search"
        )
        tapElement(containing: "площа Ринок, 1")

        tapElement(containing: "Моє місцезнаходження")
        enterSearchQuery(
            "Хрещатик",
            resultAddress: "Хрещатик, 22",
            screenshotName: "large-text-origin-search"
        )
        tapElement(containing: "Хрещатик, 22")
        tapElement(containing: "Переглянути маршрут")

        let sheet = element(containing: "Панель маршруту")
        XCTAssertTrue(sheet.waitForExistence(timeout: 5), app.debugDescription)
        sheet.tap()
        waitForSheetState(sheet, state: "Частково розгорнута")
        sheet.tap()
        waitForSheetState(sheet, state: "Розгорнута")
        assertMapAttributionDoesNotOverlapDurationChip()
        XCTAssertTrue(
            element(containing: "Почати маршрут").isHittable,
            app.debugDescription
        )
        attachScreenshot(named: "large-text-expanded-attribution")
    }

    func testAddressSearchKeyboardAcrossOrientationsAndFields() {
        tapElement(containing: "Продовжити без входу")
        tapElement(containing: "Вибрати адресу призначення")
        enterSearchQuery(
            "площа Ринок",
            resultAddress: "площа Ринок, 1",
            screenshotName: "destination-keyboard-portrait"
        )

        rotate(to: .landscapeLeft)
        app.textViews.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.95, dy: 0.5)).tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        let landscapeResult = element(containing: "площа Ринок, 1")
        scrollSearchResultIntoView(landscapeResult)
        XCTAssertTrue(landscapeResult.isHittable, app.debugDescription)
        attachScreenshot(named: "destination-keyboard-landscape")

        rotate(to: .portrait)
        scrollSearchContentToTop()
        tapElement(containing: "Моє місцезнаходження")
        XCTAssertEqual(app.textViews.firstMatch.value as? String, "")
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        attachScreenshot(named: "origin-keyboard-cleared")
        enterSearchQuery(
            "Хрещатик",
            resultAddress: "Хрещатик, 22",
            screenshotName: "origin-keyboard-query"
        )
        tapElement(containing: "Хрещатик, 22")

        tapElement(containing: "Вибрати адресу призначення")
        enterSearchQuery(
            "площа Ринок",
            resultAddress: "площа Ринок, 1",
            screenshotName: "destination-keyboard-retained-origin"
        )
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
    }

    func testDemoAddressSelectionAndPreview() {
        tapElement(containing: "Продовжити без входу")
        tapElement(containing: "Вибрати адресу призначення")
        enterSearchQuery(
            "площа Ринок",
            resultAddress: "площа Ринок, 1",
            screenshotName: "destination-keyboard-portrait"
        )
        rotate(to: .landscapeLeft)
        app.textViews.firstMatch.coordinate(withNormalizedOffset: CGVector(dx: 0.95, dy: 0.5)).tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        let landscapeResult = element(containing: "площа Ринок, 1")
        scrollSearchResultIntoView(landscapeResult)
        XCTAssertTrue(landscapeResult.isHittable, app.debugDescription)
        attachScreenshot(named: "destination-keyboard-landscape")
        rotate(to: .portrait)
        scrollSearchContentToTop()
        tapElement(containing: "Моє місцезнаходження")
        XCTAssertEqual(app.textViews.firstMatch.value as? String, "")
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        attachScreenshot(named: "origin-keyboard-cleared")
        enterSearchQuery(
            "Хрещатик",
            resultAddress: "Хрещатик, 22",
            screenshotName: "origin-keyboard-query"
        )
        tapElement(containing: "Хрещатик, 22")
        tapElement(containing: "Вибрати адресу призначення")
        enterSearchQuery(
            "площа Ринок",
            resultAddress: "площа Ринок, 1",
            screenshotName: "destination-keyboard-retained-origin"
        )
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        tapElement(containing: "площа Ринок, 1")
        tapElement(containing: "Переглянути маршрут")

        XCTAssertTrue(
            element(containing: "Почати маршрут").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertTrue(element(containing: "Mapbox").exists)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        attachScreenshot(named: "preview")

        let sheet = element(containing: "Панель маршруту")
        XCTAssertTrue(sheet.waitForExistence(timeout: 5), app.debugDescription)
        let expectedStates = [
            "Частково розгорнута",
            "Розгорнута",
            "Згорнута"
        ]
        for expectedState in expectedStates {
            sheet.tap()
            let announcement = "\(sheet.label) \(sheet.value ?? "")"
            XCTAssertTrue(announcement.contains(expectedState), announcement)
            XCTAssertTrue(element(containing: "Почати маршрут").isHittable)
            XCTAssertTrue(element(containing: "Mapbox").exists)
            if expectedState == "Розгорнута" {
                assertMapAttributionDoesNotOverlapDurationChip()
            }
            attachScreenshot(named: "sheet-\(expectedState)")
            rotate(to: .landscapeLeft)
            // The landscape layout uses a fixed side pane without the drag handle.
            XCTAssertFalse(sheet.exists, app.debugDescription)
            attachScreenshot(named: "sheet-\(expectedState)-landscape")
            let landscapeAction = element(containing: "Почати маршрут")
            XCTAssertTrue(landscapeAction.isHittable, "CTA frame \(landscapeAction.frame)\n\(app.debugDescription)")
            XCTAssertTrue(
                element(containing: "Mapbox").waitForExistence(timeout: 5),
                app.debugDescription
            )
            if expectedState == "Розгорнута" {
                assertMapAttributionDoesNotOverlapDurationChip()
            }
            rotate(to: .portrait)
            XCTAssertTrue(
                "\(sheet.label) \(sheet.value ?? "")".contains(expectedState),
                app.debugDescription
            )
        }

        let dragStart = sheet.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        let dragEnd = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.45))
        dragStart.press(forDuration: 0.2, thenDragTo: dragEnd        )
        XCTAssertTrue(
            "\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"),
            app.debugDescription
        )
        let vehicleScrollStart = app.coordinate(
            withNormalizedOffset: CGVector(dx: 0.5, dy: 0.78)
        )
        let vehicleScrollEnd = app.coordinate(
            withNormalizedOffset: CGVector(dx: 0.5, dy: 0.56)
        )
        vehicleScrollStart.press(forDuration: 0.1, thenDragTo: vehicleScrollEnd)
        let truckOption = app.buttons["Вантажне"]
        XCTAssertTrue(truckOption.waitForExistence(timeout: 5), app.debugDescription)
        truckOption.tap()
        XCTAssertTrue(truckOption.isSelected, app.debugDescription)
        XCTAssertEqual(truckOption.label, "Вантажне", app.debugDescription)
        XCTAssertTrue(
            "\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"),
            app.debugDescription
        )
        XCTAssertTrue(
            element(containing: "Вантажний транспорт · демо 01").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertTrue(element(containing: "Переглянути маршрут").isHittable, app.debugDescription)
        XCTAssertFalse(element(containing: "Легковий · 1,4 м · 1,8 т").exists, app.debugDescription)
        XCTAssertTrue(
            element(containing: "Адреси збережено. Попередній перегляд не розраховувався.").exists,
            app.debugDescription
        )
        XCTAssertFalse(element(containing: "6 год 20 хв").exists, app.debugDescription)
        attachScreenshot(named: "truck-profile-available")

        rotate(to: .landscapeLeft)
        XCTAssertTrue(
            element(containing: "Вантажний транспорт · демо 01").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertFalse(element(containing: "З поверненням").exists)
        XCTAssertTrue(
            element(containing: "Mapbox").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertFalse(sheet.exists, app.debugDescription)
        attachScreenshot(named: "preview-landscape")
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        rotate(to: .portrait)
        XCTAssertTrue(
            element(containing: "Хрещатик, 22").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        XCTAssertTrue(app.buttons["Вантажне"].isSelected)
        tapElement(containing: "Тема")
        XCTAssertFalse(element(containing: "З поверненням").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        XCTAssertTrue(app.buttons["Вантажне"].isSelected, app.debugDescription)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        rotate(to: .landscapeLeft)
        XCTAssertTrue(element(containing: "Переглянути маршрут").isHittable)
        XCTAssertTrue(element(containing: "Mapbox").exists)
        XCTAssertTrue(
            element(containing: "Адреси збережено. Попередній перегляд не розраховувався.")
                .waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertTrue(app.buttons["Вантажне"].isSelected, app.debugDescription)
        XCTAssertTrue(app.buttons["Мапа"].isSelected, app.debugDescription)
        rotate(to: .portrait)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        XCTAssertTrue(app.buttons["Вантажне"].isSelected)
        attachScreenshot(named: "theme-toggled-once")
        tapElement(containing: "Тема")
        XCTAssertFalse(element(containing: "З поверненням").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        XCTAssertTrue(app.buttons["Вантажне"].isSelected, app.debugDescription)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        attachScreenshot(named: "theme-restored")
        XCUIDevice.shared.press(.home)
        XCTAssertTrue(app.wait(for: .runningBackground, timeout: 5))
        app.activate()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10))
        XCTAssertFalse(element(containing: "З поверненням").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        XCTAssertTrue(app.buttons["Вантажне"].isSelected, app.debugDescription)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        attachScreenshot(named: "map-after-background")
        tapElement(containing: "Переглянути маршрут")
        XCTAssertTrue(
            element(containing: "Вантажний транспорт · демо 01").waitForExistence(timeout: 5),
            app.debugDescription
        )
        tapElement(containing: "Почати маршрут")
        XCTAssertTrue(
            element(containing: "Маршрут і навігацію не підключено").waitForExistence(timeout: 5),
            app.debugDescription
        )
        attachScreenshot(named: "demo-navigation-warning")
    }

    func testOriginCTASelectionSwitchCancelAndRotation() {
        tapElement(containing: "Продовжити без входу")
        tapElement(containing: "Вибрати початкову точку")
        XCTAssertTrue(app.textViews.firstMatch.label.contains("Початкова адреса"), app.debugDescription)
        enterSearchQuery("Хрещатик", resultAddress: "Хрещатик, 22", screenshotName: "origin-cta-query")
        rotate(to: .landscapeLeft)
        XCTAssertTrue(app.textViews.firstMatch.label.contains("Початкова адреса"))
        XCTAssertEqual(app.textViews.firstMatch.value as? String, "Хрещатик")
        let dismissKeyboard = app.keyboards.buttons["Hide keyboard"]
        if dismissKeyboard.waitForExistence(timeout: 1) {
            dismissKeyboard.tap()
        }
        scrollSearchResultIntoView(element(containing: "Хрещатик, 22"))
        tapElement(containing: "Хрещатик, 22")
        rotate(to: .portrait)
        XCTAssertTrue(
            element(containing: "Вибрати адресу призначення").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertTrue(element(containing: "Знайти адресу").exists)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists)

        tapElement(containing: "Вибрати адресу призначення")
        XCTAssertTrue(app.textViews.firstMatch.label.contains("Адреса призначення"))
        enterSearchQuery("Ринок", resultAddress: "площа Ринок, 1", screenshotName: "destination-after-origin")
        tapElement(containing: "Хрещатик, 22")
        XCTAssertTrue(app.textViews.firstMatch.label.contains("Початкова адреса"))
        XCTAssertEqual(app.textViews.firstMatch.value as? String, "")
        tapElement(containing: "Введіть адресу")
        XCTAssertTrue(app.textViews.firstMatch.label.contains("Адреса призначення"))
        enterSearchQuery("Ринок", resultAddress: "площа Ринок, 1", screenshotName: "switched-destination")
        tapElement(containing: "площа Ринок, 1")
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists)
        XCTAssertTrue(element(containing: "Переглянути маршрут").exists)
        tapElement(containing: "Переглянути маршрут")
        XCTAssertTrue(element(containing: "Почати маршрут").exists)

        tapElement(containing: "Редагувати адресу призначення")
        enterSearchQuery("22", resultAddress: "Хрещатик, 22", screenshotName: "uncommitted-destination")
        tapElement(containing: "Скасувати пошук")
        XCTAssertTrue(element(containing: "Почати маршрут").exists)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists)

        tapElement(containing: "Редагувати адресу призначення")
        enterSearchQuery("22", resultAddress: "Хрещатик, 22", screenshotName: "changed-destination")
        searchResult(containing: "Хрещатик, 22").tap()
        XCTAssertFalse(element(containing: "Почати маршрут").exists)
        XCTAssertFalse(element(containing: "6 год 20 хв").exists)
        XCTAssertTrue(element(containing: "Переглянути маршрут").exists)
        attachScreenshot(named: "address-change-invalidates-preview")
    }

    private func enterSearchQuery(
        _ query: String,
        resultAddress: String,
        screenshotName: String
    ) {
        let field = app.textViews.firstMatch
        XCTAssertTrue(field.waitForExistence(timeout: 5), app.debugDescription)
        field.tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        field.typeText(query)
        XCTAssertEqual(field.value as? String, query, app.debugDescription)
        let result = searchResult(containing: resultAddress)
        XCTAssertTrue(result.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(result.isHittable, app.debugDescription)
        attachScreenshot(named: screenshotName)
    }

    private func enterVoiceOverQuery(_ query: String, resultAddress: String) {
        let field = app.textViews.firstMatch
        XCTAssertTrue(field.waitForExistence(timeout: 5), app.debugDescription)
        activateWithVoiceOver(field)
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 5), app.debugDescription)
        field.typeText(query)
        XCTAssertEqual(field.value as? String, query, app.debugDescription)
        let result = searchResult(containing: resultAddress)
        XCTAssertTrue(result.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(result.isHittable, app.debugDescription)
        attachScreenshot(named: "voiceover-search-\(resultAddress)")
    }

    private func activateWithVoiceOver(_ target: XCUIElement) {
        XCTAssertTrue(target.waitForExistence(timeout: 5), app.debugDescription)
        for _ in 0..<24 where !target.isHittable {
            app.swipeRight()
        }
        XCTAssertTrue(target.isHittable, app.debugDescription)
        target.tap()
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).doubleTap()
    }

    private func rotate(to orientation: UIDeviceOrientation) {
        XCUIDevice.shared.orientation = orientation
        waitForOrientation(orientation)
    }

    private func waitForOrientation(_ orientation: UIDeviceOrientation) {
        let window = app.windows.firstMatch
        let expectation = XCTNSPredicateExpectation(
            predicate: NSPredicate { _, _ in
                let frame = window.frame
                switch orientation {
                case .landscapeLeft, .landscapeRight:
                    return frame.width > frame.height
                case .portrait, .portraitUpsideDown:
                    return frame.height > frame.width
                default:
                    return false
                }
            },
            object: nil
        )
        XCTAssertEqual(XCTWaiter.wait(for: [expectation], timeout: 5), .completed)
    }

    private func tapElement(containing text: String) {
        let target = element(containing: text)
        XCTAssertTrue(target.waitForExistence(timeout: 5), app.debugDescription)
        scrollToHittable(target)
        let hittable = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "isHittable == true"),
            object: target
        )
        XCTAssertEqual(XCTWaiter.wait(for: [hittable], timeout: 3), .completed, app.debugDescription)
        target.tap()
    }

    private func scrollToHittable(_ target: XCUIElement) {
        for _ in 0..<4 where !target.isHittable {
            app.scrollViews.firstMatch.swipeUp()
        }
    }

    private func scrollSearchResultIntoView(_ result: XCUIElement) {
        let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.25))
        let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.1))
        for _ in 0..<3 where !result.isHittable {
            start.press(forDuration: 0.1, thenDragTo: end)
        }
    }

    private func scrollSearchContentToTop() {
        let start = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.36))
        let end = app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.54))
        start.press(forDuration: 0.1, thenDragTo: end)
    }

    private func assertMapAttributionDoesNotOverlapDurationChip() {
        let attribution = element(containing: "Mapbox")
        let durationChip = app.staticTexts.matching(
            NSPredicate(format: "label == %@", "6 год 20 хв")
        ).firstMatch
        XCTAssertTrue(attribution.exists, app.debugDescription)
        XCTAssertTrue(durationChip.exists, app.debugDescription)
        XCTAssertFalse(
            attribution.frame.intersects(durationChip.frame),
            "Map attribution \(attribution.frame) overlaps duration chip \(durationChip.frame)"
        )
    }

    private func waitForSheetState(_ sheet: XCUIElement, state: String) {
        let expectation = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "value == %@", state),
            object: sheet
        )
        XCTAssertEqual(
            XCTWaiter.wait(for: [expectation], timeout: 5),
            .completed,
            app.debugDescription
        )
    }

    private func element(containing text: String) -> XCUIElement {
        let exact = app.descendants(matching: .any)
            .matching(NSPredicate(format: "label == %@", text))
            .firstMatch
        if exact.exists {
            return exact
        }
        return app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", text))
            .firstMatch
    }

    private func searchResult(containing address: String) -> XCUIElement {
        app.buttons.matching(
            NSPredicate(format: "label CONTAINS %@ AND label CONTAINS %@", address, "Вибрати адресу")
        ).firstMatch
    }

    private func attachScreenshot(named name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    private func requireVoiceOver() throws {
        guard UIAccessibility.isVoiceOverRunning else {
            throw XCTSkip("This test requires VoiceOver to be enabled on the device.")
        }
    }
}
