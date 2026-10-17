package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.domain.PlannerEngine
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PlanScreen(nextAction: com.example.domain.NextAction, onOpenAction: () -> Unit, onOpenSemester: () -> Unit, userProfile: UserProfileEntity, tasks: List<AcademicTaskEntity>, activeDate: LocalDate, now: LocalDateTime,
    onSelectDate: (LocalDate) -> Unit, onRescheduleClick: () -> Unit,
    onOpenBriefClick: (String) -> Unit, onOpenStudyClick: () -> Unit,
    onTaskClick: (String) -> Unit, onEditTask: (String) -> Unit, onAddTask: () -> Unit,
    onSettingsClick: () -> Unit) {
    val action = nextAction
    val slot = PlannerEngine.plannedSlot(userProfile, action, now)
    val today = now.toLocalDate()
    Column(Modifier.fillMaxSize().background(EvolveBlack).padding(horizontal = 24.dp).verticalScroll(rememberScrollState())) {
        TopBar(onSettingsClick = onSettingsClick)
        Spacer(Modifier.height(24.dp)); Eyebrow("Your time, considered")
        Spacer(Modifier.height(9.dp))
        Text("Make space", color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp)
        Text("for progress.", color = EvolveAccent, fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp)
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0L..6L).forEach { offset ->
                val day = today.plusDays(offset)
                val selected = day == activeDate
                Column(Modifier.clip(RoundedCornerShape(26.dp)).background(if (selected) EvolvePillBg else EvolveBlack)
                    .clickable { onSelectDate(day) }.widthIn(min = 44.dp).padding(horizontal = 13.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(day.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)).take(1), color = if (selected) EvolveBlack else EvolveDim, fontSize = 11.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(day.dayOfMonth.toString(), color = if (selected) EvolveBlack else EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 17.sp)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
        Eyebrow(activeDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)))
        Spacer(Modifier.height(20.dp))
        data class Entry(val time: String, val title: String, val copy: String, val locked: Boolean, val action: Boolean = false)
        val entries = mutableListOf(
            Entry(userProfile.collegeStart, "College", "Until ${PlannerEngine.formatTime12h(userProfile.collegeEnd)} · protected", true),
            Entry(userProfile.gymStart, "Gym", "Until ${PlannerEngine.formatTime12h(userProfile.gymEnd)} · protected", true),
            Entry(userProfile.callStart, "Evening call", "Protected for the rest of the evening", true))
        if (slot?.date == activeDate.toString() && action.actionType != "add-task")
            entries.add(Entry(slot.time24, action.title.replace("\n", " "), "${action.minutes} min · ${action.copy}", false, true))
        entries.sortedBy { PlannerEngine.time(it.time) ?: LocalTime.MAX }.forEach { entry ->
            Row(Modifier.fillMaxWidth().padding(bottom = 22.dp)) {
                Text(entry.time, Modifier.width(54.dp), color = EvolveDim, fontFamily = SpaceGroteskFontFamily, fontSize = 12.sp)
                Column(Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                    .background(if (entry.action) EvolveRaised else EvolveBlack)
                    .padding(if (entry.action) 16.dp else 0.dp)) {
                    if (entry.action) { Eyebrow("Next step", true); Spacer(Modifier.height(8.dp)) }
                    Text(entry.title, color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 17.sp)
                    Spacer(Modifier.height(5.dp))
                    Text(entry.copy, color = EvolveMuted, fontSize = 12.sp, lineHeight = 19.sp)
                    if (entry.action) TextButton(onClick = onOpenAction) { Text("Open this step ↗", color = EvolveAccent) }
                }
            }
        }
        if (slot == null) Text("No available block fits the current duration and deadline. Edit your study window or reduce the task.", color = EvolveWarm, fontSize = 13.sp, lineHeight = 21.sp)
        TextButton(onClick = onRescheduleClick) { Text("Find another time ↗", color = EvolveAccent) }
        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Eyebrow("Your work", true)
            TextButton(onClick = onAddTask) { Text("+ Add a step", color = EvolveAccent) }
        }
        if (tasks.isEmpty()) Text("Turn one actual question or topic into a small, executable step. Your plan grows from your own material.", color = EvolveMuted, fontSize = 14.sp, lineHeight = 23.sp)
        tasks.sortedWith(compareBy<AcademicTaskEntity> { it.completed }.thenBy { PlannerEngine.date(it.dueDate) ?: LocalDate.MAX }).forEach { task ->
            Row(Modifier.fillMaxWidth().clickable { onTaskClick(task.id) }.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (task.completed) "✓  ${task.title}" else task.title, color = if (task.completed) EvolveMuted else EvolveText, fontSize = 16.sp, fontFamily = SpaceGroteskFontFamily)
                    Spacer(Modifier.height(5.dp))
                    Text("${task.durationMinutes} min · ${if (task.dueDate.isBlank()) "Deadline unknown" else PlannerEngine.formatExamDate(task.dueDate)}", color = EvolveDim, fontSize = 12.sp)
                }
                TextButton(onClick = { onEditTask(task.id) }) { Text("Edit", color = EvolveAccent) }
            }
            HorizontalDivider(color = EvolveLine)
        }
        TextButton(onClick = onOpenSemester) { Text("My semester · choose a syllabus topic ↗", color = EvolveAccent) }
        Spacer(Modifier.height(28.dp)); EvolveAction("Add a study task  +", onAddTask)
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onOpenStudyClick) { Text("Explore the sample Python lesson ↗", color = EvolveMuted) }
        Text("One next block is suggested at a time. Other tasks stay in your queue; sample lessons do not become personal exam progress.", color = EvolveDim, fontSize = 11.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(120.dp))
    }
}
