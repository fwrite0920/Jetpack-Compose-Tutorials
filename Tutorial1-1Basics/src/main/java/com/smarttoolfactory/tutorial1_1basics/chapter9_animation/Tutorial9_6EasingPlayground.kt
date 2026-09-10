package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
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
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.lerp as colorLerp
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_6Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_6_title,
        introduction = "**Easing** — Compare how easing curves change the timing of a motion.",
        examples = listOf(
            AnimationExample("EasingAnimationPlayground", "Easing Playground",
                "Choose a curve, adjust duration and replay the track, wave, rotation and sparkle previews.") { EasingAnimationPlaygroundPreview() }
        )
    )
}


private const val EASING_PLAYGROUND_MIN_DURATION = 200f
private const val EASING_PLAYGROUND_MAX_DURATION = 2400f
private val PlaygroundBackgroundColor = Color(0xFFF4F5F7)
private val PlaygroundPanelColor = Color(0xFFFFFFFF)
private val PlaygroundTrackColor = Color(0xFFD7DCE2)
private val PlaygroundPrimaryTextColor = Color(0xFF171A1F)
private val PlaygroundSecondaryTextColor = Color(0xFF69707A)
private val PlaygroundAccentColor = Color(0xFFFFB300)
private val PlaygroundAccentSecondaryColor = Color(0xFFFFC83D)
private val PlaygroundReferencePanelColor = Color(0xFFF1F2F4)
private val PlaygroundReferenceBorderColor = Color(0xFF242B33)
private val PlaygroundReferenceSquareColor = Color(0xFF58E0A2)
private val PlaygroundReferenceBlueColor = Color(0xFF4AA2FF)

private data class EasingOption(
    val name: String,
    val easing: Easing,
)

data class EasingPlaygroundPreviewScope(
    val easingName: String,
    val easing: Easing,
    val timelineProgress: Float,
    val easedProgress: Float,
    val durationMillis: Int,
    val replayToken: Int,
)

data class EasingPlaygroundPreviewSlot(
    val label: String,
    val content: @Composable EasingPlaygroundPreviewScope.() -> Unit,
)

private val EasingOptions = listOf(
    EasingOption("Linear", LinearEasing),
    EasingOption("FastOutSlowIn", FastOutSlowInEasing),
    EasingOption("FastOutLinearIn", FastOutLinearInEasing),
    EasingOption("LinearOutSlowIn", LinearOutSlowInEasing),
    EasingOption("Ease", Ease),
    EasingOption("EaseIn", EaseIn),
    EasingOption("EaseOut", EaseOut),
    EasingOption("EaseInOut", EaseInOut),
    EasingOption("EaseInBack", EaseInBack),
    EasingOption("EaseOutBack", EaseOutBack),
    EasingOption("EaseInOutBack", EaseInOutBack),
    EasingOption("EaseInBounce", EaseInBounce),
    EasingOption("EaseOutBounce", EaseOutBounce),
    EasingOption("EaseInOutBounce", EaseInOutBounce),
    EasingOption("EaseInElastic", EaseInElastic),
    EasingOption("EaseOutElastic", EaseOutElastic),
    EasingOption("EaseInOutElastic", EaseInOutElastic),
    EasingOption("EaseInExpo", EaseInExpo),
    EasingOption("EaseOutExpo", EaseOutExpo),
    EasingOption("EaseInOutExpo", EaseInOutExpo),
    EasingOption("EaseInQuad", EaseInQuad),
    EasingOption("EaseOutQuad", EaseOutQuad),
    EasingOption("EaseInOutQuad", EaseInOutQuad),
    EasingOption("EaseInCubic", EaseInCubic),
    EasingOption("EaseOutCubic", EaseOutCubic),
    EasingOption("EaseInOutCubic", EaseInOutCubic),
    EasingOption("EaseInQuart", EaseInQuart),
    EasingOption("EaseOutQuart", EaseOutQuart),
    EasingOption("EaseInOutQuart", EaseInOutQuart),
    EasingOption("EaseInQuint", EaseInQuint),
    EasingOption("EaseOutQuint", EaseOutQuint),
    EasingOption("EaseInOutQuint", EaseInOutQuint),
    EasingOption("EaseInSine", EaseInSine),
    EasingOption("EaseOutSine", EaseOutSine),
    EasingOption("EaseInOutSine", EaseInOutSine),
    EasingOption("EaseInCirc", EaseInCirc),
    EasingOption("EaseOutCirc", EaseOutCirc),
    EasingOption("EaseInOutCirc", EaseInOutCirc),
)

private val DefaultEasingPlaygroundPreviewSlots = listOf(
    EasingPlaygroundPreviewSlot("Curve") {
        EasingTrackSamplePage(
            easing = easing,
            timelineProgress = timelineProgress,
            easedProgress = easedProgress,
        )
    },
    EasingPlaygroundPreviewSlot("Reference") {
        EasingReferenceExamplesPage(
            easingName = easingName,
            easing = easing,
            timelineProgress = timelineProgress,
        )
    },
    EasingPlaygroundPreviewSlot("Wave") {
        EasingWavePreviewPage(
            timelineProgress = timelineProgress,
            easedProgress = easedProgress,
        )
    },
    EasingPlaygroundPreviewSlot("Rotation") {
        EasingRotationPreviewPage(
            easedProgress = easedProgress,
        )
    },
    EasingPlaygroundPreviewSlot("Blast + Sparkle") {
        EasingBlastSparklePreviewPage(
            easing = easing,
            durationMillis = durationMillis,
            replayToken = replayToken,
        )
    },
)

@Preview(
    name = "Easing Playground",
    showBackground = true,
    backgroundColor = 0xFFF4F5F7,
    widthDp = 420,
    heightDp = 960,
)
@Composable
private fun EasingAnimationPlaygroundPreview() {
    MaterialTheme {
        EasingAnimationPlayground(
            modifier = Modifier
                .fillMaxSize()
                .background(PlaygroundBackgroundColor)
        )
    }
}

@Composable
fun EasingAnimationPlayground(
    modifier: Modifier = Modifier,
    previewSlots: List<EasingPlaygroundPreviewSlot> = DefaultEasingPlaygroundPreviewSlots,
) {
    val coroutineScope = rememberCoroutineScope()
    val timelineProgress = remember { Animatable(0f) }
    var animationJob by remember { mutableStateOf<Job?>(null) }
    var selectedIndex by remember { mutableIntStateOf(0) }
    var durationMillis by remember { mutableFloatStateOf(900f) }
    var autoReplay by remember { mutableStateOf(true) }
    var replayToken by remember { mutableIntStateOf(0) }

    val selectedEasing = EasingOptions[selectedIndex]
    val resolvedPreviewSlots = previewSlots.ifEmpty {
        DefaultEasingPlaygroundPreviewSlots
    }
    val pagerState = rememberPagerState(pageCount = { resolvedPreviewSlots.size })
    val clampedDurationMillis = durationMillis.roundToInt().coerceAtLeast(1)
    val timelineValue = timelineProgress.value.coerceIn(0f, 1f)
    val previewScope = EasingPlaygroundPreviewScope(
        easingName = selectedEasing.name,
        easing = selectedEasing.easing,
        timelineProgress = timelineValue,
        easedProgress = selectedEasing.easing.transform(timelineValue),
        durationMillis = clampedDurationMillis,
        replayToken = replayToken,
    )
    val currentPage = pagerState.currentPage.coerceIn(0, resolvedPreviewSlots.lastIndex)
    val currentPreviewSlot = resolvedPreviewSlots[currentPage]

    val replayAnimation: () -> Unit = {
        replayToken += 1
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            timelineProgress.stop()
            timelineProgress.snapTo(0f)
            timelineProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = clampedDurationMillis,
                    easing = LinearEasing,
                ),
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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Easing Playground",
            color = PlaygroundPrimaryTextColor,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
        )

        Text(
            text = "Preview the motion curve and the actual tween movement for different Compose easings.",
            color = PlaygroundSecondaryTextColor,
            fontSize = 14.sp,
            lineHeight = 22.sp,
        )

        LaunchedEffect(resolvedPreviewSlots.size) {
            if (pagerState.currentPage > resolvedPreviewSlots.lastIndex) {
                pagerState.scrollToPage(resolvedPreviewSlots.lastIndex)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                pageSpacing = 12.dp,
            ) { page ->
                resolvedPreviewSlots[page].content(previewScope)
            }

            Text(
                text = "Swipe preview: ${currentPreviewSlot.label}",
                color = PlaygroundSecondaryTextColor,
                fontSize = 14.sp,
            )

            EasingPreviewPagerIndicators(
                slots = resolvedPreviewSlots,
                currentPage = currentPage,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimationTutorialButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedIndex = if (selectedIndex == 0) {
                        EasingOptions.lastIndex
                    } else {
                        selectedIndex - 1
                    }
                    maybeReplay()
                },
            ) {
                Text("Previous")
            }

            AnimationTutorialButton(
                modifier = Modifier.weight(1f),
                onClick = replayAnimation,
            ) {
                Text("Play")
            }

            AnimationTutorialButton(
                modifier = Modifier.weight(1f),
                onClick = {
                    selectedIndex = if (selectedIndex == EasingOptions.lastIndex) {
                        0
                    } else {
                        selectedIndex + 1
                    }
                    maybeReplay()
                },
            ) {
                Text("Next")
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("Choose an easing", color = PlaygroundPrimaryTextColor,
                style = MaterialTheme.typography.titleMedium)
            Text("Swipe the gallery to see every curve. Back and elastic curves can go beyond 0–100%; a steeper line means faster motion.",
                color = PlaygroundSecondaryTextColor, style = MaterialTheme.typography.bodyMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.testTag("easing-gallery")) {
                itemsIndexed(EasingOptions) { index, option ->
                    Card(onClick = { selectedIndex = index; maybeReplay() },
                        modifier = Modifier.width(184.dp).testTag("easing-option-$index"),
                        border = if (index == selectedIndex) androidx.compose.foundation.BorderStroke(
                            2.dp, MaterialTheme.colorScheme.primary) else null,
                        colors = CardDefaults.cardColors(containerColor = PlaygroundPanelColor)) {
                        Text(option.name, Modifier.padding(10.dp),
                            color = PlaygroundPrimaryTextColor, style = MaterialTheme.typography.labelLarge)
                        EasingCurvePlot(listOf(option.name to option.easing), timelineValue,
                            Modifier.fillMaxWidth().height(140.dp))
                    }
                }
            }
            Text("Scrub time • ${(timelineValue * 100).roundToInt()}%",
                color = PlaygroundPrimaryTextColor)
            Slider(value = timelineValue, onValueChange = { value ->
                animationJob?.cancel()
                animationJob = coroutineScope.launch { timelineProgress.snapTo(value) }
            }, modifier = Modifier.testTag("playground-easing-scrubber"))

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = selectedEasing.name,
                    color = PlaygroundPrimaryTextColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${selectedIndex + 1} / ${EasingOptions.size}",
                    color = PlaygroundSecondaryTextColor,
                    fontSize = 14.sp,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Auto replay on change",
                    color = PlaygroundPrimaryTextColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                )
                Switch(
                    checked = autoReplay,
                    onCheckedChange = { autoReplay = it },
                )
            }

            EasingSlider(
                label = "Duration",
                valueText = "${durationMillis.roundToInt()} ms",
                value = durationMillis,
                valueRange = EASING_PLAYGROUND_MIN_DURATION..EASING_PLAYGROUND_MAX_DURATION,
                onValueChange = { durationMillis = it },
                onValueChangeFinished = maybeReplay,
            )

            Text(
                text = "Curve sample",
                color = PlaygroundPrimaryTextColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )

            EasingSampleTable(
                easing = selectedEasing.easing,
                timelineProgress = timelineValue,
                easedProgress = previewScope.easedProgress,
            )
        }
    }
}

@Composable
private fun EasingTrackSamplePage(
    easing: Easing,
    timelineProgress: Float,
    easedProgress: Float,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        EasingCurvePlot(listOf("Selected easing" to easing), timelineProgress,
            Modifier.fillMaxWidth().weight(1f))
        EasingMotionLane("Motion • start → destination", easedProgress, MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EasingReferenceExamplesPage(
    easingName: String,
    easing: Easing,
    timelineProgress: Float,
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(PlaygroundReferencePanelColor)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EasingReferenceChart(
            easingName = easingName,
            easing = easing,
            progress = timelineProgress,
            modifier = Modifier
                .weight(0.82f)
                .fillMaxHeight(),
        )

        EasingStoryboardExample(
            easing = easing,
            progress = timelineProgress,
            modifier = Modifier
                .weight(1.08f)
                .fillMaxHeight(),
        )
    }
}

@Composable
private fun EasingPreviewPagerIndicators(
    slots: List<EasingPlaygroundPreviewSlot>,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        slots.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .size(if (index == currentPage) 22.dp else 8.dp, 8.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == currentPage) {
                            PlaygroundAccentColor
                        } else {
                            PlaygroundTrackColor
                        }
                    )
            )
        }
    }
}

@Composable
private fun EasingWavePreviewPage(
    timelineProgress: Float,
    easedProgress: Float,
    modifier: Modifier = Modifier,
) {
    val clampedFillProgress = easedProgress.coerceIn(0f, 1f)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(PlaygroundPanelColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val previewSize = minOf(maxWidth, maxHeight) * 0.82f

        Box(
            modifier = Modifier
                .size(previewSize)
                .waveFill(
                    fillFraction = clampedFillProgress,
                    fillColor = PlaygroundAccentColor,
                    crestColor = PlaygroundAccentSecondaryColor.copy(alpha = 0.36f),
                    amplitudeFraction = 0.09f,
                    wavelengthFraction = 1.15f,
                    phaseRadians = timelineProgress * (4f * PI.toFloat()),
                )
                .clip(CircleShape)
                .background(PlaygroundTrackColor)
                .border(
                    width = 1.dp,
                    color = PlaygroundTrackColor.copy(alpha = 0.85f),
                    shape = CircleShape,
                )
        )
    }
}

@Composable
private fun EasingRotationPreviewPage(
    easedProgress: Float,
    modifier: Modifier = Modifier,
) {
    val rotationDegrees = 360f * easedProgress
    val cardScale = 0.82f + (0.18f * easedProgress)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(PlaygroundPanelColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        val orbitSize = minOf(maxWidth, maxHeight) * 0.76f
        val cardSize = orbitSize * 0.6f

        Box(
            modifier = Modifier
                .size(orbitSize)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = PlaygroundTrackColor,
                    shape = CircleShape,
                )
        )

        Box(
            modifier = Modifier
                .size(cardSize)
                .graphicsLayer {
                    rotationZ = rotationDegrees
                    scaleX = cardScale
                    scaleY = cardScale
                }
                .clip(RoundedCornerShape(28.dp))
                .background(PlaygroundReferenceBlueColor)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(PlaygroundAccentColor)
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
                    .size(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(PlaygroundPanelColor.copy(alpha = 0.92f))
            )
        }
    }
}

@Composable
private fun EasingBlastSparklePreviewPage(
    easing: Easing,
    durationMillis: Int,
    replayToken: Int,
    modifier: Modifier = Modifier,
) {
    val sparkleState = rememberSparkleAnimationState()
    val blastModel = remember(easing, durationMillis) {
        BlastAnimationModel(
            mode = BlastAnimationMode.FilledAndRing,
            color = PlaygroundAccentColor,
            animationSpec = tween(
                durationMillis = (durationMillis * 0.72f).roundToInt().coerceAtLeast(1),
                easing = easing,
            ),
            minRadius = 4.dp,
            maxRadius = 54.dp,
            strokeWidth = 4.dp,
            minAlpha = 0.18f,
            maxAlpha = 0.95f,
        )
    }
    val sparkleModel = remember(durationMillis) {
        SparkleAnimationModel(
            mode = SparkleAnimationMode.Shoot,
            colors = listOf(
                PlaygroundAccentColor,
                PlaygroundAccentSecondaryColor,
                PlaygroundReferenceBlueColor,
                PlaygroundReferenceSquareColor,
            ),
            durationMs = (durationMillis * 1.15f).roundToInt().coerceAtLeast(1),
            delayMs = (durationMillis * 0.08f).roundToInt(),
            sparkleCount = 10,
            minRadius = 2.dp,
            maxRadius = 5.dp,
            safeRadius = 22.dp,
        )
    }

    LaunchedEffect(replayToken) {
        sparkleState.stopAnimation()
        sparkleState.startAnimation(
            blastModel = blastModel,
            sparkleModel = sparkleModel,
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(28.dp))
            .background(PlaygroundPanelColor)
            .blastAndSparkle(
                state = sparkleState,
                blastModel = blastModel,
                sparkleModel = sparkleModel,
            )
    )
}

@Composable
private fun EasingReferenceChart(
    easingName: String,
    easing: Easing,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    EasingCurvePlot(listOf(easingName to easing), progress, modifier)
}

@Composable
private fun EasingStoryboardExample(
    easing: Easing,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .border(width = 1.dp, color = PlaygroundReferenceBorderColor)
    ) {
        val leftWidth = maxWidth * 0.34f
        val rightWidth = maxWidth - leftWidth
        val cellWidth = rightWidth / 2
        val halfHeight = maxHeight / 2
        val baseSquareSize = 24.dp
        val easedProgress = easing.transform(progress)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 1.dp.toPx()
            val leftSplit = size.width * 0.34f
            val middleSplit = leftSplit + (size.width - leftSplit) / 2f
            val half = size.height / 2f

            drawLine(
                color = PlaygroundReferenceBorderColor,
                start = Offset(leftSplit, 0f),
                end = Offset(leftSplit, size.height),
                strokeWidth = stroke,
            )
            drawLine(
                color = PlaygroundReferenceBorderColor,
                start = Offset(middleSplit, 0f),
                end = Offset(middleSplit, size.height),
                strokeWidth = stroke,
            )
            drawLine(
                color = PlaygroundReferenceBorderColor,
                start = Offset(leftSplit, half),
                end = Offset(size.width, half),
                strokeWidth = stroke,
            )
        }

        StoryboardSquare(
            containerLeft = 0.dp,
            containerTop = 0.dp,
            containerWidth = leftWidth,
            containerHeight = maxHeight,
            xFraction = 0.5f,
            yFraction = 0.06f + (0.78f - 0.06f) * easedProgress,
            squareSize = baseSquareSize,
        )

        StoryboardSquare(
            containerLeft = leftWidth,
            containerTop = 0.dp,
            containerWidth = cellWidth,
            containerHeight = halfHeight,
            xFraction = 0.5f,
            yFraction = 0.5f,
            squareSize = lerp(0.dp, 36.dp, easedProgress),
        )

        StoryboardSquare(
            containerLeft = leftWidth,
            containerTop = halfHeight,
            containerWidth = cellWidth,
            containerHeight = halfHeight,
            xFraction = 0.5f,
            yFraction = 0.5f,
            squareSize = baseSquareSize,
            rotationZ = 180f * easedProgress,
        )

        StoryboardSquare(
            containerLeft = leftWidth + cellWidth,
            containerTop = 0.dp,
            containerWidth = cellWidth,
            containerHeight = halfHeight,
            xFraction = 0.5f,
            yFraction = 0.5f,
            squareSize = baseSquareSize,
            color = colorLerp(
                start = PlaygroundReferenceSquareColor,
                stop = PlaygroundReferenceBlueColor,
                fraction = easedProgress,
            ),
        )

        StoryboardSquare(
            containerLeft = leftWidth + cellWidth,
            containerTop = halfHeight,
            containerWidth = cellWidth,
            containerHeight = halfHeight,
            xFraction = 0.5f,
            yFraction = 0.5f,
            squareSize = baseSquareSize,
            alpha = easedProgress,
        )
    }
}

@Composable
private fun StoryboardSquare(
    containerLeft: androidx.compose.ui.unit.Dp,
    containerTop: androidx.compose.ui.unit.Dp,
    containerWidth: androidx.compose.ui.unit.Dp,
    containerHeight: androidx.compose.ui.unit.Dp,
    xFraction: Float,
    yFraction: Float,
    squareSize: androidx.compose.ui.unit.Dp,
    color: Color = PlaygroundReferenceSquareColor,
    alpha: Float = 1f,
    rotationZ: Float = 0f,
) {
    val maxX = (containerWidth - squareSize).coerceAtLeast(0.dp)
    val maxY = (containerHeight - squareSize).coerceAtLeast(0.dp)
    val xOffset = containerLeft + maxX * xFraction.coerceIn(0f, 1f)
    val yOffset = containerTop + maxY * yFraction.coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .offset(x = xOffset, y = yOffset)
            .size(squareSize)
            .graphicsLayer {
                this.alpha = alpha.coerceIn(0f, 1f)
                this.rotationZ = rotationZ
            }
            .clip(RoundedCornerShape(4.dp))
            .background(color)
    )
}

@Composable
private fun EasingSlider(
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

@Composable
private fun EasingSampleTable(
    easing: Easing,
    timelineProgress: Float,
    easedProgress: Float,
) {
    val samples = listOf(0f, 0.1f, 0.2f, 0.35f, 0.5f, 0.65f, 0.8f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PlaygroundPanelColor)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Live t=${timelineProgress.formatFraction()} -> ${easedProgress.formatFraction()}",
            color = PlaygroundPrimaryTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
        )

        samples.forEach { sample ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "t=${sample.formatFraction()}",
                    color = PlaygroundSecondaryTextColor,
                    fontSize = 14.sp,
                )
                Text(
                    text = easing.transform(sample).formatFraction(),
                    color = PlaygroundPrimaryTextColor,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

private fun Float.formatFraction(): String = "%.2f".format(this)
