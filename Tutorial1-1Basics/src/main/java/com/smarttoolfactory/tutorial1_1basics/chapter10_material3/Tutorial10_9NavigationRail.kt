package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

@Composable
internal fun Tutorial10_9Screen() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var showFab by rememberSaveable { mutableStateOf(true) }
    var created by rememberSaveable { mutableIntStateOf(0) }
    TutorialPage(R.string.tutorial10_9_title,
        "**NavigationRail** arranges primary destinations vertically beside the screen content. Its header can hold a **FloatingActionButton**.",
        "This rail is visible at every screen width. Select a destination or use the optional action button.") {
        TutorialToggle("Show navigation FAB", showFab) { showFab = it }
        Surface(Modifier.fillMaxWidth().height(400.dp).testTag("navigation-rail-demo"),
            color = MaterialTheme.colorScheme.surfaceContainerLow) {
            Row {
                DemoNavigationRail(selected, { selected = it }, showFab, { created++ },
                    Modifier.testTag("standalone-rail"))
                Column(Modifier.weight(1f).fillMaxHeight().padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Text(navigationDestinations[selected], style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.testTag("rail-result"))
                    Text("Created: $created", Modifier.testTag("rail-created"))
                }
            }
        }
        TutorialText2("NavigationRailItem supplies selection semantics and an active indicator. rememberSaveable keeps the destination across configuration changes. The FAB creates content; it is not a destination.")
        TutorialHeader("Responsive navigation")
        TutorialText2("An app can choose a rail from 600dp of available width and a bottom bar below that. The dedicated rail preview above stays available on phones.")
        ResponsiveNavigationDemo(Modifier.fillMaxWidth().height(400.dp), selected, { selected = it }, showFab)
    }
}

@Composable
internal fun DemoNavigationRail(
    selected: Int,
    onSelect: (Int) -> Unit,
    showFab: Boolean,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(modifier = modifier,
        windowInsets = WindowInsets(0, 0, 0, 0),
        header = {
            if (showFab) FloatingActionButton(onClick = onCreate) {
                Icon(Icons.Default.Add, "Create item")
            }
        }) {
        navigationDestinations.forEachIndexed { index, label ->
            NavigationRailItem(selected = selected == index, onClick = { onSelect(index) },
                icon = { Icon(navigationDestinationIcons[index], null) }, label = { Text(label) })
        }
    }
}
