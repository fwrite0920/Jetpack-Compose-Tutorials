package com.smarttoolfactory.tutorial1_1basics.chapter10_material3

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.smarttoolfactory.tutorial1_1basics.R
import com.smarttoolfactory.tutorial1_1basics.ui.components.*
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Tutorial10_8Screen() {
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    var date by rememberSaveable { mutableStateOf<Long?>(null) }
    var start by rememberSaveable { mutableStateOf<Long?>(null) }
    var end by rememberSaveable { mutableStateOf<Long?>(null) }
    var hour by rememberSaveable { mutableIntStateOf(9) }
    var minute by rememberSaveable { mutableIntStateOf(30) }
    var timeConfirmed by rememberSaveable { mutableStateOf(false) }
    TutorialPage(R.string.tutorial10_8_title,
        "**Picker state** holds a draft selection. Confirm copies it to the result; dismiss leaves the last confirmed value unchanged.",
        "Choose a single date, a range, a dial time or keyboard time. The date formatter uses UTC because date-picker timestamps represent UTC calendar dates.") {
        Button(onClick = { dialog = 1 }) { Text("Choose date") }
        Text("Date: ${formatTutorialDate(date)}", Modifier.testTag("date-result"))
        Button(onClick = { dialog = 2 }) { Text("Choose date range") }
        Text("Range: ${formatTutorialDate(start)} – ${formatTutorialDate(end)}",
            Modifier.testTag("range-result"))
        Button(onClick = { dialog = 3 }) { Text("Choose dial time") }
        Button(onClick = { dialog = 4 }) { Text("Choose keyboard time") }
        val calendar = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, hour); set(Calendar.MINUTE, minute) }
        Text("Time: " + if (timeConfirmed) DateFormat.getTimeInstance(DateFormat.SHORT).format(calendar.time) else "Not selected",
            Modifier.testTag("time-result"))
        if (dialog == 1) {
            val state = rememberDatePickerState(initialSelectedDateMillis = date)
            DatePickerDialog(onDismissRequest = { dialog = 0 },
                confirmButton = {
                    TextButton(onClick = { date = state.selectedDateMillis; dialog = 0 },
                        enabled = state.selectedDateMillis != null) { Text("Confirm date") }
                }, dismissButton = { TextButton(onClick = { dialog = 0 }) { Text("Cancel") } }) {
                DatePicker(state, modifier = Modifier.verticalScroll(rememberScrollState()))
            }
        }
        if (dialog == 2) {
            val state = rememberDateRangePickerState(
                initialSelectedStartDateMillis = start, initialSelectedEndDateMillis = end)
            DatePickerDialog(onDismissRequest = { dialog = 0 },
                confirmButton = {
                    TextButton(onClick = {
                        start = state.selectedStartDateMillis; end = state.selectedEndDateMillis; dialog = 0
                    }, enabled = state.selectedStartDateMillis != null && state.selectedEndDateMillis != null) {
                        Text("Confirm range")
                    }
                }, dismissButton = { TextButton(onClick = { dialog = 0 }) { Text("Cancel") } }) {
                DateRangePicker(state, modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp))
            }
        }
        if (dialog == 3 || dialog == 4) {
            val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
            AlertDialog(onDismissRequest = { dialog = 0 },
                title = { Text(if (dialog == 3) "Select dial time" else "Enter time") },
                text = {
                    Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState())) {
                        if (dialog == 3) TimePicker(state) else TimeInput(state)
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        hour = state.hour; minute = state.minute; timeConfirmed = true; dialog = 0
                    }) { Text("Confirm time") }
                }, dismissButton = { TextButton(onClick = { dialog = 0 }) { Text("Cancel") } },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.widthIn(max = 400.dp).padding(16.dp))
        }
    }
}

internal fun formatTutorialDate(millis: Long?): String = millis?.let {
    DateFormat.getDateInstance(DateFormat.MEDIUM).apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(it))
} ?: "Not selected"
