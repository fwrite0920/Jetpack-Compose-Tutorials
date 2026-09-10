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

internal data class TutorialSpacing(val content: androidx.compose.ui.unit.Dp = 16.dp)
internal data class TutorialElevation(val card: androidx.compose.ui.unit.Dp = 2.dp)
internal val LocalTutorialSpacing = staticCompositionLocalOf { TutorialSpacing() }
internal val LocalTutorialElevation = compositionLocalOf { TutorialElevation() }

@Composable
internal fun Tutorial7_5Screen() {
    TutorialPage(R.string.tutorial7_5_title,
        "**CompositionLocal** supplies values down a subtree without threading them through every parameter.",
        "Spacing rarely changes, so this example uses staticCompositionLocalOf. Elevation uses compositionLocalOf. A nested provider only overrides its descendants.") {
        CustomTokenDemo()
    }
}

@Composable
private fun CustomTokenDemo() {
    var compact by rememberSaveable { mutableStateOf(false) }
    var raised by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TutorialToggle("Compact spacing", compact) { compact = it }
        TutorialToggle("Raised cards", raised) { raised = it }
        CompositionLocalProvider(
            LocalTutorialSpacing provides TutorialSpacing(if (compact) 8.dp else 24.dp),
            LocalTutorialElevation provides TutorialElevation(if (raised) 12.dp else 2.dp)
        ) {
            TokenCard("Parent tokens")
            CompositionLocalProvider(LocalTutorialSpacing provides TutorialSpacing(4.dp)) {
                TokenCard("Scoped spacing • 4dp")
            }
            TokenCard("Sibling keeps parent tokens")
        }
    }
}

@Composable
private fun TokenCard(label: String) {
    val spacing = LocalTutorialSpacing.current.content
    val elevation = LocalTutorialElevation.current.card
    Card(elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        modifier = Modifier.fillMaxWidth()) {
        Text("$label\nSpacing: $spacing • Elevation: $elevation", Modifier.padding(spacing))
    }
}
