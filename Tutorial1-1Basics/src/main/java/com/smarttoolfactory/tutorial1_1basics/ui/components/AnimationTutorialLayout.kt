package com.smarttoolfactory.tutorial1_1basics.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.ui.Material3TutorialTheme

internal data class AnimationExample(
    val id: String,
    val title: String,
    val explanation: String,
    val content: @Composable () -> Unit
)

/** Samples from one tutorial file appear consecutively, each with its own explanation and reset. */
@Composable
internal fun AnimationTutorialPage(
    @StringRes title: Int,
    introduction: String,
    examples: List<AnimationExample>,
    fitDemoContent: Boolean = false
) {
    Material3TutorialTheme {
        Surface(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                // Finite constraints let nested lists, pagers and shared transitions measure normally.
                val demoHeight = (maxHeight * 0.75f).coerceIn(480.dp, 720.dp)
                LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("animation-examples"),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item("introduction") {
                        Column {
                            TutorialHeader(stringResource(title))
                            StyleableTutorialText(introduction, bullets = false)
                        }
                    }
                    items(examples, key = { it.id }) { example ->
                        var reset by remember { mutableIntStateOf(0) }
                        Column {
                            StyleableTutorialText("**${example.title}**", bullets = false)
                            TutorialText2(example.explanation)
                            AnimationTutorialButton(
                                onClick = { reset++ },
                                modifier = Modifier.testTag("reset-${example.id}")
                            ) { Text("Reset animation") }
                            key(reset) {
                                Box(Modifier.fillMaxWidth()
                                    .then(if (fitDemoContent) Modifier else Modifier.height(demoHeight))
                                    .clipToBounds()
                                    .testTag("animation-demo-${example.id}")) {
                                    example.content()
                                }
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
