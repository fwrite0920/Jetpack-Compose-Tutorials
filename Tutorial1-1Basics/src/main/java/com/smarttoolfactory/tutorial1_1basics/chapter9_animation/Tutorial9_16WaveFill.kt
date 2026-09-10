package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.DurationBasedAnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_16Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_16_title,
        introduction = "**Wave Fill Animation** — Animate the fill level and wave phase; adjust colors, amplitude and wavelength.",
        examples = listOf(
            AnimationExample("WaveFillPreview", "Wave Fill Animation",
                "Animate the fill level and wave phase; adjust colors, amplitude and wavelength.") { WaveFillPreview() }
        )
    )
}


private const val WaveAnimationDurationMillis = 2000
private const val WavePreviewCycleDurationMillis = 2200
private const val TwoPi = (PI * 2.0).toFloat()
private const val WaveSecondaryPhaseOffsetRadians = 0.82f
private val WavePreviewFillColor = Color(0xFFFFB300)
private val WavePreviewCrestColorOptions = listOf(
    Color(0x66FFE082),
    Color(0x88FFF1A8),
    Color(0x88FFFFFF),
    Color(0x6663C6FF),
    Color(0x66FF8AAE),
)

private object WaveFillPreviewColors {
    val background = Color(0xFF000000)
    val stage = Color(0xFF16181D)
    val textPrimary = Color(0xFFF3F3F3)
    val textSecondary = Color(0xFFC1C1C4)
    val outline = Color(0xFF44464D)
    val outlineWeak = Color(0x32C1C1C4)
    val accent = Color(0xFF9CA3AF)
    val accentOn = Color(0xFF15171A)
    val iconSurface = Color(0xFFFFFFFF)
    val iconFillBase = Color(0xFFB7BCC7)
    val iconOutline = Color(0xFF20242C)
    val sphereBaseHighlight = Color(0xFFF7F9FC)
    val sphereBaseMid = Color(0xFFDCE1E8)
    val sphereBaseShadow = Color(0xFFBEC5D0)
    val sphereOutline = Color(0xFF2A2E36)
    val sphereSpecular = Color(0x59FFFFFF)
    val roundedRectBaseTop = Color(0xFFF6F8FB)
    val roundedRectBaseBottom = Color(0xFFD6DCE5)
    val roundedRectOutline = Color(0xFF262B33)
    val roundedRectHighlight = Color(0x40FFFFFF)
}

private val WaveFillPreviewColorScheme = darkColorScheme(
    primary = WaveFillPreviewColors.accent,
    onPrimary = WaveFillPreviewColors.accentOn,
    secondary = WaveFillPreviewColors.accent,
    background = WaveFillPreviewColors.background,
    surface = WaveFillPreviewColors.background,
    surfaceVariant = WaveFillPreviewColors.stage,
    onSurface = WaveFillPreviewColors.textPrimary,
    onSurfaceVariant = WaveFillPreviewColors.textSecondary,
    outline = WaveFillPreviewColors.outline,
)

private enum class WavePreviewPage(val label: String) {
    Thumb("Thumb"),
    Sphere("Sphere"),
    Rectangle("Rectangle"),
}

fun waveFillProgressAnimationSpec(
    durationMillis: Int = WavePreviewCycleDurationMillis,
): FiniteAnimationSpec<Float> = tween(
    durationMillis = durationMillis.coerceAtLeast(1),
    easing = LinearEasing,
)

fun waveFillPhaseAnimationSpec(
    durationMillis: Int = WaveAnimationDurationMillis,
): DurationBasedAnimationSpec<Float> = tween(
    durationMillis = durationMillis.coerceAtLeast(1),
    easing = LinearEasing,
)

fun infiniteWaveFillPhaseAnimationSpec(
    durationMillis: Int = WaveAnimationDurationMillis,
): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
    animation = waveFillPhaseAnimationSpec(durationMillis),
    repeatMode = RepeatMode.Restart,
)

@Composable
private fun WaveFillPreviewTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = WaveFillPreviewColorScheme,
        content = content,
    )
}

class WaveFillAnimationState {
    internal val fillFraction = Animatable(0f)
    internal val phaseRadians = Animatable(0f)
    internal var completionSignal by mutableIntStateOf(0)

    val isAnimating: Boolean
        get() = fillFraction.isRunning || phaseRadians.isRunning

    suspend fun startAnimation(
        startFillFraction: Float = 0f,
        endFillFraction: Float = 1f,
        fillDurationMillis: Int = WavePreviewCycleDurationMillis,
        waveDurationMillis: Int = WaveAnimationDurationMillis,
        fillAnimationSpec: FiniteAnimationSpec<Float> =
            waveFillProgressAnimationSpec(fillDurationMillis),
        waveAnimationSpec: FiniteAnimationSpec<Float> =
            waveFillPhaseAnimationSpec(waveDurationMillis),
    ) = coroutineScope {
        fillFraction.stop()
        phaseRadians.stop()

        val resolvedStartFill = startFillFraction.coerceIn(0f, 1f)
        val resolvedEndFill = endFillFraction.coerceIn(0f, 1f)

        fillFraction.snapTo(resolvedStartFill)
        phaseRadians.snapTo(0f)

        var completed = false
        val waveJob = launch {
            var targetPhase = 0f
            while (true) {
                targetPhase += TwoPi
                phaseRadians.animateTo(
                    targetValue = targetPhase,
                    animationSpec = waveAnimationSpec,
                )
            }
        }

        try {
            fillFraction.animateTo(
                targetValue = resolvedEndFill,
                animationSpec = fillAnimationSpec,
            )
            completed = true
        } finally {
            waveJob.cancelAndJoin()
            phaseRadians.stop()
            if (completed) {
                completionSignal += 1
            }
        }
    }

    suspend fun stopAnimation(
        resetToFillFraction: Float? = null,
    ) {
        fillFraction.stop()
        phaseRadians.stop()
        resetToFillFraction?.let {
            fillFraction.snapTo(it.coerceIn(0f, 1f))
            phaseRadians.snapTo(0f)
        }
    }
}

@Composable
fun rememberWaveFillAnimationState(): WaveFillAnimationState {
    return remember { WaveFillAnimationState() }
}

fun Modifier.waveFill(
    fillFraction: Float,
    fillColor: Color,
    crestColor: Color = fillColor.copy(alpha = 0.28f),
    amplitudeFraction: Float = 0.08f,
    wavelengthFraction: Float = 1.10f,
    durationMillis: Int = WaveAnimationDurationMillis,
    animationSpec: InfiniteRepeatableSpec<Float> =
        infiniteWaveFillPhaseAnimationSpec(durationMillis),
    phaseRadians: Float? = null,
): Modifier = composed {
    val resolvedPhaseRadians = phaseRadians ?: rememberAnimatedWavePhase(animationSpec)

    this
        .graphicsLayer {
            compositingStrategy = CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val clampedFill = fillFraction.coerceIn(0f, 1f)
            val amplitudePx = (size.height * amplitudeFraction).coerceAtLeast(1f)
            val wavelengthPx = (size.width * wavelengthFraction).coerceAtLeast(1f)

            val crestStrokeWidth = (size.minDimension * 0.035f).coerceAtLeast(1f)

            val wavePaths = if (clampedFill in 0f..1f && clampedFill != 0f && clampedFill != 1f) {
                buildWavePaths(
                    width = size.width,
                    height = size.height,
                    fillFraction = clampedFill,
                    amplitudePx = amplitudePx,
                    wavelengthPx = wavelengthPx,
                    phaseRadians = resolvedPhaseRadians
                )
            } else {
                null
            }

            onDrawWithContent {
                drawContent()

                when {
                    clampedFill <= 0f -> Unit
                    clampedFill >= 1f -> {
                        drawRect(
                            color = fillColor,
                            blendMode = BlendMode.SrcAtop
                        )
                    }

                    wavePaths != null -> {
                        drawPath(
                            path = wavePaths.fillPath,
                            color = fillColor,
                            blendMode = BlendMode.SrcAtop
                        )
                        drawPath(
                            path = wavePaths.crestPath,
                            color = crestColor,
                            style = Stroke(width = crestStrokeWidth),
                            blendMode = BlendMode.SrcAtop
                        )
                    }
                }
            }
        }
}

fun Modifier.waveFillAnimation(
    state: WaveFillAnimationState,
    fillColor: Color,
    crestColor: Color = fillColor.copy(alpha = 0.28f),
    amplitudeFraction: Float = 0.06f,
    wavelengthFraction: Float = 1.15f,
    onComplete: (() -> Unit)? = null,
): Modifier = composed {
    val latestOnComplete = rememberUpdatedState(onComplete)

    LaunchedEffect(state.completionSignal) {
        if (state.completionSignal > 0) {
            latestOnComplete.value?.invoke()
        }
    }

    this.waveFill(
        fillFraction = state.fillFraction.value,
        fillColor = fillColor,
        crestColor = crestColor,
        amplitudeFraction = amplitudeFraction,
        wavelengthFraction = wavelengthFraction,
        phaseRadians = state.phaseRadians.value,
    )
}

@Composable
private fun rememberAnimatedWavePhase(
    animationSpec: InfiniteRepeatableSpec<Float>,
): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "wave-fill")
    val phase = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = TwoPi,
        animationSpec = animationSpec,
        label = "wave-phase"
    )
    return phase.value
}

private data class WavePaths(
    val fillPath: Path,
    val crestPath: Path,
)

private fun buildWavePaths(
    width: Float,
    height: Float,
    fillFraction: Float,
    amplitudePx: Float,
    wavelengthPx: Float,
    phaseRadians: Float,
): WavePaths {
    // Tie the wave baseline to flat fill progress; offscreen clipping trims overshoot near edges.
    val waterlineY = height * (1f - fillFraction)
    val startX = -wavelengthPx
    val endX = width + wavelengthPx
    val stepX = (wavelengthPx / 28f).coerceAtLeast(2f)

    val crestPath = Path()
    val fillPath = Path()

    var x = startX
    var firstPoint = true

    fillPath.moveTo(startX, height)

    while (x <= endX) {
        val y = waveY(
            x = x,
            waterlineY = waterlineY,
            amplitudePx = amplitudePx,
            wavelengthPx = wavelengthPx,
            phaseRadians = phaseRadians
        )

        if (firstPoint) {
            crestPath.moveTo(x, y)
            fillPath.lineTo(x, y)
            firstPoint = false
        } else {
            crestPath.lineTo(x, y)
            fillPath.lineTo(x, y)
        }

        x += stepX
    }

    fillPath.lineTo(endX, height)
    fillPath.close()

    return WavePaths(
        fillPath = fillPath,
        crestPath = crestPath
    )
}

private fun waveY(
    x: Float,
    waterlineY: Float,
    amplitudePx: Float,
    wavelengthPx: Float,
    phaseRadians: Float,
): Float {
    val primaryPhase = ((x / wavelengthPx) * TwoPi) + phaseRadians
    // Keep every component periodic over the same loop so phase wrapping is seamless.
    val secondaryPhase = (primaryPhase * 2f) - phaseRadians + WaveSecondaryPhaseOffsetRadians
    val primary = sin(primaryPhase) * amplitudePx
    val secondary = sin(secondaryPhase) * amplitudePx * 0.32f
    return waterlineY + primary + secondary
}

@Composable
private fun WaveFilledThumbIcon(
    fillColor: Color,
    crestColor: Color = fillColor.copy(alpha = 0.28f),
    fillFraction: Float,
    amplitudeFraction: Float = 0.06f,
    wavelengthFraction: Float = 1.15f,
    durationMillis: Int = WaveAnimationDurationMillis,
    phaseRadians: Float? = null,
    animationState: WaveFillAnimationState? = null,
    onComplete: (() -> Unit)? = null,
    iconSizeDp: Int = 160,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(WaveFillPreviewColors.iconSurface, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val waveModifier = if (animationState != null) {
            Modifier.waveFillAnimation(
                state = animationState,
                fillColor = fillColor,
                crestColor = crestColor,
                amplitudeFraction = amplitudeFraction,
                wavelengthFraction = wavelengthFraction,
                onComplete = onComplete,
            )
        } else {
            Modifier.waveFill(
                fillFraction = fillFraction,
                fillColor = fillColor,
                crestColor = crestColor,
                amplitudeFraction = amplitudeFraction,
                wavelengthFraction = wavelengthFraction,
                durationMillis = durationMillis,
                phaseRadians = phaseRadians,
            )
        }

        Icon(
            modifier = Modifier
                .size(iconSizeDp.dp)
                .then(waveModifier),
            imageVector = Icons.Filled.ThumbUp,
            tint = WaveFillPreviewColors.iconFillBase,
            contentDescription = null
        )
        Icon(
            modifier = Modifier.size(iconSizeDp.dp),
            imageVector = Icons.Outlined.ThumbUp,
            tint = WaveFillPreviewColors.iconOutline,
            contentDescription = null
        )
    }
}

@Composable
private fun WaveFilledSphere(
    fillColor: Color,
    crestColor: Color = fillColor.copy(alpha = 0.28f),
    fillFraction: Float,
    amplitudeFraction: Float = 0.06f,
    wavelengthFraction: Float = 1.15f,
    durationMillis: Int = WaveAnimationDurationMillis,
    phaseRadians: Float? = null,
    animationState: WaveFillAnimationState? = null,
    onComplete: (() -> Unit)? = null,
    sphereSizeDp: Int = 160,
    modifier: Modifier = Modifier,
) {
    val waveModifier = if (animationState != null) {
        Modifier.waveFillAnimation(
            state = animationState,
            fillColor = fillColor,
            crestColor = crestColor,
            amplitudeFraction = amplitudeFraction,
            wavelengthFraction = wavelengthFraction,
            onComplete = onComplete,
        )
    } else {
        Modifier.waveFill(
            fillFraction = fillFraction,
            fillColor = fillColor,
            crestColor = crestColor,
            amplitudeFraction = amplitudeFraction,
            wavelengthFraction = wavelengthFraction,
            durationMillis = durationMillis,
            phaseRadians = phaseRadians,
        )
    }

    val sphereBrush = Brush.radialGradient(
        colors = listOf(
            WaveFillPreviewColors.sphereBaseHighlight,
            WaveFillPreviewColors.sphereBaseMid,
            WaveFillPreviewColors.sphereBaseShadow,
        )
    )

    Box(
        modifier = modifier
            .background(
                color = WaveFillPreviewColors.iconSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(28.dp),
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(sphereSizeDp.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(waveModifier)
                    .clip(CircleShape)
                    .background(brush = sphereBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 2.dp,
                        color = WaveFillPreviewColors.sphereOutline,
                        shape = CircleShape,
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 34.dp, top = 28.dp)
                    .size(46.dp)
                    .background(
                        color = WaveFillPreviewColors.sphereSpecular,
                        shape = CircleShape,
                    )
            )
        }
    }
}

@Composable
private fun WaveFilledRectangle(
    fillColor: Color,
    crestColor: Color = fillColor.copy(alpha = 0.28f),
    fillFraction: Float,
    amplitudeFraction: Float = 0.06f,
    wavelengthFraction: Float = 1.15f,
    durationMillis: Int = WaveAnimationDurationMillis,
    phaseRadians: Float? = null,
    animationState: WaveFillAnimationState? = null,
    onComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val waveModifier = if (animationState != null) {
        Modifier.waveFillAnimation(
            state = animationState,
            fillColor = fillColor,
            crestColor = crestColor,
            amplitudeFraction = amplitudeFraction,
            wavelengthFraction = wavelengthFraction,
            onComplete = onComplete,
        )
    } else {
        Modifier.waveFill(
            fillFraction = fillFraction,
            fillColor = fillColor,
            crestColor = crestColor,
            amplitudeFraction = amplitudeFraction,
            wavelengthFraction = wavelengthFraction,
            durationMillis = durationMillis,
            phaseRadians = phaseRadians,
        )
    }

    val shape = RectangleShape
    val surfaceBrush = Brush.linearGradient(
        colors = listOf(
            WaveFillPreviewColors.roundedRectBaseTop,
            WaveFillPreviewColors.roundedRectBaseBottom,
        )
    )

    Box(
        modifier = modifier
            .background(
                color = WaveFillPreviewColors.iconSurface.copy(alpha = 0.08f),
                shape = RoundedCornerShape(28.dp),
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.size(width = 208.dp, height = 164.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(waveModifier)
                    .clip(shape)
                    .background(brush = surfaceBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .border(
                        width = 2.dp,
                        color = WaveFillPreviewColors.roundedRectOutline,
                        shape = shape,
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp)
                    .size(width = 132.dp, height = 16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(WaveFillPreviewColors.roundedRectHighlight)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF3F5F8)
@Composable
private fun WaveFillModifierPreview() {
    WaveFillPreview()
}

@Composable
private fun WaveFillPreview(
    modifier: Modifier = Modifier,
) {
    val animationState = rememberWaveFillAnimationState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { WavePreviewPage.entries.size })
    var animationJob by remember { mutableStateOf<Job?>(null) }
    var statusText by remember { mutableStateOf("Press Start or Replay.") }

    var fillStartFraction by remember { mutableFloatStateOf(0f) }
    var fillEndFraction by remember { mutableFloatStateOf(1f) }
    var amplitudeFraction by remember { mutableFloatStateOf(0.08f) }
    var wavelengthFraction by remember { mutableFloatStateOf(1.10f) }
    var waveSpeedMs by remember { mutableFloatStateOf(1400f) }
    var fillCycleMs by remember { mutableFloatStateOf(WavePreviewCycleDurationMillis.toFloat()) }
    var crestColorIndex by remember { mutableIntStateOf(0) }
    val crestColor = WavePreviewCrestColorOptions[crestColorIndex]

    DisposableEffect(Unit) {
        onDispose {
            animationJob?.cancel()
            coroutineScope.launch {
                animationState.stopAnimation(
                    resetToFillFraction = fillStartFraction,
                )
            }
        }
    }

    WaveFillPreviewTheme {
        Column(
            modifier = Modifier
                .then(modifier)
                .fillMaxSize()
                .background(WaveFillPreviewColors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(WaveFillPreviewColors.stage),
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                    ) { page ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            when (WavePreviewPage.entries[page]) {
                                WavePreviewPage.Thumb -> WaveFilledThumbIcon(
                                    fillColor = WavePreviewFillColor,
                                    crestColor = crestColor,
                                    fillFraction = fillStartFraction,
                                    amplitudeFraction = amplitudeFraction,
                                    wavelengthFraction = wavelengthFraction,
                                    durationMillis = waveSpeedMs.roundToInt(),
                                    animationState = animationState,
                                    onComplete = {
                                        statusText = "Completed. Press Replay to run again."
                                    },
                                    iconSizeDp = 192,
                                )

                                WavePreviewPage.Sphere -> WaveFilledSphere(
                                    fillColor = WavePreviewFillColor,
                                    crestColor = crestColor,
                                    fillFraction = fillStartFraction,
                                    amplitudeFraction = amplitudeFraction,
                                    wavelengthFraction = wavelengthFraction,
                                    durationMillis = waveSpeedMs.roundToInt(),
                                    animationState = animationState,
                                    onComplete = {
                                        statusText = "Completed. Press Replay to run again."
                                    },
                                    sphereSizeDp = 192,
                                )

                                WavePreviewPage.Rectangle -> WaveFilledRectangle(
                                    fillColor = WavePreviewFillColor,
                                    crestColor = crestColor,
                                    fillFraction = fillStartFraction,
                                    amplitudeFraction = amplitudeFraction,
                                    wavelengthFraction = wavelengthFraction,
                                    durationMillis = waveSpeedMs.roundToInt(),
                                    animationState = animationState,
                                    onComplete = {
                                        statusText = "Completed. Press Replay to run again."
                                    },
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Swipe preview: ${WavePreviewPage.entries[pagerState.currentPage].label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WaveFillPreviewColors.textSecondary,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WavePreviewPage.entries.forEachIndexed { index, page ->
                        Box(
                            modifier = Modifier
                                .size(if (index == pagerState.currentPage) 22.dp else 8.dp, 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index == pagerState.currentPage) {
                                        WaveFillPreviewColors.accent
                                    } else {
                                        WaveFillPreviewColors.outlineWeak
                                    }
                                )
                        )
                        if (index == pagerState.currentPage) {
                            Text(
                                text = page.label,
                                style = MaterialTheme.typography.bodyMedium,
                                color = WaveFillPreviewColors.textPrimary,
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AnimationTutorialButton(
                    onClick = {
                        statusText = "Running."
                        val previousJob = animationJob
                        animationJob = coroutineScope.launch {
                            previousJob?.cancelAndJoin()
                            animationState.startAnimation(
                                startFillFraction = fillStartFraction,
                                endFillFraction = fillEndFraction,
                                fillDurationMillis = fillCycleMs.roundToInt(),
                                waveDurationMillis = waveSpeedMs.roundToInt(),
                            )
                        }
                    }
                ) {
                    Text(if (animationState.isAnimating) "Replay" else "Start")
                }
                AnimationTutorialButton(
                    onClick = {
                        statusText = "Stopped. Press Start or Replay."
                        val previousJob = animationJob
                        animationJob = coroutineScope.launch {
                            previousJob?.cancelAndJoin()
                            animationState.stopAnimation(
                                resetToFillFraction = fillStartFraction,
                            )
                        }
                    }
                ) {
                    Text("Stop")
                }
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.bodyMedium,
                color = WaveFillPreviewColors.textSecondary,
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                WavePreviewSlider(
                    title = "Fill start",
                    value = fillStartFraction,
                    valueText = "%.2f".format(fillStartFraction),
                    range = 0f..1f,
                    onValueChange = { fillStartFraction = it }
                )
                WavePreviewSlider(
                    title = "Fill end",
                    value = fillEndFraction,
                    valueText = "%.2f".format(fillEndFraction),
                    range = 0f..1f,
                    onValueChange = { fillEndFraction = it }
                )
                WavePreviewSlider(
                    title = "Amplitude",
                    value = amplitudeFraction,
                    valueText = "%.2f".format(amplitudeFraction),
                    range = 0.0f..0.20f,
                    onValueChange = { amplitudeFraction = it }
                )
                WavePreviewSlider(
                    title = "Wavelength",
                    value = wavelengthFraction,
                    valueText = "%.2f".format(wavelengthFraction),
                    range = 0.0f..2.00f,
                    onValueChange = { wavelengthFraction = it }
                )
                WavePreviewColorSelector(
                    title = "Crest color",
                    colors = WavePreviewCrestColorOptions,
                    selectedIndex = crestColorIndex,
                    onSelectedIndexChange = { crestColorIndex = it }
                )
                WavePreviewSlider(
                    title = "Wave speed",
                    value = waveSpeedMs,
                    valueText = "${waveSpeedMs.roundToInt()} ms",
                    range = 300f..3000f,
                    onValueChange = { waveSpeedMs = it }
                )
                WavePreviewSlider(
                    title = "Fill cycle",
                    value = fillCycleMs,
                    valueText = "${fillCycleMs.roundToInt()} ms",
                    range = 400f..4000f,
                    onValueChange = { fillCycleMs = it }
                )
            }
        }
    }
}

@Composable
private fun WavePreviewSlider(
    title: String,
    value: Float,
    valueText: String,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = "$title: $valueText",
            style = MaterialTheme.typography.bodyMedium,
            color = WaveFillPreviewColors.textPrimary,
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
        )
    }
}

@Composable
private fun WavePreviewColorSelector(
    title: String,
    colors: List<Color>,
    selectedIndex: Int,
    fillColor: Color = WavePreviewFillColor,
    onSelectedIndexChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = WaveFillPreviewColors.textPrimary,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            colors.forEachIndexed { index, color ->
                OutlinedButton(
                    onClick = { onSelectedIndexChange(index) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(
                        width = if (selectedIndex == index) 2.dp else 1.dp,
                        color = if (selectedIndex == index) {
                            WaveFillPreviewColors.textPrimary
                        } else {
                            WaveFillPreviewColors.outlineWeak
                        }
                    ),
                    modifier = Modifier.size(56.dp)
                ) {
                    val previewCrestColor = color.compositeOver(fillColor)
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(color = fillColor, shape = CircleShape)
                            .border(
                                width = 1.dp,
                                color = WaveFillPreviewColors.outlineWeak,
                                shape = CircleShape
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 6.dp)
                                .size(width = 22.dp, height = 9.dp)
                                .background(
                                    color = previewCrestColor,
                                    shape = RoundedCornerShape(50)
                                )
                        )
                    }
                }
            }
        }
    }
}
