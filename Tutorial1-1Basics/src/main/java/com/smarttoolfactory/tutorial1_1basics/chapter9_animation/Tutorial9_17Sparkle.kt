package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Ease
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInBounce
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInElastic
import androidx.compose.animation.core.EaseInExpo
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseInOutBack
import androidx.compose.animation.core.EaseInOutBounce
import androidx.compose.animation.core.EaseInOutCirc
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutElastic
import androidx.compose.animation.core.EaseInOutExpo
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseInOutQuint
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseInQuad
import androidx.compose.animation.core.EaseInQuart
import androidx.compose.animation.core.EaseInQuint
import androidx.compose.animation.core.EaseInSine
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutBounce
import androidx.compose.animation.core.EaseOutCirc
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutElastic
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.EaseOutQuint
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_17Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_17_title,
        introduction = "**Blast and Sparkle Animation** — Configure the blast, shooting particles and blinking sparkles, then start or stop the effect.",
        examples = listOf(
            AnimationExample("SparkleAnimationPreview", "Blast and Sparkle Animation",
                "Configure the blast, shooting particles and blinking sparkles, then start or stop the effect.") { SparkleAnimationPreview() }
        )
    )
}


private val sparklePreviewPaletteColors = listOf(
    Color(0xFF9CA3AF),
    Color(0xFF6B7280),
    Color(0xFF1E88E5),
    Color(0xFF1D89F4),
    Color(0xFF1FB65F),
    Color(0xFF16A34A),
    Color(0xFFEAB308),
    Color(0xFFE0AA00),
    Color(0xFFEC4899),
    Color(0xFFF97316),
    Color(0xFF7C3AED),
)

private val sparklePreviewPaletteNames = listOf(
    "Default dark",
    "Default light",
    "Blue dark",
    "Blue light",
    "Green dark",
    "Green light",
    "Yellow dark",
    "Yellow light",
    "Pink",
    "Orange",
    "Purple",
)

private object SparkleAnimationPreviewColors {
    val background = Color(0xFF000000)
    val stage = Color(0xFF16181D)
    val textPrimary = Color(0xFFF3F3F3)
    val textSecondary = Color(0xFFC1C1C4)
    val outline = Color(0xFF44464D)
    val accent = Color(0xFF9CA3AF)
    val accentOn = Color(0xFF15171A)
}

private val SparkleAnimationPreviewColorScheme = darkColorScheme(
    primary = SparkleAnimationPreviewColors.accent,
    onPrimary = SparkleAnimationPreviewColors.accentOn,
    secondary = SparkleAnimationPreviewColors.accent,
    background = SparkleAnimationPreviewColors.background,
    surface = SparkleAnimationPreviewColors.background,
    surfaceVariant = SparkleAnimationPreviewColors.stage,
    onSurface = SparkleAnimationPreviewColors.textPrimary,
    onSurfaceVariant = SparkleAnimationPreviewColors.textSecondary,
    outline = SparkleAnimationPreviewColors.outline,
)

@Composable
private fun SparkleAnimationPreviewTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SparkleAnimationPreviewColorScheme,
        content = content,
    )
}

private enum class PreviewBlinkTimingMode {
    Default,
    FixedCycles,
    DurationRange,
}

private data class BlastPreviewEasingOption(
    val name: String,
    val specFactory: (Int) -> FiniteAnimationSpec<Float>,
)

private val blastPreviewEasingOptions = listOf(
    BlastPreviewEasingOption("Build up", ::blastBuildUpAnimationSpec),
    BlastPreviewEasingOption("Linear") { tween(durationMillis = it, easing = LinearEasing) },
    BlastPreviewEasingOption("FastOutSlowIn") {
        tween(durationMillis = it, easing = FastOutSlowInEasing)
    },
    BlastPreviewEasingOption("FastOutLinearIn") {
        tween(durationMillis = it, easing = FastOutLinearInEasing)
    },
    BlastPreviewEasingOption("LinearOutSlowIn") {
        tween(durationMillis = it, easing = LinearOutSlowInEasing)
    },
    BlastPreviewEasingOption("Ease") { tween(durationMillis = it, easing = Ease) },
    BlastPreviewEasingOption("EaseIn") { tween(durationMillis = it, easing = EaseIn) },
    BlastPreviewEasingOption("EaseOut") { tween(durationMillis = it, easing = EaseOut) },
    BlastPreviewEasingOption("EaseInOut") { tween(durationMillis = it, easing = EaseInOut) },
    BlastPreviewEasingOption("EaseInBack") { tween(durationMillis = it, easing = EaseInBack) },
    BlastPreviewEasingOption("EaseOutBack") { tween(durationMillis = it, easing = EaseOutBack) },
    BlastPreviewEasingOption("EaseInOutBack") {
        tween(durationMillis = it, easing = EaseInOutBack)
    },
    BlastPreviewEasingOption("EaseInBounce") {
        tween(durationMillis = it, easing = EaseInBounce)
    },
    BlastPreviewEasingOption("EaseOutBounce") {
        tween(durationMillis = it, easing = EaseOutBounce)
    },
    BlastPreviewEasingOption("EaseInOutBounce") {
        tween(durationMillis = it, easing = EaseInOutBounce)
    },
    BlastPreviewEasingOption("EaseInElastic") {
        tween(durationMillis = it, easing = EaseInElastic)
    },
    BlastPreviewEasingOption("EaseOutElastic") {
        tween(durationMillis = it, easing = EaseOutElastic)
    },
    BlastPreviewEasingOption("EaseInOutElastic") {
        tween(durationMillis = it, easing = EaseInOutElastic)
    },
    BlastPreviewEasingOption("EaseInExpo") { tween(durationMillis = it, easing = EaseInExpo) },
    BlastPreviewEasingOption("EaseOutExpo") { tween(durationMillis = it, easing = EaseOutExpo) },
    BlastPreviewEasingOption("EaseInOutExpo") {
        tween(durationMillis = it, easing = EaseInOutExpo)
    },
    BlastPreviewEasingOption("EaseInQuad") { tween(durationMillis = it, easing = EaseInQuad) },
    BlastPreviewEasingOption("EaseOutQuad") { tween(durationMillis = it, easing = EaseOutQuad) },
    BlastPreviewEasingOption("EaseInOutQuad") {
        tween(durationMillis = it, easing = EaseInOutQuad)
    },
    BlastPreviewEasingOption("EaseInCubic") {
        tween(durationMillis = it, easing = EaseInCubic)
    },
    BlastPreviewEasingOption("EaseOutCubic") {
        tween(durationMillis = it, easing = EaseOutCubic)
    },
    BlastPreviewEasingOption("EaseInOutCubic") {
        tween(durationMillis = it, easing = EaseInOutCubic)
    },
    BlastPreviewEasingOption("EaseInQuart") {
        tween(durationMillis = it, easing = EaseInQuart)
    },
    BlastPreviewEasingOption("EaseOutQuart") {
        tween(durationMillis = it, easing = EaseOutQuart)
    },
    BlastPreviewEasingOption("EaseInOutQuart") {
        tween(durationMillis = it, easing = EaseInOutQuart)
    },
    BlastPreviewEasingOption("EaseInQuint") {
        tween(durationMillis = it, easing = EaseInQuint)
    },
    BlastPreviewEasingOption("EaseOutQuint") {
        tween(durationMillis = it, easing = EaseOutQuint)
    },
    BlastPreviewEasingOption("EaseInOutQuint") {
        tween(durationMillis = it, easing = EaseInOutQuint)
    },
    BlastPreviewEasingOption("EaseInSine") {
        tween(durationMillis = it, easing = EaseInSine)
    },
    BlastPreviewEasingOption("EaseOutSine") {
        tween(durationMillis = it, easing = EaseOutSine)
    },
    BlastPreviewEasingOption("EaseInOutSine") {
        tween(durationMillis = it, easing = EaseInOutSine)
    },
    BlastPreviewEasingOption("EaseInCirc") {
        tween(durationMillis = it, easing = EaseInCirc)
    },
    BlastPreviewEasingOption("EaseOutCirc") {
        tween(durationMillis = it, easing = EaseOutCirc)
    },
    BlastPreviewEasingOption("EaseInOutCirc") {
        tween(durationMillis = it, easing = EaseInOutCirc)
    },
)

private fun cycleIndex(
    index: Int,
    delta: Int,
    size: Int,
): Int {
    if (size <= 0) return 0
    val candidate = (index + delta) % size
    return if (candidate < 0) candidate + size else candidate
}

@Composable
private fun PreviewSliderControl(
    title: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$title: $valueText",
            style = MaterialTheme.typography.bodyMedium,
            color = SparkleAnimationPreviewColors.textPrimary,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
        )
    }
}

@Composable
private fun PreviewChoiceButtons(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = SparkleAnimationPreviewColors.textPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEachIndexed { index, label ->
                if (index == selectedIndex) {
                    Button(onClick = { onSelected(index) }) {
                        Text(label)
                    }
                } else {
                    OutlinedButton(onClick = { onSelected(index) }) {
                        Text(label)
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewRepeatControl(
    title: String,
    repeatCount: Int,
    infinite: Boolean,
    onInfiniteChange: (Boolean) -> Unit,
    onRepeatCountChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$title: ${if (infinite) "Forever" else repeatCount}",
            style = MaterialTheme.typography.bodyMedium,
            color = SparkleAnimationPreviewColors.textPrimary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (infinite) {
                Button(onClick = { onInfiniteChange(true) }) {
                    Text("Forever")
                }
            } else {
                OutlinedButton(onClick = { onInfiniteChange(true) }) {
                    Text("Forever")
                }
            }
            if (!infinite) {
                OutlinedButton(onClick = { onRepeatCountChange(max(1, repeatCount - 1)) }) {
                    Text("-")
                }
                OutlinedButton(onClick = { onRepeatCountChange(repeatCount + 1) }) {
                    Text("+")
                }
            } else {
                OutlinedButton(onClick = { onInfiniteChange(false) }) {
                    Text("Use limit")
                }
            }
        }
    }
}

@Composable
private fun SparkleAnimationPreviewControls(
    drawLayer: AnimationDrawLayer,
    onDrawLayerChange: (AnimationDrawLayer) -> Unit,
    blastMode: BlastAnimationMode,
    onBlastModeChange: (BlastAnimationMode) -> Unit,
    blastEasingIndex: Int,
    onBlastEasingIndexChange: (Int) -> Unit,
    useCustomBlastColor: Boolean,
    onUseCustomBlastColorChange: (Boolean) -> Unit,
    blastColorIndex: Int,
    onBlastColorIndexChange: (Int) -> Unit,
    blastDurationMs: Float,
    onBlastDurationMsChange: (Float) -> Unit,
    blastDelayMs: Float,
    onBlastDelayMsChange: (Float) -> Unit,
    blastMinRadiusDp: Float,
    onBlastMinRadiusDpChange: (Float) -> Unit,
    blastMaxRadiusDp: Float,
    onBlastMaxRadiusDpChange: (Float) -> Unit,
    blastStrokeWidthDp: Float,
    onBlastStrokeWidthDpChange: (Float) -> Unit,
    blastMinAlpha: Float,
    onBlastMinAlphaChange: (Float) -> Unit,
    blastMaxAlpha: Float,
    onBlastMaxAlphaChange: (Float) -> Unit,
    blastRepeatCount: Int,
    blastRepeatForever: Boolean,
    onBlastRepeatForeverChange: (Boolean) -> Unit,
    onBlastRepeatCountChange: (Int) -> Unit,
    sparkleMode: SparkleAnimationMode,
    onSparkleModeChange: (SparkleAnimationMode) -> Unit,
    useSingleSparkleColor: Boolean,
    onUseSingleSparkleColorChange: (Boolean) -> Unit,
    sparkleColorIndex: Int,
    onSparkleColorIndexChange: (Int) -> Unit,
    sparkleDurationMs: Float,
    onSparkleDurationMsChange: (Float) -> Unit,
    sparkleDelayMs: Float,
    onSparkleDelayMsChange: (Float) -> Unit,
    sparkleCount: Float,
    onSparkleCountChange: (Float) -> Unit,
    sparkleMinRadiusDp: Float,
    onSparkleMinRadiusDpChange: (Float) -> Unit,
    sparkleMaxRadiusDp: Float,
    onSparkleMaxRadiusDpChange: (Float) -> Unit,
    sparkleSafeRadiusDp: Float,
    onSparkleSafeRadiusDpChange: (Float) -> Unit,
    blinkTimingMode: PreviewBlinkTimingMode,
    onBlinkTimingModeChange: (PreviewBlinkTimingMode) -> Unit,
    sparkleBlinkCycles: Float,
    onSparkleBlinkCyclesChange: (Float) -> Unit,
    sparkleMinBlinkDurationMs: Float,
    onSparkleMinBlinkDurationMsChange: (Float) -> Unit,
    sparkleMaxBlinkDurationMs: Float,
    onSparkleMaxBlinkDurationMsChange: (Float) -> Unit,
    sparkleRepeatCount: Int,
    sparkleRepeatForever: Boolean,
    onSparkleRepeatForeverChange: (Boolean) -> Unit,
    onSparkleRepeatCountChange: (Int) -> Unit,
) {
    val selectedBlastEasing = blastPreviewEasingOptions[blastEasingIndex]

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PreviewChoiceButtons(
            title = "Draw layer",
            options = listOf("Above", "Behind"),
            selectedIndex = if (drawLayer == AnimationDrawLayer.AboveContent) 0 else 1,
            onSelected = {
                onDrawLayerChange(
                    if (it == 0) AnimationDrawLayer.AboveContent else AnimationDrawLayer.BehindContent
                )
            },
        )

        Text(
            text = "Blast",
            style = MaterialTheme.typography.titleMedium,
            color = SparkleAnimationPreviewColors.textPrimary,
        )

        PreviewChoiceButtons(
            title = "Blast mode",
            options = listOf("Filled + Ring", "Ring reveal"),
            selectedIndex = if (blastMode == BlastAnimationMode.FilledAndRing) 0 else 1,
            onSelected = {
                onBlastModeChange(
                    if (it == 0) BlastAnimationMode.FilledAndRing else BlastAnimationMode.RingReveal
                )
            },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = {
                    onBlastEasingIndexChange(
                        cycleIndex(blastEasingIndex, -1, blastPreviewEasingOptions.size)
                    )
                }
            ) {
                Text("Prev")
            }
            OutlinedButton(
                onClick = {
                    onBlastEasingIndexChange(
                        cycleIndex(blastEasingIndex, 1, blastPreviewEasingOptions.size)
                    )
                }
            ) {
                Text("Next")
            }
        }
        Text(
            text = "Blast easing: ${selectedBlastEasing.name} (${blastEasingIndex + 1}/${blastPreviewEasingOptions.size})",
            style = MaterialTheme.typography.bodySmall,
            color = SparkleAnimationPreviewColors.textSecondary,
        )

        PreviewChoiceButtons(
            title = "Blast color",
            options = listOf("Auto", "Custom"),
            selectedIndex = if (useCustomBlastColor) 1 else 0,
            onSelected = { onUseCustomBlastColorChange(it == 1) },
        )

        if (useCustomBlastColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onBlastColorIndexChange(
                            cycleIndex(blastColorIndex, -1, sparklePreviewPaletteColors.size)
                        )
                    }
                ) {
                    Text("Prev")
                }
                OutlinedButton(
                    onClick = {
                        onBlastColorIndexChange(
                            cycleIndex(blastColorIndex, 1, sparklePreviewPaletteColors.size)
                        )
                    }
                ) {
                    Text("Next")
                }
            }
            Text(
                text = "Selected: ${sparklePreviewPaletteNames[blastColorIndex]}",
                style = MaterialTheme.typography.bodySmall,
                color = SparkleAnimationPreviewColors.textSecondary,
            )
        }

        PreviewSliderControl(
            title = "Blast duration",
            value = blastDurationMs,
            valueText = "${blastDurationMs.roundToInt()} ms",
            range = 80f..4000f,
            onValueChange = onBlastDurationMsChange,
        )
        PreviewSliderControl(
            title = "Blast delay",
            value = blastDelayMs,
            valueText = "${blastDelayMs.roundToInt()} ms",
            range = 0f..1200f,
            onValueChange = onBlastDelayMsChange,
        )
        PreviewSliderControl(
            title = "Blast min radius",
            value = blastMinRadiusDp,
            valueText = "${blastMinRadiusDp.roundToInt()} dp",
            range = 1f..24f,
            onValueChange = onBlastMinRadiusDpChange,
        )
        PreviewSliderControl(
            title = "Blast max radius",
            value = blastMaxRadiusDp,
            valueText = "${blastMaxRadiusDp.roundToInt()} dp",
            range = 8f..48f,
            onValueChange = onBlastMaxRadiusDpChange,
        )
        PreviewSliderControl(
            title = "Blast stroke width",
            value = blastStrokeWidthDp,
            valueText = "${(blastStrokeWidthDp * 10f).roundToInt() / 10f} dp",
            range = 0.5f..20f,
            onValueChange = onBlastStrokeWidthDpChange,
        )
        PreviewSliderControl(
            title = "Blast min alpha",
            value = blastMinAlpha,
            valueText = "${(blastMinAlpha * 100f).roundToInt() / 100f}",
            range = 0f..blastMaxAlpha.coerceAtLeast(0f),
            onValueChange = onBlastMinAlphaChange,
        )
        PreviewSliderControl(
            title = "Blast max alpha",
            value = blastMaxAlpha,
            valueText = "${(blastMaxAlpha * 100f).roundToInt() / 100f}",
            range = blastMinAlpha.coerceAtMost(1f)..1f,
            onValueChange = onBlastMaxAlphaChange,
        )
        PreviewRepeatControl(
            title = "Blast repeat",
            repeatCount = blastRepeatCount,
            infinite = blastRepeatForever,
            onInfiniteChange = onBlastRepeatForeverChange,
            onRepeatCountChange = onBlastRepeatCountChange,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sparkle",
            style = MaterialTheme.typography.titleMedium,
            color = SparkleAnimationPreviewColors.textPrimary,
        )

        PreviewChoiceButtons(
            title = "Sparkle mode",
            options = listOf("Shoot", "Blink"),
            selectedIndex = if (sparkleMode == SparkleAnimationMode.Shoot) 0 else 1,
            onSelected = {
                onSparkleModeChange(
                    if (it == 0) SparkleAnimationMode.Shoot else SparkleAnimationMode.Blink
                )
            },
        )

        PreviewChoiceButtons(
            title = "Sparkle colors",
            options = listOf("Palette", "Single"),
            selectedIndex = if (useSingleSparkleColor) 1 else 0,
            onSelected = { onUseSingleSparkleColorChange(it == 1) },
        )

        if (useSingleSparkleColor) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        onSparkleColorIndexChange(
                            cycleIndex(sparkleColorIndex, -1, sparklePreviewPaletteColors.size)
                        )
                    }
                ) {
                    Text("Prev")
                }
                OutlinedButton(
                    onClick = {
                        onSparkleColorIndexChange(
                            cycleIndex(sparkleColorIndex, 1, sparklePreviewPaletteColors.size)
                        )
                    }
                ) {
                    Text("Next")
                }
            }
            Text(
                text = "Selected: ${sparklePreviewPaletteNames[sparkleColorIndex]}",
                style = MaterialTheme.typography.bodySmall,
                color = SparkleAnimationPreviewColors.textSecondary,
            )
        }

        PreviewSliderControl(
            title = "Sparkle duration",
            value = sparkleDurationMs,
            valueText = "${sparkleDurationMs.roundToInt()} ms",
            range = 80f..2200f,
            onValueChange = onSparkleDurationMsChange,
        )
        PreviewSliderControl(
            title = "Sparkle delay",
            value = sparkleDelayMs,
            valueText = "${sparkleDelayMs.roundToInt()} ms",
            range = 0f..1200f,
            onValueChange = onSparkleDelayMsChange,
        )
        PreviewSliderControl(
            title = "Sparkle count",
            value = sparkleCount,
            valueText = sparkleCount.roundToInt().toString(),
            range = 1f..18f,
            steps = 15,
            onValueChange = onSparkleCountChange,
        )
        PreviewSliderControl(
            title = "Sparkle min radius",
            value = sparkleMinRadiusDp,
            valueText = "${(sparkleMinRadiusDp * 10f).roundToInt() / 10f} dp",
            range = 0.5f..10f,
            onValueChange = onSparkleMinRadiusDpChange,
        )
        PreviewSliderControl(
            title = "Sparkle max radius",
            value = sparkleMaxRadiusDp,
            valueText = "${(sparkleMaxRadiusDp * 10f).roundToInt() / 10f} dp",
            range = 1f..16f,
            onValueChange = onSparkleMaxRadiusDpChange,
        )
        PreviewSliderControl(
            title = "Sparkle safe radius",
            value = sparkleSafeRadiusDp,
            valueText = "${(sparkleSafeRadiusDp * 10f).roundToInt() / 10f} dp",
            range = 0f..80f,
            onValueChange = onSparkleSafeRadiusDpChange,
        )

        PreviewChoiceButtons(
            title = "Blink timing",
            options = listOf("Default", "Cycles", "Duration"),
            selectedIndex = when (blinkTimingMode) {
                PreviewBlinkTimingMode.Default -> 0
                PreviewBlinkTimingMode.FixedCycles -> 1
                PreviewBlinkTimingMode.DurationRange -> 2
            },
            onSelected = {
                onBlinkTimingModeChange(
                    when (it) {
                        1 -> PreviewBlinkTimingMode.FixedCycles
                        2 -> PreviewBlinkTimingMode.DurationRange
                        else -> PreviewBlinkTimingMode.Default
                    }
                )
            },
        )

        if (blinkTimingMode == PreviewBlinkTimingMode.FixedCycles) {
            PreviewSliderControl(
                title = "Blink cycles",
                value = sparkleBlinkCycles,
                valueText = "${(sparkleBlinkCycles * 10f).roundToInt() / 10f}",
                range = 0.1f..8f,
                onValueChange = onSparkleBlinkCyclesChange,
            )
        }

        if (blinkTimingMode == PreviewBlinkTimingMode.DurationRange) {
            PreviewSliderControl(
                title = "Min blink duration",
                value = sparkleMinBlinkDurationMs,
                valueText = "${sparkleMinBlinkDurationMs.roundToInt()} ms",
                range = 40f..1500f,
                onValueChange = onSparkleMinBlinkDurationMsChange,
            )
            PreviewSliderControl(
                title = "Max blink duration",
                value = sparkleMaxBlinkDurationMs,
                valueText = "${sparkleMaxBlinkDurationMs.roundToInt()} ms",
                range = sparkleMinBlinkDurationMs.coerceAtLeast(40f)..1800f,
                onValueChange = onSparkleMaxBlinkDurationMsChange,
            )
        }

        PreviewRepeatControl(
            title = "Sparkle repeat",
            repeatCount = sparkleRepeatCount,
            infinite = sparkleRepeatForever,
            onInfiniteChange = onSparkleRepeatForeverChange,
            onRepeatCountChange = onSparkleRepeatCountChange,
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SparkleAnimationPreview(
    modifier: Modifier = Modifier,
) {
    val sparkleState = rememberSparkleAnimationState()
    val scope = rememberCoroutineScope()
    var animationJob by remember { mutableStateOf<Job?>(null) }
    var running by remember { mutableStateOf(false) }

    var blastDurationMs by remember { mutableFloatStateOf(260f) }
    var blastDelayMs by remember { mutableFloatStateOf(0f) }
    var blastMinRadiusDp by remember { mutableFloatStateOf(3f) }
    var blastMaxRadiusDp by remember { mutableFloatStateOf(16f) }
    var blastStrokeWidthDp by remember { mutableFloatStateOf(2f) }
    var blastMinAlpha by remember { mutableFloatStateOf(0f) }
    var blastMaxAlpha by remember { mutableFloatStateOf(1f) }
    var blastMode by remember { mutableStateOf(BlastAnimationMode.FilledAndRing) }
    var blastEasingIndex by remember { mutableIntStateOf(0) }
    var blastRepeatCount by remember { mutableIntStateOf(1) }
    var blastRepeatForever by remember { mutableStateOf(false) }
    var useCustomBlastColor by remember { mutableStateOf(false) }
    var blastColorIndex by remember { mutableIntStateOf(0) }
    var drawLayer by remember { mutableStateOf(AnimationDrawLayer.AboveContent) }

    var sparkleMode by remember { mutableStateOf(SparkleAnimationMode.Shoot) }
    var sparkleDurationMs by remember { mutableFloatStateOf(900f) }
    var sparkleDelayMs by remember { mutableFloatStateOf(0f) }
    var sparkleCount by remember { mutableFloatStateOf(8f) }
    var sparkleMinRadiusDp by remember { mutableFloatStateOf(2f) }
    var sparkleMaxRadiusDp by remember { mutableFloatStateOf(6f) }
    var sparkleSafeRadiusDp by remember { mutableFloatStateOf(0f) }
    var blinkTimingMode by remember { mutableStateOf(PreviewBlinkTimingMode.Default) }
    var sparkleBlinkCycles by remember { mutableFloatStateOf(2f) }
    var sparkleMinBlinkDurationMs by remember { mutableFloatStateOf(240f) }
    var sparkleMaxBlinkDurationMs by remember { mutableFloatStateOf(480f) }
    var sparkleRepeatCount by remember { mutableIntStateOf(1) }
    var sparkleRepeatForever by remember { mutableStateOf(false) }
    var useSingleSparkleColor by remember { mutableStateOf(false) }
    var sparkleColorIndex by remember { mutableIntStateOf(0) }

    val blastColor = if (useCustomBlastColor) {
        sparklePreviewPaletteColors[blastColorIndex]
    } else {
        Color.Unspecified
    }
    val sparkleColor = if (useSingleSparkleColor) {
        sparklePreviewPaletteColors[sparkleColorIndex]
    } else {
        Color.Unspecified
    }

    val blastAnimationSpec = remember(blastDurationMs, blastEasingIndex) {
        blastPreviewEasingOptions[blastEasingIndex].specFactory(blastDurationMs.roundToInt())
    }
    val blastModel = remember(
        blastMode,
        blastColor,
        blastAnimationSpec,
        blastDelayMs,
        blastMinRadiusDp,
        blastMaxRadiusDp,
        blastStrokeWidthDp,
        blastMinAlpha,
        blastMaxAlpha,
        blastRepeatForever,
        blastRepeatCount,
    ) {
        BlastAnimationModel(
            mode = blastMode,
            color = blastColor,
            animationSpec = blastAnimationSpec,
            delayMs = blastDelayMs.roundToInt(),
            minRadius = blastMinRadiusDp.dp,
            maxRadius = max(blastMaxRadiusDp, blastMinRadiusDp + 1f).dp,
            strokeWidth = blastStrokeWidthDp.dp,
            minAlpha = blastMinAlpha,
            maxAlpha = max(blastMaxAlpha, blastMinAlpha),
            repeatCount = if (blastRepeatForever) Int.MAX_VALUE else blastRepeatCount,
        )
    }
    val sparkleModel = SparkleAnimationModel(
        mode = sparkleMode,
        color = sparkleColor,
        colors = if (useSingleSparkleColor) emptyList() else sparklePreviewPaletteColors,
        durationMs = sparkleDurationMs.roundToInt(),
        delayMs = sparkleDelayMs.roundToInt(),
        sparkleCount = sparkleCount.roundToInt(),
        minRadius = sparkleMinRadiusDp.dp,
        maxRadius = max(sparkleMaxRadiusDp, sparkleMinRadiusDp + 1f).dp,
        safeRadius = sparkleSafeRadiusDp.dp,
        blinkCycles = if (blinkTimingMode == PreviewBlinkTimingMode.FixedCycles) {
            sparkleBlinkCycles
        } else {
            null
        },
        minBlinkDurationMs = if (blinkTimingMode == PreviewBlinkTimingMode.DurationRange) {
            sparkleMinBlinkDurationMs.roundToInt()
        } else {
            null
        },
        maxBlinkDurationMs = if (blinkTimingMode == PreviewBlinkTimingMode.DurationRange) {
            max(sparkleMaxBlinkDurationMs, sparkleMinBlinkDurationMs).roundToInt()
        } else {
            null
        },
        repeatCount = if (sparkleRepeatForever) Int.MAX_VALUE else sparkleRepeatCount,
    )

    LaunchedEffect(running, blastModel, sparkleModel) {
        animationJob?.cancel()
        animationJob = null
        sparkleState.stopAnimation()
        if (running) {
            animationJob = scope.launch {
                sparkleState.startAnimation(
                    blastModel = blastModel,
                    sparkleModel = sparkleModel,
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            animationJob?.cancel()
            scope.launch { sparkleState.stopAnimation() }
        }
    }

    SparkleAnimationPreviewTheme {
        Column(
            modifier = Modifier
                .then(modifier)
                .fillMaxSize()
                .background(SparkleAnimationPreviewColors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(SparkleAnimationPreviewColors.stage)
                    .blastAndSparkle(
                        state = sparkleState,
                        blastModel = blastModel,
                        sparkleModel = sparkleModel,
                        drawLayer = drawLayer,
                    ),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimationTutorialButton(onClick = { running = true }, modifier = Modifier.weight(1f)) {
                    Text("Start")
                }
                AnimationTutorialButton(onClick = { running = false }, modifier = Modifier.weight(1f)) {
                    Text("Stop")
                }
            }

            Text(
                text = if (sparkleState.isAnimating) "Animation running" else "Animation idle",
                style = MaterialTheme.typography.bodyMedium,
                color = SparkleAnimationPreviewColors.textSecondary,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SparkleAnimationPreviewControls(
                    drawLayer = drawLayer,
                    onDrawLayerChange = { drawLayer = it },
                    blastMode = blastMode,
                    onBlastModeChange = { blastMode = it },
                    blastEasingIndex = blastEasingIndex,
                    onBlastEasingIndexChange = { blastEasingIndex = it },
                    useCustomBlastColor = useCustomBlastColor,
                    onUseCustomBlastColorChange = { useCustomBlastColor = it },
                    blastColorIndex = blastColorIndex,
                    onBlastColorIndexChange = { blastColorIndex = it },
                    blastDurationMs = blastDurationMs,
                    onBlastDurationMsChange = { blastDurationMs = it },
                    blastDelayMs = blastDelayMs,
                    onBlastDelayMsChange = { blastDelayMs = it },
                    blastMinRadiusDp = blastMinRadiusDp,
                    onBlastMinRadiusDpChange = { blastMinRadiusDp = it },
                    blastMaxRadiusDp = blastMaxRadiusDp,
                    onBlastMaxRadiusDpChange = { blastMaxRadiusDp = it },
                    blastStrokeWidthDp = blastStrokeWidthDp,
                    onBlastStrokeWidthDpChange = { blastStrokeWidthDp = it },
                    blastMinAlpha = blastMinAlpha,
                    onBlastMinAlphaChange = { blastMinAlpha = it },
                    blastMaxAlpha = blastMaxAlpha,
                    onBlastMaxAlphaChange = { blastMaxAlpha = it },
                    blastRepeatCount = blastRepeatCount,
                    blastRepeatForever = blastRepeatForever,
                    onBlastRepeatForeverChange = { blastRepeatForever = it },
                    onBlastRepeatCountChange = { blastRepeatCount = it },
                    sparkleMode = sparkleMode,
                    onSparkleModeChange = { sparkleMode = it },
                    useSingleSparkleColor = useSingleSparkleColor,
                    onUseSingleSparkleColorChange = { useSingleSparkleColor = it },
                    sparkleColorIndex = sparkleColorIndex,
                    onSparkleColorIndexChange = { sparkleColorIndex = it },
                    sparkleDurationMs = sparkleDurationMs,
                    onSparkleDurationMsChange = { sparkleDurationMs = it },
                    sparkleDelayMs = sparkleDelayMs,
                    onSparkleDelayMsChange = { sparkleDelayMs = it },
                    sparkleCount = sparkleCount,
                    onSparkleCountChange = { sparkleCount = it },
                    sparkleMinRadiusDp = sparkleMinRadiusDp,
                    onSparkleMinRadiusDpChange = { sparkleMinRadiusDp = it },
                    sparkleMaxRadiusDp = sparkleMaxRadiusDp,
                    onSparkleMaxRadiusDpChange = { sparkleMaxRadiusDp = it },
                    sparkleSafeRadiusDp = sparkleSafeRadiusDp,
                    onSparkleSafeRadiusDpChange = { sparkleSafeRadiusDp = it },
                    blinkTimingMode = blinkTimingMode,
                    onBlinkTimingModeChange = { blinkTimingMode = it },
                    sparkleBlinkCycles = sparkleBlinkCycles,
                    onSparkleBlinkCyclesChange = { sparkleBlinkCycles = it },
                    sparkleMinBlinkDurationMs = sparkleMinBlinkDurationMs,
                    onSparkleMinBlinkDurationMsChange = { sparkleMinBlinkDurationMs = it },
                    sparkleMaxBlinkDurationMs = sparkleMaxBlinkDurationMs,
                    onSparkleMaxBlinkDurationMsChange = { sparkleMaxBlinkDurationMs = it },
                    sparkleRepeatCount = sparkleRepeatCount,
                    sparkleRepeatForever = sparkleRepeatForever,
                    onSparkleRepeatForeverChange = { sparkleRepeatForever = it },
                    onSparkleRepeatCountChange = { sparkleRepeatCount = it },
                )
            }
        }
    }
}

@Preview(
    name = "Sparkle Animation Preview",
    showBackground = true,
    backgroundColor = 0xFF0F1014,
    widthDp = 420,
    heightDp = 1200,
)
@Composable
private fun SparkleAnimationPreviewPreview() {
    SparkleAnimationPreview()
}
