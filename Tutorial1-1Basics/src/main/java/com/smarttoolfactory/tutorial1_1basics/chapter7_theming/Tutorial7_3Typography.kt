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
internal fun Tutorial7_3Screen() {
    var size by rememberSaveable { mutableFloatStateOf(18f) }
    var weight by rememberSaveable { mutableFloatStateOf(400f) }
    var lineHeight by rememberSaveable { mutableFloatStateOf(28f) }
    TutorialPage(R.string.tutorial7_3_title,
        "**Typography** assigns a role to each text style. M3 separates display, headline, title, body and label scales.",
        "Change the body style below. Font size changes glyphs; line height changes the space between baselines.") {
        TutorialSlider("Font size (sp)", size, 12f..32f) { size = it }
        TutorialSlider("Font weight", weight, 100f..900f) { weight = it }
        TutorialSlider("Line height (sp)", lineHeight, 20f..52f) { lineHeight = it }
        MaterialTheme(typography = MaterialTheme.typography.copy(bodyLarge = MaterialTheme.typography.bodyLarge.copy(
            fontSize = size.sp, fontWeight = FontWeight(weight.toInt()), lineHeight = lineHeight.sp
        ))) {
            Text("A weekend of discovery", style = MaterialTheme.typography.headlineMedium)
            Text("Explore nearby places", style = MaterialTheme.typography.titleLarge)
            Text("FEATURED COLLECTION", style = MaterialTheme.typography.labelLarge)
            Text("Use a clear hierarchy to help people scan a screen. Longer body text needs comfortable line spacing and a readable weight.",
                style = MaterialTheme.typography.bodyLarge)
        }
        TutorialComparison(first = {
            TutorialHeader("Material 2 scale")
            M2Text("Headline 4", style = M2Theme.typography.h4)
            M2Text("Subtitle 1", style = M2Theme.typography.subtitle1)
            M2Text("Body 1", style = M2Theme.typography.body1)
            M2Text("BUTTON", style = M2Theme.typography.button)
        }, second = {
            TutorialHeader("Material 3 scale")
            Text("Headline medium", style = MaterialTheme.typography.headlineMedium)
            Text("Title medium", style = MaterialTheme.typography.titleMedium)
            Text("Body large", style = MaterialTheme.typography.bodyLarge)
            Text("Label large", style = MaterialTheme.typography.labelLarge)
        })
        TutorialText2("Migration mapping:\nh1 → displayLarge • h2 → displayMedium • h3 → displaySmall\nh4 → headlineMedium • h5 → headlineSmall • h6 → titleLarge\nsubtitle1 → titleMedium • subtitle2 → titleSmall\nbody1 → bodyLarge • body2 → bodyMedium\nbutton → labelLarge • caption → bodySmall • overline → labelSmall\nM3 also adds headlineLarge and labelMedium.")
    }
}
