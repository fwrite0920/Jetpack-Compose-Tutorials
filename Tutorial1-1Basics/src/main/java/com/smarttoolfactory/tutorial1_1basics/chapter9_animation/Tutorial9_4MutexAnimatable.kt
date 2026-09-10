package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_4Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_4_title,
        fitDemoContent = true,
        introduction = "**Animatable** gives coroutine control over motion. Cancellation lets a new request interrupt the old one.",
        examples = listOf(
            AnimationExample("MutexMutatorMutexTest1", "Mutex and MutatorMutex",
                "Mutex queues callers. MutatorMutex lets an equal- or higher-priority request cancel the current mutation.") { MutexMutatorMutexTest1() },
            AnimationExample("MutexMutatorMutexTest2", "Concurrent Mutation",
                "Try the existing concurrent animation experiment and compare competing mutations.") { MutexMutatorMutexTest2() }
        )
    )
}

@Preview
@Composable
fun MutexMutatorMutexTest1() {

    val mutex = remember { Mutex() }
    val mutatorMutex = remember { MutatorMutex() }
    val scope = rememberCoroutineScope()

    val animatableMutex = remember { Animatable(0f) }
    val animatableMutatorMutex = remember { Animatable(0f) }
    var counterForMutatorMutex by remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Animatable Mutex: ${animatableMutex.value.toInt()}", fontSize = 20.sp)
        Text("Animatable MutatorMutex: ${animatableMutatorMutex.value.toInt()}", fontSize = 20.sp)
        Text("counterForMutatorMutex: $counterForMutatorMutex", fontSize = 20.sp)

        AnimationTutorialButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch(Dispatchers.Default) {
                    val isLocked = mutex.isLocked
                    println("Mutex locked: $isLocked, in thread: ${Thread.currentThread().name}")
                    // When new block is invoked it starts after first one is completed
                    mutex.withLock {
                        println("Mutex running in thread: ${Thread.currentThread().name}")

                        try {
//                            animatableMutex.snapTo(0f)
//                            animatableMutex.animateTo(
//                                targetValue = 100f,
//                                animationSpec = tween(5000, easing = LinearEasing)
//                            )

                            counterForMutatorMutex = 0
                            repeat(100) {
                                counterForMutatorMutex = it
                                delay(50)
                            }
                        } catch (e: CancellationException) {
                            println("Mutex Exception: ${e.message}")
                        }
                    }
                }
            }
        ) {
            Text("mutex.withLock")
        }

        AnimationTutorialButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                scope.launch(Dispatchers.Default) {
                    // MutatorMutex mutate will enforce that only a single caller may be active at a time.
                    // If the new caller has a priority equal to or higher than the call in progress, the call in
                    // progress will be cancelled, throwing CancellationException and the new caller's
                    // block will be invoked.
                    try {
                        mutatorMutex.mutate {
                            println("MutatorMutex is running..., in thread: ${Thread.currentThread().name}")
                            try {
//                                animatableMutatorMutex.snapTo(0f)
//                                animatableMutatorMutex.animateTo(
//                                    targetValue = 100f,
//                                    animationSpec = tween(5000, easing = LinearEasing)
//                                )
                                counterForMutatorMutex = 0
                                repeat(100) {
                                    counterForMutatorMutex = it
                                    delay(50)
                                }
                            } catch (e: CancellationException) {
                                println("MutatorMutex Exception: $e")
                            }
                        }
                    } catch (e: Exception) {
                        println("MutatorMutex Scope Exception: $e")
                    }
                }
            }
        ) {
            Text("mutatorMutex.mutate")
        }

        AnimationTutorialButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                scope.launch(Dispatchers.Default) {
                    // MutatorMutex tryMutate does not cancel while tryMutate black is being invoked
                    try {
                        mutatorMutex.tryMutate {
                            println("MutatorMutex is running..., in thread: ${Thread.currentThread().name}")
                            animatableMutatorMutex.snapTo(0f)
                            try {
                                animatableMutatorMutex.animateTo(
                                    targetValue = 100f,
                                    animationSpec = tween(5000, easing = LinearEasing)
                                )
                            } catch (e: CancellationException) {
                                println("MutatorMutex Exception: $e")
                            }
                        }
                    } catch (e: Exception) {
                        println("MutatorMutex Scope Exception: $e")
                    }
                }
            }
        ) {
            Text("mutatorMutex.tryMutate")
        }
    }
}

@Preview
@Composable
fun MutexMutatorMutexTest2() {

    val mutex = remember { Mutex() }
    val mutatorMutex = remember { MutatorMutex() }
    val scope = rememberCoroutineScope()

    val animatable = remember { Animatable(0f) }

    var counter by remember {
        mutableIntStateOf(0)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text("Value: ${animatable.value.toInt()}", fontSize = 26.sp)
        Text("counter: $counter", fontSize = 26.sp)

        AnimationTutorialButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scope.launch(Dispatchers.Default) {
                    val isLocked = mutex.isLocked
                    println("Mutex locked: $isLocked, in thread: ${Thread.currentThread().name}")
                    // When new block is invoked it starts after first one is completed
                    mutex.withLock {
                        try {
                            animatable.snapTo(0f)
                            animatable.animateTo(
                                targetValue = 100f,
                                animationSpec = tween(5000, easing = LinearEasing)
                            )
                        } catch (e: CancellationException) {
                            println("Mutex Exception: ${e.message}")
                        }

                        counter = 0
                        repeat(100) {
                            counter = it
                            println("Mutex counter: $counter")
                            delay(50)
                        }
                    }
                }
            }
        ) {
            Text("Start with Mutex")
        }

        AnimationTutorialButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                scope.launch(Dispatchers.Default) {
                    // MutatorMutex tryMutate does not cancel while tryMutate black is being invoked
                    try {
                        mutatorMutex.tryMutate {
                            println("MutatorMutex is running..., in thread: ${Thread.currentThread().name}")
                            try {
                                animatable.snapTo(0f)
                                animatable.animateTo(
                                    targetValue = 100f,
                                    animationSpec = tween(5000, easing = LinearEasing)
                                )
                            } catch (e: CancellationException) {
                                println("MutatorMutex Exception: $e")
                            }
                            counter = 0
                            repeat(100) {
                                counter = it
                                println("🚀 MutatorMutex counter: $counter")
                                delay(50)
                            }
                        }
                    } catch (e: Exception) {
                        println("MutatorMutex Scope Exception: $e")
                    }
                }
            }
        ) {
            Text("Start with MutatorMutex")
        }
    }
}
