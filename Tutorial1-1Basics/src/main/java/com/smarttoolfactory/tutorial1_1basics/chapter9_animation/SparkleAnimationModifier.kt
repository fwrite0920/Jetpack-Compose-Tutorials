package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInCirc
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

private const val DefaultSparkleDurationMillis = 900
private const val DefaultBlastDurationMillis = 260
private const val ShootSparkleStartAnchorFraction = 0.3f
private const val SparkleBlinkStartFraction = 0.74f

private val DefaultBlastColor = Color(0xFFFFB300)
private val DefaultAccentSparkleColors = listOf(
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

private val DefaultAccentSparkleColorNames = listOf(
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

enum class SparkleAnimationMode {
    Shoot,
    Blink,
}

enum class BlastAnimationMode {
    FilledAndRing,
    RingReveal,
}

data class BlastAnimationModel(
    val mode: BlastAnimationMode = BlastAnimationMode.FilledAndRing,
    val color: Color = DefaultBlastColor,
    val animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = DefaultBlastDurationMillis,
        easing = EaseInCirc
    ),
    val delayMs: Int = 0,
    val minRadius: Dp = 3.dp,
    val maxRadius: Dp = 16.dp,
    val strokeWidth: Dp = 2.dp,
    val minAlpha: Float = 0f,
    val maxAlpha: Float = 1f,
    val repeatCount: Int = 1,
)

data class SparkleAnimationModel(
    val mode: SparkleAnimationMode = SparkleAnimationMode.Shoot,
    val color: Color = Color.Unspecified,
    val colors: List<Color> = emptyList(),
    val durationMs: Int = DefaultSparkleDurationMillis,
    val delayMs: Int = 0,
    val sparkleCount: Int = 8,
    val minRadius: Dp = 2.dp,
    val maxRadius: Dp = 6.dp,
    val safeRadius: Dp = 0.dp,
    val blinkCycles: Float? = null,
    val minBlinkDurationMs: Int? = null,
    val maxBlinkDurationMs: Int? = null,
    val repeatCount: Int = 1,
)

private data class ShootSparkleSpec(
    val angleRadians: Float,
    val startDistancePx: Float,
    val travelDistancePx: Float,
    val startFraction: Float,
    val endFraction: Float,
    val minRadiusPx: Float,
    val maxRadiusPx: Float,
    val rotationRadians: Float,
    val blinkCycles: Float,
    val blinkPhase: Float,
    val color: Color,
)

private data class BlinkSparkleSpec(
    val centerX: Float,
    val centerY: Float,
    val startFraction: Float,
    val endFraction: Float,
    val minRadiusPx: Float,
    val maxRadiusPx: Float,
    val blinkCycles: Float,
    val blinkPhase: Float,
    val color: Color,
)

class SparkleAnimationState internal constructor() {
    internal val blastProgress = Animatable(0f)
    internal val sparkleProgress = Animatable(0f)
    internal var blastGeneration by mutableIntStateOf(0)
    internal var sparkleGeneration by mutableIntStateOf(0)

    val isAnimating: Boolean
        get() = blastProgress.isRunning || sparkleProgress.isRunning

    suspend fun startAnimation(
        blastModel: BlastAnimationModel = BlastAnimationModel(),
        sparkleModel: SparkleAnimationModel = SparkleAnimationModel(),
    ) = coroutineScope {
        launch {
            runRepeatedAnimation(
                progress = blastProgress,
                animationSpec = blastModel.animationSpec,
                delayMs = blastModel.delayMs,
                repeatCount = blastModel.repeatCount,
                onCycleStart = { blastGeneration += 1 }
            )
        }
        launch {
            runRepeatedAnimation(
                progress = sparkleProgress,
                animationSpec = tween(
                    durationMillis = sparkleModel.durationMs.coerceAtLeast(1),
                    easing = LinearEasing,
                ),
                delayMs = sparkleModel.delayMs,
                repeatCount = sparkleModel.repeatCount,
                onCycleStart = { sparkleGeneration += 1 }
            )
        }
    }

    suspend fun stopAnimation() {
        blastProgress.stop()
        sparkleProgress.stop()
        blastProgress.snapTo(0f)
        sparkleProgress.snapTo(0f)
    }
}

@Composable
fun rememberSparkleAnimationState(): SparkleAnimationState {
    return remember { SparkleAnimationState() }
}

@Composable
fun Modifier.blastAndSparkle(
    state: SparkleAnimationState,
    blastModel: BlastAnimationModel = BlastAnimationModel(),
    sparkleModel: SparkleAnimationModel = SparkleAnimationModel(),
    drawLayer: AnimationDrawLayer = AnimationDrawLayer.AboveContent,
): Modifier {
    val resolvedSparkleColors = remember(sparkleModel.color, sparkleModel.colors) {
        resolveSparkleColors(sparkleModel)
    }
    val resolvedBlastColor = remember(blastModel.color) {
        resolveBlastColor(blastModel)
    }
    val sparkleGeneration = state.sparkleGeneration

    return this.then(
        Modifier.drawWithCache {
            val center = Offset(size.width / 2f, size.height / 2f)
            val minDimension = min(size.width, size.height)
            val blastMinRadiusPx = blastModel.minRadius.toPx().coerceAtLeast(1f)
            val blastMaxRadiusPx = min(blastModel.maxRadius.toPx(), minDimension * 0.9f)
                .coerceAtLeast(blastMinRadiusPx)
            val blastStrokeWidthPx = blastModel.strokeWidth.toPx().coerceAtLeast(0.5f)
            val sparkleCount = sparkleModel.sparkleCount.coerceAtLeast(0)

            val shootSparkles = if (sparkleModel.mode == SparkleAnimationMode.Shoot) {
                generateShootSparkleSpecs(
                    count = sparkleCount,
                    sparkleDurationMs = sparkleModel.durationMs,
                    safeRadiusPx = sparkleModel.safeRadius.toPx(),
                    maxTravelDistancePx = minDimension * 1.15f,
                    minSparkleRadiusPx = sparkleModel.minRadius.toPx(),
                    maxSparkleRadiusPx = sparkleModel.maxRadius.toPx(),
                    random = Random(
                        31 * size.width.toBits() +
                            17 * size.height.toBits() +
                            sparkleCount * 13 +
                            resolvedSparkleColors.hashCode() +
                            sparkleGeneration * 37
                    ),
                    colors = resolvedSparkleColors,
                    blinkCyclesOverride = sparkleModel.blinkCycles,
                    minBlinkDurationMs = sparkleModel.minBlinkDurationMs,
                    maxBlinkDurationMs = sparkleModel.maxBlinkDurationMs,
                )
            } else {
                emptyList()
            }

            val blinkSparkles = if (sparkleModel.mode == SparkleAnimationMode.Blink) {
                generateBlinkSparkleSpecs(
                    count = sparkleCount,
                    sparkleDurationMs = sparkleModel.durationMs,
                    width = size.width,
                    height = size.height,
                    safeRadiusPx = sparkleModel.safeRadius.toPx(),
                    minSparkleRadiusPx = sparkleModel.minRadius.toPx(),
                    maxSparkleRadiusPx = sparkleModel.maxRadius.toPx(),
                    random = Random(
                        41 * size.width.toBits() +
                            23 * size.height.toBits() +
                            sparkleCount * 19 +
                            resolvedSparkleColors.hashCode() +
                            sparkleGeneration * 43
                    ),
                    colors = resolvedSparkleColors,
                    blinkCyclesOverride = sparkleModel.blinkCycles,
                    minBlinkDurationMs = sparkleModel.minBlinkDurationMs,
                    maxBlinkDurationMs = sparkleModel.maxBlinkDurationMs,
                )
            } else {
                emptyList()
            }

            val drawAnimationLayer: DrawScope.() -> Unit = {
                val blastProgress = state.blastProgress.value
                if (blastModel.repeatCount != 0 && blastProgress > 0f) {
                    drawBlast(
                        progress = blastProgress,
                        center = center,
                        color = resolvedBlastColor,
                        mode = blastModel.mode,
                        minRadiusPx = blastMinRadiusPx,
                        maxRadiusPx = blastMaxRadiusPx,
                        strokeWidthPx = blastStrokeWidthPx,
                        minAlpha = blastModel.minAlpha,
                        maxAlpha = blastModel.maxAlpha,
                    )
                }

                val sparkleProgress = state.sparkleProgress.value
                if (sparkleModel.repeatCount != 0 && sparkleProgress > 0f) {
                    when (sparkleModel.mode) {
                        SparkleAnimationMode.Shoot -> {
                            shootSparkles.forEach { sparkle ->
                                drawShootSparkle(
                                    sparkle = sparkle,
                                    overallProgress = sparkleProgress,
                                    center = center,
                                )
                            }
                        }

                        SparkleAnimationMode.Blink -> {
                            blinkSparkles.forEach { sparkle ->
                                drawBlinkSparkle(
                                    sparkle = sparkle,
                                    overallProgress = sparkleProgress,
                                )
                            }
                        }
                    }
                }
            }

            onDrawWithContent {
                when (drawLayer) {
                    AnimationDrawLayer.BehindContent -> {
                        drawAnimationLayer()
                        drawContent()
                    }

                    AnimationDrawLayer.AboveContent -> {
                        drawContent()
                        drawAnimationLayer()
                    }
                }
            }
        }
    )
}

private fun resolveSparkleColors(model: SparkleAnimationModel): List<Color> {
    return when {
        model.colors.isNotEmpty() -> model.colors
        model.color != Color.Unspecified -> listOf(model.color)
        else -> DefaultAccentSparkleColors
    }
}

private fun resolveBlastColor(
    model: BlastAnimationModel,
): Color {
    return when {
        model.color != Color.Unspecified -> model.color
        else -> DefaultBlastColor
    }
}

fun blastBuildUpAnimationSpec(
    durationMillis: Int = DefaultBlastDurationMillis,
): FiniteAnimationSpec<Float> {
    val clampedDurationMillis = durationMillis.coerceAtLeast(1)
    return keyframes {
        this.durationMillis = clampedDurationMillis
        0f at 0
        0.03f at (clampedDurationMillis * 0.18f).roundToInt()
        0.09f at (clampedDurationMillis * 0.42f).roundToInt()
        0.22f at (clampedDurationMillis * 0.62f).roundToInt()
        1f at clampedDurationMillis
    }
}

private suspend fun runRepeatedAnimation(
    progress: Animatable<Float, AnimationVector1D>,
    animationSpec: FiniteAnimationSpec<Float>,
    delayMs: Int,
    repeatCount: Int,
    onCycleStart: () -> Unit,
) {
    if (repeatCount <= 0) {
        progress.snapTo(0f)
        return
    }

    var completedRuns = 0
    while (repeatCount == Int.MAX_VALUE || completedRuns < repeatCount) {
        if (delayMs > 0) {
            delay(delayMs.toLong())
        }
        onCycleStart()
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = animationSpec,
        )
        completedRuns += 1
    }

    progress.snapTo(0f)
}

private fun generateShootSparkleSpecs(
    count: Int,
    sparkleDurationMs: Int,
    safeRadiusPx: Float,
    maxTravelDistancePx: Float,
    minSparkleRadiusPx: Float,
    maxSparkleRadiusPx: Float,
    random: Random,
    colors: List<Color>,
    blinkCyclesOverride: Float? = null,
    minBlinkDurationMs: Int? = null,
    maxBlinkDurationMs: Int? = null,
): List<ShootSparkleSpec> {
    if (count <= 0) return emptyList()
    val palette = colors.ifEmpty { listOf(Color.White) }
    val clampedSafeRadiusPx = safeRadiusPx.coerceAtLeast(0f)
    val minTravelDistancePx = max(maxTravelDistancePx * 0.4f, clampedSafeRadiusPx + maxSparkleRadiusPx)

    return List(count) { index ->
        val baseAngle = (2f * PI.toFloat() * index) / count
        val angleOffset = random.nextFloat() * (PI.toFloat() / count) - (PI.toFloat() / (count * 2f))
        val startFraction = lerp(
            ShootSparkleStartAnchorFraction * 0.45f,
            ShootSparkleStartAnchorFraction + 0.18f,
            random.nextFloat(),
        )
        val endFraction = lerp(0.78f, 1f, random.nextFloat())
        val visibleBlinkDurationMs = sparkleDurationMs.coerceAtLeast(1) *
            (endFraction - startFraction).coerceAtLeast(0.01f) *
            (1f - SparkleBlinkStartFraction)
        ShootSparkleSpec(
            angleRadians = baseAngle + angleOffset,
            startDistancePx = clampedSafeRadiusPx,
            travelDistancePx = lerp(
                start = minTravelDistancePx.coerceAtMost(maxTravelDistancePx),
                stop = maxTravelDistancePx,
                fraction = random.nextFloat(),
            ),
            startFraction = startFraction,
            endFraction = endFraction,
            minRadiusPx = lerp(minSparkleRadiusPx * 0.75f, minSparkleRadiusPx * 1.2f, random.nextFloat()),
            maxRadiusPx = lerp(maxSparkleRadiusPx * 0.8f, maxSparkleRadiusPx * 1.4f, random.nextFloat()),
            rotationRadians = lerp(0f, PI.toFloat(), random.nextFloat()),
            blinkCycles = resolveBlinkCycles(
                visibleDurationMs = visibleBlinkDurationMs,
                defaultMinCycles = 1.5f,
                defaultMaxCycles = 3.5f,
                blinkCyclesOverride = blinkCyclesOverride,
                minBlinkDurationMs = minBlinkDurationMs,
                maxBlinkDurationMs = maxBlinkDurationMs,
                random = random,
            ),
            blinkPhase = lerp(0f, 2f * PI.toFloat(), random.nextFloat()),
            color = palette[random.nextInt(palette.size)],
        )
    }
}

private fun generateBlinkSparkleSpecs(
    count: Int,
    sparkleDurationMs: Int,
    width: Float,
    height: Float,
    safeRadiusPx: Float,
    minSparkleRadiusPx: Float,
    maxSparkleRadiusPx: Float,
    random: Random,
    colors: List<Color>,
    blinkCyclesOverride: Float? = null,
    minBlinkDurationMs: Int? = null,
    maxBlinkDurationMs: Int? = null,
): List<BlinkSparkleSpec> {
    if (count <= 0) return emptyList()
    val palette = colors.ifEmpty { listOf(Color.White) }
    val horizontalInset = width * 0.18f
    val verticalInset = height * 0.18f

    return List(count) {
        val center = generateBlinkSparkleCenter(
            width = width,
            height = height,
            horizontalInset = horizontalInset,
            verticalInset = verticalInset,
            safeRadiusPx = safeRadiusPx,
            random = random,
        )
        val startFraction = lerp(0f, 0.35f, random.nextFloat())
        val endFraction = lerp(0.72f, 1f, random.nextFloat())
        val visibleBlinkDurationMs = sparkleDurationMs.coerceAtLeast(1) *
            (endFraction - startFraction).coerceAtLeast(0.01f)
        BlinkSparkleSpec(
            centerX = center.x,
            centerY = center.y,
            startFraction = startFraction,
            endFraction = endFraction,
            minRadiusPx = lerp(minSparkleRadiusPx * 0.7f, minSparkleRadiusPx * 1.15f, random.nextFloat()),
            maxRadiusPx = lerp(maxSparkleRadiusPx * 0.8f, maxSparkleRadiusPx * 1.35f, random.nextFloat()),
            blinkCycles = resolveBlinkCycles(
                visibleDurationMs = visibleBlinkDurationMs,
                defaultMinCycles = 1.8f,
                defaultMaxCycles = 4.2f,
                blinkCyclesOverride = blinkCyclesOverride,
                minBlinkDurationMs = minBlinkDurationMs,
                maxBlinkDurationMs = maxBlinkDurationMs,
                random = random,
            ),
            blinkPhase = lerp(0f, 2f * PI.toFloat(), random.nextFloat()),
            color = palette[random.nextInt(palette.size)],
        )
    }
}

private fun resolveBlinkCycles(
    visibleDurationMs: Float,
    defaultMinCycles: Float,
    defaultMaxCycles: Float,
    blinkCyclesOverride: Float?,
    minBlinkDurationMs: Int?,
    maxBlinkDurationMs: Int?,
    random: Random,
): Float {
    if (blinkCyclesOverride != null) {
        return blinkCyclesOverride.coerceAtLeast(0.1f)
    }

    val resolvedBlinkDurationMs = resolveBlinkDurationMs(
        minBlinkDurationMs = minBlinkDurationMs,
        maxBlinkDurationMs = maxBlinkDurationMs,
        random = random,
    )
    if (resolvedBlinkDurationMs != null) {
        return (visibleDurationMs / resolvedBlinkDurationMs)
            .coerceAtLeast(0.1f)
    }

    return lerp(defaultMinCycles, defaultMaxCycles, random.nextFloat())
}

private fun resolveBlinkDurationMs(
    minBlinkDurationMs: Int?,
    maxBlinkDurationMs: Int?,
    random: Random,
): Float? {
    val resolvedMinBlinkDurationMs = (minBlinkDurationMs ?: maxBlinkDurationMs)
        ?.coerceAtLeast(1)
        ?: return null
    val resolvedMaxBlinkDurationMs = (maxBlinkDurationMs ?: resolvedMinBlinkDurationMs)
        .coerceAtLeast(resolvedMinBlinkDurationMs)

    return lerp(
        start = resolvedMinBlinkDurationMs.toFloat(),
        stop = resolvedMaxBlinkDurationMs.toFloat(),
        fraction = random.nextFloat(),
    )
}

private fun generateBlinkSparkleCenter(
    width: Float,
    height: Float,
    horizontalInset: Float,
    verticalInset: Float,
    safeRadiusPx: Float,
    random: Random,
): Offset {
    val left = horizontalInset
    val right = width - horizontalInset
    val top = verticalInset
    val bottom = height - verticalInset
    val center = Offset(width / 2f, height / 2f)

    if (safeRadiusPx <= 0f) {
        return Offset(
            x = lerp(left, right, random.nextFloat()),
            y = lerp(top, bottom, random.nextFloat()),
        )
    }

    val maxPossibleDistance = maxDistanceToRectCorner(
        center = center,
        left = left,
        right = right,
        top = top,
        bottom = bottom,
    )
    val clampedSafeRadiusPx = safeRadiusPx.coerceIn(0f, maxPossibleDistance)

    repeat(48) {
        val angle = random.nextFloat() * (2f * PI.toFloat())
        val maxDistanceForAngle = distanceToRectBounds(
            center = center,
            left = left,
            right = right,
            top = top,
            bottom = bottom,
            angleRadians = angle,
        )
        if (maxDistanceForAngle >= clampedSafeRadiusPx) {
            val distance = lerp(
                start = clampedSafeRadiusPx,
                stop = maxDistanceForAngle,
                fraction = random.nextFloat(),
            )
            return Offset(
                x = center.x + cosine(angle) * distance,
                y = center.y + sine(angle) * distance,
            )
        }
    }

    val fallbackAngle = findBestRectAngle(
        center = center,
        left = left,
        right = right,
        top = top,
        bottom = bottom,
    )
    val fallbackDistance = min(
        clampedSafeRadiusPx,
        distanceToRectBounds(
            center = center,
            left = left,
            right = right,
            top = top,
            bottom = bottom,
            angleRadians = fallbackAngle,
        ),
    )
    return Offset(
        x = center.x + cosine(fallbackAngle) * fallbackDistance,
        y = center.y + sine(fallbackAngle) * fallbackDistance,
    )
}

private fun distanceToRectBounds(
    center: Offset,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
    angleRadians: Float,
): Float {
    val directionX = cosine(angleRadians)
    val directionY = sine(angleRadians)
    var distance = Float.POSITIVE_INFINITY

    if (directionX > 0f) {
        distance = min(distance, (right - center.x) / directionX)
    } else if (directionX < 0f) {
        distance = min(distance, (left - center.x) / directionX)
    }

    if (directionY > 0f) {
        distance = min(distance, (bottom - center.y) / directionY)
    } else if (directionY < 0f) {
        distance = min(distance, (top - center.y) / directionY)
    }

    return distance.coerceAtLeast(0f)
}

private fun maxDistanceToRectCorner(
    center: Offset,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
): Float {
    val corners = listOf(
        Offset(left, top),
        Offset(right, top),
        Offset(left, bottom),
        Offset(right, bottom),
    )
    return corners.maxOf { (it - center).getDistance() }
}

private fun findBestRectAngle(
    center: Offset,
    left: Float,
    right: Float,
    top: Float,
    bottom: Float,
): Float {
    var bestAngle = 0f
    var bestDistance = 0f

    repeat(72) { index ->
        val angle = index * (2f * PI.toFloat() / 72f)
        val distance = distanceToRectBounds(
            center = center,
            left = left,
            right = right,
            top = top,
            bottom = bottom,
            angleRadians = angle,
        )
        if (distance > bestDistance) {
            bestDistance = distance
            bestAngle = angle
        }
    }

    return bestAngle
}

private fun DrawScope.drawBlast(
    progress: Float,
    center: Offset,
    color: Color,
    mode: BlastAnimationMode,
    minRadiusPx: Float,
    maxRadiusPx: Float,
    strokeWidthPx: Float,
    minAlpha: Float,
    maxAlpha: Float,
) {
    val blastProgress = progress.coerceIn(0f, 1f)
    if (blastProgress <= 0f) return

    when (mode) {
        BlastAnimationMode.FilledAndRing -> {
            drawFilledAndRingBlast(
                blastProgress = blastProgress,
                center = center,
                color = color,
                minRadiusPx = minRadiusPx,
                maxRadiusPx = maxRadiusPx,
                strokeWidthPx = strokeWidthPx,
                minAlpha = minAlpha,
                maxAlpha = maxAlpha,
            )
        }

        BlastAnimationMode.RingReveal -> {
            drawRingRevealBlast(
                blastProgress = blastProgress,
                center = center,
                color = color,
                minRadiusPx = minRadiusPx,
                maxRadiusPx = maxRadiusPx,
                strokeWidthPx = strokeWidthPx,
                minAlpha = minAlpha,
                maxAlpha = maxAlpha,
            )
        }
    }
}

private fun DrawScope.drawFilledAndRingBlast(
    blastProgress: Float,
    center: Offset,
    color: Color,
    minRadiusPx: Float,
    maxRadiusPx: Float,
    strokeWidthPx: Float,
    minAlpha: Float,
    maxAlpha: Float,
) {
    val easedBlastProgress = easeOutCubic(blastProgress)
    val outerRadius = lerp(
        start = minRadiusPx,
        stop = maxRadiusPx,
        fraction = easedBlastProgress,
    )

    val filledAlphaProgress = 1f - normalizedProgress(
        value = blastProgress,
        start = 0f,
        end = 0.3f,
    )
    if (filledAlphaProgress > 0f) {
        val filledAlpha = resolveRangedAlpha(
            alphaProgress = filledAlphaProgress,
            minAlpha = minAlpha,
            maxAlpha = maxAlpha,
        )
        drawCircle(
            color = color.copy(alpha = filledAlpha),
            radius = outerRadius,
            center = center,
        )
    }

    val initialRingWidth = max(strokeWidthPx, minRadiusPx * 0.9f)
    val ringWidth = lerp(
        start = initialRingWidth,
        stop = 0f,
        fraction = blastProgress,
    )
    val ringAlphaProgress = 1f - blastProgress
    if (ringWidth <= 0f || ringAlphaProgress <= 0f) return

    val ringRadius = (outerRadius - ringWidth / 2f).coerceAtLeast(0f)
    val ringAlpha = resolveRangedAlpha(
        alphaProgress = ringAlphaProgress,
        minAlpha = minAlpha,
        maxAlpha = maxAlpha,
    )
    drawCircle(
        color = color.copy(alpha = ringAlpha),
        radius = ringRadius,
        center = center,
        style = Stroke(width = ringWidth),
    )
}

private fun DrawScope.drawRingRevealBlast(
    blastProgress: Float,
    center: Offset,
    color: Color,
    minRadiusPx: Float,
    maxRadiusPx: Float,
    strokeWidthPx: Float,
    minAlpha: Float,
    maxAlpha: Float,
) {
    val easedBlastProgress = easeOutCubic(blastProgress)
    val outerRadius = lerp(
        start = minRadiusPx,
        stop = maxRadiusPx,
        fraction = easedBlastProgress,
    )
    val ringWidth = lerp(
        start = max(strokeWidthPx, minRadiusPx * 0.9f),
        stop = 0f,
        fraction = blastProgress,
    )
    val ringAlphaProgress = normalizedProgress(
        value = blastProgress,
        start = 0f,
        end = 0.36f,
    )
    if (ringWidth <= 0f || ringAlphaProgress <= 0f) return

    val ringRadius = (outerRadius - ringWidth / 2f).coerceAtLeast(0f)
    val ringAlpha = resolveRangedAlpha(
        alphaProgress = ringAlphaProgress,
        minAlpha = minAlpha,
        maxAlpha = maxAlpha,
    )
    drawCircle(
        color = color.copy(alpha = ringAlpha),
        radius = ringRadius,
        center = center,
        style = Stroke(width = ringWidth),
    )
}

private fun resolveRangedAlpha(
    alphaProgress: Float,
    minAlpha: Float,
    maxAlpha: Float,
): Float {
    val clampedMinAlpha = minAlpha.coerceIn(0f, 1f)
    val clampedMaxAlpha = maxAlpha.coerceIn(clampedMinAlpha, 1f)
    return lerp(
        start = clampedMinAlpha,
        stop = clampedMaxAlpha,
        fraction = alphaProgress.coerceIn(0f, 1f),
    )
}

private fun DrawScope.drawShootSparkle(
    sparkle: ShootSparkleSpec,
    overallProgress: Float,
    center: Offset,
) {
    val localProgress = normalizedProgress(
        value = overallProgress,
        start = sparkle.startFraction,
        end = sparkle.endFraction,
    )
    if (localProgress <= 0f) return

    val travelProgress = easeOutCubic(localProgress)
    val currentDistance = lerp(
        start = sparkle.startDistancePx,
        stop = sparkle.travelDistancePx,
        fraction = travelProgress,
    )
    val position = Offset(
        x = center.x + cosine(sparkle.angleRadians) * currentDistance,
        y = center.y + sine(sparkle.angleRadians) * currentDistance,
    )

    val growthProgress = normalizedProgress(localProgress, 0f, 0.55f)
    val currentRadius = lerp(
        start = sparkle.minRadiusPx,
        stop = sparkle.maxRadiusPx,
        fraction = easeOutCubic(growthProgress),
    )

    val morphProgress = normalizedProgress(localProgress, 0.18f, 0.6f)
    val fadeProgress = normalizedProgress(localProgress, 0.72f, 1f)
    val fadeAlpha = 1f - fadeProgress
    if (fadeAlpha <= 0f) return

    val blinkAlpha = if (localProgress < SparkleBlinkStartFraction) {
        1f
    } else {
        val blinkProgress = normalizedProgress(localProgress, SparkleBlinkStartFraction, 1f)
        val pulse = 0.5f + 0.5f * sine(
            blinkProgress * sparkle.blinkCycles * (2f * PI.toFloat()) + sparkle.blinkPhase,
        )
        lerp(0.35f, 1f, pulse)
    }
    val finalAlpha = (fadeAlpha * blinkAlpha).coerceIn(0f, 1f)
    if (finalAlpha <= 0f) return

    if (morphProgress < 0.12f) {
        drawCircle(
            color = sparkle.color.copy(alpha = finalAlpha),
            radius = currentRadius,
            center = position,
        )
        return
    }

    val sparklePath = buildSparklePath(
        center = position,
        radius = currentRadius,
        morphProgress = morphProgress,
        rotationRadians = sparkle.rotationRadians,
    )

    drawPath(
        path = sparklePath,
        color = sparkle.color.copy(alpha = finalAlpha),
    )
}

private fun DrawScope.drawBlinkSparkle(
    sparkle: BlinkSparkleSpec,
    overallProgress: Float,
) {
    val localProgress = normalizedProgress(
        value = overallProgress,
        start = sparkle.startFraction,
        end = sparkle.endFraction,
    )
    if (localProgress <= 0f) return

    val growProgress = normalizedProgress(localProgress, 0f, 0.28f)
    val currentRadius = if (growProgress <= 0f) {
        sparkle.minRadiusPx
    } else {
        lerp(
            start = sparkle.minRadiusPx,
            stop = sparkle.maxRadiusPx,
            fraction = easeOutCubic(growProgress),
        )
    }

    val fadeProgress = normalizedProgress(localProgress, 0.78f, 1f)
    val fadeAlpha = 1f - fadeProgress
    if (fadeAlpha <= 0f) return

    val pulse = 0.5f + 0.5f * sine(
        localProgress * sparkle.blinkCycles * (2f * PI.toFloat()) + sparkle.blinkPhase,
    )
    val blinkAlpha = lerp(0.28f, 1f, pulse)
    val finalAlpha = (fadeAlpha * blinkAlpha).coerceIn(0f, 1f)
    if (finalAlpha <= 0f) return

    val morphProgress = normalizedProgress(localProgress, 0.08f, 0.34f)
    val center = Offset(sparkle.centerX, sparkle.centerY)

    if (morphProgress < 0.12f) {
        drawCircle(
            color = sparkle.color.copy(alpha = finalAlpha),
            radius = currentRadius,
            center = center,
        )
        return
    }

    val sparklePath = buildSparklePath(
        center = center,
        radius = currentRadius,
        morphProgress = morphProgress,
        rotationRadians = -PI.toFloat() / 2f,
    )

    drawPath(
        path = sparklePath,
        color = sparkle.color.copy(alpha = finalAlpha),
    )
}

private fun buildSparklePath(
    center: Offset,
    radius: Float,
    morphProgress: Float,
    rotationRadians: Float,
): Path {
    val path = Path()
    val innerRadius = lerp(radius, radius * 0.36f, morphProgress.coerceIn(0f, 1f))

    repeat(8) { index ->
        val angle = rotationRadians + index * (PI.toFloat() / 4f) - (PI.toFloat() / 2f)
        val targetRadius = if (index % 2 == 0) radius else innerRadius
        val x = center.x + cosine(angle) * targetRadius
        val y = center.y + sine(angle) * targetRadius

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }
    path.close()
    return path
}

private fun normalizedProgress(
    value: Float,
    start: Float,
    end: Float,
): Float {
    if (end <= start) return 0f
    if (value <= start) return 0f
    if (value >= end) return 1f
    return (value - start) / (end - start)
}

private fun easeOutCubic(value: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    return 1f - (1f - clamped) * (1f - clamped) * (1f - clamped)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    val clampedFraction = fraction.coerceIn(0f, 1f)
    return start + (stop - start) * clampedFraction
}

private fun sine(value: Float): Float = sin(value.toDouble()).toFloat()

private fun cosine(value: Float): Float = cos(value.toDouble()).toFloat()
