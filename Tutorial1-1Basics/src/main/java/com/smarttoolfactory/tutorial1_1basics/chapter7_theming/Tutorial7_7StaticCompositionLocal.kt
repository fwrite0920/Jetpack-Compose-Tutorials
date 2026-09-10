package com.smarttoolfactory.tutorial1_1basics.chapter7_theming

import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialPage
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialHeader
import com.smarttoolfactory.tutorial1_1basics.ui.components.TutorialText2
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttoolfactory.tutorial1_1basics.ui.components.StyleableTutorialText
import com.smarttoolfactory.tutorial1_1basics.ui.components.getRandomColor

@Composable
internal fun Tutorial7_7Screen() {
    TutorialPage(R.string.tutorial7_7_title,
        "**CompositionLocal** provides values to a subtree.",
        "Explore each example below and compare the values inherited from the nearest provider.") {
        TutorialHeader("Local vs static")
        TutorialText2("A tracked local invalidates readers. A static local invalidates its provider content; skippable children can still be skipped. Borders change when their scope recomposes.")
        Box(Modifier.fillMaxWidth().height(440.dp)) { LocalVsStaticCompositionLocalTest() }
        TutorialHeader("Provided colors")
        TutorialText2("A custom static local can carry a palette. Change the provider to update this preview.")
        Box(Modifier.fillMaxWidth().height(240.dp)) { StaticCompositionLocalOfTest() }
    }
}

val LocalColors: ProvidableCompositionLocal<Colors> = staticCompositionLocalOf {
    lightColors()
}

val LocalStaticCounter: ProvidableCompositionLocal<Int> = staticCompositionLocalOf { 0 }
val LocalCounter: ProvidableCompositionLocal<Int> = compositionLocalOf { 0 }


@Preview
@Composable
internal fun StaticCompositionLocalOfTest() {
    var isLightTheme by remember {
        mutableStateOf(true)
    }
    CompositionLocalProvider(
        LocalColors provides if (isLightTheme) lightColors(
        ) else darkColors()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LocalColors.current.background)
                .padding(16.dp)
        ) {
            Text("Selected theme: ${if (isLightTheme) "Light" else "Dark"}",
                color = LocalColors.current.onBackground)
            Spacer(modifier = Modifier.weight(1f))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    isLightTheme = isLightTheme.not()
                }
            ) {
                Text("Change Theme")
            }
        }
    }
}

@Preview
@Composable
fun LocalVsStaticCompositionLocalTest() {

    var counter by remember {
        mutableIntStateOf(0)
    }

    var counterStatic by remember {
        mutableIntStateOf(0)
    }
    CompositionLocalProvider(LocalCounter provides counter) {
        CompositionLocalProvider(LocalStaticCounter provides counterStatic) {
            Content(
                onCLickLocal = {
                    counter++
                },
                onCLickStaticLocal = {
                    counterStatic++
                }
            )
        }
    }
}

@Composable
private fun Content(
    onCLickLocal: () -> Unit,
    onCLickStaticLocal: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(4.dp, getRandomColor())
            .padding(16.dp)
    ) {
        StyleableTutorialText(
            text = "Changing **compositionLocalOf** invalidates its readers. Changing " +
                    "**staticCompositionLocalOf** invalidates the provider content. " +
                    "Compose can still skip eligible child calls.",
            bullets = false
        )
        LocalCounterText()
        LocalStaticCounterText()

        Spacer(modifier = Modifier.weight(1f))
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onCLickLocal()
            }
        ) {
            LocalCounterText()
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onCLickStaticLocal()
            }
        ) {
            LocalStaticCounterText()
        }
    }
}

@Composable
private fun LocalCounterText() {
    val counter = LocalCounter.current
    Text(
        text = "LocalCounter $counter",
        fontSize = 20.sp,
        modifier = Modifier.fillMaxWidth().border(2.dp, getRandomColor())
    )
}

@Composable
private fun LocalStaticCounterText() {
    val counter = LocalStaticCounter.current
    Text(
        text = "LocalStaticCounter $counter",
        fontSize = 20.sp,
        modifier = Modifier.fillMaxWidth().border(2.dp, getRandomColor())
    )
}
