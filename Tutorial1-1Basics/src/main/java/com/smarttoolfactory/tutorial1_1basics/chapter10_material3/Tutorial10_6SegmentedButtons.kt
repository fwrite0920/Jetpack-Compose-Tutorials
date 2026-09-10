package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

@Composable
internal fun Tutorial10_6Screen() {
    var single by rememberSaveable { mutableIntStateOf(0) }
    var multiple by rememberSaveable { mutableIntStateOf(0) }
    val periods = listOf("Day", "Week", "Month")
    val filters = listOf("Saved", "Starred", "Unread")
    TutorialPage(R.string.tutorial10_6_title,
        "**SegmentedButton** groups closely related choices. Single-choice uses selected; multiple-choice uses checked.",
        "itemShape rounds only the outside edges. The row provides radio-group or toggle semantics, so no custom role is needed.") {
        TutorialHeader("Single choice")
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            periods.forEachIndexed { index, label ->
                SegmentedButton(selected = single == index, onClick = { single = index },
                    shape = SegmentedButtonDefaults.itemShape(index, periods.size),
                    modifier = Modifier.testTag("single-$label")) { Text(label) }
            }
        }
        Text("Period: ${periods[single]}", Modifier.testTag("single-result"))
        TutorialHeader("Multiple choice")
        MultiChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            filters.forEachIndexed { index, label ->
                val checked = multiple and (1 shl index) != 0
                SegmentedButton(checked = checked,
                    onCheckedChange = { multiple = multiple xor (1 shl index) },
                    shape = SegmentedButtonDefaults.itemShape(index, filters.size),
                    modifier = Modifier.testTag("multiple-$label"),
                    icon = { Icon(if (checked) Icons.Default.Check else Icons.Default.Star, null) }) {
                    Text(label)
                }
            }
        }
        Text("Filters: " + filters.filterIndexed { index, _ -> multiple and (1 shl index) != 0 }
            .joinToString().ifEmpty { "None" }, Modifier.testTag("multiple-result"))
    }
}
