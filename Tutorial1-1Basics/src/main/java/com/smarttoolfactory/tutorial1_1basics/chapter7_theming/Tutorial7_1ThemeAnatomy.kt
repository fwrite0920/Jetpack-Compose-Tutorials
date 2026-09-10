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
internal fun Tutorial7_1Screen() {
    var accent by rememberSaveable { mutableIntStateOf(0) }
    TutorialPage(R.string.tutorial7_1_title,
        "**MaterialTheme** provides color, typography and shape tokens to its descendants.",
        "Select an accent. Both previews read the same primary token; each design system keeps its own component defaults.") {
        TutorialChoices(listOf("Indigo", "Green", "Berry"), accent) { accent = it }
        val primary = listOf(Color(0xFF495D92), Color(0xFF006C4C), Color(0xFF8E4162))[accent]
        TutorialComparison(first = {
            TutorialHeader("Material 2 • Colors")
            M2Theme(colors = M2Theme.colors.copy(primary = primary, onPrimary = Color.White)) {
                M2Button(onClick = {}) { M2Text("Primary button") }
                M2Card(backgroundColor = M2Theme.colors.primary) {
                    M2Text("A shared color token", Modifier.padding(16.dp),
                        color = M2Theme.colors.onPrimary)
                }
            }
            TutorialText2("primary → primary\nprimaryVariant → primaryContainer (review contrast)\nsecondary → secondary\nsurface → surface\nonSurface → onSurface")
        }, second = {
            TutorialHeader("Material 3 • ColorScheme")
            MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(primary = primary, onPrimary = Color.White)) {
                Button(onClick = {}) { Text("Primary button") }
                Card(colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)) {
                    Text("A shared color token", Modifier.padding(16.dp))
                }
            }
            TutorialText2("M3 adds tonal container roles and tertiary accents. A container and its on-container color form a readable pair; migration is a design choice, not a rename.")
        })
        TutorialHeader("Three systems, one theme")
        TutorialText2("Color gives meaning and contrast. Typography gives content a hierarchy. Shapes group related surfaces. Read tokens through MaterialTheme so a scoped override reaches every consumer.")
    }
}
