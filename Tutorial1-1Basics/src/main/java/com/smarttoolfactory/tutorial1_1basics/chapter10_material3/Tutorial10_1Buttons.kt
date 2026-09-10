package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button as M2Button
import androidx.compose.material.OutlinedButton as M2OutlinedButton
import androidx.compose.material.TextButton as M2TextButton
import androidx.compose.material.Text as M2Text
import androidx.compose.material.ButtonDefaults as M2ButtonDefaults
import androidx.compose.material.MaterialTheme as M2Theme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

@Composable
internal fun Tutorial10_1Screen() {
    var enabled by rememberSaveable { mutableStateOf(true) }
    var icons by rememberSaveable { mutableStateOf(true) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var styled by rememberSaveable { mutableStateOf(false) }
    var clicks by rememberSaveable { mutableIntStateOf(0) }
    TutorialPage(R.string.tutorial10_1_title,
        "**Buttons** communicate emphasis. M2 has contained, outlined and text buttons; M3 adds **filled tonal** and **elevated** buttons.",
        "Compare the same actions across both systems. Loading replaces the icon and prevents duplicate submissions.") {
        TutorialToggle("Buttons enabled", enabled) { enabled = it }
        TutorialToggle("Show button icons", icons) { icons = it }
        TutorialToggle("Loading content", loading) { loading = it }
        TutorialToggle("Custom colors and shape", styled) { styled = it }
        Text("Button clicks: $clicks", Modifier.testTag("button-result"))
        val active = enabled && !loading
        val click = { clicks++; Unit }
        TutorialComparison(first = {
            TutorialHeader("Material 2")
            val shape = if (styled) RoundedCornerShape(12.dp) else M2Theme.shapes.small
            M2Button(onClick = click, enabled = active, shape = shape,
                colors = M2ButtonDefaults.buttonColors(
                    backgroundColor = if (styled) M2Theme.colors.secondary else M2Theme.colors.primary),
                modifier = Modifier.testTag("m2-filled")) { ButtonContent("M2 Filled", icons, loading, material2 = true) }
            M2OutlinedButton(onClick = click, enabled = active, shape = shape) {
                ButtonContent("M2 Outlined", icons, loading, material2 = true)
            }
            M2TextButton(onClick = click, enabled = active, shape = shape) {
                ButtonContent("M2 Text", icons, loading, material2 = true)
            }
            TutorialText2("Contained buttons carry the primary action. Outlined buttons have medium emphasis; text buttons suit lower-priority actions.")
        }, second = {
            TutorialHeader("Material 3")
            val shape = if (styled) RoundedCornerShape(12.dp) else ButtonDefaults.shape
            Button(onClick = click, enabled = active, shape = shape,
                colors = ButtonDefaults.buttonColors(containerColor =
                    if (styled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("m3-filled")) { ButtonContent("M3 Filled", icons, loading) }
            FilledTonalButton(onClick = click, enabled = active, shape = shape) {
                ButtonContent("M3 Filled tonal", icons, loading)
            }
            ElevatedButton(onClick = click, enabled = active, shape = shape) {
                ButtonContent("M3 Elevated", icons, loading)
            }
            OutlinedButton(onClick = click, enabled = active, shape = shape) {
                ButtonContent("M3 Outlined", icons, loading)
            }
            TextButton(onClick = click, enabled = active, shape = shape) {
                ButtonContent("M3 Text", icons, loading)
            }
            TutorialText2("Filled is high emphasis. Tonal provides a softer container. Elevated stands out against a patterned surface. Outlined and text variants keep secondary actions quieter.")
        })
    }
}

@Composable
private fun RowScope.ButtonContent(label: String, icon: Boolean, loading: Boolean, material2: Boolean = false) {
    val contentColor = if (material2) androidx.compose.material.LocalContentColor.current
        .copy(alpha = androidx.compose.material.LocalContentAlpha.current)
        else LocalContentColor.current
    if (loading) {
        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
            color = contentColor)
        Spacer(Modifier.width(8.dp))
    } else if (icon) {
        Icon(Icons.Default.Favorite, contentDescription = null,
            modifier = Modifier.size(18.dp), tint = contentColor)
        Spacer(Modifier.width(8.dp))
    }
    val text = if (loading) "$label • Loading" else label
    if (material2) M2Text(text) else Text(text)
}
