# Mesh gradient tutorials

Five cards in **Graphics** use the `MeshGradientPainter` API supplied by the existing Compose dependencies. Search for `Mesh Gradient` or `MeshGradientPainter` to find them.

| Card | Sample |
| --- | --- |
| 6-42 Mesh Gradient Basics | One patch, four corner colors, normalized coordinates and three palettes. |
| 6-43 Mesh Gradient Control Points | Four patches, a movable center vertex, explicit Bézier tangents and a bilinear/bicubic color comparison. |
| 6-44 Animated Mesh Gradient | Nine patches with four moving interior vertices, adjustable movement, play/pause/resume and reset. |
| 6-45 Mesh Gradient Editor | Insert rows/columns, select and drag vertices, adjust X/Y, and set individual colors with swatches and RGB sliders. |
| 6-46 Animated Mesh Gradient Editor | Edit positions and colors in Start/End meshes, then play, pause and resume their interpolation. |

Each card has a matching `Tutorial6_<number>Screen` entry point and preview in `chapter6_graphics`. `MeshGradientSamples.kt` shares palettes, vertex markers and the preview surface. `MeshGradientEditor.kt` and `MeshGradientEditorState.kt` supply the editor controls and saved mesh data. Animation reads its phase during drawing; Reset returns to the initial mesh with playback stopped.

The editors start with nine vertices and support up to 36 (6 rows × 6 columns). Insertions preserve existing vertex positions/colors and interpolate the new vertices. The rectangular grid requires adding a complete row or column. The selected vertex determines the insertion location. Both animation frames receive the same insertion so each point keeps its corresponding endpoint.

Tap a visible vertex or use the vertex menu to select it. Corners are anchored; boundary points move along their edge; interior points are constrained between adjacent vertices. Each vertex has independent RGB controls and color swatches. Edit Start / Edit End selects a keyframe; Play previews the position and color transition, and Pause holds its current appearance. Returning to a frame resumes editing without overwriting either endpoint. Mesh dimensions, both sets of vertex data and the selection survive state restoration; playback returns to editing after recreation.

Editor previews stay visible above scrolling controls on phones and beside them at content widths of 600dp or more. The animation has an adjustable 4–16 second cycle and observes the system animation duration scale.

On Android 10 (API 29) and later, the preview paints directly onto the hardware canvas. Older supported versions use a cached software bitmap because hardware `drawVertices` is unavailable there. The preview height is capped at 320dp so tablet layouts retain room for controls.

`MeshGradientTutorialTest` checks card navigation, search tags, rendered color changes, state restoration, reset, animation pause/resume and phone/tablet widths. `MeshGradientEditorTest` covers dragging, insertion preservation and limits, independent keyframes, color editing, playback, state restoration and controls in short/tablet layouts. Run their instrumentation checks with:

```sh
./gradlew :Tutorial1-1Basics:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.smarttoolfactory.tutorial1_1basics.MeshGradientTutorialTest,com.smarttoolfactory.tutorial1_1basics.MeshGradientEditorTest
```

Captured meshes are saved in the debug app's internal `files/mesh-gradients` directory.

Validated on 2026-09-09: both debug APK builds succeeded, and all eight mesh instrumentation checks passed together on Android 11 (API 30). Phone, short-window and simulated tablet layouts were checked; editor and animation captures were visually reviewed. Tests also cover the redraw that occurs when a row or column is inserted, ensuring a retiring painter retains a matching vertex count. The pre-29 software path was compiled but was not tested on an older device.

API references: [Mesh gradient guide](https://developer.android.com/develop/ui/compose/graphics/draw/mesh-gradient) and [MeshGradientPainter](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/MeshGradientPainter).
