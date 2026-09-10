package com.smarttoolfactory.tutorial1_1basics.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Consistent action control for both the Material 2 and Material 3 animation examples. */
@Composable
internal fun AnimationTutorialButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 48.dp),
        enabled = enabled, interactionSource = interactionSource) {
        CompositionLocalProvider(
            androidx.compose.material.LocalContentColor provides LocalContentColor.current,
            androidx.compose.material.LocalContentAlpha provides 1f
        ) { content() }
    }
}
