package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_15Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_15_title,
        introduction = "**Rotation Animation** — Compare hinge points, rotation presets and gestures, then replay the motion.",
        examples = listOf(
            AnimationExample("RotationAnimationPreview", "Rotation Animation",
                "Compare hinge points, rotation presets and gestures, then replay the motion.") { RotationAnimationPreview() }
        )
    )
}


private const val DEFAULT_START_ANGLE_DEGREES = 0f
private const val DEFAULT_PEAK_ANGLE_DEGREES = -20f
private const val DEFAULT_END_ANGLE_DEGREES = DEFAULT_START_ANGLE_DEGREES
private const val DEFAULT_ROTATION_HOLD_DURATION_MILLIS = 90
private const val DEFAULT_ROTATE_UP_DURATION_MILLIS = 240
private const val DEFAULT_ROTATE_BACK_DURATION_MILLIS = 150
private val RotationPreviewBackgroundColor = Color(0xFF000000)
private val RotationPreviewPanelColor = Color(0xFF141414)
private val RotationPreviewChipBorderColor = Color(0x33FFFFFF)
private val RotationPreviewChipTextColor = Color(0xFFF4F4F5)
private val RotationPreviewChipSelectedColor = Color(0xFFF4F4F5)
private val RotationPreviewChipSelectedTextColor = Color(0xFF0B0B0B)
private val RotationPreviewTitleColor = Color(0xFFF4F4F5)
private val RotationPreviewSubtitleColor = Color(0x99FFFFFF)
private val RotationPreviewChipShape = RoundedCornerShape(16.dp)

private data class RotationPreviewOption(
    val label: String,
    val subtitle: String,
    val peakAngleDegrees: Float,
    val pivotFractionX: Float,
    val pivotFractionY: Float,
    val holdDurationMillis: Int,
    val rotateUpDurationMillis: Int,
    val rotateBackDurationMillis: Int,
    val repeatDelayMillis: Long,
)

private val RotationPreviewOptions = listOf(
    RotationPreviewOption(
        label = "Door",
        subtitle = "Left hinge",
        peakAngleDegrees = -20f,
        pivotFractionX = 0f,
        pivotFractionY = 1f,
        holdDurationMillis = 90,
        rotateUpDurationMillis = 240,
        rotateBackDurationMillis = 150,
        repeatDelayMillis = 720L,
    ),
    RotationPreviewOption(
        label = "Center",
        subtitle = "Balanced tilt",
        peakAngleDegrees = -14f,
        pivotFractionX = 0.5f,
        pivotFractionY = 1f,
        holdDurationMillis = 60,
        rotateUpDurationMillis = 220,
        rotateBackDurationMillis = 170,
        repeatDelayMillis = 680L,
    ),
    RotationPreviewOption(
        label = "Swing",
        subtitle = "Right hinge",
        peakAngleDegrees = 20f,
        pivotFractionX = 1f,
        pivotFractionY = 1f,
        holdDurationMillis = 90,
        rotateUpDurationMillis = 240,
        rotateBackDurationMillis = 150,
        repeatDelayMillis = 720L,
    ),
)

private data class RotationPreviewGesture(
    val label: String,
    val emoji: String,
)

private val RotationPreviewGestures = listOf(
    RotationPreviewGesture(
        label = "Thumb up",
        emoji = "\uD83D\uDC4D",
    ),
    RotationPreviewGesture(
        label = "Thumb down",
        emoji = "\uD83D\uDC4E",
    ),
)

fun rotateUpAnimationSpec(
    durationMillis: Int = DEFAULT_ROTATE_UP_DURATION_MILLIS,
): FiniteAnimationSpec<Float> = tween(
    durationMillis = durationMillis.coerceAtLeast(1),
    easing = LinearOutSlowInEasing,
)

fun rotateBackAnimationSpec(
    durationMillis: Int = DEFAULT_ROTATE_BACK_DURATION_MILLIS,
): FiniteAnimationSpec<Float> = tween(
    durationMillis = durationMillis.coerceAtLeast(1),
    easing = FastOutLinearInEasing,
)

class RotationAnimationState {
    internal val rotationDegrees = Animatable(0f)

    val isAnimating: Boolean
        get() = rotationDegrees.isRunning

    suspend fun startAnimation(
        startAngleDegrees: Float = DEFAULT_START_ANGLE_DEGREES,
        peakAngleDegrees: Float = DEFAULT_PEAK_ANGLE_DEGREES,
        endAngleDegrees: Float = DEFAULT_END_ANGLE_DEGREES,
        rotateUpSpec: FiniteAnimationSpec<Float> = rotateUpAnimationSpec(),
        rotateBackSpec: FiniteAnimationSpec<Float> = rotateBackAnimationSpec(),
        holdDurationMillis: Int = DEFAULT_ROTATION_HOLD_DURATION_MILLIS,
    ) {
        rotationDegrees.stop()
        rotationDegrees.snapTo(startAngleDegrees)
        rotationDegrees.animateTo(
            targetValue = peakAngleDegrees,
            animationSpec = rotateUpSpec,
        )
        if (holdDurationMillis > 0) {
            delay(holdDurationMillis.toLong())
        }
        rotationDegrees.animateTo(
            targetValue = endAngleDegrees,
            animationSpec = rotateBackSpec,
        )
    }

    suspend fun stopAnimation() {
        rotationDegrees.stop()
        rotationDegrees.snapTo(0f)
    }
}

fun Modifier.rotationAnimation(
    state: RotationAnimationState,
    pivotFractionX: Float = 0.5f,
    pivotFractionY: Float = 1.05f,
): Modifier = this.then(
    RotationAnimationElement(
        state = state,
        pivotFractionX = pivotFractionX,
        pivotFractionY = pivotFractionY,
    )
)

private data class RotationAnimationElement(
    val state: RotationAnimationState,
    val pivotFractionX: Float,
    val pivotFractionY: Float,
) : ModifierNodeElement<RotationAnimationNode>() {

    override fun create(): RotationAnimationNode {
        return RotationAnimationNode(
            state = state,
            pivotFractionX = pivotFractionX,
            pivotFractionY = pivotFractionY,
        )
    }

    override fun update(node: RotationAnimationNode) {
        node.state = state
        node.pivotFractionX = pivotFractionX
        node.pivotFractionY = pivotFractionY
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "rotationAnimation"
        properties["state"] = state
        properties["pivotFractionX"] = pivotFractionX
        properties["pivotFractionY"] = pivotFractionY
    }
}

private class RotationAnimationNode(
    var state: RotationAnimationState,
    var pivotFractionX: Float,
    var pivotFractionY: Float,
) : Modifier.Node(), DrawModifierNode {

    override fun ContentDrawScope.draw() {
        val drawScope = this
        val pivot = Offset(
            x = size.width * pivotFractionX,
            y = size.height * pivotFractionY,
        )
        rotate(
            degrees = state.rotationDegrees.value,
            pivot = pivot,
        ) {
            drawScope.drawContent()
        }
    }
}

@Preview(
    name = "Rotation Animation",
    showBackground = true,
    backgroundColor = 0xFF0B0B0B,
)
@Composable
private fun RotationAnimationPreview() {
    val rotationState = remember { RotationAnimationState() }
    val coroutineScope = rememberCoroutineScope()
    var selectedOption by remember { mutableStateOf(RotationPreviewOptions.first()) }
    var selectedGesture by remember { mutableStateOf(RotationPreviewGestures.first()) }

    var replayToken by remember { mutableIntStateOf(0) }
    DisposableEffect(selectedOption, replayToken) {
        val job = coroutineScope.launch {
            run {
                rotationState.startAnimation(
                    startAngleDegrees = 0f,
                    peakAngleDegrees = selectedOption.peakAngleDegrees,
                    endAngleDegrees = 0f,
                    rotateUpSpec = rotateUpAnimationSpec(selectedOption.rotateUpDurationMillis),
                    rotateBackSpec = rotateBackAnimationSpec(selectedOption.rotateBackDurationMillis),
                    holdDurationMillis = selectedOption.holdDurationMillis,
                )
                delay(selectedOption.repeatDelayMillis)
            }
        }
        onDispose {
            job.cancel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RotationPreviewBackgroundColor),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AnimationTutorialButton(onClick = { replayToken++ }) {
                Text("Replay rotation")
            }
            Text(
                text = "Rotation Preview",
                color = RotationPreviewTitleColor,
                fontSize = 20.sp,
            )

            Text(
                text = "Tap a preset to swap the hinge point and motion curve.",
                color = RotationPreviewSubtitleColor,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RotationPreviewOptions.forEach { option ->
                    RotationPreviewChip(
                        option = option,
                        selected = option == selectedOption,
                    ) {
                        selectedOption = option
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RotationPreviewGestures.forEach { gesture ->
                    RotationPreviewChoiceChip(
                        label = gesture.label,
                        subtitle = null,
                        selected = gesture == selectedGesture,
                    ) {
                        selectedGesture = gesture
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(96.dp)
                .rotationAnimation(
                    state = rotationState,
                    pivotFractionX = selectedOption.pivotFractionX,
                    pivotFractionY = selectedOption.pivotFractionY,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = selectedGesture.emoji,
                fontSize = 56.sp,
            )
        }
    }
}

@Composable
private fun RotationPreviewChip(
    modifier: Modifier = Modifier,
    option: RotationPreviewOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    RotationPreviewChoiceChip(
        modifier = modifier,
        label = option.label,
        subtitle = option.subtitle,
        selected = selected,
        onClick = onClick,
    )
}

@Composable
private fun RotationPreviewChoiceChip(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RotationPreviewChipShape)
            .background(
                color = if (selected) {
                    RotationPreviewChipSelectedColor
                } else {
                    RotationPreviewPanelColor
                },
                shape = RotationPreviewChipShape
            )
            .border(
                width = 1.dp,
                color = if (selected) Color.Transparent else RotationPreviewChipBorderColor,
                shape = RotationPreviewChipShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = if (selected) {
                RotationPreviewChipSelectedTextColor
            } else {
                RotationPreviewChipTextColor
            },
            fontSize = 14.sp,
        )
        subtitle?.let {
            Text(
                text = it,
                color = if (selected) {
                    RotationPreviewChipSelectedTextColor.copy(alpha = 0.7f)
                } else {
                    RotationPreviewSubtitleColor
                },
                fontSize = 11.sp,
                lineHeight = 14.sp,
            )
        }
    }
}
