package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Chip as M2Chip
import androidx.compose.material.FilterChip as M2FilterChip
import androidx.compose.material.Text as M2Text
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Tutorial10_2Screen() {
    var enabled by rememberSaveable { mutableStateOf(true) }
    var m2Selected by rememberSaveable { mutableStateOf(false) }
    var m3Selected by rememberSaveable { mutableStateOf(false) }
    var elevatedSelected by rememberSaveable { mutableStateOf(false) }
    var inputSelected by rememberSaveable { mutableStateOf(false) }
    var personVisible by rememberSaveable { mutableStateOf(true) }
    var action by rememberSaveable { mutableStateOf("Choose a chip") }
    TutorialPage(R.string.tutorial10_2_title,
        "**Chips** represent compact actions, filters, entered information or suggestions. Their purpose determines the variant.",
        "Toggle filters, select the input chip, remove its person or try an action. All variants expose their enabled and selected state.") {
        TutorialToggle("Chips enabled", enabled) { enabled = it }
        Text(action, Modifier.testTag("chip-result"))
        TutorialComparison(first = {
            TutorialHeader("Material 2")
            M2Chip(onClick = { action = "M2 action selected" }, enabled = enabled,
                leadingIcon = { androidx.compose.material.Icon(Icons.Default.Add, contentDescription = null) }) { M2Text("M2 Chip") }
            M2FilterChip(selected = m2Selected, onClick = { m2Selected = !m2Selected },
                enabled = enabled, modifier = Modifier.testTag("m2-filter"),
                selectedIcon = { androidx.compose.material.Icon(Icons.Default.Check, contentDescription = null) }) { M2Text("M2 Filter") }
            TutorialText2("Chip performs an action; FilterChip represents a selected option.")
        }, second = {
            TutorialHeader("Material 3")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { action = "Reminder added" }, enabled = enabled,
                    label = { Text("Assist") }, leadingIcon = { Icon(Icons.Default.Add, null) })
                ElevatedAssistChip(onClick = { action = "Elevated reminder added" }, enabled = enabled,
                    label = { Text("Elevated assist") })
                FilterChip(selected = m3Selected, onClick = { m3Selected = !m3Selected }, enabled = enabled,
                    modifier = Modifier.testTag("m3-filter"), label = { Text("Filter") },
                    leadingIcon = { Icon(if (m3Selected) Icons.Default.Check else Icons.Default.Star, null) })
                ElevatedFilterChip(selected = elevatedSelected, enabled = enabled,
                    onClick = { elevatedSelected = !elevatedSelected }, label = { Text("Elevated filter") })
                SuggestionChip(onClick = { action = "Suggestion applied" }, enabled = enabled,
                    label = { Text("Suggestion") }, icon = { Icon(Icons.Default.Lightbulb, null) })
                ElevatedSuggestionChip(onClick = { action = "Elevated suggestion applied" }, enabled = enabled,
                    label = { Text("Elevated suggestion") })
                if (personVisible) {
                    InputChip(selected = inputSelected, enabled = enabled,
                        onClick = { inputSelected = !inputSelected },
                        label = { Text("Alex") }, modifier = Modifier.testTag("input-chip"),
                        avatar = {
                            Image(painterResource(R.drawable.avatar_1_raster), "Alex avatar",
                                Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        },
                        trailingIcon = {
                            IconButton(onClick = { personVisible = false; action = "Alex removed" },
                                enabled = enabled, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Remove Alex")
                            }
                        })
                } else {
                    TextButton(onClick = { personVisible = true; inputSelected = false }) { Text("Restore Alex") }
                }
            }
            TutorialText2("Assist chips act on content. Filter chips narrow a set. Input chips represent entered information, often with removal. Suggestions help people choose a next step. Elevated variants add separation from the background.")
        })
        TutorialText2("Use segmented buttons for a small, fixed group of related choices such as Day / Week / Month. Chips work well for independent filters or a changing set of tags.")
    }
}
