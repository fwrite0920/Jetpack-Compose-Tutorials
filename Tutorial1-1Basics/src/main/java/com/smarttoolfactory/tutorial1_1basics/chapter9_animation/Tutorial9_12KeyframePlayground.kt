package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_12Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_12_title,
        introduction = "**Keyframe Animation Playground** — Change keyframe timing and values to shape motion between explicit checkpoints.",
        examples = listOf(
            AnimationExample("KeyframeAnimationPlayground", "Keyframe Animation Playground",
                "Change keyframe timing and values to shape motion between explicit checkpoints.") { KeyframeAnimationPlaygroundPreview() }
        )
    )
}


private const val MIN_DURATION_MILLIS = 200f
private const val MAX_DURATION_MILLIS = 2400f
private const val KEYFRAME_GAP_PERCENT = 5f
private val PlaygroundBackgroundColor = Color(0xFFF4F5F7)
private val PlaygroundPanelColor = Color(0xFFFFFFFF)
private val PlaygroundTrackColor = Color(0xFFD7DCE2)
private val PlaygroundPrimaryTextColor = Color(0xFF171A1F)
private val PlaygroundSecondaryTextColor = Color(0xFF69707A)
private val PlaygroundBorderColor = Color(0x1A171A1F)
private val PlaygroundAccentColor = Color(0xFFFFB300)
private val PlaygroundAccentSecondaryColor = Color(0xFFFFC83D)
private val PlaygroundAccentTertiaryColor = Color(0xFFFF8A00)
private const val PlaygroundMarkerAlpha = 0.6f

@Preview(
    name = "Keyframe Playground",
    showBackground = true,
    backgroundColor = 0xFFF4F5F7,
    widthDp = 420,
    heightDp = 920,
)
@Composable
private fun KeyframeAnimationPlaygroundPreview() {
    MaterialTheme {
        KeyframeAnimationPlayground(
            modifier = Modifier
                .fillMaxSize()
                .background(PlaygroundBackgroundColor)
        )
    }
}

@Composable
fun KeyframeAnimationPlayground(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    var durationMillis by remember { mutableFloatStateOf(900f) }
    var keyframeOnePercent by remember { mutableFloatStateOf(18f) }
    var keyframeOneValue by remember { mutableFloatStateOf(0.08f) }
    var keyframeTwoPercent by remember { mutableFloatStateOf(62f) }
    var keyframeTwoValue by remember { mutableFloatStateOf(0.34f) }
    var autoReplay by remember { mutableStateOf(true) }

    val replayAnimation: () -> Unit = {
        val clampedDuration = durationMillis.roundToInt().coerceAtLeast(1)
        val firstTime = (clampedDuration * (keyframeOnePercent / 100f)).roundToInt()
        val secondTime = (clampedDuration * (keyframeTwoPercent / 100f)).roundToInt()

        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            progress.stop()
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    this.durationMillis = clampedDuration
                    0f at 0 using LinearEasing
                    keyframeOneValue.coerceIn(0f, 1f) at firstTime using LinearOutSlowInEasing
                    keyframeTwoValue.coerceIn(0f, 1f) at secondTime using FastOutSlowInEasing
                    1f at clampedDuration using LinearEasing
                },
            )
        }
    }

    val maybeReplay: () -> Unit = {
        if (autoReplay) {
            replayAnimation()
        }
    }

    Column(
        modifier = modifier
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Keyframe Playground",
            color = PlaygroundPrimaryTextColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(PlaygroundPanelColor)
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            val dotSize = lerp(18.dp, 30.dp, progress.value)
            val trackWidth = maxWidth - dotSize
            val dotOffset = trackWidth * progress.value

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(PlaygroundTrackColor)
            )

            KeyframeMarker(
                fraction = keyframeOnePercent / 100f,
                availableWidth = trackWidth,
                color = PlaygroundAccentSecondaryColor,
            )

            KeyframeMarker(
                fraction = keyframeTwoPercent / 100f,
                availableWidth = trackWidth,
                color = PlaygroundAccentTertiaryColor,
            )

            Box(
                modifier = Modifier
                    .offset(x = dotOffset)
                    .size(dotSize)
                    .align(Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(PlaygroundAccentColor)
            )

            Text(
                modifier = Modifier.align(Alignment.BottomStart),
                text = "Progress ${"%.2f".format(progress.value)}",
                color = PlaygroundPrimaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AnimationTutorialButton(
                    modifier = Modifier.weight(1f),
                    onClick = replayAnimation,
                ) {
                    Text("Replay")
                }

                AnimationTutorialButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        animationJob?.cancel()
                        coroutineScope.launch {
                            progress.stop()
                            progress.snapTo(0f)
                        }
                    },
                ) {
                    Text("Reset")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Auto replay on slider release",
                    color = PlaygroundPrimaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = autoReplay,
                    onCheckedChange = { autoReplay = it },
                )
            }

            KeyframeSlider(
                label = "Duration",
                valueText = "${durationMillis.roundToInt()} ms",
                value = durationMillis,
                valueRange = MIN_DURATION_MILLIS..MAX_DURATION_MILLIS,
                onValueChange = { durationMillis = it },
                onValueChangeFinished = maybeReplay,
            )

            KeyframeSlider(
                label = "Keyframe 1 Time",
                valueText = "${keyframeOnePercent.roundToInt()}%",
                value = keyframeOnePercent,
                valueRange = 0f..(keyframeTwoPercent - KEYFRAME_GAP_PERCENT).coerceAtLeast(KEYFRAME_GAP_PERCENT),
                onValueChange = { keyframeOnePercent = it },
                onValueChangeFinished = maybeReplay,
            )

            KeyframeSlider(
                label = "Keyframe 1 Value",
                valueText = keyframeOneValue.formatAsFraction(),
                value = keyframeOneValue,
                valueRange = 0f..1f,
                onValueChange = { keyframeOneValue = it },
                onValueChangeFinished = maybeReplay,
            )

            KeyframeSlider(
                label = "Keyframe 2 Time",
                valueText = "${keyframeTwoPercent.roundToInt()}%",
                value = keyframeTwoPercent,
                valueRange = (keyframeOnePercent + KEYFRAME_GAP_PERCENT).coerceAtMost(100f - KEYFRAME_GAP_PERCENT)..100f,
                onValueChange = { keyframeTwoPercent = it },
                onValueChangeFinished = maybeReplay,
            )

            KeyframeSlider(
                label = "Keyframe 2 Value",
                valueText = keyframeTwoValue.formatAsFraction(),
                value = keyframeTwoValue,
                valueRange = 0f..1f,
                onValueChange = { keyframeTwoValue = it },
                onValueChangeFinished = maybeReplay,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Current spec",
                color = PlaygroundPrimaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(PlaygroundPanelColor)
                    .padding(16.dp)
            ) {
                Text(
                    text = buildString {
                        append("0.00 at 0 ms\n")
                        append("${keyframeOneValue.formatAsFraction()} at ${(durationMillis * keyframeOnePercent / 100f).roundToInt()} ms\n")
                        append("${keyframeTwoValue.formatAsFraction()} at ${(durationMillis * keyframeTwoPercent / 100f).roundToInt()} ms\n")
                        append("1.00 at ${durationMillis.roundToInt()} ms")
                    },
                    color = PlaygroundSecondaryTextColor,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.KeyframeMarker(
    fraction: Float,
    availableWidth: androidx.compose.ui.unit.Dp,
    color: Color,
) {
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .offset(x = availableWidth * fraction.coerceIn(0f, 1f))
            .width(2.dp)
            .height(72.dp)
            .alpha(PlaygroundMarkerAlpha)
            .background(color)
    )
}

@Composable
private fun KeyframeSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = PlaygroundPrimaryTextColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = valueText,
                color = PlaygroundSecondaryTextColor,
                fontSize = 14.sp,
            )
        }

        Slider(
            value = value.coerceIn(valueRange.start, valueRange.endInclusive),
            onValueChange = onValueChange,
            valueRange = valueRange,
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}

private fun Float.formatAsFraction(): String = "%.2f".format(this)
