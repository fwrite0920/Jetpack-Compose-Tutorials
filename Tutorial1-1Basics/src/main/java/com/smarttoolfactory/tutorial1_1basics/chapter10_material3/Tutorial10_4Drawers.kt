package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import kotlinx.coroutines.launch

@Composable
internal fun Tutorial10_4Screen() {
    var pattern by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    var gestures by rememberSaveable { mutableStateOf(true) }
    TutorialPage(R.string.tutorial10_4_title,
        "**DrawerState** controls modal and dismissible drawers. Call **open** and **close** from a coroutine.",
        "Modal drawers overlay content, dismissible drawers push it aside, and permanent drawers remain visible. Adaptive uses a permanent drawer from 600dp.") {
        TutorialChoices(listOf("Modal", "Dismissible", "Permanent", "Adaptive"), pattern) { pattern = it }
        TutorialToggle("Drawer gestures", gestures) { gestures = it }
        BoxWithConstraints(Modifier.fillMaxWidth().height(430.dp)) {
            val actual = if (pattern == 3) { if (maxWidth >= 600.dp) 2 else 0 } else pattern
            key(actual) {
                val state = rememberDrawerState(DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val labels = listOf("Inbox", "Saved", "Settings")
                val items: @Composable ColumnScope.() -> Unit = {
                    Text("Destinations", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                    labels.forEachIndexed { index, label ->
                        NavigationDrawerItem(label = { Text(label) }, selected = selected == index,
                            onClick = {
                                selected = index
                                scope.launch { state.close() }
                            }, modifier = Modifier.padding(horizontal = 8.dp))
                    }
                    if (actual != 2) TextButton(onClick = { scope.launch { state.close() } }) { Text("Close drawer") }
                }
                val body: @Composable () -> Unit = {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            if (actual != 2) FilledTonalButton(onClick = { scope.launch { state.open() } }) {
                                Icon(Icons.Default.Menu, null)
                                Text("Open drawer")
                            }
                            Text(labels[selected], style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.testTag("drawer-result"))
                            Text(if (actual == 2) "Always visible" else "State: ${state.currentValue}")
                        }
                    }
                }
                when (actual) {
                    0 -> ModalNavigationDrawer(drawerState = state, gesturesEnabled = gestures,
                        drawerContent = { ModalDrawerSheet(Modifier.width(260.dp),
                            windowInsets = WindowInsets(0, 0, 0, 0), content = items) }, content = body)
                    1 -> DismissibleNavigationDrawer(drawerState = state, gesturesEnabled = gestures,
                        drawerContent = { DismissibleDrawerSheet(Modifier.width(240.dp),
                            windowInsets = WindowInsets(0, 0, 0, 0), content = items) }, content = body)
                    else -> PermanentNavigationDrawer(drawerContent = {
                        PermanentDrawerSheet(Modifier.width(if (maxWidth >= 600.dp) 240.dp else 172.dp),
                            windowInsets = WindowInsets(0, 0, 0, 0), content = items)
                    }, content = body)
                }
            }
        }
    }
}
