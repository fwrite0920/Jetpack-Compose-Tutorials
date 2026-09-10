package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationTutorialPage

@Composable
internal fun Tutorial9_21Screen() {
    AnimationTutorialPage(
        title = R.string.tutorial9_21_title,
        fitDemoContent = true,
        introduction = "**Animated Thinking Text** — Run or pause the moving text highlight and adjust the gradient progress manually.",
        examples = listOf(
            AnimationExample("ThinkingTextDemo", "Animated Thinking Text",
                "Run or pause the moving text highlight and adjust the gradient progress manually.") { ThinkingTextDemo() }
        )
    )
}


@Composable
internal fun ThinkingAnimatedText(
    text: String,
    baseColor: Color,
    highlightColor: Color,
    modifier: Modifier = Modifier,
    brushProgress: Float? = null
) {
    val animatedBrushProgress = brushProgress ?: rememberThinkingTextBrushProgress(
        label = "thinking_text_brush"
    )

    Text(
        text = text,
        modifier = modifier.thinkingTextGradient(
            brushProgress = animatedBrushProgress,
            baseColor = baseColor,
            highlightColor = highlightColor
        ),
        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.SemiBold
        ),
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun ThinkingTextDemo() {
    var running by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        TutorialToggle("Animate text highlight", running) { running = it }
        TutorialSlider("Highlight progress (%)", progress * 100f, 0f..100f) { progress = it / 100f }
        ThinkingAnimatedText("Thinking through the next step…",
            MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.primary,
            brushProgress = if (running) null else progress)
    }
}
