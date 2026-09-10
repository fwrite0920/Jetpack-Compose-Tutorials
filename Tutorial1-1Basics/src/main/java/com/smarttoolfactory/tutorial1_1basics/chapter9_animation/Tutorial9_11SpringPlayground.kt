package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_11Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_11_title,
        introduction = "**Spring Animation Playground** — Tune stiffness, damping ratio and initial velocity, then replay the spring.",
        examples = listOf(
            AnimationExample("SpringAnimationPlayground", "Spring Animation Playground",
                "Tune stiffness, damping ratio and initial velocity, then replay the spring.") { SpringAnimationPlaygroundPreview() }
        )
    )
}


private const val SPRING_PLAYGROUND_MIN_DAMPING = 0.05f
private const val SPRING_PLAYGROUND_MAX_DAMPING = 1.6f
private const val SPRING_PLAYGROUND_MIN_STIFFNESS = 40f
private const val SPRING_PLAYGROUND_MAX_STIFFNESS = 1400f
private const val SPRING_PLAYGROUND_MIN_VELOCITY = -8f
private const val SPRING_PLAYGROUND_MAX_VELOCITY = 8f

private val SpringPlaygroundBackgroundColor = Color(0xFFF4F5F7)
private val SpringPlaygroundPanelColor = Color(0xFFFFFFFF)
private val SpringPlaygroundTrackColor = Color(0xFFD7DCE2)
private val SpringPlaygroundBorderColor = Color(0x1A171A1F)
private val SpringPlaygroundPrimaryTextColor = Color(0xFF171A1F)
private val SpringPlaygroundSecondaryTextColor = Color(0xFF69707A)
private val SpringPlaygroundAccentColor = Color(0xFF3276FF)
private val SpringPlaygroundAccentSecondaryColor = Color(0xFF7DA7FF)
private val SpringPlaygroundAccentSoftColor = Color(0x1A3276FF)
private val SpringPlaygroundTargetColor = Color(0xFFFFA23F)

@Preview(
    name = "Spring Playground",
    showBackground = true,
    backgroundColor = 0xFFF4F5F7,
    widthDp = 420,
    heightDp = 920,
)
@Composable
private fun SpringAnimationPlaygroundPreview() {
    MaterialTheme {
        SpringAnimationPlayground(
            modifier = Modifier
                .fillMaxSize()
                .background(SpringPlaygroundBackgroundColor),
        )
    }
}

@Composable
fun SpringAnimationPlayground(
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    var dampingRatio by remember { mutableFloatStateOf(0.42f) }
    var stiffness by remember { mutableFloatStateOf(260f) }
    var initialVelocity by remember { mutableFloatStateOf(0f) }
    var autoReplay by remember { mutableStateOf(true) }

    val replayAnimation: () -> Unit = {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            progress.stop()
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = dampingRatio,
                    stiffness = stiffness,
                ),
                initialVelocity = initialVelocity,
            )
        }
    }

    val maybeReplay: () -> Unit = {
        if (autoReplay) {
            replayAnimation()
        }
    }

    val rawProgress = progress.value
    val displayProgress = rawProgress.coerceIn(-0.18f, 1.18f)
    val overshootAmount = (rawProgress - rawProgress.coerceIn(0f, 1f)).absoluteValue

    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = "Spring Playground",
            color = SpringPlaygroundPrimaryTextColor,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Tune damping, stiffness, and launch velocity to see how a spring settles, overshoots, and snaps into place.",
            color = SpringPlaygroundSecondaryTextColor,
            fontSize = 14.sp,
            lineHeight = 22.sp,
        )

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(SpringPlaygroundPanelColor)
                .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            val puckSize = 28.dp
            val trackInset = 18.dp
            val travelWidth = (maxWidth - (trackInset * 2) - puckSize).coerceAtLeast(0.dp)
            val puckOffset = trackInset + (travelWidth * displayProgress)
            val targetOffset = trackInset + travelWidth
            val cardScale = (0.82f + rawProgress * 0.18f).coerceIn(0.6f, 1.25f)
            val cardRotation = (rawProgress - 1f) * -9f

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 74.dp)
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(SpringPlaygroundTrackColor),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = trackInset)
                    .padding(top = 65.dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SpringPlaygroundAccentSecondaryColor),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = targetOffset)
                    .padding(top = 60.dp)
                    .size(width = 3.dp, height = 20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(SpringPlaygroundTargetColor),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = puckOffset)
                    .padding(top = 56.dp)
                    .size(puckSize)
                    .clip(CircleShape)
                    .background(SpringPlaygroundAccentColor),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        rotationZ = cardRotation
                    }
                    .clip(RoundedCornerShape(22.dp))
                    .background(SpringPlaygroundAccentSoftColor)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Spring",
                    color = SpringPlaygroundAccentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomStart),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Value ${"%.3f".format(rawProgress)}",
                    color = SpringPlaygroundPrimaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Overshoot ${"%.3f".format(overshootAmount)}",
                    color = SpringPlaygroundSecondaryTextColor,
                    fontSize = 13.sp,
                )
            }

            Column(
                modifier = Modifier.align(Alignment.BottomEnd),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Start velocity ${"%.1f".format(initialVelocity)}",
                    color = SpringPlaygroundPrimaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (rawProgress > 1f) "Past target" else "Settling to target",
                    color = SpringPlaygroundSecondaryTextColor,
                    fontSize = 13.sp,
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
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
                    color = SpringPlaygroundPrimaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = autoReplay,
                    onCheckedChange = { autoReplay = it },
                )
            }

            SpringPlaygroundSlider(
                label = "Damping ratio",
                value = dampingRatio,
                valueRange = SPRING_PLAYGROUND_MIN_DAMPING..SPRING_PLAYGROUND_MAX_DAMPING,
                valueText = "%.2f".format(dampingRatio),
                onValueChange = { dampingRatio = it },
                onValueChangeFinished = maybeReplay,
            )

            SpringPlaygroundSlider(
                label = "Stiffness",
                value = stiffness,
                valueRange = SPRING_PLAYGROUND_MIN_STIFFNESS..SPRING_PLAYGROUND_MAX_STIFFNESS,
                valueText = stiffness.roundToInt().toString(),
                onValueChange = { stiffness = it },
                onValueChangeFinished = maybeReplay,
            )

            SpringPlaygroundSlider(
                label = "Initial velocity",
                value = initialVelocity,
                valueRange = SPRING_PLAYGROUND_MIN_VELOCITY..SPRING_PLAYGROUND_MAX_VELOCITY,
                valueText = "%.1f".format(initialVelocity),
                onValueChange = { initialVelocity = it },
                onValueChangeFinished = maybeReplay,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SpringPlaygroundSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SpringPlaygroundPanelColor)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = SpringPlaygroundPrimaryTextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = valueText,
                color = SpringPlaygroundSecondaryTextColor,
                fontSize = 14.sp,
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
        )
    }
}
