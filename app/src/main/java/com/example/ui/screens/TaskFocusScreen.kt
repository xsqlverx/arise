package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicTaskEntity
import com.example.data.model.ClassTipEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun TaskFocusScreen(task: AcademicTaskEntity, tips: List<ClassTipEntity>, seconds: Int, running: Boolean,
    onTimer: () -> Unit, onComplete: () -> Unit, onEdit: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(EvolveBlack).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text("← Your plan", color = EvolveMuted) }
            TextButton(onClick = onEdit) { Text("Edit step", color = EvolveAccent) }
        }
        Spacer(Modifier.height(28.dp))
        Eyebrow("One step. Your full attention.", true)
        Spacer(Modifier.height(12.dp))
        Text(task.title, color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 34.sp, lineHeight = 40.sp)
        Spacer(Modifier.height(28.dp))
        Box(Modifier.fillMaxWidth().height(224.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(210.dp)) {
                drawArc(EvolveRaised, -90f, 360f, false, style = Stroke(2.dp.toPx()))
                drawArc(EvolveAccent, -90f, 360f * (1f - seconds.toFloat() / (task.durationMinutes * 60)).coerceIn(0f, 1f),
                    false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(String.format(Locale.US, "%02d:%02d", seconds / 60, seconds % 60), color = EvolveText,
                    fontFamily = SpaceGroteskFontFamily, fontSize = 44.sp)
                TextButton(onClick = onTimer) { Text(if (running) "Pause focus" else "Start focus", color = EvolveAccent) }
            }
        }
        Spacer(Modifier.height(24.dp))
        Eyebrow("Start here", true)
        Spacer(Modifier.height(12.dp))
        Text(task.instructions, color = EvolveText, fontFamily = ManropeFontFamily, fontSize = 15.sp, lineHeight = 25.sp)
        val pinned = tips.filter { it.pinned && !it.isSample }.take(3)
        if (pinned.isNotEmpty()) {
            var notesOpen by rememberSaveable(task.id) { mutableStateOf(false) }
            Spacer(Modifier.height(20.dp))
            EvolveDisclosure("From your class notebook",notesOpen,{ notesOpen = !notesOpen }) {
                pinned.forEach { Text(it.text,color = EvolveMuted,fontSize = 14.sp,lineHeight = 23.sp); Spacer(Modifier.height(12.dp)) }
            }
        }
        Spacer(Modifier.height(32.dp))
        Text("Before you finish: close the source and explain what you did, or check your working against it.",
            color = EvolveMuted, fontSize = 13.sp, lineHeight = 21.sp)
        Spacer(Modifier.height(18.dp))
        if (task.completed) { SavedMark(); Spacer(Modifier.height(12.dp)) }
        EvolveAction(if (task.completed) "Reopen this step" else "I completed this step  ↗", onComplete)
        Spacer(Modifier.height(12.dp))
        Text("Self-reported activity. The timer does not award mastery.", color = EvolveDim, fontSize = 11.sp)
        Spacer(Modifier.height(40.dp))
    }
}
