package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_13Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_13_title,
        fitDemoContent = true,
        introduction = "**Elastic Press** — Press and hold the button, then release it to compare compression with a spring rebound.",
        examples = listOf(
            AnimationExample("ElasticPressDemo", "Elastic Press",
                "Press and hold the button, then release it to compare compression with a spring rebound.") { ElasticPressDemo() }
        )
    )
}


@Composable
fun Modifier.elasticPress(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.88f,
): Modifier = composed {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) {
            tween(durationMillis = 90)
        } else {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium,
            )
        },
        label = "elastic_press_scale",
    )

    graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
private fun ElasticPressDemo() {
    val interaction = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    var scale by remember { mutableFloatStateOf(0.88f) }
    var clicks by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialSlider("Pressed scale (%)", scale * 100f, 50f..98f) { scale = it / 100f }
        AnimationTutorialButton(onClick = { clicks++ }, interactionSource = interaction,
            modifier = Modifier.elasticPress(interaction, scale).testTag("elastic-button")) {
            Text("Press and hold")
        }
        Text("Clicks: $clicks", Modifier.testTag("elastic-result"))
    }
}
