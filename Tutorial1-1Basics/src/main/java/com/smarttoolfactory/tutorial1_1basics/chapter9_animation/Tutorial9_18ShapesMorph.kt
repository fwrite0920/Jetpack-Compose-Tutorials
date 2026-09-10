package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_18Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_18_title,
        introduction = "**Shape Morphing** — Compare polygon presets and edit morph progress, rounding and vertices.",
        examples = listOf(
            AnimationExample("ShapesDemoBadgeToStarPreview", "Badge to Star",
                "Morph a rounded badge into a star.") { ShapesDemoBadgeToStarPreview() },
            AnimationExample("ShapesDemoCircleToBurstPreview", "Circle to Burst",
                "Morph a circle into an eight-point burst.") { ShapesDemoCircleToBurstPreview() },
            AnimationExample("ShapesEditorDemoStarPreview", "Shape Editor",
                "Edit vertices, radii, rounding and morph progress.") { ShapesEditorDemoStarPreview() }
        )
    )
}


private enum class ShapesDemoSelectionMode {
    Shape,
    Edit
}

private data class ShapesDemoPreset(
    val id: String,
    val label: String,
    val polygon: RoundedPolygon
)

private val ShapesDemoTargetSelectionColor = Color(0xFFFF4D6D)
private val ShapesDemoAccentColor = Color(0xFF8C19FF)
private val ShapesDemoSurfaceColor = Color(0xFF090B11)
private val ShapesDemoPanelColor = Color(0xFF11151C)
private val ShapesDemoPanelBorderColor = Color(0x1FFFFFFF)
private val ShapesDemoPrimaryTextColor = Color(0xFFF3F7FB)
private val ShapesDemoSecondaryTextColor = Color(0xFFABB6C5)
private val ShapesDemoControlSurfaceColor = Color(0xFF25262A)
private val ShapesDemoControlBorderColor = Color(0x4DFFFFFF)
private val ShapesDemoTopBarBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF4300A8),
        Color(0xFF5B00F0)
    )
)
private val ShapesDemoScreenHorizontalPadding = 16.dp
private const val ShapesDemoTitle = "Shapes Demo"
private const val ShapesDemoDescription =
    "Browse the ShapesDemo-inspired preset gallery and scrub the morph between the selected source and target forms."
private const val ShapesDemoPresetsLabel = "Presets"
private const val ShapesDemoShapeModeLabel = "Shape"
private const val ShapesDemoEditModeLabel = "Edit"
private const val ShapesDemoModeHint =
    "Shape picks the source preset. Edit picks the target preset."
private const val ShapesDemoProgressLabel = "Morph progress"
private const val ShapesDemoPreviewLabel = "Preview"
private const val ShapesDemoSourceLabel = "Source"
private const val ShapesDemoTargetLabel = "Target"
private const val ShapesDemoSourceShortLabel = "S"
private const val ShapesDemoTargetShortLabel = "T"
private val ShapesDemoButtonBrush = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF5B00F0),
        Color(0xFF8C19FF)
    )
)
private val ShapesDemoActiveTrackColor = Color(0xFF7A00FF)
private val ShapesDemoInactiveTrackColor = Color(0xFF260055)

private enum class ShapesEditorBaseShape(
    val label: String,
    val defaults: ShapesEditorDefaults
) {
    Star(
        label = "Star",
        defaults = ShapesEditorDefaults(
            sides = 8f,
            innerRadiusRatio = 0.72f,
            roundRadius = 0.18f,
            smoothing = 0f,
            innerRoundRadius = 0.14f,
            innerSmoothing = 0f,
            rotation = 0f
        )
    ),
    SoftBadge(
        label = "SoftBadge",
        defaults = ShapesEditorDefaults(
            sides = 8f,
            innerRadiusRatio = 0.80f,
            roundRadius = 0.22f,
            smoothing = 0.82f,
            innerRoundRadius = 0.16f,
            innerSmoothing = 0.58f,
            rotation = 0f
        )
    ),
    Polygon(
        label = "Polygon",
        defaults = ShapesEditorDefaults(
            sides = 8f,
            innerRadiusRatio = 1f,
            roundRadius = 0.16f,
            smoothing = 0.74f,
            innerRoundRadius = 0.16f,
            innerSmoothing = 0.74f,
            rotation = 0f
        )
    )
}

private data class ShapesEditorDefaults(
    val sides: Float,
    val innerRadiusRatio: Float,
    val roundRadius: Float,
    val smoothing: Float,
    val innerRoundRadius: Float,
    val innerSmoothing: Float,
    val rotation: Float
)

@Composable
private fun ShapesMorphPlayground(
    modifier: Modifier = Modifier,
    initialSourceShapeId: String = "soft_badge",
    initialTargetShapeId: String = "star5",
    initialMorphProgress: Float = 0.22f,
    initialSelectionMode: ShapesDemoSelectionMode = ShapesDemoSelectionMode.Shape
) {
    val presets = remember { buildShapesDemoPresets() }
    var selectionMode by rememberSaveable(initialSelectionMode) {
        mutableStateOf(initialSelectionMode)
    }
    var sourceShapeId by rememberSaveable(initialSourceShapeId) {
        mutableStateOf(initialSourceShapeId)
    }
    var targetShapeId by rememberSaveable(initialTargetShapeId) {
        mutableStateOf(initialTargetShapeId)
    }
    var morphProgress by rememberSaveable(initialMorphProgress) {
        mutableFloatStateOf(initialMorphProgress)
    }

    val sourcePreset = remember(sourceShapeId, presets) {
        presets.firstOrNull { it.id == sourceShapeId } ?: presets.first()
    }
    val targetPreset = remember(targetShapeId, presets) {
        presets.firstOrNull { it.id == targetShapeId } ?: presets.last()
    }

    val topAppbarHeightWithInsets = 40.dp
    val topAppbarGradientHeight = 56.dp

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ShapesDemoSurfaceColor)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topAppbarGradientHeight)
                .background(brush = ShapesDemoTopBarBrush)
                .align(Alignment.TopCenter)
        )

        Text(
            text = ShapesDemoTitle,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = ShapesDemoPrimaryTextColor
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topAppbarHeightWithInsets)
                .padding(
                    horizontal = ShapesDemoScreenHorizontalPadding,
                    vertical = 8.dp
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            DemoPanel(
                contentPadding = PaddingValues(8.dp)
            ) {
                Text(
                    text = ShapesDemoPreviewLabel,
                    color = ShapesDemoPrimaryTextColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    ThinkingShapeCanvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        startPolygon = sourcePreset.polygon,
                        endPolygon = targetPreset.polygon,
                        morphProgress = morphProgress,
                        rotationDegrees = 0f,
                        visualScale = 1.34f,
                        brush = SolidColor(Color.White),
                        showOutline = false
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DemoPanel {
                    Text(
                        text = ShapesDemoPresetsLabel,
                        color = ShapesDemoPrimaryTextColor,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = ShapesDemoModeHint,
                        color = ShapesDemoSecondaryTextColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                    SelectionModeRow(
                        mode = selectionMode,
                        onModeSelected = { selectionMode = it }
                    )
                    PresetGrid(
                        presets = presets,
                        sourceShapeId = sourceShapeId,
                        targetShapeId = targetShapeId,
                        onPresetClick = { presetId ->
                            if (selectionMode == ShapesDemoSelectionMode.Shape) {
                                sourceShapeId = presetId
                            } else {
                                targetShapeId = presetId
                            }
                        }
                    )
                }

                DemoPanel {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SelectionLabel(
                            title = ShapesDemoSourceLabel,
                            value = sourcePreset.label,
                            color = ShapesDemoAccentColor
                        )
                        SelectionLabel(
                            title = ShapesDemoTargetLabel,
                            value = targetPreset.label,
                            color = ShapesDemoTargetSelectionColor
                        )
                    }

                    Text(
                        text = ShapesDemoProgressLabel,
                        color = ShapesDemoPrimaryTextColor,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = morphProgress,
                        onValueChange = { morphProgress = it },
                        valueRange = 0f..1f
                    )
                    Text(
                        text = "${(morphProgress * 100f).roundToInt()}%",
                        color = ShapesDemoSecondaryTextColor,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun DemoPanel(
    contentPadding: PaddingValues = PaddingValues(12.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(ShapesDemoPanelColor)
            .border(
                width = 1.dp,
                color = ShapesDemoPanelBorderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun SelectionModeRow(
    mode: ShapesDemoSelectionMode,
    onModeSelected: (ShapesDemoSelectionMode) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SelectionModeChip(
            label = ShapesDemoShapeModeLabel,
            selected = mode == ShapesDemoSelectionMode.Shape,
            onClick = { onModeSelected(ShapesDemoSelectionMode.Shape) }
        )
        SelectionModeChip(
            label = ShapesDemoEditModeLabel,
            selected = mode == ShapesDemoSelectionMode.Edit,
            onClick = { onModeSelected(ShapesDemoSelectionMode.Edit) }
        )
    }
}

@Composable
private fun SelectionModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                if (selected) ShapesDemoAccentColor else ShapesDemoControlSurfaceColor
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    ShapesDemoAccentColor
                } else {
                    ShapesDemoControlBorderColor
                },
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = if (selected) Color.White else ShapesDemoPrimaryTextColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun PresetGrid(
    presets: List<ShapesDemoPreset>,
    sourceShapeId: String,
    targetShapeId: String,
    onPresetClick: (String) -> Unit
) {
    val rows = remember(presets) { presets.chunked(5) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowPresets ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowPresets.forEach { preset ->
                    val isSource = preset.id == sourceShapeId
                    val isTarget = preset.id == targetShapeId
                    PresetCell(
                        modifier = Modifier.weight(1f),
                        preset = preset,
                        isSource = isSource,
                        isTarget = isTarget,
                        onClick = { onPresetClick(preset.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PresetCell(
    preset: ShapesDemoPreset,
    isSource: Boolean,
    isTarget: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = when {
        isTarget -> ShapesDemoTargetSelectionColor
        isSource -> ShapesDemoAccentColor
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black)
            .border(
                width = if (isSource || isTarget) 2.dp else 1.dp,
                color = if (isSource || isTarget) borderColor else ShapesDemoControlBorderColor.copy(alpha = 0.58f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        ThinkingShapeCanvas(
            modifier = Modifier.size(56.dp),
            startPolygon = preset.polygon,
            rotationDegrees = 0f,
            visualScale = 0.96f,
            brush = SolidColor(Color.White),
            showOutline = false
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isSource) {
                PresetBadge(
                    label = ShapesDemoSourceShortLabel,
                    color = ShapesDemoAccentColor
                )
            }
            if (isTarget) {
                PresetBadge(
                    label = ShapesDemoTargetShortLabel,
                    color = ShapesDemoTargetSelectionColor
                )
            }
        }
    }
}

@Composable
private fun PresetBadge(
    label: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SelectionLabel(
    title: String,
    value: String,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            color = ShapesDemoPrimaryTextColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.widthIn(max = 160.dp)
        )
    }
}

private fun buildShapesDemoPresets(): List<ShapesDemoPreset> {
    return listOf(
        ShapesDemoPreset(
            id = "circle",
            label = "Circle",
            polygon = regularRoundedPolygon(vertices = 28, roundingRadius = 0.48f, smoothing = 1f)
        ),
        ShapesDemoPreset(
            id = "ripple_badge",
            label = "Ripple badge",
            polygon = wavyPolygon(lobes = 10, outerRadius = 1f, innerRadius = 0.94f, roundingRadius = 0.30f, smoothing = 1f)
        ),
        ShapesDemoPreset(
            id = "clover",
            label = "Clover",
            polygon = wavyPolygon(lobes = 4, outerRadius = 1f, innerRadius = 0.78f, roundingRadius = 0.42f, smoothing = 1f)
        ),
        ShapesDemoPreset(
            id = "soft_triangle",
            label = "Soft triangle",
            polygon = regularRoundedPolygon(vertices = 3, roundingRadius = 0.42f, smoothing = 1f)
        ),
        ShapesDemoPreset(
            id = "soft_badge",
            label = "Soft badge",
            polygon = wavyPolygon(lobes = 8, outerRadius = 1f, innerRadius = 0.84f, roundingRadius = 0.40f, smoothing = 0.98f)
        ),
        ShapesDemoPreset(
            id = "scallop",
            label = "Scallop",
            polygon = wavyPolygon(lobes = 16, outerRadius = 1f, innerRadius = 0.96f, roundingRadius = 0.34f, smoothing = 1f)
        ),
        ShapesDemoPreset(
            id = "slash_left",
            label = "Slash",
            polygon = roundedRectPolygon(width = 1.52f, height = 0.34f, rotationDegrees = -46f)
        ),
        ShapesDemoPreset(
            id = "slash_right",
            label = "Backslash",
            polygon = roundedRectPolygon(width = 1.52f, height = 0.34f, rotationDegrees = 46f)
        ),
        ShapesDemoPreset(
            id = "pebble",
            label = "Pebble",
            polygon = customRoundedPolygon(
                vertices = polarVertices(
                    radii = listOf(1f, 0.95f, 0.98f, 0.94f, 1f, 0.96f, 0.99f, 0.93f, 0.98f, 0.95f, 0.97f, 0.94f)
                ),
                roundingRadius = 0.34f,
                smoothing = 1f
            )
        ),
        ShapesDemoPreset(
            id = "soft_pyramid",
            label = "Rounded triangle",
            polygon = regularRoundedPolygon(vertices = 3, roundingRadius = 0.26f, smoothing = 0.72f)
        ),
        ShapesDemoPreset(
            id = "arch",
            label = "Arch",
            polygon = customRoundedPolygon(
                vertices = floatArrayOf(
                    -0.86f, -1f,
                    -0.86f, 0.18f,
                    -0.54f, 0.92f,
                    0.54f, 0.92f,
                    0.86f, 0.18f,
                    0.86f, -1f
                ),
                roundingRadius = 0.34f,
                smoothing = 0.92f
            )
        ),
        ShapesDemoPreset(
            id = "square",
            label = "Square",
            polygon = roundedRectPolygon(width = 1.46f, height = 1.46f, roundingRadius = 0.04f, smoothing = 0.12f)
        ),
        ShapesDemoPreset(
            id = "pentagon",
            label = "Pentagon",
            polygon = regularRoundedPolygon(vertices = 5, roundingRadius = 0.08f, smoothing = 0.16f)
        ),
        ShapesDemoPreset(
            id = "star5",
            label = "Star",
            polygon = starPolygon(points = 5, outerRadius = 1f, innerRadius = 0.38f)
        ),
        ShapesDemoPreset(
            id = "star8",
            label = "Burst",
            polygon = starPolygon(points = 8, outerRadius = 1f, innerRadius = 0.56f)
        )
    )
}

private fun regularRoundedPolygon(
    vertices: Int,
    roundingRadius: Float,
    smoothing: Float,
    radius: Float = 1f
): RoundedPolygon {
    return customRoundedPolygon(
        vertices = polarVertices(
            radii = List(vertices) { radius },
            rotationDegrees = -90f
        ),
        roundingRadius = roundingRadius,
        smoothing = smoothing
    )
}

private fun wavyPolygon(
    lobes: Int,
    outerRadius: Float,
    innerRadius: Float,
    roundingRadius: Float,
    smoothing: Float
): RoundedPolygon {
    return customRoundedPolygon(
        vertices = polarVertices(
            radii = List(lobes * 2) { index ->
                if (index % 2 == 0) outerRadius else innerRadius
            },
            rotationDegrees = -90f
        ),
        roundingRadius = roundingRadius,
        smoothing = smoothing
    )
}

private fun starPolygon(
    points: Int,
    outerRadius: Float,
    innerRadius: Float
): RoundedPolygon {
    return customRoundedPolygon(
        vertices = polarVertices(
            radii = List(points * 2) { index ->
                if (index % 2 == 0) outerRadius else innerRadius
            },
            rotationDegrees = -90f
        ),
        roundingRadius = 0.06f,
        smoothing = 0.14f
    )
}

private fun roundedRectPolygon(
    width: Float,
    height: Float,
    rotationDegrees: Float = 0f,
    roundingRadius: Float = 0.48f,
    smoothing: Float = 1f
): RoundedPolygon {
    val halfWidth = width / 2f
    val halfHeight = height / 2f
    val baseVertices = floatArrayOf(
        -halfWidth, -halfHeight,
        halfWidth, -halfHeight,
        halfWidth, halfHeight,
        -halfWidth, halfHeight
    )
    return customRoundedPolygon(
        vertices = rotateVertices(baseVertices, rotationDegrees),
        roundingRadius = roundingRadius,
        smoothing = smoothing
    )
}

private fun customRoundedPolygon(
    vertices: FloatArray,
    roundingRadius: Float,
    smoothing: Float
): RoundedPolygon {
    return RoundedPolygon(
        vertices = vertices,
        rounding = CornerRounding(
            radius = roundingRadius,
            smoothing = smoothing
        ),
        centerX = 0f,
        centerY = 0f
    )
}

private fun polarVertices(
    radii: List<Float>,
    rotationDegrees: Float = -90f
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

private fun rotateVertices(
    vertices: FloatArray,
    degrees: Float
): FloatArray {
    if (degrees == 0f) return vertices
    val radians = degrees * (PI / 180f)
    val cosTheta = cos(radians)
    val sinTheta = sin(radians)
    return FloatArray(vertices.size).also { rotated ->
        var index = 0
        while (index < vertices.size) {
            val x = vertices[index]
            val y = vertices[index + 1]
            rotated[index] = ((x * cosTheta) - (y * sinTheta)).toFloat()
            rotated[index + 1] = ((x * sinTheta) + (y * cosTheta)).toFloat()
            index += 2
        }
    }
}

@Composable
private fun ShapesMorphEditorPlayground(
    modifier: Modifier = Modifier,
    initialBaseShape: ShapesEditorBaseShape = ShapesEditorBaseShape.Star
) {
    val defaults = initialBaseShape.defaults
    var baseShape by rememberSaveable { mutableStateOf(initialBaseShape) }
    var sides by rememberSaveable { mutableFloatStateOf(defaults.sides) }
    var innerRadiusRatio by rememberSaveable { mutableFloatStateOf(defaults.innerRadiusRatio) }
    var roundRadius by rememberSaveable { mutableFloatStateOf(defaults.roundRadius) }
    var smoothing by rememberSaveable { mutableFloatStateOf(defaults.smoothing) }
    var innerRoundRadius by rememberSaveable { mutableFloatStateOf(defaults.innerRoundRadius) }
    var innerSmoothing by rememberSaveable { mutableFloatStateOf(defaults.innerSmoothing) }
    var rotation by rememberSaveable { mutableFloatStateOf(defaults.rotation) }
    var autoSize by rememberSaveable { mutableStateOf(true) }

    fun applyDefaults(shape: ShapesEditorBaseShape) {
        baseShape = shape
        sides = shape.defaults.sides
        innerRadiusRatio = shape.defaults.innerRadiusRatio
        roundRadius = shape.defaults.roundRadius
        smoothing = shape.defaults.smoothing
        innerRoundRadius = shape.defaults.innerRoundRadius
        innerSmoothing = shape.defaults.innerSmoothing
        rotation = shape.defaults.rotation
    }

    val polygon = remember(
        baseShape,
        sides,
        innerRadiusRatio,
        roundRadius,
        smoothing,
        innerRoundRadius,
        innerSmoothing,
        rotation
    ) {
        buildEditorDemoPolygon(
            baseShape = baseShape,
            sides = sides.roundToInt(),
            innerRadiusRatio = innerRadiusRatio,
            roundRadius = roundRadius,
            smoothing = smoothing,
            innerRoundRadius = innerRoundRadius,
            innerSmoothing = innerSmoothing,
            rotation = rotation
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ShapesDemoSurfaceColor)
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(brush = ShapesDemoTopBarBrush)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DemoPanel(contentPadding = PaddingValues(12.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(116.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .border(
                            width = 1.dp,
                            color = ShapesDemoControlBorderColor.copy(alpha = 0.58f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    ThinkingShapeCanvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(84.dp)
                            .align(Alignment.Center)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        startPolygon = polygon,
                        rotationDegrees = 0f,
                        visualScale = if (autoSize) 1.12f else 0.98f,
                        brush = SolidColor(Color.White),
                        showOutline = false
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DemoPanel(contentPadding = PaddingValues(16.dp)) {
                    ShapesEditorHeaderRow(
                        label = "Base Shape:",
                        content = {
                            ShapesEditorActionButton(
                                label = baseShape.label,
                                onClick = {
                                    val nextShape = when (baseShape) {
                                        ShapesEditorBaseShape.Star -> ShapesEditorBaseShape.SoftBadge
                                        ShapesEditorBaseShape.SoftBadge -> ShapesEditorBaseShape.Polygon
                                        ShapesEditorBaseShape.Polygon -> ShapesEditorBaseShape.Star
                                    }
                                    applyDefaults(nextShape)
                                }
                            )
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        ShapesEditorActionButton(
                            label = "Reset View",
                            compact = true,
                            onClick = { applyDefaults(baseShape) }
                        )
                    }

                    ShapesEditorSliderRow(
                        label = "Sides",
                        value = sides,
                        valueRange = 3f..12f,
                        steps = 8,
                        onValueChange = { sides = it }
                    )
                    ShapesEditorSliderRow(
                        label = "InnerRadiusRatio",
                        value = innerRadiusRatio,
                        valueRange = 0.2f..1f,
                        onValueChange = { innerRadiusRatio = it }
                    )
                    ShapesEditorSliderRow(
                        label = "RoundRadius",
                        value = roundRadius,
                        valueRange = 0f..0.48f,
                        onValueChange = { roundRadius = it }
                    )
                    ShapesEditorSliderRow(
                        label = "Smoothing",
                        value = smoothing,
                        valueRange = 0f..1f,
                        onValueChange = { smoothing = it }
                    )
                    ShapesEditorSliderRow(
                        label = "InnerRoundRadius",
                        value = innerRoundRadius,
                        valueRange = 0f..0.48f,
                        onValueChange = { innerRoundRadius = it }
                    )
                    ShapesEditorSliderRow(
                        label = "InnerSmoothing",
                        value = innerSmoothing,
                        valueRange = 0f..1f,
                        onValueChange = { innerSmoothing = it }
                    )
                    ShapesEditorSliderRow(
                        label = "Rotation",
                        value = rotation,
                        valueRange = 0f..360f,
                        onValueChange = { rotation = it }
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ShapesEditorActionButton(
                            label = "Accept",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {}
                        )
                        ShapesEditorActionButton(
                            label = "Shape",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val nextShape = when (baseShape) {
                                    ShapesEditorBaseShape.Star -> ShapesEditorBaseShape.SoftBadge
                                    ShapesEditorBaseShape.SoftBadge -> ShapesEditorBaseShape.Polygon
                                    ShapesEditorBaseShape.Polygon -> ShapesEditorBaseShape.Star
                                }
                                applyDefaults(nextShape)
                            }
                        )
                        ShapesEditorActionButton(
                            label = "AutoSize",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { autoSize = !autoSize }
                        )
                        ShapesEditorActionButton(
                            label = "Dump to Logcat",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapesEditorHeaderRow(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = label,
            color = ShapesDemoPrimaryTextColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
        content()
    }
}

@Composable
private fun ShapesEditorSliderRow(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    steps: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.widthIn(min = 112.dp),
            color = ShapesDemoPrimaryTextColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Normal
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = ShapesDemoActiveTrackColor,
                activeTrackColor = ShapesDemoActiveTrackColor,
                inactiveTrackColor = ShapesDemoInactiveTrackColor
            )
        )
    }
}

@Composable
private fun ShapesEditorActionButton(
    label: String,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(brush = ShapesDemoButtonBrush)
            .clickable(onClick = onClick)
            .padding(
                horizontal = if (compact) 8.dp else 14.dp,
                vertical = if (compact) 6.dp else 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            style = if (compact) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.titleMedium
            },
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun buildEditorDemoPolygon(
    baseShape: ShapesEditorBaseShape,
    sides: Int,
    innerRadiusRatio: Float,
    roundRadius: Float,
    smoothing: Float,
    innerRoundRadius: Float,
    innerSmoothing: Float,
    rotation: Float
): RoundedPolygon {
    return when (baseShape) {
        ShapesEditorBaseShape.Polygon -> customRoundedPolygon(
            vertices = polarVertices(
                radii = List(sides.coerceAtLeast(3)) { 1f },
                rotationDegrees = rotation - 90f
            ),
            roundingRadius = roundRadius,
            smoothing = smoothing
        )
        ShapesEditorBaseShape.Star -> alternatingRoundedPolygon(
            sides = sides.coerceAtLeast(3),
            innerRadiusRatio = innerRadiusRatio,
            outerRoundRadius = roundRadius,
            outerSmoothing = smoothing,
            innerRoundRadius = innerRoundRadius,
            innerSmoothing = innerSmoothing,
            rotation = rotation
        )
        ShapesEditorBaseShape.SoftBadge -> alternatingRoundedPolygon(
            sides = sides.coerceAtLeast(3),
            innerRadiusRatio = innerRadiusRatio.coerceAtLeast(0.55f),
            outerRoundRadius = roundRadius,
            outerSmoothing = smoothing.coerceAtLeast(0.45f),
            innerRoundRadius = innerRoundRadius,
            innerSmoothing = innerSmoothing.coerceAtLeast(0.3f),
            rotation = rotation
        )
    }
}

private fun alternatingRoundedPolygon(
    sides: Int,
    innerRadiusRatio: Float,
    outerRoundRadius: Float,
    outerSmoothing: Float,
    innerRoundRadius: Float,
    innerSmoothing: Float,
    rotation: Float
): RoundedPolygon {
    val pointCount = sides.coerceAtLeast(3) * 2
    val vertices = polarVertices(
        radii = List(pointCount) { index ->
            if (index % 2 == 0) {
                1f
            } else {
                innerRadiusRatio.coerceIn(0.2f, 1f)
            }
        },
        rotationDegrees = rotation - 90f
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

@Preview(
    name = "Shapes Demo Badge To Star",
    showBackground = true,
    backgroundColor = 0xFF090B11,
    widthDp = 420,
    heightDp = 980
)
@Composable
private fun ShapesDemoBadgeToStarPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ShapesMorphPlayground(
            initialSourceShapeId = "soft_badge",
            initialTargetShapeId = "star5",
            initialMorphProgress = 0.22f,
            initialSelectionMode = ShapesDemoSelectionMode.Edit
        )
    }
}

@Preview(
    name = "Shapes Demo Circle To Burst",
    showBackground = true,
    backgroundColor = 0xFF090B11,
    widthDp = 420,
    heightDp = 980
)
@Composable
private fun ShapesDemoCircleToBurstPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ShapesMorphPlayground(
            initialSourceShapeId = "circle",
            initialTargetShapeId = "star8",
            initialMorphProgress = 0.56f,
            initialSelectionMode = ShapesDemoSelectionMode.Edit
        )
    }
}

@Preview(
    name = "Shapes Editor Demo Star",
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 420,
    heightDp = 980
)
@Composable
private fun ShapesEditorDemoStarPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ShapesMorphEditorPlayground(
            initialBaseShape = ShapesEditorBaseShape.Star
        )
    }
}
