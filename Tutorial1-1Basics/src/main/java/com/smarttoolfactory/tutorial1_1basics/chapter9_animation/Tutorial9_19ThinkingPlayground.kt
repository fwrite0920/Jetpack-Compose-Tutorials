package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlinx.coroutines.launch
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_19Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_19_title,
        introduction = "**Thinking Shape Playground** — Adjust shape families, morph progress, rotation and gradient motion.",
        examples = listOf(
            AnimationExample("ThinkingShapePlayground", "Thinking Shape Playground",
                "Adjust shape families, morph progress, rotation and gradient motion.") { ThinkingShapePlaygroundPreview() }
        )
    )
}


private val ThinkingShapePlaygroundBackgroundColor = Color(0xFF070A10)
private val ThinkingShapePlaygroundPanelColor = Color(0xFF101720)
private val ThinkingShapePlaygroundBorderColor = Color(0x1FFFFFFF)
private val ThinkingShapePlaygroundPrimaryTextColor = Color(0xFFF3F7FB)
private val ThinkingShapePlaygroundSecondaryTextColor = Color(0xFFA7B4C5)
private val ThinkingShapePlaygroundTrackColor = Color(0xFF243241)
private val ThinkingShapePlaygroundShapeGradientColors = listOf(
    Color(0xFF38BDF8),
    Color(0xFF2DD4BF),
    Color(0xFFF472B6),
    Color(0xFF38BDF8)
)
private val ThinkingShapePlaygroundOutlineColor = Color(0x526C7A8B)

private const val DefaultPlaygroundProgress = 0.32f

private enum class PlaygroundShapeFamily(val label: String) {
    Ai("AI"),
    Polygon("Polygon"),
    Star("Star");

    fun next(): PlaygroundShapeFamily {
        return when (this) {
            Ai -> Polygon
            Polygon -> Star
            Star -> Ai
        }
    }
}

private data class PlaygroundShapeConfig(
    val family: PlaygroundShapeFamily,
    val sides: Int,
    val innerRadiusRatio: Float,
    val outerRoundRadius: Float,
    val outerSmoothing: Float,
    val innerRoundRadius: Float,
    val innerSmoothing: Float
)

private val DefaultPlaygroundStartConfig = PlaygroundShapeConfig(
    family = PlaygroundShapeFamily.Ai,
    sides = 4,
    innerRadiusRatio = 0.34f,
    outerRoundRadius = 0.06f,
    outerSmoothing = 0.86f,
    innerRoundRadius = 0.14f,
    innerSmoothing = 0.76f
)

private val DefaultPlaygroundEndConfig = PlaygroundShapeConfig(
    family = PlaygroundShapeFamily.Polygon,
    sides = 8,
    innerRadiusRatio = 0.48f,
    outerRoundRadius = 0.07f,
    outerSmoothing = 0.1f,
    innerRoundRadius = 0.08f,
    innerSmoothing = 0f
)

@Composable
fun ThinkingShapePlayground(
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val startPageScrollState = rememberScrollState()
    val endPageScrollState = rememberScrollState()
    var startConfig by remember { mutableStateOf(DefaultPlaygroundStartConfig) }
    var endConfig by remember { mutableStateOf(DefaultPlaygroundEndConfig) }
    var morphProgress by remember { mutableFloatStateOf(DefaultPlaygroundProgress) }
    var rotationDegrees by remember { mutableFloatStateOf(DefaultThinkingRotationDegrees) }
    val headerShapeBrushProgress = rememberThinkingBrushProgress(
        label = "thinking_playground_header_brush"
    )
    val headerTextBrushProgress = rememberThinkingTextBrushProgress(
        label = "thinking_playground_header_text_brush"
    )

    val startPolygon = remember(startConfig) {
        buildPlaygroundMorphPolygon(startConfig)
    }
    val endPolygon = remember(endConfig) {
        buildPlaygroundMorphPolygon(endConfig)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Thinking Shape Playground",
            color = ThinkingShapePlaygroundPrimaryTextColor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        PlaygroundPanel(
            contentPadding = PaddingValues(8.dp),
            verticalSpacing = 6.dp
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ThinkingMorphingShape(
                    brushProgress = headerShapeBrushProgress,
                    gradientColors = ThinkingShapePlaygroundShapeGradientColors,
                    outlineColor = ThinkingShapePlaygroundOutlineColor
                )
                ThinkingAnimatedText(
                    text = stringResource(R.string.message_thinking),
                    brushProgress = headerTextBrushProgress,
                    baseColor = ThinkingShapePlaygroundSecondaryTextColor,
                    highlightColor = ThinkingShapePlaygroundPrimaryTextColor
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShapeStageCard(
                    modifier = Modifier.weight(1f),
                    title = "Start",
                    subtitle = startConfig.stageSubtitle()
                ) {
                    ThinkingShapeCanvas(
                        modifier = Modifier.size(60.dp),
                        startPolygon = startPolygon,
                        rotationDegrees = rotationDegrees,
                        gradientColors = ThinkingShapePlaygroundShapeGradientColors,
                        outlineColor = ThinkingShapePlaygroundOutlineColor
                    )
                }
                ShapeStageCard(
                    modifier = Modifier.weight(1f),
                    title = "Morph",
                    subtitle = "${(morphProgress * 100f).roundToInt()}%"
                ) {
                    ThinkingShapeCanvas(
                        modifier = Modifier.size(60.dp),
                        startPolygon = startPolygon,
                        endPolygon = endPolygon,
                        morphProgress = morphProgress,
                        rotationDegrees = rotationDegrees,
                        gradientColors = ThinkingShapePlaygroundShapeGradientColors,
                        outlineColor = ThinkingShapePlaygroundOutlineColor
                    )
                }
                ShapeStageCard(
                    modifier = Modifier.weight(1f),
                    title = "End",
                    subtitle = endConfig.stageSubtitle()
                ) {
                    ThinkingShapeCanvas(
                        modifier = Modifier.size(60.dp),
                        startPolygon = endPolygon,
                        rotationDegrees = rotationDegrees,
                        gradientColors = ThinkingShapePlaygroundShapeGradientColors,
                        outlineColor = ThinkingShapePlaygroundOutlineColor
                    )
                }
            }
        }

        PlaygroundPanel(
            contentPadding = PaddingValues(8.dp),
            verticalSpacing = 6.dp
        ) {
            Text(
                text = "Playback",
                color = ThinkingShapePlaygroundPrimaryTextColor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            PlaygroundSlider(
                label = "Morph progress",
                valueText = "${(morphProgress * 100f).roundToInt()}%",
                value = morphProgress,
                valueRange = 0f..1f,
                onValueChange = { morphProgress = it }
            )

            PlaygroundSlider(
                label = "Rotation",
                valueText = "${rotationDegrees.roundToInt()}°",
                value = rotationDegrees,
                valueRange = 0f..360f,
                onValueChange = { rotationDegrees = it }
            )
        }

        PlaygroundPanel(
            contentPadding = PaddingValues(6.dp),
            verticalSpacing = 6.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlaygroundPagerTab(
                    title = "Start shape",
                    selected = pagerState.currentPage == 0,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    }
                )
                PlaygroundPagerTab(
                    title = "End shape",
                    selected = pagerState.currentPage == 1,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    }
                )
            }
        }

        HorizontalPager(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            state = pagerState,
            pageSpacing = 8.dp
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
                    .verticalScroll(
                        state = if (page == 0) {
                            startPageScrollState
                        } else {
                            endPageScrollState
                        }
                    ),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (page == 0) {
                    PlaygroundShapeConfigPanel(
                        title = "Start shape",
                        config = startConfig,
                        onConfigChange = { startConfig = it }
                    )
                } else {
                    PlaygroundShapeConfigPanel(
                        title = "End shape",
                        config = endConfig,
                        onConfigChange = { endConfig = it }
                    )
                }
            }
        }

        AnimationTutorialButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                startConfig = DefaultPlaygroundStartConfig
                endConfig = DefaultPlaygroundEndConfig
                morphProgress = DefaultPlaygroundProgress
                rotationDegrees = DefaultThinkingRotationDegrees
            }
        ) {
            Text("Reset")
        }
    }
}

@Composable
private fun PlaygroundPagerTab(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        onClick = onClick
    ) {
        Text(
            text = title,
            color = if (selected) {
                ThinkingShapePlaygroundPrimaryTextColor
            } else {
                ThinkingShapePlaygroundSecondaryTextColor
            }
        )
    }
}

@Composable
private fun PlaygroundShapeConfigPanel(
    title: String,
    config: PlaygroundShapeConfig,
    onConfigChange: (PlaygroundShapeConfig) -> Unit
) {
    PlaygroundPanel(
        contentPadding = PaddingValues(8.dp),
        verticalSpacing = 6.dp
    ) {
        Text(
            text = title,
            color = ThinkingShapePlaygroundPrimaryTextColor,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Base shape",
                color = ThinkingShapePlaygroundPrimaryTextColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Button(
                onClick = {
                    onConfigChange(
                        config.copy(
                            family = config.family.next(),
                            sides = if (config.family.next() == PlaygroundShapeFamily.Ai) 4 else config.sides
                        )
                    )
                }
            ) {
                Text(config.family.label)
            }
        }

        if (config.family != PlaygroundShapeFamily.Ai) {
            PlaygroundSlider(
                label = "Sides",
                valueText = config.sides.toString(),
                value = config.sides.toFloat(),
                valueRange = ThinkingShapeMinVertices.toFloat()..8f,
                steps = 8 - ThinkingShapeMinVertices - 1,
                onValueChange = {
                    onConfigChange(
                        config.copy(
                            sides = it.roundToInt().coerceIn(ThinkingShapeMinVertices, 8)
                        )
                    )
                }
            )
        }

        PlaygroundSlider(
            label = "Inner radius",
            valueText = "%.2f".format(config.innerRadiusRatio),
            value = config.innerRadiusRatio,
            valueRange = 0.12f..0.9f,
            onValueChange = { onConfigChange(config.copy(innerRadiusRatio = it)) }
        )

        PlaygroundSlider(
            label = "Outer round",
            valueText = "%.2f".format(config.outerRoundRadius),
            value = config.outerRoundRadius,
            valueRange = 0f..0.34f,
            onValueChange = { onConfigChange(config.copy(outerRoundRadius = it)) }
        )

        PlaygroundSlider(
            label = "Outer smoothing",
            valueText = "%.2f".format(config.outerSmoothing),
            value = config.outerSmoothing,
            valueRange = 0f..1f,
            onValueChange = { onConfigChange(config.copy(outerSmoothing = it)) }
        )

        PlaygroundSlider(
            label = "Inner round",
            valueText = "%.2f".format(config.innerRoundRadius),
            value = config.innerRoundRadius,
            valueRange = 0f..0.34f,
            onValueChange = { onConfigChange(config.copy(innerRoundRadius = it)) }
        )

        PlaygroundSlider(
            label = "Inner smoothing",
            valueText = "%.2f".format(config.innerSmoothing),
            value = config.innerSmoothing,
            valueRange = 0f..1f,
            onValueChange = { onConfigChange(config.copy(innerSmoothing = it)) }
        )
    }
}

@Composable
private fun PlaygroundPanel(
    contentPadding: PaddingValues = PaddingValues(8.dp),
    verticalSpacing: androidx.compose.ui.unit.Dp = 6.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(ThinkingShapePlaygroundPanelColor)
            .border(
                width = 1.dp,
                color = ThinkingShapePlaygroundBorderColor,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        content = content
    )
}

@Composable
private fun ShapeStageCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(ThinkingShapePlaygroundTrackColor.copy(alpha = 0.42f))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            color = ThinkingShapePlaygroundPrimaryTextColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtitle,
            color = ThinkingShapePlaygroundSecondaryTextColor,
            style = MaterialTheme.typography.labelSmall
        )
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun PlaygroundSlider(
    label: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    steps: Int = 0
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = ThinkingShapePlaygroundPrimaryTextColor,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = valueText,
                color = ThinkingShapePlaygroundSecondaryTextColor,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

private fun PlaygroundShapeConfig.stageSubtitle(): String {
    return if (family == PlaygroundShapeFamily.Ai) {
        family.label
    } else {
        "${family.label} • $sides"
    }
}

private fun buildPlaygroundMorphPolygon(
    config: PlaygroundShapeConfig
): RoundedPolygon {
    return when (config.family) {
        PlaygroundShapeFamily.Ai -> alternatingPlaygroundPolygon(
            sides = 4,
            innerRadiusRatio = config.innerRadiusRatio,
            outerRoundRadius = config.outerRoundRadius,
            outerSmoothing = config.outerSmoothing,
            innerRoundRadius = config.innerRoundRadius,
            innerSmoothing = config.innerSmoothing
        )
        PlaygroundShapeFamily.Polygon -> buildThinkingPolygonVariant(config.sides)
        PlaygroundShapeFamily.Star -> alternatingPlaygroundPolygon(
            sides = config.sides,
            innerRadiusRatio = config.innerRadiusRatio,
            outerRoundRadius = config.outerRoundRadius,
            outerSmoothing = config.outerSmoothing,
            innerRoundRadius = config.innerRoundRadius,
            innerSmoothing = config.innerSmoothing
        )
    }
}

private fun alternatingPlaygroundPolygon(
    sides: Int,
    innerRadiusRatio: Float,
    outerRoundRadius: Float,
    outerSmoothing: Float,
    innerRoundRadius: Float,
    innerSmoothing: Float
): RoundedPolygon {
    val pointCount = sides.coerceIn(ThinkingShapeMinVertices, 8) * 2
    val vertices = playgroundPolarVertices(
        radii = List(pointCount) { index ->
            if (index % 2 == 0) 1f else innerRadiusRatio.coerceIn(0.12f, 1f)
        },
        rotationDegrees = -90f
    )
    val perVertexRounding = List(pointCount) { index ->
        if (index % 2 == 0) {
            CornerRounding(
                radius = outerRoundRadius.coerceIn(0f, 0.48f),
                smoothing = outerSmoothing.coerceIn(0f, 1f)
            )
        } else {
            CornerRounding(
                radius = innerRoundRadius.coerceIn(0f, 0.48f),
                smoothing = innerSmoothing.coerceIn(0f, 1f)
            )
        }
    }
    return RoundedPolygon(
        vertices = vertices,
        perVertexRounding = perVertexRounding,
        centerX = 0f,
        centerY = 0f
    )
}

private fun playgroundPolarVertices(
    radii: List<Float>,
    rotationDegrees: Float
): FloatArray {
    val result = FloatArray(radii.size * 2)
    val angleStep = 360.0 / radii.size.toDouble()
    radii.forEachIndexed { index, radius ->
        val angleRadians = Math.toRadians(rotationDegrees + (index * angleStep))
        result[index * 2] = (cos(angleRadians) * radius).toFloat()
        result[(index * 2) + 1] = (sin(angleRadians) * radius).toFloat()
    }
    return result
}

@Preview(
    name = "Thinking Shape Playground",
    showBackground = true,
    backgroundColor = 0xFF070A10,
    widthDp = 420,
    heightDp = 980
)
@Composable
private fun ThinkingShapePlaygroundPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ThinkingShapePlayground(
            modifier = Modifier
                .fillMaxSize()
                .background(ThinkingShapePlaygroundBackgroundColor)
        )
    }
}
