package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Tutorial10_7Screen() {
    var presentation by rememberSaveable { mutableIntStateOf(0) }
    var submitted by rememberSaveable { mutableStateOf("None") }
    TutorialPage(R.string.tutorial10_7_title,
        "**SearchBarState** manages expansion; **TextFieldState** manages input. Results here are filtered locally.",
        "Choose a result presentation, tap the search field and type. Submit from the keyboard or select a result; the clear action keeps the search open.") {
        TutorialChoices(listOf("Full screen", "Docked"), presentation) { presentation = it }
        Text("Submitted: $submitted", Modifier.testTag("search-result"))
        key(presentation) {
            val search = rememberSearchBarState()
            val input = rememberTextFieldState()
            val scope = rememberCoroutineScope()
            val focus = LocalFocusManager.current
            val topics = remember { listOf("Buttons", "Chips", "Navigation bar", "Navigation rail",
                "Carousels", "Segmented buttons", "Date picker", "Time picker") }
            val close: () -> Unit = {
                scope.launch { search.animateToCollapsed(); focus.clearFocus() }
            }
            val inputField: @Composable () -> Unit = {
                SearchBarDefaults.InputField(searchBarState = search, textFieldState = input,
                    onSearch = { submitted = it.ifBlank { "None" }; close() },
                    placeholder = { Text("Search components") },
                    modifier = Modifier.testTag("m3-search-input"),
                    leadingIcon = {
                        if (search.currentValue == SearchBarValue.Expanded) {
                            IconButton(onClick = close) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Close search")
                            }
                        } else Icon(Icons.Default.Search, null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { input.setTextAndPlaceCursorAtEnd("") }) {
                            Icon(Icons.Default.Close, "Clear search")
                        }
                    })
            }
            val results: @Composable ColumnScope.() -> Unit = {
                val query = input.text.toString()
                val filtered = topics.filter { it.contains(query, ignoreCase = true) }
                val resultsState = rememberLazyListState()
                // A LazyColumn otherwise preserves the old first visible item's key when
                // clearing a filter, potentially leaving the first results above the viewport.
                LaunchedEffect(query) { resultsState.scrollToItem(0) }
                if (filtered.isEmpty()) Text("No matching components", Modifier.padding(16.dp))
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp), state = resultsState) {
                    items(filtered, key = { it }) { topic ->
                        ListItem(headlineContent = { Text(topic) }, modifier = Modifier
                            .testTag("search-option-$topic").clickable {
                                input.setTextAndPlaceCursorAtEnd(topic)
                                submitted = topic
                                close()
                            })
                    }
                }
            }
            Box(Modifier.fillMaxWidth()) {
                SearchBar(state = search, inputField = inputField, modifier = Modifier.fillMaxWidth())
                if (presentation == 0) {
                    ExpandedFullScreenSearchBar(state = search, inputField = inputField, content = results)
                } else {
                    ExpandedDockedSearchBar(state = search, inputField = inputField, content = results)
                }
            }
        }
        TutorialText2("Full-screen results provide room for a longer search task. Docked results retain nearby context. Both presentations share the same state-based API.")
    }
}
