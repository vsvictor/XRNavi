# S07/S08 exact Figma assets and visual verification

Source file: Figma `atSi2dQTrI7763POEO2w1a`.

This update replaces only S08's Mustang hero and S07/S08 icons. It does not change screen geometry, navigation, settings behavior, demo data, routing semantics, or unavailable-action messaging. Figma content that depicts a real person, company, vehicle ownership, statistics, or connected services is not copied into the demo.

## Figma frames and viewport dimensions

Dimensions below are Figma frame pixels, not UIKit points or Compose dp. The frames include the system-like regions drawn in the design; runtime screenshots are not stretched to these dimensions.

| Layout | Figma viewport | S07 Light | S07 Dark | S08 Light | S08 Dark |
|---|---:|---|---|---|---|
| Phone Portrait | 402 × 874 | `179:854` | `3:11931` | `179:970` | `3:12026` |
| Phone Landscape | 874 × 402 | `179:4234` | `111:753` | `179:4269` | `111:709` |
| Tablet Portrait | 834 × 1194 | `179:6433` | `95:76593` | `179:6522` | `95:76508` |
| Tablet Landscape | 1120 × 768 | `179:9983` | `81:62011` | `179:10073` | `81:61928` |

Figma's recorded system-like regions include a 48 px phone portrait status bar and an 87 px bottom navigation/home-indicator area; tablet portrait uses a 28 px top safe area, 88 px app header, 72 px section navigation and 20 px gesture area; tablet landscape uses a 28 px top safe area, 72 px header, 648 px work area and 20 px gesture area. Phone landscape places its content inside a 56 px navigation rail and includes the gesture indicator at the bottom edge.

## Exact hero-photo resources

Both source URLs use a `.png` suffix, but the downloaded bytes are JPEG. The original source is 1584 × 672. The profile now uses separate theme-specific resources; SignIn and registration retain their existing shared assets. Compose continues to use `ContentScale.Crop`, preserving the photo's aspect ratio without stretching.

| App theme | Figma asset ID | Source nodes | Repository resource | SHA-256 |
|---|---|---|---|---|
| Light | `f5f233278c14275a51b2fb1cb465d4f999ff132e` | `302:310`, `302:340`, `302:398`, `302:452` | `shared/src/commonMain/composeResources/drawable/profile_mustang_hero_light.jpg` | `ecdf69851874a01c9fdeabf2fff760b2e81491de8918c8962ac4070fc444ba52` |
| Dark | `f1bdea1d4c90f26ccaf537df6caa1d983ea744ab` | `302:134`, `302:164`, `302:222`, `302:276` | `shared/src/commonMain/composeResources/drawable/profile_mustang_hero_dark.jpg` | `79981d63c7f16dc30431211c152faf0a4b2ae15676079a8965f01b52f6d1cfca` |

## Exact icon vectors

The path data is represented by common `ImageVector` builders, so no SVG runtime decoder is used. Light and dark source paths are geometrically identical except for Help, whose source vectors differ; both exact variants are retained. The source SVG digests below refer to the Figma exports used to construct the vectors.

| Icon | Light node / SVG SHA-256 | Dark node / SVG SHA-256 | Viewport | Source colors |
|---|---|---|---:|---|
| Back | `179:865` / `6d6174aaef5897665fb57f5b2f45e4ad331e93ce83a14edcc8fe6b5d3d3e3fb3` | `3:12454` / `8db782a13925e719f4029c9593e329d7da323c8553a88a2a433a3f62e78bf15a` | 22 × 22 | `#172033` / `#F2F5FA` |
| Search | `179:870` / `bb5185814c40b80a02403b0cf5cc8ebd3fedef3a61b64a75543b938efefaab08` | `3:12457` / `0d0f1e3e5835909ce440b47a376b244f49a8acd34c1ded40bb8def59f746175b` | 22 × 22 | `#526178` / `#A8B5C7` |
| Help | `179:6445` / `6c84da92f4b0a8cd416e553ebd7e6e914ac3cf98932ad60056ac78c1ec06c135` | `81:63412` / `d93c8075166bca516da5e80c33fa283ede39c809f050629b29f5a811d9232cff` | 24 × 24 | `#526178` / `#A8B5C7` |
| Chevron right | `179:879` / `5ac3bb7d37ab65f011e86c47b1a3d90526691073083e6ecb5298f2b63f00fa29` | `3:12463` / `6412e00c31a2fc1b69874f3f943e04815770cbd11d08c092db1b8e9a7deb898c` | 18 × 18 | `#526178` / `#A8B5C7` |
| Languages | `179:887` / `677f5437986e9735834661d5d73d500de07fc9f6d2347173a45f42459e058008` | `3:12466` / `0e3979c3a7f3b8e61953ed06b39fb1f15233916e3ab83ba17383adb65748fbad` | 19 × 19 | `#526178` / `#A8B5C7` |
| Moon | `179:896` / `d99573e09aee5440f701a8155e2f5593cfc0bd5e690a3ef829d0067086406c10` | `3:12472` / `d2d8e0536ccea60d656ad3e511729fbd8f673bc38493fdf4bbffe5f1ff7367e2` | 19 × 19 | `#526178` / `#A8B5C7` |
| Volume | `179:905` / `a9d237783924fbcc8c2b2063ab446a3b65eec43e8233a6059e88a713c9e8359d` | `3:12478` / `0065bd8a52ae40e5b4a35ba63cbbc57d0a64314b25458e117e66d94d3c7a1916` | 19 × 19 | `#526178` / `#A8B5C7` |
| Scan | `179:913` / `42ec3aa594f8c52982b819af42ca1eb8e07d2891e15fb998291edb4eed50535a` | `3:12481` / `8df554a445c0280a22f20bc48e1ff035160e864ebc36162a2c2e957b63f20906` | 19 × 19 | `#526178` / `#A8B5C7` |
| Route | `179:926` / `0df154d296e6264ae86a27c241f8e5a0f69bfd9f3463cf28d515741211bcf0a1` | `3:12484` / `d2ecd79fd846887eb2a8e1c309bf7a30111f0ab54da5e3dcc0ddf3d4383018e2` | 19 × 19 | `#854D06` / `#FFC95C` |
| Lock | `179:931` / `f558ff258c94845c4b1cefb37ad6406fbcbc82764b8bd8036b8d0ae50796bfd7` | `3:12487` / `e9c63f4bf3bb04198a92aeaf2e8f3786549e9fc55500d27a767b39825ba68b62` | 15 × 15 | `#854D06` / `#FFC95C` |
| Gauge | `179:934` / `9c29cfcf40f216d30005fb04cc1ff1e1f239c9982fe5b8cb8fc4b19e56304dc5` | `3:12490` / `0d8aae116b8f303734fe287b49e74a6d6e1ceb50e948743cfbfcf32669e61d43` | 19 × 19 | `#854D06` / `#FFC95C` |
| Download | `179:942` / `3b19987077eed066153efd6730c5d75ffd8a8e7d1896bc2c1eb055c490781c2e` | `3:12496` / `61deda941e977c060ee7778e2d9caedf2fa10b00da3087afda177c7153b5b931` | 21 × 21 | `#526178` / `#A8B5C7` |

Stroke-only vectors use the Figma path geometry and 1-unit round caps. Dark Help uses its source stroke width of 1.8; light Help preserves its filled, masked source geometry and clips it to its original circular mask. Chevron arrows in S07/S08 are now vectors instead of text glyphs. Existing text, click targets and accessibility descriptions remain on the same controls.

The 24 original light/dark SVG exports are preserved under `docs/figma-assets/s07-s08/` using the icon names in the table (for example, `arrow-left.svg` and `dark-arrow-left.svg`). Their SHA-256 digests match the values above. A path-data check found all 25 SVG path records represented in `shared/src/commonMain/kotlin/com/mobilespace/xrnavi/AccountIcon.kt`; the app uses Compose `ImageVector` and does not decode SVG at runtime. The app-wide navigation rail icons remain the pre-existing PNG resources and are not represented by this set of verified vector exports.

## Visual comparison method and limits

Figma reference images are captured at the frame dimensions above. Runtime evidence is from iOS Simulator UI-test screenshots; those files remain at their captured pixel dimensions and are not rescaled to match Figma. No overlay or pixel-diff is claimed: Figma pixels cannot be treated as UIKit points, and the simulator viewport, density, system chrome and the intentionally different demo content are not a matched pair. The comparison is manual at original capture sizes. Areas containing Figma's fictional identity/organization/metrics are not treated as product content, but layout geometry is not excluded.

App theme is selected independently from system appearance in the UI matrix. The recorded simulator runs identify the devices and OS versions below; per-screenshot viewport, system appearance, and Dynamic Type metadata are not archived in this document. Orientation follows the Figma layout, and S07/S08 content is captured from the top of its scroll container. Simulator evidence is not a physical-device result.

## Verification record

The source SVG hashes above were rechecked against all 24 archived exports; all 25 path records are present in the `ImageVector` source. The original hero exports in `/tmp/xrnavi-figma-assets/` were byte-for-byte identical to the corresponding app resources. The `.png` source filenames contain JPEG data (1584 × 672); the app resources are stored as `.jpg`.

- Shared tests: 52 passed, 0 failures, 0 errors, 0 skipped.
- Android Debug: `assembleDebug` succeeded. APK SHA-256: `c2c6229bf6a6fe7cfa18949216a781cc7aacf612622031e7c686912075f98a50`.
- iOS Simulator: Kotlin compilation succeeded. `testFigmaVariantMatrix` passed on iPhone 16 Pro and iPad Pro 13-inch M4, both running iOS 18.6 (1 test per device; 0 failures and 0 skipped on each). The app-bundle file-manifest SHA-256 is `d59649a94afe492feba796c6b764cf8b9b34aecd56ec6f95d829cc8046f2771f`.
- Runtime warning: a prior run reported a QoS/priority-inversion warning involving `dispatch_semaphore_wait`; it did not recur in the latest matrix run. Its cause is not established and is not considered fixed.

Build identity: HEAD `65c92efe2b726d204eae66d69a2d3ed3978a6a6b`; the worktree is dirty, including unrelated changes, so HEAD alone does not identify the tested build. Simulator build products and source exports under `/tmp` are local evidence only. No public evidence links are claimed.

## Evidence classes and exclusions

- **A — Independently performed here:** exact photo-byte matches; 24 archived SVG source exports and path-data verification; shared tests; Android Debug build; iOS Simulator UI matrix on the two devices listed above.
- **B — User-reported:** none used for the asset-source claims above.
- **C — Historical:** earlier geometry, assertions and deferred checks are documented in the prior S07/S08 report; not reused as current-cycle proof.
- **D — Not verified here:** photo/font licensing; exact vector provenance for the app-wide navigation PNGs; physical X9 signing and device checks; VoiceOver/TalkBack announcement/navigation; P0–P4; release readiness; a matched numeric pixel-diff; reproducibility/root cause of the prior `dispatch_semaphore_wait` warning.

Figma presence and asset delivery do not establish licensing. The hero source downloads and simulator evidence under `/tmp` are local evidence, not public links.
