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
internal fun Tutorial7_4Screen() {
    var small by rememberSaveable { mutableFloatStateOf(8f) }
    var medium by rememberSaveable { mutableFloatStateOf(12f) }
    var large by rememberSaveable { mutableFloatStateOf(24f) }
    var text by rememberSaveable { mutableStateOf("Theme tokens") }
    var selected by rememberSaveable { mutableStateOf(false) }
    TutorialPage(R.string.tutorial7_4_title,
        "**Shapes** centralizes corner treatments. A component can read a theme token or supply its own **shape**.",
        "Move each slider. The button and text field explicitly read small, the card reads medium, and the outer surface reads large. M3 buttons otherwise use a fully rounded default.") {
        TutorialSlider("Small corners (dp)", small, 0f..32f) { small = it }
        TutorialSlider("Medium corners (dp)", medium, 0f..40f) { medium = it }
        TutorialSlider("Large corners (dp)", large, 0f..56f) { large = it }
        MaterialTheme(shapes = MaterialTheme.shapes.copy(
            small = RoundedCornerShape(small.dp), medium = RoundedCornerShape(medium.dp),
            large = RoundedCornerShape(large.dp))) {
            Surface(shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = {}, shape = MaterialTheme.shapes.small) { Text("Theme small") }
                    Card { Text("Card • theme medium", Modifier.padding(24.dp)) }
                    FilterChip(selected = selected, onClick = { selected = !selected },
                        label = { Text("Chip • theme small") })
                    OutlinedTextField(value = text, onValueChange = { text = it },
                        label = { Text("Text field") }, shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth())
                    Button(onClick = {}, shape = RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp)) {
                        Text("One-component override")
                    }
                }
            }
        }
    }
}
