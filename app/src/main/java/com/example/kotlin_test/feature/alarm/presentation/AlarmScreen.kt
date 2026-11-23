package com.example.kotlin_test.feature.alarm.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.app.TimePickerDialog
import java.time.LocalDateTime
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.kotlin_test.feature.alarm.domain.model.Alarm
import com.example.kotlin_test.feature.alarm.domain.model.AlarmSchedulePolicy
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Night = Color(0xFF0A0C12)
private val Panel = Color(0xFF151922)
private val PanelRaised = Color(0xFF1C222D)
private val Accent = Color(0xFF91E3EC)
private val Ink = Color(0xFF10171A)
private val MainText = Color(0xFFF4F6FA)
private val MutedText = Color(0xFF929BAA)
private val Divider = Color(0xFF292F3A)

@Composable
fun AlarmRoute(viewModel: AlarmViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    AlarmScreen(
        state = state,
        onCreateAlarm = { hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes ->
            viewModel.createAlarm(hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes)
        },
        onUpdateAlarm = { id, hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes ->
            viewModel.updateAlarm(id, hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes)
        },
        onToggleAlarm = { id, enabled ->
            viewModel.setAlarmEnabled(id, enabled)
            if (enabled) requestNotificationPermission(context, notificationPermission::launch)
        },
        onRemoveAlarm = viewModel::removeAlarm,
        onDismissExactAlarmPrompt = viewModel::dismissExactAlarmPrompt,
        onOpenExactAlarmSettings = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:${context.packageName}")
                    )
                )
            }
        },
        onRequestNotificationPermission = {
            requestNotificationPermission(context, notificationPermission::launch)
        }
    )
}

@Composable
private fun AlarmScreen(
    state: AlarmUiState,
    onCreateAlarm: (Int, Int, String, Set<DayOfWeek>, String, Boolean, Int) -> Unit,
    onUpdateAlarm: (Int, Int, Int, String, Set<DayOfWeek>, String, Boolean, Int) -> Unit,
    onToggleAlarm: (Int, Boolean) -> Unit,
    onRemoveAlarm: (Int) -> Unit,
    onDismissExactAlarmPrompt: () -> Unit,
    onOpenExactAlarmSettings: () -> Unit,
    onRequestNotificationPermission: () -> Unit
) {
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Night,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingAlarm = null
                    showDialog = true
                },
                containerColor = Accent,
                contentColor = Ink,
                shape = CircleShape
            ) { Text("+", fontSize = 32.sp, fontWeight = FontWeight.Light) }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Night)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DAYBREAK", color = Accent, fontSize = 12.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
                Surface(color = PanelRaised, shape = CircleShape) {
                    Text("ALARM", color = MutedText, fontSize = 10.sp, letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp))
                }
            }

            Spacer(Modifier.height(34.dp))
            Text(
                text = when (state.currentTime.hour) {
                    in 5..11 -> "Good morning"
                    in 12..17 -> "Good afternoon"
                    else -> "Good evening"
                },
                color = MutedText,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                state.currentTime.format(DateTimeFormatter.ofPattern("h:mm", Locale.getDefault())),
                color = MainText,
                fontSize = 66.sp,
                lineHeight = 72.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = (-2).sp
            )
            Text(
                text = state.currentTime.format(DateTimeFormatter.ofPattern("a", Locale.getDefault())) +
                    "   ·   " + LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())),
                color = MutedText,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(38.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your alarms", color = MainText, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    if (state.alarms.isEmpty()) "NONE SET" else "${state.alarms.size} SAVED",
                    color = MutedText,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp
                )
            }
            Spacer(Modifier.height(14.dp))

            if (state.alarms.isEmpty()) {
                EmptyAlarmsCard(onAdd = {
                    editingAlarm = null
                    showDialog = true
                })
            } else {
                state.alarms.forEach { alarm ->
                    AlarmCard(
                        alarm = alarm,
                        onToggle = { enabled -> onToggleAlarm(alarm.id, enabled) },
                        onDelete = { pendingDelete = alarm },
                        onEdit = {
                            editingAlarm = alarm
                            showDialog = true
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            Spacer(Modifier.height(78.dp))
            Text(
                "A calmer start begins the night before.",
                color = MutedText,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDialog) {
        AlarmDialog(
            alarmToEdit = editingAlarm,
            onDismiss = {
                showDialog = false
                editingAlarm = null
            },
            onSave = { hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes ->
                val currentEditing = editingAlarm
                if (currentEditing == null) {
                    onCreateAlarm(hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes)
                } else {
                    onUpdateAlarm(currentEditing.id, hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes)
                }
                onRequestNotificationPermission()
                showDialog = false
                editingAlarm = null
            }
        )
    }

    pendingDelete?.let { alarm ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove this alarm?", color = MainText) },
            text = { Text("This alarm will be deleted from Daybreak.", color = MutedText) },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveAlarm(alarm.id)
                    pendingDelete = null
                }) { Text("Remove", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Keep", color = MutedText) }
            },
            containerColor = Panel,
            titleContentColor = MainText,
            textContentColor = MutedText
        )
    }

    if (state.exactAlarmAccessRequired) {
        AlertDialog(
            onDismissRequest = onDismissExactAlarmPrompt,
            title = { Text("Allow exact alarms", color = MainText) },
            text = {
                Text(
                    "Android may delay alarms until this access is enabled. Turn it on for reliable wake-up times.",
                    color = MutedText
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDismissExactAlarmPrompt()
                    onOpenExactAlarmSettings()
                }) { Text("Open settings", color = Accent) }
            },
            dismissButton = {
                TextButton(onClick = onDismissExactAlarmPrompt) { Text("Not now", color = MutedText) }
            },
            containerColor = Panel,
            titleContentColor = MainText,
            textContentColor = MutedText
        )
    }
}

@Composable
private fun EmptyAlarmsCard(onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Panel)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(58.dp).background(PanelRaised, CircleShape), contentAlignment = Alignment.Center) {
                Text("◷", color = Accent, fontSize = 30.sp)
            }
            Spacer(Modifier.height(16.dp))
            Text("No alarms yet", color = MainText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Set a time that gives tomorrow a softer start.", color = MutedText, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            FilledTonalButton(
                onClick = onAdd,
                shape = RoundedCornerShape(14.dp),
                colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                    containerColor = Accent,
                    contentColor = Ink
                )
            ) { Text("Set an alarm", fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun AlarmCard(alarm: Alarm, onToggle: (Boolean) -> Unit, onDelete: () -> Unit, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Panel)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 16.dp, bottom = 16.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onEdit)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    val hour12 = if (alarm.hour % 12 == 0) 12 else alarm.hour % 12
                    Text(
                        String.format(Locale.getDefault(), "%d:%02d", hour12, alarm.minute),
                        color = if (alarm.enabled) MainText else MutedText,
                        fontSize = 40.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (alarm.hour < 12) "AM" else "PM",
                        color = Accent.copy(alpha = if (alarm.enabled) 1f else 0.45f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(alarm.label, color = MainText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(3.dp))
                Text(AlarmSchedulePolicy.repeatSummary(alarm.repeatDays) + "  ·  Snooze ${alarm.snoozeMinutes}m", color = MutedText, fontSize = 12.sp)
                if (alarm.enabled) {
                    Spacer(Modifier.height(2.dp))
                    val timeRemaining = getTimeRemaining(alarm.hour, alarm.minute, alarm.repeatDays)
                    Text(timeRemaining, color = Accent.copy(alpha = 0.7f), fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = onToggle,
                    modifier = Modifier.semantics { contentDescription = "Enable ${alarm.label}" },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Ink,
                        checkedTrackColor = Accent,
                        uncheckedThumbColor = MutedText,
                        uncheckedTrackColor = PanelRaised,
                        uncheckedBorderColor = PanelRaised
                    )
                )
                TextButton(onClick = onDelete) { Text("Remove", color = MutedText, fontSize = 11.sp) }
            }
        }
    }
}

private fun getTimeRemaining(hour: Int, minute: Int, repeatDays: Set<DayOfWeek>): String {
    val now = LocalDateTime.now()
    val alarmTime = LocalDateTime.now().withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    
    val targetDateTime = if (repeatDays.isEmpty()) {
        // One-time alarm
        if (alarmTime.isBefore(now)) {
            alarmTime.plusDays(1)
        } else {
            alarmTime
        }
    } else {
        // Repeating alarm - find next occurrence
        var nextAlarm = alarmTime
        if (nextAlarm.isBefore(now)) {
            nextAlarm = nextAlarm.plusDays(1)
        }
        // Find the next matching day
        var attempts = 0
        while (nextAlarm.dayOfWeek !in repeatDays && attempts < 7) {
            nextAlarm = nextAlarm.plusDays(1)
            attempts++
        }
        nextAlarm
    }
    
    val duration = java.time.Duration.between(now, targetDateTime)
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    
    return when {
        hours > 24 -> "in ${hours / 24}d ${hours % 24}h"
        hours > 0 -> "in ${hours}h ${minutes}m"
        minutes > 0 -> "in ${minutes}m"
        else -> "now"
    }
}

@Composable
private fun AlarmDialog(
    alarmToEdit: Alarm? = null,
    onDismiss: () -> Unit,
    onSave: (Int, Int, String, Set<DayOfWeek>, String, Boolean, Int) -> Unit
) {
    val context = LocalContext.current
    val defaultTime = run {
        val rounded = LocalTime.now().plusMinutes(5).withSecond(0).withNano(0)
        val roundedMinute = ((rounded.minute + 4) / 5) * 5
        if (roundedMinute >= 60) rounded.plusHours(1).withMinute(0) else rounded.withMinute(roundedMinute)
    }
    var hour by remember { mutableStateOf(alarmToEdit?.hour ?: defaultTime.hour) }
    var minute by remember { mutableStateOf(alarmToEdit?.minute ?: defaultTime.minute) }
    var label by remember { mutableStateOf(alarmToEdit?.label ?: "") }
    var repeatDays by remember { mutableStateOf(alarmToEdit?.repeatDays ?: emptySet()) }
    var snoozeMinutes by remember { mutableStateOf(alarmToEdit?.snoozeMinutes ?: 5) }
    var soundUri by remember { mutableStateOf(alarmToEdit?.soundUri ?: android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString()) }
    var soundName by remember {
        mutableStateOf(
            if (alarmToEdit?.soundUri != null && alarmToEdit.soundUri != android.provider.Settings.System.DEFAULT_NOTIFICATION_URI.toString())
                "Custom sound"
            else "Default"
        )
    }
    var vibrate by remember { mutableStateOf(alarmToEdit?.vibrate ?: true) }
    val days = listOf(
        DayOfWeek.MONDAY to "M", DayOfWeek.TUESDAY to "T", DayOfWeek.WEDNESDAY to "W",
        DayOfWeek.THURSDAY to "T", DayOfWeek.FRIDAY to "F", DayOfWeek.SATURDAY to "S",
        DayOfWeek.SUNDAY to "S"
    )
    val snoozeOptions = listOf(5, 10, 15, 20, 30)

    val soundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            soundUri = uri.toString()
            soundName = "Custom sound"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (alarmToEdit == null) "New alarm" else "Edit alarm", color = MainText) },
        text = {
            Column {
                TextButton(onClick = {
                    TimePickerDialog(context, { _, selectedHour, selectedMinute ->
                        hour = selectedHour
                        minute = selectedMinute
                    }, hour, minute, false).show()
                }) {
                    Text(
                        String.format(Locale.getDefault(), "%d:%02d %s", if (hour % 12 == 0) 12 else hour % 12, minute, if (hour < 12) "AM" else "PM"),
                        color = Accent,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Light
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MainText,
                        unfocusedTextColor = MainText,
                        focusedBorderColor = Accent,
                        unfocusedBorderColor = Divider,
                        focusedLabelColor = Accent,
                        unfocusedLabelColor = MutedText,
                        cursorColor = Accent,
                        focusedContainerColor = Panel,
                        unfocusedContainerColor = Panel
                    )
                )
                Spacer(Modifier.height(16.dp))
                Text("REPEAT", color = MutedText, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    days.forEach { (day, shortName) ->
                        FilterChip(
                            selected = day in repeatDays,
                            onClick = { repeatDays = if (day in repeatDays) repeatDays - day else repeatDays + day },
                            label = { Text(shortName, fontSize = 11.sp) },
                            modifier = Modifier.semantics { contentDescription = day.name.lowercase().replaceFirstChar { it.uppercase() } },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = PanelRaised,
                                labelColor = MutedText,
                                selectedContainerColor = Accent,
                                selectedLabelColor = Ink
                            ),
                            border = null
                        )
                    }
                }
                Text(
                    if (repeatDays.isEmpty()) "Rings once" else AlarmSchedulePolicy.repeatSummary(repeatDays),
                    color = MutedText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("SNOOZE DURATION", color = MutedText, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    snoozeOptions.forEach { minutes ->
                        FilterChip(
                            selected = snoozeMinutes == minutes,
                            onClick = { snoozeMinutes = minutes },
                            label = { Text("${minutes}m", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = PanelRaised,
                                labelColor = MutedText,
                                selectedContainerColor = Accent,
                                selectedLabelColor = Ink
                            ),
                            border = null
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("SOUND", color = MutedText, fontSize = 10.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(7.dp))
                FilledTonalButton(
                    onClick = {
                        soundPickerLauncher.launch(arrayOf("audio/*"))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.filledTonalButtonColors(
                        containerColor = PanelRaised,
                        contentColor = MainText
                    )
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text(soundName, fontSize = 13.sp)
                        if (soundName != "Default") {
                            Text("Custom file selected", color = Accent, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration", color = MainText, fontSize = 14.sp)
                    Switch(
                        checked = vibrate,
                        onCheckedChange = { vibrate = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Ink,
                            checkedTrackColor = Accent,
                            uncheckedThumbColor = MutedText,
                            uncheckedTrackColor = PanelRaised,
                            uncheckedBorderColor = PanelRaised
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(hour, minute, label, repeatDays, soundUri, vibrate, snoozeMinutes) }) {
                Text(if (alarmToEdit == null) "Save alarm" else "Save changes", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MutedText) }
        },
        containerColor = Panel,
        titleContentColor = MainText,
        textContentColor = MainText
    )
}

private fun requestNotificationPermission(context: android.content.Context, launch: (String) -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) {
        launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
