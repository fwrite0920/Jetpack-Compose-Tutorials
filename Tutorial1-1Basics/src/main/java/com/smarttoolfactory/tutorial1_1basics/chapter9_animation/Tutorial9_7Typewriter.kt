package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay

import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialButton
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_7Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_7_title,
        fitDemoContent = true,
        introduction = "Animate **text**, **list items** and **pager pages** while keeping their identity stable.",
        examples = listOf(
            AnimationExample("ComposeTypeWriterPreview", "Typewriter Text",
                "Text appears character by character. Reset restarts the coroutine.") { ComposeTypeWriterPreview() }
        )
    )
}

@Preview
@Composable
fun ComposeTypeWriterPreview() {
    ConsoleTypewriter(
        text = "hello world: hello world! hello world. hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world hello world ",
//        text = "booting...\nloading modules...\nready>",
        charDelayMs = { ch ->
            when (ch) {
                '\n' -> 220L
                '.', '!', '?', ':' -> 120L
                ' ' -> 10L
                else -> 18L
            }
        },
        showCursor = false
    )
}

@Composable
fun ConsoleTypewriter(
    text: String,
    modifier: Modifier = Modifier,
    charDelayMs: (Char) -> Long = { 18L },   // customize per character if you want
    startDelayMs: Long = 0L,
    showCursor: Boolean = true,
    cursorChar: String = "█",
    cursorBlinkMs: Long = 500L,
    onFinished: (() -> Unit)? = null,
) {
    var visibleCount by remember(text) { mutableIntStateOf(0) }
    var cursorOn by remember { mutableStateOf(true) }

    // Typing loop
    LaunchedEffect(text, startDelayMs) {
        visibleCount = 0
        if (startDelayMs > 0) delay(startDelayMs)

        while (visibleCount < text.length) {
            val nextChar = text[visibleCount]
            visibleCount++
            delay(charDelayMs(nextChar))
        }
        onFinished?.invoke()
    }

    // Cursor blink loop
    LaunchedEffect(showCursor, cursorBlinkMs, cursorChar) {
        if (!showCursor) return@LaunchedEffect
        while (true) {
            cursorOn = !cursorOn
            delay(cursorBlinkMs)
        }
    }

    val typed = text.take(visibleCount)
    val cursor = if (showCursor && cursorOn) cursorChar else ""

    Text(
        text = typed + cursor,
        modifier = modifier,
        fontFamily = FontFamily.Monospace
    )
}
