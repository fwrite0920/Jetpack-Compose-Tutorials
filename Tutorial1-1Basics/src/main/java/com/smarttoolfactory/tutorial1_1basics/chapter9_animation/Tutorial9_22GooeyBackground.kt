package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInParent

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.GooeyUnionParams
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.GooeyUnionState
import com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.computeGooeyUnion
import kotlin.math.max
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage
import androidx.compose.runtime.Composable

@Composable
internal fun Tutorial9_22Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_22_title,
        fitDemoContent = true,
        introduction = "**Gooey Union Background** — Expand and collapse the input-shaped surface. Its two rounded shapes join through a deforming bridge.",
        examples = listOf(
            AnimationExample("GooeyBackgroundDemo", "Gooey Union Background",
                "Expand and collapse the input-shaped surface. Its two rounded shapes join through a deforming bridge.") { GooeyBackgroundDemo() }
        )
    )
}


/**
 * Draws a gooey-union pill background behind a chat input [Row][androidx.compose.foundation.layout.Row].
 *
 * The background is the union of two shapes that merge with a gooey (metaball-like)
 * bridge when they overlap:
 *
 * - **Dynamic shape** — a pill around the "+" (add-attachment) button. Its width
 *   lerps from a wide capsule that covers the button–column intersection down to
 *   the button size as the input expands, driven by [progress] (`0 = collapsed`,
 *   `1 = expanded`). Its height lerps between [initialContentHeight] and
 *   [targetContentHeight].
 *
 * - **Static shape** — a rounded rectangle that wraps the text-field column
 *   (attachments + input row). Its right and bottom edges are always pinned to
 *   the Row's actual draw size, and its left edge comes from [columnRect].
 *
 * ## Layout-aware rendering
 *
 * Both shapes derive their dimensions from actual layout rects rather than
 * pre-computed values:
 *
 * | Input                | Source                                                |
 * |----------------------|-------------------------------------------------------|
 * | Static width/height  | `DrawScope.size` (actual Row size at draw time)       |
 * | Static left edge     | [columnRect]`.left` (from `onGloballyPositioned`)     |
 * | Dynamic center Y     | Computed from `size.height`, [progress], and padding  |
 * | Dynamic pill width   | Lerped from intersection width to button size          |
 *
 * This eliminates async lag during expand/collapse and attachment animations —
 * the path always matches the Row's current frame.
 *
 * All geometry is computed in [drawBehind] from the current layout and progress.
 * This includes width-only changes during expansion. Updating [gooeyState] keeps
 * the existing chapter-six attach/detach and wobble state machine in sync.
 *
 * @param buttonRect Layout rect of the "+" button in the Row's coordinate space,
 *   obtained via `onGloballyPositioned { boundsInParent() }`.
 * @param columnRect Layout rect of the text-field Column in the Row's coordinate
 *   space, obtained via `onGloballyPositioned { boundsInParent() }`.
 * @param gooeyState Shared state object that drives the attach/detach state machine
 *   and caches the async gooey result.
 * @param gooeyBaseParams Base configuration for the gooey union (sample counts,
 *   stretch, bridge, detach thresholds, etc.).
 * @param progress Expand/collapse animation progress (`0f` = collapsed/idle,
 *   `1f` = expanded/active). Drives the dynamic pill dimensions and the button's
 *   vertical centering.
 * @param initialContentHeight Row height when collapsed (idle state).
 * @param targetContentHeight Row height when expanded (active state). Also used
 *   as the button size since the button matches the active height.
 * @param contentCornerDp Corner radius for the static shape, animated between
 *   the idle and active values.
 * @param surfaceColor Fill color for the union path.
 * @param borderColor Stroke color for the union path outline.
 */
internal fun Modifier.gooeyUnionBackground(
    buttonRect: Rect,
    columnRect: Rect,
    gooeyState: GooeyUnionState,
    gooeyBaseParams: GooeyUnionParams,
    progress: Float,
    initialContentHeight: Dp,
    targetContentHeight: Dp,
    contentCornerDp: Dp,
    surfaceColor: Color,
    borderColor: Color,
): Modifier = drawBehind {
    if (buttonRect == Rect.Zero || columnRect == Rect.Zero) return@drawBehind

    // Compute dynamic pill dimensions from progress.
    val initialHeightPx = initialContentHeight.toPx()
    val targetSizePx = targetContentHeight.toPx()
    val pillHeightPx = lerp(initialHeightPx, targetSizePx, progress)

    // Button is bottom-aligned — compute center Y from actual Row height.
    val currentContentPx = lerp(initialHeightPx, targetSizePx, progress)
    val bottomPadPx = ((currentContentPx - targetSizePx) / 2f).coerceAtLeast(0f)
    val dynamicCenterY = size.height - bottomPadPx - targetSizePx / 2f

    // Static rect: right edge = Row right, bottom = Row bottom.
    val staticLeft = columnRect.left
    val staticWidth = size.width - staticLeft
    val staticHeight = size.height

    // Recompute from the current progress and layout, including width-only changes.
    val unionPath = run {
        val intersectionExtra = 40.dp.toPx()
        val pillIntersectW = max(
            pillHeightPx + intersectionExtra,
            staticLeft + intersectionExtra
        )
        val pillWidthPx = lerp(pillIntersectW, targetSizePx, progress)
        val cornerPx = contentCornerDp.toPx().coerceAtMost(staticHeight / 2f)

        val drawParams = gooeyBaseParams.copy(
            dynamicRectWidthPx = pillWidthPx,
            dynamicRectHeightPx = pillHeightPx,
            dynamicCornerPx = pillHeightPx / 2f,
            staticRectWidthPx = staticWidth,
            staticRectHeightPx = staticHeight,
            staticCornerPx = cornerPx,
        )
        val drawStaticCenter = Offset(
            x = staticLeft + staticWidth / 2f,
            y = staticHeight / 2f,
        )
        val drawDynamicCenter = Offset(
            x = pillWidthPx / 2f,
            y = dynamicCenterY,
        )

        // Update gooey state so the state machine (attach/detach, wobble)
        // reacts on the next composition frame via snapshotFlow.
        gooeyState.params = drawParams
        gooeyState.staticCenter = drawStaticCenter
        gooeyState.dynamicCenter = drawDynamicCenter

        computeGooeyUnion(
            params = drawParams,
            staticCenter = drawStaticCenter,
            dynamicCenter = drawDynamicCenter,
            isAttached = gooeyState.isAttached,
            isPullingApart = gooeyState.isPullingApart,
            stretchAmount = gooeyState.stretchAnim.value,
            detachWobbleAmount = gooeyState.wobbleAnim.value,
            detachAngleRad = gooeyState.detachAngleRad,
            wavePhase = gooeyState.wavePhaseAnim.value,
        ).unionPath
    }

    drawPath(path = unionPath, color = surfaceColor)
    drawPath(
        path = unionPath,
        color = borderColor,
        style = Stroke(width = 1.dp.toPx())
    )
}

@Composable
private fun GooeyBackgroundDemo() {
    var expanded by remember { mutableStateOf(false) }
    val progress by androidx.compose.animation.core.animateFloatAsState(
        if (expanded) 1f else 0f, androidx.compose.animation.core.tween(800), label = "Gooey expansion")
    val params = remember {
        GooeyUnionParams(
            dynamicShape = com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.DynamicShape.RoundedRect)
    }
    val state = com.smarttoolfactory.tutorial1_1basics.chapter6_graphics.rememberGooeyUnionState(params)
    var buttonRect by remember { mutableStateOf(Rect.Zero) }
    var columnRect by remember { mutableStateOf(Rect.Zero) }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        AnimationTutorialButton(onClick = { expanded = !expanded }, modifier = Modifier.testTag("gooey-toggle")) {
            Text(if (expanded) "Collapse background" else "Expand background")
        }
        Row(Modifier.fillMaxWidth().height((72 + 72 * progress).dp)
            .gooeyUnionBackground(buttonRect, columnRect, state, params, progress,
                72.dp, 48.dp, 24.dp, MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.primary),
            verticalAlignment = androidx.compose.ui.Alignment.Bottom) {
            Box(Modifier.size(48.dp).onGloballyPositioned { buttonRect = it.boundsInParent() },
                contentAlignment = androidx.compose.ui.Alignment.Center) { Text("+") }
            Spacer(Modifier.width((4 + 20 * progress).dp))
            Box(Modifier.weight(1f).fillMaxHeight()
                .onGloballyPositioned { columnRect = it.boundsInParent() },
                contentAlignment = androidx.compose.ui.Alignment.Center) { Text("Message") }
        }
    }
}
