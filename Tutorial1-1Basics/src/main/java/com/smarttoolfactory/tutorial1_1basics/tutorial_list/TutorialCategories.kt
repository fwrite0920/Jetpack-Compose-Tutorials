package com.smarttoolfactory.tutorial1_1basics.tutorial_list

import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.chapter7_theming.*
import com.smarttoolfactory.tutorial1_1basics.chapter9_animation.*
import com.smarttoolfactory.tutorial1_1basics.chapter10_material3.*
import com.smarttoolfactory.tutorial1_1basics.model.TutorialCategory
import com.smarttoolfactory.tutorial1_1basics.model.TutorialSectionModel

internal const val TAG_MATERIAL_3 = "Material 3"
internal const val TAG_THEMING = "Theming"
internal const val TAG_ANIMATION_CHAPTER = "Animation"
internal val Material3ListColor = Color(0xFF26A69A)
internal val ThemingListColor = Color(0xFFAB47BC)
internal val AnimationListColor = Color(0xFF5C6BC0)

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class,
    ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
@Composable
internal fun createTutorialCategories(onBack: () -> Unit): List<TutorialCategory> = listOf(
    TutorialCategory(stringResource(R.string.category_components), createComponentTutorialList(onBack)),
    TutorialCategory(stringResource(R.string.category_material_3), createMaterial3TutorialList()),
    TutorialCategory(stringResource(R.string.category_layout), createLayoutTutorialList()),
    TutorialCategory(stringResource(R.string.category_state), createStateTutorialList()),
    TutorialCategory(stringResource(R.string.category_gesture), createGestureTutorialList()),
    TutorialCategory(stringResource(R.string.category_graphics), createGraphicsTutorialList()),
    TutorialCategory(stringResource(R.string.category_theming), createThemingTutorialList()),
    TutorialCategory(stringResource(R.string.category_animation), createAnimationTutorialList())
)

@Composable
private fun tutorial(
    @StringRes title: Int, @StringRes description: Int,
    categoryTag: String, tags: String, color: Color,
    action: @Composable () -> Unit
) = TutorialSectionModel(
    title = stringResource(title), description = stringResource(description),
    tags = listOf(categoryTag) + tags.split(", "), tagColor = color, action = action
)

@Composable
internal fun createThemingTutorialList(): List<TutorialSectionModel> = listOf(
    tutorial(R.string.tutorial7_1_title, R.string.tutorial7_1_description,
        TAG_THEMING, "Colors, ColorScheme, MaterialTheme, Tokens", ThemingListColor) { Tutorial7_1Screen() },
    tutorial(R.string.tutorial7_2_title, R.string.tutorial7_2_description,
        TAG_THEMING, "Dark, Theme, Dynamic, Color, ColorScheme", ThemingListColor) { Tutorial7_2Screen() },
    tutorial(R.string.tutorial7_3_title, R.string.tutorial7_3_description,
        TAG_THEMING, "Typography, Font, TextStyle", ThemingListColor) { Tutorial7_3Screen() },
    tutorial(R.string.tutorial7_4_title, R.string.tutorial7_4_description,
        TAG_THEMING, "Shapes, Button, Card, Chip, TextField", ThemingListColor) { Tutorial7_4Screen() },
    tutorial(R.string.tutorial7_5_title, R.string.tutorial7_5_description,
        TAG_THEMING, "CompositionLocal, Recomposition, Spacing, Elevation", ThemingListColor) { Tutorial7_5Screen() },
    tutorial(R.string.tutorial7_6_title, R.string.tutorial7_6_description,
        TAG_THEMING, "CompositionLocal, LocalContentAlpha, LocalContentColor, Icon", ThemingListColor) { Tutorial7_6Screen() },
    tutorial(R.string.tutorial7_7_title, R.string.tutorial7_7_description,
        TAG_THEMING, "CompositionLocal, staticCompositionLocalOf, Recomposition, Colors", ThemingListColor) { Tutorial7_7Screen() }
)

@Composable
internal fun createMaterial3TutorialList(): List<TutorialSectionModel> = listOf(
    tutorial(R.string.tutorial10_1_title, R.string.tutorial10_1_description,
        TAG_MATERIAL_3, "Button, Filled, Tonal, Elevated, Outlined, Text", Material3ListColor) { Tutorial10_1Screen() },
    tutorial(R.string.tutorial10_2_title, R.string.tutorial10_2_description,
        TAG_MATERIAL_3, "Chip, FilterChip, InputChip, AssistChip, SuggestionChip", Material3ListColor) { Tutorial10_2Screen() },
    tutorial(R.string.tutorial10_3_title, R.string.tutorial10_3_description,
        TAG_MATERIAL_3, "NavigationBar, BottomNavigation, Selection", Material3ListColor) { Tutorial10_3Screen() },
    tutorial(R.string.tutorial10_4_title, R.string.tutorial10_4_description,
        TAG_MATERIAL_3, "Drawer, DrawerState, Modal, Permanent, Adaptive", Material3ListColor) { Tutorial10_4Screen() },
    tutorial(R.string.tutorial10_5_title, R.string.tutorial10_5_description,
        TAG_MATERIAL_3, "Carousel, Item width, Minimum, Maximum, Preferred width, Mask, Fling", Material3ListColor) { Tutorial10_5Screen() },
    tutorial(R.string.tutorial10_6_title, R.string.tutorial10_6_description,
        TAG_MATERIAL_3, "SegmentedButton, Selection, Semantics", Material3ListColor) { Tutorial10_6Screen() },
    tutorial(R.string.tutorial10_7_title, R.string.tutorial10_7_description,
        TAG_MATERIAL_3, "Search, SearchBarState, TextFieldState", Material3ListColor) { Tutorial10_7Screen() },
    tutorial(R.string.tutorial10_8_title, R.string.tutorial10_8_description,
        TAG_MATERIAL_3, "DatePicker, DateRangePicker, TimePicker, TimeInput, Dialog", Material3ListColor) { Tutorial10_8Screen() },
    tutorial(R.string.tutorial10_9_title, R.string.tutorial10_9_description,
        TAG_MATERIAL_3, "NavigationRail, Adaptive, FAB", Material3ListColor) { Tutorial10_9Screen() }
)

@Composable
internal fun createAnimationTutorialList(): List<TutorialSectionModel> = listOf(
    tutorial(R.string.tutorial9_1_title, R.string.tutorial9_1_description,
        TAG_ANIMATION_CHAPTER, "Animation API, StateAnimation, VisibilityDecision, AnimatedVisibility, AnimatedContent, Crossfade, CoordinatedTransition, Transition, InfiniteTransition, RepeatingAnimation, AnimatableInterruption",
        AnimationListColor) { Tutorial9_1Screen() },
    tutorial(R.string.tutorial9_2_title, R.string.tutorial9_2_description,
        TAG_ANIMATION_CHAPTER, "AnimatedVisibility, AnimatedContent, AnimatedVisibilityTransition, AnimatedVisibilityCloseTest, MutableTransitionState, PoppingInCard, AnimateEnterExit",
        AnimationListColor) { Tutorial9_2Screen() },
    tutorial(R.string.tutorial9_3_title, R.string.tutorial9_3_description,
        TAG_ANIMATION_CHAPTER, "AnimatedVisibility, AnimatedContent, SimpleAnimatedContent, AnimateIncrementDecrement, Animation API, Transition, InfiniteTransition, TransitionExtensionAnimatedContent, SlideIntoContainer",
        AnimationListColor) { Tutorial9_3Screen() },
    tutorial(R.string.tutorial9_4_title, R.string.tutorial9_4_description,
        TAG_ANIMATION_CHAPTER, "Animatable, Mutex, MutatorMutex, MutexMutatorMutexTest1, MutexMutatorMutexTest2",
        AnimationListColor) { Tutorial9_4Screen() },
    tutorial(R.string.tutorial9_5_title, R.string.tutorial9_5_description,
        TAG_ANIMATION_CHAPTER, "Easing, EasingCurve, EasingComparison",
        AnimationListColor) { Tutorial9_5Screen() },
    tutorial(R.string.tutorial9_6_title, R.string.tutorial9_6_description,
        TAG_ANIMATION_CHAPTER, "Easing, EasingAnimationPlayground",
        AnimationListColor) { Tutorial9_6Screen() },
    tutorial(R.string.tutorial9_7_title, R.string.tutorial9_7_description,
        TAG_ANIMATION_CHAPTER, "Text, LazyRow, Pager, ComposeTypeWriter",
        AnimationListColor) { Tutorial9_7Screen() },
    tutorial(R.string.tutorial9_8_title, R.string.tutorial9_8_description,
        TAG_ANIMATION_CHAPTER, "Text, LazyRow, Pager, LazyRowSnapAndDeleteAnimation",
        AnimationListColor) { Tutorial9_8Screen() },
    tutorial(R.string.tutorial9_9_title, R.string.tutorial9_9_description,
        TAG_ANIMATION_CHAPTER, "Text, LazyRow, Pager, HorizontalPagerDelete",
        AnimationListColor) { Tutorial9_9Screen() },
    tutorial(R.string.tutorial9_10_title, R.string.tutorial9_10_description,
        TAG_ANIMATION_CHAPTER, "Particle, Canvas, ShakeTest, SingleParticleTrajectory, ParticleAnimation",
        AnimationListColor) { Tutorial9_10Screen() },
    tutorial(R.string.tutorial9_11_title, R.string.tutorial9_11_description,
        TAG_ANIMATION_CHAPTER, "Spring, Animatable",
        AnimationListColor) { Tutorial9_11Screen() },
    tutorial(R.string.tutorial9_12_title, R.string.tutorial9_12_description,
        TAG_ANIMATION_CHAPTER, "Keyframes, Animatable",
        AnimationListColor) { Tutorial9_12Screen() },
    tutorial(R.string.tutorial9_13_title, R.string.tutorial9_13_description,
        TAG_ANIMATION_CHAPTER, "InteractionSource, Spring, Press",
        AnimationListColor) { Tutorial9_13Screen() },
    tutorial(R.string.tutorial9_14_title, R.string.tutorial9_14_description,
        TAG_ANIMATION_CHAPTER, "InteractionSource, Modifier.Node, Shake",
        AnimationListColor) { Tutorial9_14Screen() },
    tutorial(R.string.tutorial9_15_title, R.string.tutorial9_15_description,
        TAG_ANIMATION_CHAPTER, "Rotation, Pivot, Draw",
        AnimationListColor) { Tutorial9_15Screen() },
    tutorial(R.string.tutorial9_16_title, R.string.tutorial9_16_description,
        TAG_ANIMATION_CHAPTER, "Wave, Canvas, Fill",
        AnimationListColor) { Tutorial9_16Screen() },
    tutorial(R.string.tutorial9_17_title, R.string.tutorial9_17_description,
        TAG_ANIMATION_CHAPTER, "Particle, Sparkle, Blast",
        AnimationListColor) { Tutorial9_17Screen() },
    tutorial(R.string.tutorial9_18_title, R.string.tutorial9_18_description,
        TAG_ANIMATION_CHAPTER, "Morph, RoundedPolygon, Shapes",
        AnimationListColor) { Tutorial9_18Screen() },
    tutorial(R.string.tutorial9_19_title, R.string.tutorial9_19_description,
        TAG_ANIMATION_CHAPTER, "Morph, Gradient, Shapes",
        AnimationListColor) { Tutorial9_19Screen() },
    tutorial(R.string.tutorial9_20_title, R.string.tutorial9_20_description,
        TAG_ANIMATION_CHAPTER, "InfiniteTransition, Morph, Indicator",
        AnimationListColor) { Tutorial9_20Screen() },
    tutorial(R.string.tutorial9_21_title, R.string.tutorial9_21_description,
        TAG_ANIMATION_CHAPTER, "Text, Gradient, Draw",
        AnimationListColor) { Tutorial9_21Screen() },
    tutorial(R.string.tutorial9_22_title, R.string.tutorial9_22_description,
        TAG_ANIMATION_CHAPTER, "Gooey, Canvas, Bounds",
        AnimationListColor) { Tutorial9_22Screen() },
    tutorial(R.string.tutorial9_23_title, R.string.tutorial9_23_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, sharedElement, sharedBounds, ListToDetails",
        AnimationListColor) { Tutorial9_23Screen() },
    tutorial(R.string.tutorial9_24_title, R.string.tutorial9_24_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, sharedElement, sharedBounds, SharedBounds, SharedElementScope_CompositionLocal",
        AnimationListColor) { Tutorial9_24Screen() },
    tutorial(R.string.tutorial9_25_title, R.string.tutorial9_25_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, sharedElement, sharedBounds, AnimatedVisibilitySharedElementShortenedExample",
        AnimationListColor) { Tutorial9_25Screen() },
    tutorial(R.string.tutorial9_26_title, R.string.tutorial9_26_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, sharedElement, sharedBounds, AnimatedVisibilitySharedElementBlurLayer",
        AnimationListColor) { Tutorial9_26Screen() },
    tutorial(R.string.tutorial9_27_title, R.string.tutorial9_27_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, Navigation, Overlay, SharedElement_ManualVisibleControl, SharedElementWithCallerManagedVisibility",
        AnimationListColor) { Tutorial9_27Screen() },
    tutorial(R.string.tutorial9_28_title, R.string.tutorial9_28_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, BoundsTransform, PlaceholderSize, SharedElementApp_BoundsTransformExample",
        AnimationListColor) { Tutorial9_28Screen() },
    tutorial(R.string.tutorial9_29_title, R.string.tutorial9_29_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, BoundsTransform, PlaceholderSize, SharedElement_Clipping",
        AnimationListColor) { Tutorial9_29Screen() },
    tutorial(R.string.tutorial9_30_title, R.string.tutorial9_30_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, BoundsTransform, PlaceholderSize, SharedElementApp_ResizeModeExample",
        AnimationListColor) { Tutorial9_30Screen() },
    tutorial(R.string.tutorial9_31_title, R.string.tutorial9_31_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, BoundsTransform, PlaceholderSize, SharedElement_SkipLookaheadSize",
        AnimationListColor) { Tutorial9_31Screen() },
    tutorial(R.string.tutorial9_32_title, R.string.tutorial9_32_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, Navigation, Overlay, SharedElementRenderInSharedTransitionScopeOverlay",
        AnimationListColor) { Tutorial9_32Screen() },
    tutorial(R.string.tutorial9_33_title, R.string.tutorial9_33_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, BoundsTransform, PlaceholderSize, SharedElementPlaceholderSize",
        AnimationListColor) { Tutorial9_33Screen() },
    tutorial(R.string.tutorial9_34_title, R.string.tutorial9_34_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, Navigation, Overlay, SharedElement_PredictiveBack",
        AnimationListColor) { Tutorial9_34Screen() },
    tutorial(R.string.tutorial9_35_title, R.string.tutorial9_35_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, Navigation, Overlay, SharedElement_Pager",
        AnimationListColor) { Tutorial9_35Screen() },
    tutorial(R.string.tutorial9_36_title, R.string.tutorial9_36_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, Navigation, Overlay, SharedElementSheetToScreen",
        AnimationListColor) { Tutorial9_36Screen() },
    tutorial(R.string.tutorial9_37_title, R.string.tutorial9_37_description,
        TAG_ANIMATION_CHAPTER, "SharedTransition, Navigation, Overlay, SharedElementsample2",
        AnimationListColor) { Tutorial9_37Screen() }
)
