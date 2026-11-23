package com.example.kotlin_test.feature.alarm.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private val Night = Color(0xFF0A0C12)
private val Panel = Color(0xFF151922)
private val Accent = Color(0xFF91E3EC)
private val MainText = Color(0xFFF4F6FA)
private val MutedText = Color(0xFF929BAA)

@Composable
fun RingingRoute(
    viewModel: RingingViewModel,
    alarmId: Int,
    onStartRinging: (String, Boolean) -> Unit,
    onClose: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.alarm) {
        state.alarm?.let { alarm ->
            onStartRinging(alarm.soundUri, alarm.vibrate)
        }
    }

    LaunchedEffect(state.isClosed) {
        if (state.isClosed) {
            onClose()
        }
    }

    RingingScreen(
        alarm = state.alarm,
        onSnooze = { viewModel.snooze(alarmId) },
        onDismiss = { viewModel.dismiss(alarmId) }
    )
}

@Composable
private fun RingingScreen(
    alarm: com.example.kotlin_test.feature.alarm.domain.model.Alarm?,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { _, dragAmount ->
                        offsetY += dragAmount
                    },
                    onDragEnd = {
                        if (offsetY < -120f) {
                            onSnooze()
                        } else if (offsetY > 120f) {
                            onDismiss()
                        }
                        offsetY = 0f
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .offset { IntOffset(0, offsetY.toInt()) }
        ) {
            Box(
                modifier = Modifier.size(120.dp).background(Panel, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🔔", fontSize = 48.sp)
            }
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                text = alarm?.label?.ifBlank { "Alarm" } ?: "Alarm",
                color = MainText,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Text(
                text = "Slide up for Snooze (${alarm?.snoozeMinutes ?: 5}m)\nSlide down to Dismiss",
                color = MutedText,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
            ) {
                FilledTonalButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Accent,
                        contentColor = Color(0xFF10171A)
                    )
                ) {
                    Text("Dismiss (Swipe Down)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
                
                FilledTonalButton(
                    onClick = onSnooze,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Panel,
                        contentColor = MainText
                    )
                ) {
                    Text("Snooze (${alarm?.snoozeMinutes ?: 5}m - Swipe Up)", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}
