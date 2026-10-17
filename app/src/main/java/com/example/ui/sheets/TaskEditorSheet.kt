package com.example.ui.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.AcademicTaskEntity
import com.example.data.model.SubjectEntity
import com.example.ui.components.*
import java.util.UUID

@Composable
fun TaskEditorSheet(task: AcademicTaskEntity?, subjects: List<SubjectEntity>, saving: Boolean,
    onSave: (AcademicTaskEntity) -> Unit, onClose: () -> Unit) {
    var id by rememberSaveable { mutableStateOf(task?.id ?: "task-${UUID.randomUUID()}") }
    var title by rememberSaveable { mutableStateOf(task?.title ?: "") }
    var instructions by rememberSaveable { mutableStateOf(task?.instructions ?: "") }
    var subject by rememberSaveable { mutableStateOf(task?.subjectId ?: subjects.firstOrNull()?.id.orEmpty()) }
    var duration by rememberSaveable { mutableStateOf((task?.durationMinutes ?: 25).toString()) }
    var due by rememberSaveable { mutableStateOf(task?.dueDate.orEmpty()) }
    EvolveSheet(if (task == null) "Make the next step clear." else "Refine this step.", onClose,
        footer = { EvolveAction(if (saving) "Saving…" else "Save this step", {
            onSave(AcademicTaskEntity(id, subject, title.trim(), instructions, due.trim(), duration.toIntOrNull() ?: 0,
                task?.completed ?: false, task?.createdAt ?: System.currentTimeMillis()))
        }, enabled = !saving) }) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ChoiceStrip("Subject", subjects.map { it.id to it.name }, subject, { subject = it })
            Spacer(Modifier.height(20.dp))
            EvolveField("One specific piece of work", title, { title = it }, hint = "e.g. Attempt question 1 from my assignment", enabled = !saving)
            EvolveField("How will you start?", instructions, { instructions = it }, multiline = true,
                hint = "Actual question, notebook reference, or first step…", enabled = !saving)
            EvolveField("Focus block · minutes (5–120)", duration, { duration = it }, enabled = !saving)
            EvolveField("Due date · YYYY-MM-DD · optional", due, { due = it }, hint = "Leave blank if unknown", enabled = !saving)
        }
    }
}
