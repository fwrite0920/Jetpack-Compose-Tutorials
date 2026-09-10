# Tutorial sections

The Basics app has eight categories, registered in `tutorial_list/TutorialCategories.kt`. The registry supplies home tabs, pager pages, search metadata and title-based navigation routes. Semantics remains outside the home tabs.

The new curriculum contains seven Theming cards, nine Material 3 cards and 37 Animation cards covering 58 distinct demos. Animation files follow the existing `Tutorial9_<number><Topic>.kt` naming convention and expose matching `Tutorial9_<number>Screen` entry composables. Every tutorial source file has its own card; multiple samples in that file appear consecutively with their own explanation, bounded demo area and Reset action. Shared-element files remain separate tutorials. Their original full-screen presentation has been restored: no added teaching/reset wrapper, themed surface or clipping. Files with two samples show consecutive full-viewport examples. The placeholder-size example uses `PlaceholderSize.ContentSize` and `PlaceholderSize.AnimatedSize`.

Theming uses separate numbered files, `Tutorial7_<number><Topic>.kt`: Theme Anatomy, Light/Dark/Dynamic Color, Typography, Shapes, Custom Tokens, CompositionLocal and Static CompositionLocal. The two CompositionLocal tutorials display their original examples consecutively with tutorial headings and explanations; no sample selector is used.

The expanded easing playground remains at 9-6. Posted samples are registered as 9-11 Spring, 9-12 Keyframes, 9-13 Elastic Press, 9-14 Shake on Touch, 9-15 Rotation, 9-16 Wave Fill, 9-17 Blast and Sparkle, 9-18 Shape Morphing (three consecutive examples), 9-19 Thinking Shape Playground, 9-20 Morphing Thinking Indicator, 9-21 Animated Thinking Text and 9-22 Gooey Union Background. Shared animations follow at 9-23 through 9-37. Supporting modifiers and geometry remain alongside the numbered tutorial files.

The posted code uses this app's package, resource and Material 3 color scheme. The shape examples depend on [Graphics Shapes 1.1.0](https://developer.android.com/jetpack/androidx/releases/graphics); the gooey background reuses the existing chapter-six union geometry and state machine.

New screens reuse `TutorialHeader`, `TutorialText2` and `StyleableTutorialText`. Material 3 previews use `Material3TutorialTheme`; the app-level Material 2 theme and historical examples remain available. Comparisons stack on compact screens and use columns when the available content width reaches 600dp. Navigation Bar (10-3) and Navigation Rail (10-9) are separate tutorials. The rail tutorial always shows an interactive rail, including on phones, followed by a responsive example that applies the 600dp threshold within its own bounds.

The carousel tutorial has a Multi-browse / Uncontained selector and a live preview above scrolling controls. Multi-browse exposes preferred width and minimum/maximum small-item widths; uncontained exposes fixed item width. The small-item bounds stay ordered and below preferred width. Each style retains its width settings when switching, and Reset restores the defaults.

Dynamic color is optional and uses the device palette on Android 12 and later. The supplied light/dark schemes provide the fallback. Date picker values are formatted as UTC calendar dates to avoid shifting the selected date in another time zone. Picker dialogs commit their drafts only on confirmation.

## Validation

From the project root:

```sh
./gradlew :Tutorial1-1Basics:assembleDebug
./gradlew :Tutorial1-1Basics:assembleDebugAndroidTest
./gradlew :Tutorial1-1Basics:connectedDebugAndroidTest
```

`TutorialSectionsTest` covers category navigation, searchable metadata, M2/M3 buttons and chips, segmented selection, the standalone rail route, responsive navigation and saved state, drawers, carousel selection and width bounds, short-window controls, state-based search, picker confirmation/dismissal, animation timing and reset, every animation file and its consecutive samples, and light/dark rendering.

Run on both phone and tablet configurations. The smoke tests also simulate a tablet content width through `LocalDensity`, without changing emulator settings. Selected screenshots are saved in the debug app's internal `files/tutorial-smoke` directory and can be retrieved with `adb exec-out run-as com.smarttoolfactory.tutorial1_1basics cat files/tutorial-smoke/<name>.png`. Predictive-back gesture progress additionally needs a supported Android version with gesture navigation enabled.

The initial section implementation passed 15 instrumentation tests on an Android 12 (API 31) emulator at approximately 393dp phone width and 800dp tablet width. Light/dark screenshots were reviewed at both widths. The revised rail, carousel and consecutive-animation layout add coverage described above. Predictive-back gesture progress additionally requires manual testing on a supported device.

Validated the revised implementation on 2026-09-09: both APK build tasks succeeded and all 17 instrumentation tests passed on Android 11 (API 30). Every animation sample opened at approximately 411dp phone width and 864dp simulated tablet width. Light/dark component rendering passed at both widths; rail, carousel and stacked-animation screenshots were reviewed. Carousel tests verified live preferred-width changes, ordered small-item bounds, selection and reset, plus reachable controls in a 480dp-tall window. Navigation tests verified the standalone phone rail, saved selection and the 599dp/600dp responsive boundary.

Posted-sample validation on 2026-09-09: both APK builds passed and the complete 19-test suite passed on an isolated Android 12 (API 31) emulator. After the final percentage-label and playground-background fixes, four focused tests passed again: modifier controls, spring/keyframe replay and reset, light/dark rendering, and all 58 sample entries at phone and simulated tablet widths. Reviewed screenshots include spring, wave fill, sparkle, shape morphing, thinking shapes and the gooey background.

Theming split validation on 2026-09-09: both APK builds passed. Three focused instrumentation tests passed on Android 12 (API 31), covering searchable metadata, consecutive CompositionLocal examples and interactive scoped tokens/counters/palette changes, plus light/dark rendering at phone and simulated tablet widths. The static-local phone screenshot was reviewed.

Easing visual update: 9-5 now teaches curve slope through labeled plots, a linear reference, synchronized motion lanes, scrubbing, pause/reset and duration controls. 9-6 retains its previews and adds a gallery of all 38 easing curves. Graph bounds include overshoot; plotting samples Easing.transform directly instead of recording wall-clock timestamps during drawing. Both APK builds and three focused UI tests passed on API 31, covering playback/scrubbing/reset, gallery selection and light/dark phone/tablet rendering. Curve and gallery phone screenshots were reviewed.

Shared-transition presentation restoration: both APK builds passed, and two focused API 31 instrumentation tests passed. All 15 shared-transition tutorial entries opened, both multi-example files exposed their second sample, and both placeholder modes opened and returned successfully.

Animation spacing and controls update (2026-09-10): compact lessons 9-1–4, 9-7–9, 9-13–14 and 9-20–22 use content height instead of the shared 480–720dp viewport. Larger canvas/playground lessons retain finite bounds. The enter/exit background has an explicit 200dp stage, and the previously empty SlideIntoContainer sample now renders a navigable three-level menu. Standard animation actions and reset controls use a shared purple pill button, with full-width standalone actions. Both APK builds passed; three API 31 tests verified compact heights, expansion/reset behavior and all 58 examples at phone/tablet widths. The updated 9-1 screenshot was reviewed.

## API references

- [Material 3 theming](https://developer.android.com/develop/ui/compose/designsystems/material3)
- [Material 2 to Material 3 migration](https://developer.android.com/develop/ui/compose/designsystems/material2-material3)
- [Button variants](https://developer.android.com/develop/ui/compose/components/button)
- [Carousel API](https://developer.android.com/reference/kotlin/androidx/compose/material3/carousel/HorizontalMultiBrowseCarousel.composable)
- [Choosing an animation API](https://developer.android.com/develop/ui/compose/animation/choose-api)

The implementation targets the existing Compose BOM 2026.08.00 dependency upgrade, including Material 3 1.4.0. Search uses `SearchBarState`, `TextFieldState`, `ExpandedFullScreenSearchBar` and `ExpandedDockedSearchBar`.
