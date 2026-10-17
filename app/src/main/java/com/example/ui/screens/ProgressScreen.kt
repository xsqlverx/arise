package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate

@Composable
fun ProgressScreen(curriculumAttempts: List<CurriculumAttemptEntity>, onOpenSemester: () -> Unit, sessions: List<StudySessionEntity>, tasks: List<AcademicTaskEntity>,
    onOpenSubjectsClick: () -> Unit, onSettingsClick: () -> Unit, onOpenSample: () -> Unit) {
    val completed = tasks.count { it.completed }
    val sample = sessions.filter { it.total > 0 && it.score in 0..it.total && it.isSample && it.evidenceType == "sample_quiz" }
    val legacy = sessions.filter { it.evidenceType == "legacy" && it.total > 0 }
    Column(Modifier.fillMaxSize().background(EvolveBlack).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        TopBar(onSettingsClick = onSettingsClick)
        Spacer(Modifier.height(24.dp)); Eyebrow("A change you can see")
        Spacer(Modifier.height(10.dp))
        Text("Becoming,", fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp, color = EvolveText)
        Text("day by day.", fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp, color = EvolveAccent)
        Box(Modifier.fillMaxWidth().height(260.dp)) {
            ContourLandscape(height = 260.dp)
            Column(Modifier.align(Alignment.TopStart).padding(top = 22.dp)) {
                Text(completed.toString().padStart(2, '0'), fontSize = 48.sp, fontFamily = SpaceGroteskFontFamily, color = EvolveText)
                Text("steps you marked complete", fontSize = 12.sp, color = EvolveMuted)
            }
            Text("YOUR LANDSCAPE · A VISUAL MOTIF", Modifier.align(Alignment.BottomStart).padding(bottom = 12.dp),
                color = EvolveDim, fontSize = 9.sp, letterSpacing = 1.sp)
        }
        Spacer(Modifier.height(20.dp)); Eyebrow("Your activity", true)
        Spacer(Modifier.height(14.dp))
        Text(if (tasks.isEmpty()) "Your first step starts the record." else "$completed completed · ${tasks.count { !it.completed }} in your queue",
            fontFamily = SpaceGroteskFontFamily, color = EvolveText, fontSize = 23.sp, lineHeight = 30.sp)
        Spacer(Modifier.height(10.dp))
        Text("Task completion is self-reported activity. It does not measure understanding, syllabus coverage or exam readiness.",
            color = EvolveMuted, fontSize = 13.sp, lineHeight = 22.sp)
        Spacer(Modifier.height(24.dp)); HorizontalDivider(color = EvolveLine)
        Spacer(Modifier.height(24.dp)); Eyebrow("Syllabus starter checks", true)
        Spacer(Modifier.height(12.dp))
        Text("${curriculumAttempts.size} saved attempts · ${curriculumAttempts.map { it.moduleId }.distinct().size} starter concepts tried",
            color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 23.sp, lineHeight = 30.sp)
        Text("These are narrow authored checks. Repeats use the same questions; there is no whole-module completion percentage.", color = EvolveMuted, fontSize = 13.sp, lineHeight = 22.sp)
        curriculumAttempts.take(12).forEach { a ->
            Spacer(Modifier.height(12.dp))
            Text("${a.moduleId.uppercase()} · ${a.score}/${a.total} · review ${a.reviewDate}",color = EvolveAccent,fontSize = 12.sp)
        }
        TextButton(onClick = onOpenSemester) { Text("Choose a concept / see uncovered topics ↗",color=EvolveAccent) }
        Spacer(Modifier.height(24.dp)); Eyebrow("Sample practice · separate from your coursework", true)
        Spacer(Modifier.height(12.dp))
        if (sample.isEmpty()) {
            Text("No sample checks yet.", fontFamily = SpaceGroteskFontFamily, color = EvolveText, fontSize = 23.sp)
        } else {
            val correct = sample.sumOf { it.score }
            val total = sample.sumOf { it.total }
            Text("$correct / $total", fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp, color = EvolveText)
            Text("answers correct across ${sample.size} sample attempts", color = EvolveMuted, fontSize = 13.sp)
            Spacer(Modifier.height(24.dp))
            Eyebrow("Sample accuracy · last seven days")
            Spacer(Modifier.height(16.dp))
            TrendChart((6L downTo 0L).map { offset ->
                val day = LocalDate.now().minusDays(offset)
                val records = sample.filter { it.date == day.toString() }
                day to if (records.isEmpty()) null else records.sumOf { it.score }.toFloat() / records.sumOf { it.total }
            })
            Spacer(Modifier.height(12.dp))
            Text("Gaps mean no sample check that day. The list below gives each recorded score.", color = EvolveDim, fontSize = 11.sp)
            sample.take(5).forEach {
                Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${it.date} · Python sample", color = EvolveMuted, fontSize = 12.sp)
                    Text("${it.score}/${it.total}", color = EvolveAccent, fontFamily = SpaceGroteskFontFamily, fontSize = 16.sp)
                }
                HorizontalDivider(color = EvolveLine)
            }
        }
        Spacer(Modifier.height(18.dp)); EvolveAction("Try the sample lesson  ↗", onOpenSample)
        if (legacy.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("${legacy.size} older checks are kept in your backup. Their source was not recorded, so they are excluded from these totals.",
                color = EvolveDim, fontSize = 12.sp, lineHeight = 19.sp)
        }
        Spacer(Modifier.height(18.dp))
        TextButton(onClick = onOpenSubjectsClick) { Text("Open your class notebooks ↗", color = EvolveAccent) }
        Spacer(Modifier.height(120.dp))
    }
}
