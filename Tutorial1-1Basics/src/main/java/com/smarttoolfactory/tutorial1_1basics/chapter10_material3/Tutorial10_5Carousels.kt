package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.carousel.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.Material3TutorialTheme
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Tutorial10_5Screen() {
    val images = listOf(R.drawable.cupcake, R.drawable.donut, R.drawable.eclair,
        R.drawable.froyo, R.drawable.gingerbread, R.drawable.honeycomb)
    val labels = listOf("Cupcake", "Donut", "Eclair", "Froyo", "Gingerbread", "Honeycomb")
    val browse = rememberCarouselState { images.size }
    val uncontained = rememberCarouselState { images.size }
    val scope = rememberCoroutineScope()
    var style by rememberSaveable { mutableIntStateOf(0) }
    var preferredWidth by rememberSaveable { mutableFloatStateOf(186f) }
    var minSmallWidth by rememberSaveable { mutableFloatStateOf(40f) }
    var maxSmallWidth by rememberSaveable { mutableFloatStateOf(56f) }
    var itemWidth by rememberSaveable { mutableFloatStateOf(186f) }
    var spacing by rememberSaveable { mutableFloatStateOf(8f) }
    var padding by rememberSaveable { mutableFloatStateOf(16f) }
    var singleAdvance by rememberSaveable { mutableStateOf(true) }
    var selected by rememberSaveable { mutableStateOf("None") }

    Material3TutorialTheme {
        Surface(Modifier.fillMaxSize()) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val scrollWholePage = maxHeight < 680.dp
                Column(Modifier.fillMaxSize().then(
                    if (scrollWholePage) Modifier.verticalScroll(rememberScrollState()) else Modifier
                ).padding(12.dp)) {
                    TutorialHeader(stringResource(R.string.tutorial10_5_title))
                    StyleableTutorialText(
                        "**Carousels** reveal a collection through masked items. Choose a style, then change its widths.",
                        bullets = false)
                    TutorialChoices(listOf("Multi-browse", "Uncontained"), style) { style = it }
                    TutorialText2("Swipe or tap an image. Width controls below update this preview.")

                    val itemContent: @Composable CarouselItemScope.(Int) -> Unit = { index ->
                        Image(painterResource(images[index]), labels[index],
                            Modifier.fillMaxWidth().height(160.dp)
                                .maskClip(MaterialTheme.shapes.extraLarge)
                                .clickable { selected = labels[index] },
                            contentScale = ContentScale.Crop)
                    }
                    if (style == 0) {
                        HorizontalMultiBrowseCarousel(
                            state = browse,
                            modifier = Modifier.fillMaxWidth().height(160.dp).testTag("multi-browse-carousel"),
                            preferredItemWidth = preferredWidth.dp,
                            minSmallItemWidth = minSmallWidth.dp,
                            maxSmallItemWidth = maxSmallWidth.dp,
                            itemSpacing = spacing.dp,
                            contentPadding = PaddingValues(horizontal = padding.dp),
                            flingBehavior = if (singleAdvance) CarouselDefaults.singleAdvanceFlingBehavior(browse)
                                else CarouselDefaults.multiBrowseFlingBehavior(browse),
                            content = itemContent
                        )
                    } else {
                        HorizontalUncontainedCarousel(
                            state = uncontained,
                            modifier = Modifier.fillMaxWidth().height(160.dp).testTag("uncontained-carousel"),
                            itemWidth = itemWidth.dp,
                            itemSpacing = spacing.dp,
                            contentPadding = PaddingValues(horizontal = padding.dp),
                            flingBehavior = CarouselDefaults.noSnapFlingBehavior(),
                            content = itemContent
                        )
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (style == 0) {
                            Text("Multi-browse item: ${browse.currentItem + 1}", Modifier.testTag("browse-position"))
                            TextButton(onClick = {
                                scope.launch { browse.animateScrollToItem((browse.currentItem + 1) % images.size) }
                            }) { Text("Next multi-browse") }
                        } else {
                            Text("Uncontained item: ${uncontained.currentItem + 1}", Modifier.testTag("uncontained-position"))
                            TextButton(onClick = {
                                scope.launch { uncontained.animateScrollToItem((uncontained.currentItem + 1) % images.size) }
                            }) { Text("Next uncontained") }
                        }
                    }
                    Text("Selected image: $selected", Modifier.testTag("carousel-selection"))
                    HorizontalDivider(Modifier.padding(vertical = 8.dp))

                    // Pin the preview when space allows; short windows scroll the whole page.
                    Column(Modifier.fillMaxWidth().then(
                        if (scrollWholePage) Modifier
                        else Modifier.weight(1f).verticalScroll(rememberScrollState())
                    )
                        .testTag("carousel-controls"), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (style == 0) {
                            TutorialSlider("Preferred item width (dp)", preferredWidth, 120f..360f) {
                                preferredWidth = it
                                maxSmallWidth = maxSmallWidth.coerceAtMost(it - 8f)
                            }
                            TutorialSlider("Minimum small item width (dp)", minSmallWidth, 16f..96f) {
                                minSmallWidth = it
                                maxSmallWidth = maxSmallWidth.coerceAtLeast(it)
                            }
                            TutorialSlider("Maximum small item width (dp)", maxSmallWidth,
                                minSmallWidth..minOf(160f, preferredWidth - 8f)) { maxSmallWidth = it }
                            TutorialText2("Minimum and maximum bound the small masked items. Preferred width is a layout hint for large items; the fitted width can differ. The controls keep minimum ≤ maximum < preferred.")
                            TutorialToggle("Single-advance fling", singleAdvance) { singleAdvance = it }
                        } else {
                            TutorialSlider("Item width (dp)", itemWidth, 96f..360f) { itemWidth = it }
                            TutorialText2("Uncontained uses one fixed item width. It has no minimum/maximum small-item parameters; the trailing item is masked at the edge. This example uses free flinging.")
                        }
                        TutorialSlider("Item spacing (dp)", spacing, 0f..24f) { spacing = it }
                        TutorialSlider("Content padding (dp)", padding, 0f..32f) { padding = it }
                        TextButton(onClick = {
                            preferredWidth = 186f
                            minSmallWidth = 40f
                            maxSmallWidth = 56f
                            itemWidth = 186f
                            spacing = 8f
                            padding = 16f
                            singleAdvance = true
                        }) { Text("Reset carousel options") }
                        TutorialText2("maskClip follows the animated item mask. Images are bundled with the app so both styles work offline.")
                    }
                }
            }
        }
    }
}
