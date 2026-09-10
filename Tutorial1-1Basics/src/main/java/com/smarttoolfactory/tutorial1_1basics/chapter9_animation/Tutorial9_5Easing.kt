package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
internal fun Tutorial9_5Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_5_title,
        introduction = "**Easing** maps time to motion. A steep curve moves quickly; a flat curve moves slowly.",
        examples = listOf(
            AnimationExample("EasingCurveDemo", "Read an easing curve",
                "Follow the cursor from left to right. Its height is the object's progress, not its physical path.") { EasingCurveDemo() },
            AnimationExample("EasingComparisonDemo", "Same duration, different journeys",
                "Every lane starts together and finishes after the same duration. Only the easing changes.") { EasingComparisonDemo() }
        )
    )
}

private data class CurveLesson(val name: String, val easing: Easing, val explanation: String)
private val CurveLessons = listOf(
    CurveLesson("Linear", LinearEasing, "Equal time steps cover equal distances. The slope stays constant."),
    CurveLesson("Ease in", FastOutLinearInEasing, "Start gently and accelerate. The curve becomes steeper toward the end."),
    CurveLesson("Ease out", LinearOutSlowInEasing, "Move quickly at first, then settle gently. The curve flattens near the end."),
    CurveLesson("Ease in-out", FastOutSlowInEasing, "Accelerate, then decelerate. Most movement happens in the middle."),
    CurveLesson("Back", EaseOutBack, "Go beyond the destination, then return. Progress can exceed 100%."),
    CurveLesson("Bounce", EaseOutBounce, "Reach the destination and bounce back. Changes in slope create the impacts.")
)

@Composable
internal fun EasingCurveDemo() = EasingLesson(compare = false)

@Composable
internal fun EasingComparisonDemo() = EasingLesson(compare = true)

@Composable
private fun EasingLesson(compare: Boolean) {
    val clock = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var selected by remember { mutableIntStateOf(3) }
    var duration by remember { mutableFloatStateOf(1800f) }
    val lesson = CurveLessons[selected]
    val curves = if (compare) CurveLessons.take(4) else listOf(lesson)
    val colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.error)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!compare) {
            TutorialChoices(CurveLessons.map { it.name }, selected) { selected = it }
            TutorialText2(lesson.explanation)
        }
        EasingCurvePlot(curves.map { it.name to it.easing }, clock.value,
            Modifier.fillMaxWidth().height(220.dp), colors)
        curves.forEachIndexed { index, curve ->
            EasingMotionLane(curve.name, curve.easing.transform(clock.value), colors[index % colors.size])
        }
        Text("Time ${(clock.value * 100).roundToInt()}%" + if (!compare)
            "  •  Progress ${(lesson.easing.transform(clock.value) * 100).roundToInt()}%" else "  •  All lanes share this clock",
            style = MaterialTheme.typography.labelLarge, modifier = Modifier.testTag("easing-readout"))
        Slider(clock.value, onValueChange = { value ->
            job?.cancel()
            job = scope.launch { clock.snapTo(value) }
        }, modifier = Modifier.testTag("easing-scrubber"))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimationTutorialButton(onClick = {
                job?.cancel()
                job = scope.launch {
                    clock.snapTo(0f)
                    clock.animateTo(1f, tween(duration.roundToInt(), easing = LinearEasing))
                }
            }) { Text("Play") }
            AnimationTutorialButton(onClick = { job?.cancel() }) { Text("Pause") }
            AnimationTutorialButton(onClick = {
                job?.cancel()
                job = scope.launch { clock.snapTo(0f) }
            }) { Text("Reset") }
        }
        TutorialSlider("Duration (ms)", duration, 400f..4000f) { duration = it }
        TutorialText2("Drag the timeline to inspect a moment. The dashed diagonal is linear timing; the colored curve shows eased progress.")
    }
}

/** Sample the easing function directly; drawing never mutates animation state. */
@Composable
internal fun EasingCurvePlot(
    curves: List<Pair<String, Easing>>,
    time: Float,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colorScheme.primary)
) {
    val grid = MaterialTheme.colorScheme.outlineVariant
    val ink = MaterialTheme.colorScheme.onSurfaceVariant
    val surface = MaterialTheme.colorScheme.surfaceContainerLow
    val samples = remember(curves) { curves.map { (_, easing) -> List(301) { easing.transform(it / 300f) } } }
    val low = minOf(0f, samples.minOf { it.min() }) - .08f
    val high = maxOf(1f, samples.maxOf { it.max() }) + .08f
    Surface(modifier, shape = MaterialTheme.shapes.large, color = surface) {
        Canvas(Modifier.fillMaxSize().padding(8.dp)) {
            val left = 36.dp.toPx()
            val top = 20.dp.toPx()
            val w = (size.width - left - 12.dp.toPx()).coerceAtLeast(1f)
            val h = (size.height - top - 30.dp.toPx()).coerceAtLeast(1f)
            fun point(x: Float, y: Float) = Offset(left + w*x, top + h*(high-y)/(high-low))
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = ink.toArgb(); textSize = 10.dp.toPx()
            }
            fun label(text: String, x: Float, y: Float) { drawContext.canvas.nativeCanvas.drawText(text,x,y,paint) }
            for (i in 0..4) {
                val f = i/4f
                drawLine(grid, point(f,low),point(f,high),1.dp.toPx())
                drawLine(grid,point(0f,f),point(1f,f),1.dp.toPx())
                label("${i*25}", 0f, point(0f,f).y + 3.dp.toPx())
                label("${i*25}",point(f,0f).x - 6.dp.toPx(),size.height - 14.dp.toPx())
            }
            label("Progress %",left,12.dp.toPx())
            label("Time % →",left+w/2-20.dp.toPx(),size.height-1.dp.toPx())
            drawLine(grid,point(0f,0f),point(1f,1f),2.dp.toPx(),pathEffect=PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(),5.dp.toPx())))
            samples.forEachIndexed { index, values ->
                val color = colors[index % colors.size]
                val path = Path().apply { values.forEachIndexed { step, value ->
                    val p = point(step/300f,value)
                    if(step==0) moveTo(p.x,p.y) else lineTo(p.x,p.y)
                } }
                drawPath(path,color.copy(alpha=.10f),style=Stroke(10.dp.toPx(),cap=StrokeCap.Round))
                drawPath(path,color,style=Stroke(2.5.dp.toPx(),cap=StrokeCap.Round))
                val cursor = point(time,curves[index].second.transform(time))
                drawLine(color.copy(alpha=.35f),point(time,low),cursor,1.dp.toPx())
                drawCircle(color.copy(alpha=.18f),10.dp.toPx(),cursor)
                drawCircle(color,5.dp.toPx(),cursor)
                drawCircle(surface,2.dp.toPx(),cursor)
            }
        }
    }
}

@Composable
internal fun EasingMotionLane(name: String, progress: Float, color: Color) {
    val track = MaterialTheme.colorScheme.outlineVariant
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            Text(name, color=color,style=MaterialTheme.typography.labelLarge)
            Text("${(progress*100).roundToInt()}%",style=MaterialTheme.typography.labelMedium)
        }
        Canvas(Modifier.fillMaxWidth().height(30.dp)) {
            // Reserve space on both ends so back/elastic motion can overshoot visibly.
            val start = size.width*.17f
            val end = size.width*.83f
            val y = size.height/2
            drawLine(track,Offset(start,y),Offset(end,y),4.dp.toPx(),cap=StrokeCap.Round)
            for(i in 0..4) drawCircle(track,3.dp.toPx(),Offset(start+(end-start)*i/4f,y))
            drawCircle(color,8.dp.toPx(),Offset(start+(end-start)*progress,y))
            drawCircle(color.copy(alpha=.14f),13.dp.toPx(),Offset(start+(end-start)*progress,y))
        }
    }
}
