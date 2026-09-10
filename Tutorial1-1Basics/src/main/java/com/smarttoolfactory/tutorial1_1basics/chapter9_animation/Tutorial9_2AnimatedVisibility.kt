package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateRect
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_2Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_2_title,
        fitDemoContent = true,
        introduction = "**AnimatedVisibility** animates appearance and disappearance. **AnimatedContent** transitions between content states.",
        examples = listOf(
            AnimationExample("AnimatedVisibilityTransitionSample", "Visibility Transition State",
                "Observe current and target visibility while changing the transition state.") { AnimatedVisibilityTransitionSample() },
            AnimationExample("AnimatedVisibilityCloseTest", "Close After an Exit Animation",
                "Trigger an exit and observe when the content leaves composition.") { AnimatedVisibilityCloseTest() },
            AnimationExample("MutableTransitionStatePreview", "MutableTransitionState",
                "Give visibility an initial state and a different target to animate its first appearance.") { MutableTransitionStatePreview() },
            AnimationExample("PoppingInCardPreview", "Popping Card",
                "Run the card's entrance and exit transitions.") { PoppingInCardPreview() },
            AnimationExample("AnimateEnterExitSample", "animateEnterExit",
                "Children can choose their own entrance and exit within an AnimatedVisibility scope.") { AnimateEnterExitSample() }
        )
    )
}

// TODO Create a sample with SeekableTransitionState too

@Preview
@Composable
fun AnimatedVisibilityTransitionSample() {
    val visibleState: MutableTransitionState<Boolean> = remember { MutableTransitionState(false) }
    val transition: Transition<Boolean> = rememberTransition(visibleState)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimationTutorialButton(
            onClick = {
                visibleState.targetState = visibleState.targetState.not()
            }
        ) {
            Text("Update Target State")
        }

        Text(
            "State currentState: ${visibleState.currentState}\n" +
                    "targetState: ${visibleState.targetState}\n" +
                    "isIdle:  ${visibleState.isIdle}",
            fontSize = 16.sp
        )

        if (transition.targetState || transition.currentState) {
            Popup(
                properties = PopupProperties(focusable = true),
                offset = IntOffset(200, 400),
                onDismissRequest = {
                    visibleState.targetState = false
                }
            ) {
                transition.AnimatedVisibility(
                    visible = { targetSelected -> targetSelected },
                    enter = fadeIn(
                        animationSpec = tween(600)
                    ),
                    exit = fadeOut(
                        animationSpec = tween(600)
                    )
                ) {
                    Box(modifier = Modifier.background(Color.Red).padding(16.dp)) {
                        Text("Popup Content...")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AnimatedVisibilityCloseTest() {
    val visibleState = remember { MutableTransitionState(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        AnimationTutorialButton(
            onClick = {
                visibleState.targetState = visibleState.targetState.not()
            }
        ) {
            Text(
                "State currentState: ${visibleState.currentState}\n" +
                        "targetState: ${visibleState.targetState}\n" +
                        "isIdle:  ${visibleState.isIdle}"
            )
        }

        val context = LocalContext.current

        // Check animation end, need a flag to prevent triggering at the start, skipped if deliberately
        LaunchedEffect(key1 = visibleState.currentState, key2 = visibleState.targetState) {
            if (visibleState.targetState == visibleState.currentState) {
                Toast.makeText(context, "Animation finished", Toast.LENGTH_SHORT).show()
            }
        }

        AnimatedVisibility(
            visibleState = visibleState,
            enter = fadeIn(
                animationSpec = tween(5000)
            ),
            exit = fadeOut(
                tween(5000)
            )
        ) {
            Box(modifier = Modifier.background(Color.Red).padding(16.dp)) {
                Text("Test Composable...")
            }
        }
    }

}

@Preview
@Composable
fun MutableTransitionStatePreview() {
    val visibleState: MutableTransitionState<Boolean> = remember { MutableTransitionState(false) }
    val transition: Transition<Boolean> = rememberTransition(visibleState)

    val size by transition.animateDp(
        label = "",
        transitionSpec = {
            tween(3000)
        }
    ) { visible ->
        if (visible) 200.dp else 100.dp
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AnimationTutorialButton(
            onClick = {
                visibleState.targetState = visibleState.targetState.not()
            }
        ) {

            Text(
                "State currentState: ${visibleState.currentState}, " +
                        "targetState: ${visibleState.targetState},isIdle:  ${visibleState.isIdle}"
            )
        }


        Box(
            modifier = Modifier.size(size).background(Color.Red)
        )
    }
}

@Preview
@Composable
fun PoppingInCardPreview() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PoppingInCard()
    }
}

// This composable enters the composition with a custom enter transition. This is achieved by
// defining a different initialState than the first target state using `MutableTransitionState`
@Composable
fun PoppingInCard() {
    // Creates a transition state with an initial state where visible = false
    val visibleState = remember { MutableTransitionState(false) }

    LaunchedEffect(Unit) {
        // Sets the target state of the transition state to true. As it's different than the initial
        // state, a transition from not visible to visible will be triggered.
        visibleState.targetState = true
    }

    // Creates a transition with the transition state created above.
    val transition: Transition<Boolean> = rememberTransition(visibleState)
    // Adds a scale animation to the transition to scale the card up when transitioning in.
    val scale by transition.animateFloat(
        // Uses a custom spring for the transition.
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy) }, label = ""
    ) { visible ->
        if (visible) 1f else 0.8f
    }

    // Adds an elevation animation that animates the dp value of the animation.
    val elevation by transition.animateDp(
        // Uses a tween animation
        transitionSpec = {
            // Uses different animations for when animating from visible to not visible, and
            // the other way around
            if (false isTransitioningTo true) {
                tween(1000)
            } else {
                spring()
            }
        }, label = ""
    ) { visible ->
        if (visible) 10.dp else 0.dp
    }

    transition.animateRect(label = "") { visible ->
        if (visible) {
            Rect.Zero
        } else {
            Rect(
                offset = Offset.Zero,
                size = Size(100f, 100f)
            )
        }
    }

    AnimationTutorialButton(
        onClick = {
            val currentState = visibleState.targetState
            visibleState.targetState = currentState.not()
        }
    ) {
        Text("VisibleState ${visibleState.targetState}")
    }
    Card(
        Modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .size(200.dp, 100.dp)
            .fillMaxWidth(),
        elevation = elevation
    ) {}
}

@Preview
@Composable
fun AnimateEnterExitSample() {
    var visible by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AnimationTutorialButton(
            onClick = {
                visible = visible.not()
            }
        ) {
            Text("Visible $visible")
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(
                tween(5000)
            ),
            exit = fadeOut(
                tween(5000)
            )
        ) {
            // Fade in/out the background and the foreground.
            Box(Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray)) {
                Box(
                    Modifier
                        .align(Alignment.Center)
                        .animateEnterExit(
                            // Slide in/out the inner box.
                            enter = slideInVertically(
                                initialOffsetY = { fullHeight: Int ->
                                    -fullHeight * 2
                                }
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { fullHeight: Int ->
                                    -fullHeight * 2
                                }
                            )
                        )
                        .sizeIn(minWidth = 256.dp, minHeight = 64.dp)
                        .background(Color.Red)
                ) {
                    // Content of the notification…
                }
            }
        }
    }
}
