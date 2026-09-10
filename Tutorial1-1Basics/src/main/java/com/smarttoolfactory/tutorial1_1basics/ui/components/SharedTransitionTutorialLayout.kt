package com.smarttoolfactory.tutorial1_1basics.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * Preserve the original samples' available bounds and inherited Material 2 theme.
 * A file with multiple samples keeps them consecutive, each with a full viewport.
 * Do not clip shared overlays or wrap the demos in an additional themed surface.
 */
@Composable
internal fun SharedTransitionTutorialPage(examples: List<AnimationExample>) {
    if (examples.size == 1) {
        val example = examples.single()
        Box(Modifier.fillMaxSize().testTag("animation-demo-${example.id}")) {
            example.content()
        }
    } else {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val viewportHeight = maxHeight
            LazyColumn(Modifier.fillMaxSize().testTag("animation-examples")) {
                items(examples, key = { it.id }) { example ->
                    Box(Modifier.fillMaxWidth().height(viewportHeight)
                        .testTag("animation-demo-${example.id}")) {
                        example.content()
                    }
                }
            }
        }
    }
}
