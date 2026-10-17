package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicTaskEntity
import com.example.data.model.ClassTipEntity
import com.example.ui.components.Eyebrow
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveGreen
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale

@Composable
fun TaskFocusScreen(
    task: AcademicTaskEntity,
    tips: List<ClassTipEntity>,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    onToggleTimer: () -> Unit,
    onToggleCompleted: () -> Unit,
    onEditTask: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBackClick() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("task_focus_back_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = EvolveMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Plan & Tasks",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
            }

            IconButton(onClick = onEditTask, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit task",
                    tint = EvolveMuted,
                    modifier = Modifier.size(17.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Eyebrow(task.subjectId)
        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = task.title,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 30.sp,
            lineHeight = 36.sp,
            color = EvolveText
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Timer box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                .background(Color(0x08FFFFFF), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK FOCUS TIMER",
                        fontFamily = ManropeFontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 1.4.sp,
                        color = EvolveDim
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    Text(
                        text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 44.sp,
                        color = EvolveText
                    )
                }

                IconButton(
                    onClick = onToggleTimer,
                    modifier = Modifier
                        .size(50.dp)
                        .border(1.dp, EvolveLine, CircleShape)
                        .testTag("task_timer_toggle")
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                        contentDescription = if (isTimerRunning) "Pause" else "Start",
                        tint = EvolveAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (task.instructions.isNotBlank()) {
            Text(
                text = "INSTRUCTIONS",
                fontFamily = ManropeFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = EvolveDim
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = task.instructions,
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = EvolveText
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        if (tips.isNotEmpty()) {
            Text(
                text = "CLASS NOTEBOOK TIPS FOR THIS SUBJECT",
                fontFamily = ManropeFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.4.sp,
                color = EvolveDim
            )
            Spacer(modifier = Modifier.height(8.dp))
            tips.take(2).forEach { tip ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x0EB9A2F5))
                        .border(1.dp, Color(0x33B9A2F5), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = tip.text,
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFFBBB3CC)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onToggleCompleted,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("toggle_task_completed_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (task.completed) Color(0xFF2E4033) else EvolvePillBg,
                contentColor = if (task.completed) EvolveGreen else EvolvePillText
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (task.completed) "Marked complete" else "Mark task as complete",
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
