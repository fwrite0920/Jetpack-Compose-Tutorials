package com.smarttoolfactory.tutorial1_1basics.chapter7_theming

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme as M2Theme
import androidx.compose.material.Button as M2Button
import androidx.compose.material.Card as M2Card
import androidx.compose.material.Text as M2Text
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.*
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

@Composable
internal fun Tutorial7_2Screen() {
    var mode by rememberSaveable { mutableIntStateOf(0) }
    var dynamic by rememberSaveable { mutableStateOf(false) }
    TutorialPage(R.string.tutorial7_2_title,
        "**ColorScheme** pairs background roles with content colors. **Dynamic color** is available from Android 12.",
        "Choose System, Light or Dark and compare the semantic roles. The custom palette is the fallback on older devices.") {
        TutorialChoices(TutorialThemeMode.entries.map { it.name }, mode) { mode = it }
        TutorialToggle("Dynamic color", dynamic) { dynamic = it }
        if (dynamic && Build.VERSION.SDK_INT < 31) {
            TutorialText2("This device uses the custom palette because dynamic color requires Android 12.")
        }
        Material3TutorialTheme(mode = TutorialThemeMode.entries[mode], dynamicColor = dynamic) {
            Surface(shape = MaterialTheme.shapes.large, tonalElevation = 2.dp) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val c = MaterialTheme.colorScheme
                    Text("Palette preview", style = MaterialTheme.typography.titleLarge)
                    listOf(
                        Triple("primary / onPrimary", c.primary, c.onPrimary),
                        Triple("primaryContainer / onPrimaryContainer", c.primaryContainer, c.onPrimaryContainer),
                        Triple("secondary / onSecondary", c.secondary, c.onSecondary),
                        Triple("secondaryContainer / onSecondaryContainer", c.secondaryContainer, c.onSecondaryContainer),
                        Triple("tertiary / onTertiary", c.tertiary, c.onTertiary),
                        Triple("surface / onSurface", c.surface, c.onSurface),
                        Triple("surfaceContainer / onSurface", c.surfaceContainer, c.onSurface),
                        Triple("error / onError", c.error, c.onError),
                        Triple("errorContainer / onErrorContainer", c.errorContainer, c.onErrorContainer)
                    ).forEach { (role, background, foreground) ->
                        Surface(color = background, contentColor = foreground,
                            shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth()) {
                            Text(role, Modifier.padding(12.dp), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    Button(onClick = {}) { Text("Themed action") }
                }
            }
        }
    }
}
