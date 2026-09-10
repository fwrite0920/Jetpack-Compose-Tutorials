package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_1Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_1_title,
        fitDemoContent = true,
        introduction = "Animate a **value**, coordinate properties or change content with a Compose animation API.",
        examples = listOf(
            AnimationExample("StateAnimationDemo", "Animate as State",
                "Tap Expand, then Collapse before the animation ends to retarget size and color.") { StateAnimationDemo() },
            AnimationExample("VisibilityDecisionDemo", "Visibility and Child Enter/Exit",
                "Show and hide content; its child has an independent enter/exit animation.") { VisibilityDecisionDemo() },
            AnimationExample("CrossfadeDemo", "Crossfade",
                "Swap two content blocks with an opacity transition.") { CrossfadeDemo() },
            AnimationExample("CoordinatedTransitionDemo", "Coordinated Transitions",
                "One target changes several visual properties together.") { CoordinatedTransitionDemo() },
            AnimationExample("RepeatingAnimationDemo", "Infinite Transition",
                "Start or stop a repeating scale and opacity pulse.") { RepeatingAnimationDemo() },
            AnimationExample("AnimatableInterruptionDemo", "Animatable and Interruption",
                "Start a sequence, interrupt it, or run its two properties concurrently.") { AnimatableInterruptionDemo() }
        )
    )
}

@Composable
internal fun StateAnimationDemo() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val size by animateDpAsState(if (expanded) 160.dp else 64.dp,
        animationSpec = tween(600), label = "Size")
    val color by animateColorAsState(if (expanded) MaterialTheme.colorScheme.tertiary
        else MaterialTheme.colorScheme.primary, animationSpec = tween(600), label = "Color")
    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimationTutorialButton(onClick = { expanded = !expanded }, modifier = Modifier.testTag("animate-state")) {
            Text(if (expanded) "Collapse" else "Expand")
        }
        Text("Animated size: ${size.value.roundToInt()}dp", Modifier.testTag("animated-size"))
        Box(Modifier.size(size).background(color, RoundedCornerShape(16.dp)))
        TutorialText2("animate*AsState follows a target value automatically. Changing the target during motion retargets the running animation.")
    }
}

@Composable
internal fun VisibilityDecisionDemo() {
    var visible by rememberSaveable { mutableStateOf(true) }
    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimationTutorialButton(onClick = { visible = !visible }) { Text(if (visible) "Hide content" else "Show content") }
        AnimatedVisibility(visible, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Card {
                Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("A composed item")
                    Text("The child slides within the parent's fade.",
                        Modifier.animateEnterExit(enter = slideInHorizontally(), exit = slideOutHorizontally()))
                }
            }
        }
        TutorialText2("AnimatedVisibility keeps content composed until its exit finishes, then removes it. Changing only alpha keeps it in the layout.")
    }
}

@Composable
internal fun CrossfadeDemo() {
    var index by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialChoices(listOf("Overview", "Details"), index) { index = it }
        Crossfade(index, animationSpec = tween(600), label = "Crossfade") { page ->
            Card(Modifier.fillMaxWidth()) {
                Text(if (page == 0) "A concise overview" else "Details fade into the same container.",
                    Modifier.padding(32.dp))
            }
        }
        TutorialText2("Crossfade blends content opacity. Use AnimatedContent when size or spatial direction should also communicate a change.")
    }
}

@Composable
internal fun CoordinatedTransitionDemo() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val transition = updateTransition(expanded, label = "Card transition")
    val width by transition.animateDp(label = "Width", transitionSpec = { tween(700) }) {
        if (it) 240.dp else 100.dp
    }
    val corner by transition.animateDp(label = "Corners", transitionSpec = { tween(700) }) {
        if (it) 32.dp else 4.dp
    }
    val color by transition.animateColor(label = "Container", transitionSpec = { tween(700) }) {
        if (it) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer
    }
    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimationTutorialButton(onClick = { expanded = !expanded }) { Text("Toggle transition") }
        Surface(Modifier.width(width).height(120.dp), shape = RoundedCornerShape(corner), color = color) {
            Box(contentAlignment = Alignment.Center) { Text(if (expanded) "Expanded" else "Compact") }
        }
        Text(if (transition.isRunning) "Transition running" else "Transition settled")
        TutorialText2("One Transition coordinates width, shape and color. Each property can use a different spec while sharing the same target state.")
    }
}

@Composable
internal fun RepeatingAnimationDemo() {
    var running by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialToggle("Repeat animation", running) { running = it }
        if (running) {
            val infinite = rememberInfiniteTransition(label = "Pulse")
            val scale by infinite.animateFloat(0.7f, 1.2f,
                infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "Scale")
            val alpha by infinite.animateFloat(0.4f, 1f,
                infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "Alpha")
            Pulse(scale, alpha)
        } else Pulse(1f, 1f)
        TutorialText2("rememberInfiniteTransition runs while it remains in composition. Stop removes that transition and restores the resting state.")
    }
}

@Composable
private fun Pulse(scale: Float, alpha: Float) {
    Box(Modifier.padding(24.dp).size(100.dp).graphicsLayer {
        scaleX = scale; scaleY = scale; this.alpha = alpha
    }.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50)))
}

@Composable
internal fun AnimatableInterruptionDemo() {
    val position = remember { Animatable(0f) }
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    var job by remember { mutableStateOf<Job?>(null) }
    var concurrent by rememberSaveable { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }

    fun animate(target: Float) {
        // Capture velocity before cancelling the owner; cancellation resets Animatable's velocity.
        val velocity = position.velocity
        job?.cancel()
        job = scope.launch {
            status = "Running"
            if (concurrent) {
                coroutineScope {
                    launch { position.animateTo(target, spring(stiffness = Spring.StiffnessLow), initialVelocity = velocity) }
                    launch { scale.animateTo(if (target == 1f) 1.5f else 1f, tween(700)) }
                }
            } else {
                position.animateTo(target, spring(stiffness = Spring.StiffnessLow), initialVelocity = velocity)
                scale.animateTo(if (target == 1f) 1.5f else 1f, tween(700))
            }
            status = "Finished"
        }
    }
    Column(Modifier.fillMaxWidth().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialToggle("Run concurrently", concurrent) { concurrent = it }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimationTutorialButton(onClick = { animate(1f) }, modifier = Modifier.testTag("start-animation")) { Text("Start") }
            AnimationTutorialButton(onClick = { animate(0f) }) { Text("Reverse") }
            AnimationTutorialButton(onClick = {
                job?.cancel()
                scope.launch { position.stop(); scale.stop(); status = "Stopped" }
            }) { Text("Stop") }
        }
        Text("Animation: $status", Modifier.testTag("animation-status"))
        Text("Value: ${(position.value * 100).roundToInt()}% • Velocity: ${position.velocity.roundToInt()}")
        BoxWithConstraints(Modifier.fillMaxWidth().height(130.dp)) {
            Box(Modifier.offset(x = (maxWidth - 80.dp) * position.value).padding(12.dp)
                .size(48.dp).graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                .testTag("animatable-value").semantics {
                    progressBarRangeInfo = ProgressBarRangeInfo(position.value.coerceIn(0f, 1f), 0f..1f)
                })
        }
        TutorialText2("Suspend calls run in sequence. launch runs independent properties together. A new request cancels the previous sequence and carries its current velocity into the next spring.")
    }
}
