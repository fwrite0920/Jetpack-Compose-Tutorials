package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage
import androidx.compose.runtime.Composable

@Composable
internal fun Tutorial9_14Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_14_title,
        fitDemoContent = true,
        introduction = "**Shake on Touch** — Press the button to start a short shake. Adjust the amplitude and observe completion.",
        examples = listOf(
            AnimationExample("ShakeTouchDemo", "Shake on Touch",
                "Press the button to start a short shake. Adjust the amplitude and observe completion.") { ShakeTouchDemo() }
        )
    )
}


fun Modifier.shakeTouch(
    interactionSource: InteractionSource,
    amplitude: Dp = 2.dp,
    stepDurationMillis: Int = 34,
    oscillationCount: Int = 4,
    settleAnimationSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
    ),
    onComplete: () -> Unit = {},
): Modifier = this.then(
    ShakeTouchElement(
        interactionSource = interactionSource,
        amplitude = amplitude,
        stepDurationMillis = stepDurationMillis,
        oscillationCount = oscillationCount,
        settleAnimationSpec = settleAnimationSpec,
        onComplete = onComplete,
    )
)

private data class ShakeTouchElement(
    val interactionSource: InteractionSource,
    val amplitude: Dp,
    val stepDurationMillis: Int,
    val oscillationCount: Int,
    val settleAnimationSpec: FiniteAnimationSpec<Float>,
    val onComplete: () -> Unit,
) : ModifierNodeElement<ShakeTouchNode>() {

    override fun create(): ShakeTouchNode {
        return ShakeTouchNode(
            interactionSource = interactionSource,
            amplitude = amplitude,
            stepDurationMillis = stepDurationMillis,
            oscillationCount = oscillationCount,
            settleAnimationSpec = settleAnimationSpec,
            onComplete = onComplete,
        )
    }

    override fun update(node: ShakeTouchNode) {
        node.update(
            interactionSource = interactionSource,
            amplitude = amplitude,
            stepDurationMillis = stepDurationMillis,
            oscillationCount = oscillationCount,
            settleAnimationSpec = settleAnimationSpec,
            onComplete = onComplete,
        )
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shakeTouch"
        properties["interactionSource"] = interactionSource
        properties["amplitude"] = amplitude
        properties["stepDurationMillis"] = stepDurationMillis
        properties["oscillationCount"] = oscillationCount
        properties["settleAnimationSpec"] = settleAnimationSpec
    }
}

private class ShakeTouchNode(
    private var interactionSource: InteractionSource,
    private var amplitude: Dp,
    private var stepDurationMillis: Int,
    private var oscillationCount: Int,
    private var settleAnimationSpec: FiniteAnimationSpec<Float>,
    private var onComplete: () -> Unit,
) : Modifier.Node(), DrawModifierNode, CompositionLocalConsumerModifierNode {

    private val translationXAnim = Animatable(0f)
    private var interactionJob: Job? = null
    private var shakeJob: Job? = null

    override fun onAttach() {
        restartInteractionCollection()
    }

    override fun onDetach() {
        interactionJob?.cancel()
        interactionJob = null
        shakeJob?.cancel()
        shakeJob = null
    }

    fun update(
        interactionSource: InteractionSource,
        amplitude: Dp,
        stepDurationMillis: Int,
        oscillationCount: Int,
        settleAnimationSpec: FiniteAnimationSpec<Float>,
        onComplete: () -> Unit,
    ) {
        val sourceChanged = this.interactionSource !== interactionSource
        this.interactionSource = interactionSource
        this.amplitude = amplitude
        this.stepDurationMillis = stepDurationMillis
        this.oscillationCount = oscillationCount
        this.settleAnimationSpec = settleAnimationSpec
        this.onComplete = onComplete

        if (sourceChanged && isAttached) {
            restartInteractionCollection()
        }
    }

    override fun ContentDrawScope.draw() {
        val drawScope = this
        translate(left = translationXAnim.value, top = 0f) {
            drawScope.drawContent()
        }
    }

    private fun restartInteractionCollection() {
        interactionJob?.cancel()
        interactionJob = coroutineScope.launch {
            interactionSource.interactions.collect { interaction ->
                if (interaction is PressInteraction.Press) {
                    startShakeBurst()
                }
            }
        }
    }

    private fun startShakeBurst() {
        shakeJob?.cancel()
        shakeJob = coroutineScope.launch {
            val amplitudePx = with(currentValueOf(LocalDensity)) {
                amplitude.toPx().coerceAtLeast(0f)
            }
            val clampedStepDurationMillis = stepDurationMillis.coerceAtLeast(1)
            val clampedOscillationCount = oscillationCount.coerceAtLeast(1)

            translationXAnim.stop()
            translationXAnim.snapTo(0f)
            invalidateDraw()

            if (amplitudePx == 0f) {
                onComplete()
            } else {
                repeat(clampedOscillationCount) { index ->
                    val direction = if (index % 2 == 0) 1f else -1f
                    translationXAnim.animateTo(
                        targetValue = amplitudePx * direction,
                        animationSpec = tween(
                            durationMillis = clampedStepDurationMillis,
                            easing = LinearEasing,
                        ),
                    ) {
                        this@ShakeTouchNode.invalidateDraw()
                    }
                }

                translationXAnim.animateTo(
                    targetValue = 0f,
                    animationSpec = settleAnimationSpec,
                ) {
                    this@ShakeTouchNode.invalidateDraw()
                }
                onComplete()
            }
        }
    }
}

@Composable
private fun ShakeTouchDemo() {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    var amplitude by remember { mutableFloatStateOf(8f) }
    var completed by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialSlider("Shake amplitude (dp)", amplitude, 1f..24f) { amplitude = it }
        AnimationTutorialButton(onClick = {}, interactionSource = interaction,
            modifier = Modifier.shakeTouch(interaction, amplitude.dp, onComplete = { completed++ })
                .testTag("shake-button")) { Text("Press to shake") }
        Text("Completed: $completed", Modifier.testTag("shake-result"))
    }
}
