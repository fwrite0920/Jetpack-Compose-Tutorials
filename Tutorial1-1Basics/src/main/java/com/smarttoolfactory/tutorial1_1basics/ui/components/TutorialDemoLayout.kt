package com.smarttoolfactory.tutorial1_1basics.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.ui.Material3TutorialTheme

@Composable
internal fun TutorialPage(
    @StringRes title: Int,
    introduction: String,
    instruction: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Material3TutorialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TutorialHeader(stringResource(title))
                StyleableTutorialText(introduction, bullets = false)
                TutorialText2(instruction)
                content()
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
internal fun TutorialChoices(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        options.forEachIndexed { index, label ->
            FilterChip(
                selected = selected == index,
                onClick = { onSelect(index) },
                label = { Text(label) }
            )
        }
    }
}

@Composable
internal fun TutorialToggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange,
            modifier = Modifier.testTag(label))
    }
}

@Composable
internal fun TutorialSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>,
                            onChange: (Float) -> Unit) {
    Text("$label: ${value.toInt()}")
    Slider(value = value, onValueChange = onChange, valueRange = range,
        modifier = Modifier.testTag(label))
}

/** Comparisons have independent columns so large font sizes remain readable on phones. */
@Composable
internal fun TutorialComparison(first: @Composable () -> Unit, second: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= 600.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { first() }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { second() }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                first()
                HorizontalDivider()
                second()
            }
        }
    }
}

internal data class TutorialDemo(
    val title: String,
    val explanation: String,
    val content: @Composable () -> Unit
)

/** Full-height examples receive finite constraints; teaching text remains above the demo. */
@Composable
internal fun TutorialDemoHost(@StringRes title: Int, introduction: String, demos: List<TutorialDemo>) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var reset by remember { mutableIntStateOf(0) }
    var menuOpen by remember { mutableStateOf(false) }
    Material3TutorialTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(12.dp)) {
                TutorialHeader(stringResource(title))
                StyleableTutorialText(introduction, bullets = false)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(onClick = { menuOpen = true }, modifier = Modifier
                            .fillMaxWidth().testTag("demo-selector")) {
                            Text(demos[selected].title)
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false },
                            modifier = Modifier.heightIn(max = 320.dp)) {
                            demos.forEachIndexed { index, demo ->
                                DropdownMenuItem(text = { Text(demo.title) }, onClick = {
                                    selected = index
                                    menuOpen = false
                                }, modifier = Modifier.testTag("demo-option-$index"))
                            }
                        }
                    }
                    TextButton(onClick = { reset++ }, modifier = Modifier.testTag("reset-demo")) {
                        Text("Reset")
                    }
                }
                TutorialText2(demos[selected].explanation)
                HorizontalDivider()
                key(selected, reset) {
                    Box(Modifier.fillMaxWidth().weight(1f).clipToBounds()
                        .testTag("demo-$selected")) { demos[selected].content() }
                }
            }
        }
    }
}
