package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation as M2BottomNavigation
import androidx.compose.material.BottomNavigationItem as M2BottomNavigationItem
import androidx.compose.material.Text as M2Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*

internal val navigationDestinations = listOf("Explore", "Favorites", "Profile")
internal val navigationDestinationIcons = listOf(Icons.Default.Home, Icons.Default.Favorite, Icons.Default.Person)

@Composable
internal fun Tutorial10_3Screen() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    TutorialPage(R.string.tutorial10_3_title,
        "**NavigationBar** places primary destinations along the bottom of a screen. Compare it with Material 2 **BottomNavigation**.",
        "Select a destination in either bar. Both previews share a saved selection.") {
        TutorialHeader("Material 2 • BottomNavigation")
        M2BottomNavigation {
            navigationDestinations.forEachIndexed { index, label ->
                M2BottomNavigationItem(selected = selected == index, onClick = { selected = index },
                    icon = { androidx.compose.material.Icon(navigationDestinationIcons[index], null) },
                    label = { M2Text(label) })
            }
        }
        TutorialHeader("Material 3 • NavigationBar")
        DemoNavigationBar(selected, onSelect = { selected = it })
        Text("Selected destination: ${navigationDestinations[selected]}", Modifier.testTag("bar-result"))
        TutorialText2("Navigation bars work well for three to five primary destinations on compact screens. Navigation Rail has its own tutorial with a vertical layout and an optional floating action button.")
        TutorialText2("Use rememberSaveable for the selected destination. Insets are zero inside this preview because its parent already handles the system bars.")
    }
}

@Composable
internal fun ResponsiveNavigationDemo(
    modifier: Modifier = Modifier,
    selected: Int = 0,
    onSelect: (Int) -> Unit = {},
    showFab: Boolean = true
) {
    var created by rememberSaveable { mutableIntStateOf(0) }
    BoxWithConstraints(modifier) {
        val rail = maxWidth >= 600.dp
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxSize()) {
            Row {
                if (rail) {
                    DemoNavigationRail(selected, onSelect, showFab, { created++ },
                        Modifier.testTag("responsive-rail"))
                }
                Column(Modifier.weight(1f)) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(navigationDestinations[selected], style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.testTag("navigation-result"))
                            Text(if (rail) "Rail • width ≥ 600dp" else "Bar • width < 600dp")
                            Text("Created: $created")
                            if (!rail && showFab) FloatingActionButton(onClick = { created++ }) {
                                Icon(Icons.Default.Add, "Create item")
                            }
                        }
                    }
                    if (!rail) DemoNavigationBar(selected, onSelect, Modifier.testTag("responsive-bar"))
                }
            }
        }
    }
}

@Composable
private fun DemoNavigationBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier, windowInsets = WindowInsets(0, 0, 0, 0)) {
        navigationDestinations.forEachIndexed { index, label ->
            NavigationBarItem(selected = selected == index, onClick = { onSelect(index) },
                icon = { Icon(navigationDestinationIcons[index], null) }, label = { Text(label) })
        }
    }
}
