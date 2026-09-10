package com.smarttoolfactory.tutorial1_1basics.chapter9_animation

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialChoices

import com.smarttoolfactory.tutorial1_1basics.ui.components.AnimationExample
import com.smarttoolfactory.tutorial1_1basics.ui.components.SharedTransitionTutorialPage

@Composable
internal fun Tutorial9_33Screen() {
    SharedTransitionTutorialPage(
        examples = listOf(
            AnimationExample("SharedElementPlaceholderSizeDemo", "Shared Element Placeholder Size",
                "Switch ContentSize and AnimatedSize, then open an image. Watch how neighboring content reacts.") { SharedElementPlaceholderSizeDemo() }
        )
    )
}

val listSnacks = listOf(
    SnackItem("Cupcake", "", R.drawable.cupcake),
    SnackItem("Donut", "", R.drawable.donut),
    SnackItem("Eclair", "", R.drawable.eclair),
    SnackItem("Froyo", "", R.drawable.froyo),
    SnackItem("Gingerbread", "", R.drawable.gingerbread),
    SnackItem("Honeycomb", "", R.drawable.honeycomb),
)

data class SnackItem(val name: String, val description: String, @DrawableRes val image: Int)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun SharedElementPlaceholderSizeDemo() {
    var selected by rememberSaveable { mutableIntStateOf(-1) }
    var placeholderIndex by rememberSaveable { mutableIntStateOf(0) }
    val listState = rememberLazyListState()
    val shape = RoundedCornerShape(16.dp)
    val placeholder = if (placeholderIndex == 0) SharedTransitionScope.PlaceholderSize.ContentSize
        else SharedTransitionScope.PlaceholderSize.AnimatedSize
    BackHandler(selected >= 0) { selected = -1 }
    Column(Modifier.fillMaxSize().padding(12.dp)) {
        TutorialChoices(listOf("ContentSize", "AnimatedSize"), placeholderIndex) { placeholderIndex = it }
        SharedTransitionLayout(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(
                targetState = selected,
                contentKey = { it >= 0 },
                label = "Placeholder size",
                transitionSpec = { EnterTransition.None togetherWith ExitTransition.None }
            ) { target ->
                if (target < 0) {
                    Column {
                        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            itemsIndexed(listSnacks, key = { _, snack -> snack.name }) { index, snack ->
                                Image(painterResource(snack.image), "Open ${snack.name}",
                                    Modifier
                                        .sharedElement(
                                            rememberSharedContentState("placeholder-image-$index"),
                                            animatedVisibilityScope = this@AnimatedContent,
                                            placeholderSize = placeholder,
                                            boundsTransform = { _, _ -> tween(1000, easing = FastOutSlowInEasing) },
                                            clipInOverlayDuringTransition = OverlayClip(shape)
                                        )
                                        .width(100.dp).height(140.dp).clip(shape)
                                        .clickable { selected = index },
                                    contentScale = ContentScale.Crop)
                            }
                        }
                        Text("Neighbor below the shared images", Modifier.padding(12.dp))
                        Image(painterResource(R.drawable.eclair), "Neighbor image",
                            Modifier.fillMaxWidth().height(100.dp).clip(shape), contentScale = ContentScale.Crop)
                    }
                } else {
                    Column {
                        TextButton(onClick = { selected = -1 }) { Text("Back to snacks") }
                        Image(painterResource(listSnacks[target].image), listSnacks[target].name,
                            Modifier
                                .sharedElement(
                                    rememberSharedContentState("placeholder-image-$target"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    placeholderSize = placeholder,
                                    boundsTransform = { _, _ -> tween(1000, easing = FastOutSlowInEasing) },
                                    clipInOverlayDuringTransition = OverlayClip(shape)
                                )
                                .fillMaxWidth().weight(1f).clip(shape),
                            contentScale = ContentScale.Crop)
                    }
                }
            }
        }
    }
}
