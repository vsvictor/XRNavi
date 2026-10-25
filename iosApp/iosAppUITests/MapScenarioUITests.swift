import XCTest

final class MapScenarioUITests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
        app = XCUIApplication()
        app.launch()
        XCTAssertTrue(app.wait(for: .runningForeground, timeout: 10))
        XCTAssertTrue(element(containing: "З поверненням").waitForExistence(timeout: 10))
        XCUIDevice.shared.orientation = .portrait
        waitForOrientation(.portrait)
    }

    func testSignInButtonRespondsToTap() {
        app.buttons["Увійти"].tap()
        XCTAssertTrue(element(containing: "Авторизацію не підключено").waitForExistence(timeout: 5))
    }

    func testDemoEntryNavigatesToMap() {
        tapElement(containing: "Продовжити без входу")
        XCTAssertTrue(element(containing: "Вибрати адресу призначення").waitForExistence(timeout: 5))
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

        XCTAssertTrue(
            element(containing: "Почати маршрут").waitForExistence(timeout: 5),
            app.debugDescription
        )
        XCTAssertTrue(element(containing: "Демонстраційна мапа").exists)
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
            XCTAssertTrue(element(containing: "Демонстраційна мапа").exists)
            if expectedState == "Розгорнута" {
                assertMapAttributionDoesNotOverlapDurationChip()
            }
            attachScreenshot(named: "sheet-\(expectedState)")
            rotate(to: .landscapeLeft)
            XCTAssertTrue(
                "\(sheet.label) \(sheet.value ?? "")".contains(expectedState),
                app.debugDescription
            )
            attachScreenshot(named: "sheet-\(expectedState)-landscape")
            let landscapeAction = element(containing: "Почати маршрут")
            XCTAssertTrue(landscapeAction.isHittable, "CTA frame \(landscapeAction.frame)\n\(app.debugDescription)")
            XCTAssertTrue(element(containing: "Демонстраційна мапа").exists)
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
        let vehicleDetails = element(containing: "Легковий · 1,4 м · 1,8 т")
        XCTAssertTrue(vehicleDetails.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(vehicleDetails.isHittable, app.debugDescription)
        XCTAssertTrue(element(containing: "Почати маршрут").isHittable)
        attachScreenshot(named: "sheet-drag-expanded")

        rotate(to: .landscapeLeft)
        XCTAssertTrue(element(containing: "Почати маршрут").waitForExistence(timeout: 5))
        XCTAssertFalse(element(containing: "З поверненням").exists)
        XCTAssertTrue(element(containing: "Демонстраційна мапа").exists)
        XCTAssertTrue(
            "\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"),
            app.debugDescription
        )
        attachScreenshot(named: "preview-landscape")

        sheet.tap()
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Згорнута"))
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        rotate(to: .portrait)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Згорнута"))
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists)
        sheet.tap()
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Частково розгорнута"))
        sheet.tap()
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        XCTAssertTrue(app.buttons["Вантажне"].isSelected)
        print("XR_SWITCH_TO_LIGHT_NOW")
        Thread.sleep(forTimeInterval: 10)
        XCTAssertFalse(element(containing: "З поверненням").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        XCTAssertTrue(app.buttons["Вантажне"].isSelected, app.debugDescription)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        rotate(to: .landscapeLeft)
        XCTAssertTrue(element(containing: "Почати маршрут").isHittable)
        XCTAssertTrue(element(containing: "Демонстраційна мапа").exists)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists)
        XCTAssertEqual(
            app.buttons.matching(NSPredicate(format: "isSelected == true")).count,
            1,
            app.debugDescription
        )
        rotate(to: .portrait)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        XCTAssertTrue(app.buttons["Вантажне"].isSelected)
        attachScreenshot(named: "system-theme-light-after")
        print("XR_SWITCH_TO_DARK_NOW")
        Thread.sleep(forTimeInterval: 10)
        XCTAssertFalse(element(containing: "З поверненням").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "Хрещатик, 22").exists, app.debugDescription)
        XCTAssertTrue(element(containing: "площа Ринок, 1").exists, app.debugDescription)
        XCTAssertTrue(app.buttons["Вантажне"].isSelected, app.debugDescription)
        XCTAssertTrue("\(sheet.label) \(sheet.value ?? "")".contains("Розгорнута"))
        attachScreenshot(named: "system-theme-dark-after")
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
        tapElement(containing: "Почати маршрут")
        XCTAssertTrue(
            element(containing: "сумісність маршруту не перевірена").exists,
            app.debugDescription
        )
        attachScreenshot(named: "demo-navigation-warning")
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
        let result = element(containing: resultAddress)
        XCTAssertTrue(result.waitForExistence(timeout: 5), app.debugDescription)
        XCTAssertTrue(result.isHittable, app.debugDescription)
        attachScreenshot(named: screenshotName)
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
        let attribution = element(containing: "Демонстраційна мапа")
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
            predicate: NSPredicate(format: "value CONTAINS %@", state),
            object: sheet
        )
        XCTAssertEqual(
            XCTWaiter.wait(for: [expectation], timeout: 5),
            .completed,
            app.debugDescription
        )
    }

    private func element(containing text: String) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS %@", text))
            .firstMatch
    }

    private func attachScreenshot(named name: String) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
