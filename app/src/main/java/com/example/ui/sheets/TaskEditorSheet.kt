package com.example.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AcademicTaskEntity
import com.example.data.model.SubjectEntity
import com.example.ui.components.EvolveField
import com.example.ui.components.EvolveSheet
import com.example.ui.components.Eyebrow
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily
import java.util.UUID

@Composable
fun TaskEditorSheet(
    task: AcademicTaskEntity?,
    subjects: List<SubjectEntity>,
    isSaving: Boolean,
    onSave: (AcademicTaskEntity) -> Unit,
    onClose: () -> Unit
) {
    var title by remember { mutableStateOf(task?.title.orEmpty()) }
    var instructions by remember { mutableStateOf(task?.instructions.orEmpty()) }
    var selectedSubjectId by remember {
        mutableStateOf(task?.subjectId ?: subjects.firstOrNull()?.id ?: "python")
    }
    var durationMinutes by remember { mutableIntStateOf(task?.durationMinutes ?: 25) }
    var dueDate by remember { mutableStateOf(task?.dueDate.orEmpty()) }

    val durationOptions = listOf(15, 25, 40, 60)

    EvolveSheet(
        title = if (task != null) "Edit practice task" else "New practice task",
        onClose = onClose,
        footer = {
            Button(
                onClick = {
                    val toSave = task?.copy(
                        title = title.trim(),
                        instructions = instructions.trim(),
                        subjectId = selectedSubjectId,
                        durationMinutes = durationMinutes,
                        dueDate = dueDate.trim()
                    ) ?: AcademicTaskEntity(
                        id = UUID.randomUUID().toString(),
                        title = title.trim(),
                        instructions = instructions.trim(),
                        subjectId = selectedSubjectId,
                        durationMinutes = durationMinutes,
                        dueDate = dueDate.trim(),
                        completed = false
                    )
                    onSave(toSave)
                },
                enabled = !isSaving && title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_task_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvolvePillBg,
                    contentColor = EvolvePillText,
                    disabledContainerColor = Color(0x33E8E2F3),
                    disabledContentColor = Color(0x5517131D)
                )
            ) {
                Text(
                    text = if (isSaving) "Saving…" else if (task != null) "Update task" else "Add to plan",
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Eyebrow("Subject")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                subjects.forEach { sub ->
                    val isSelected = sub.id == selectedSubjectId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x33B9A2F5) else Color(0x0AFFFFFF))
                            .border(1.dp, if (isSelected) EvolveAccent else EvolveLine, RoundedCornerShape(10.dp))
                            .clickable { selectedSubjectId = sub.id }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = sub.name,
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = if (isSelected) EvolveText else EvolveMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            EvolveField(
                label = "Task Title",
                value = title,
                onValueChange = { title = it },
                hint = "e.g. Worked Problem: Matrix Eigenvalues",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))
            EvolveField(
                label = "Instructions / Page Target",
                value = instructions,
                onValueChange = { instructions = it },
                hint = "Open textbook pg 142 or syllabus problem set 3…",
                multiline = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(18.dp))
            Eyebrow("Target Duration")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                durationOptions.forEach { mins ->
                    val isSelected = durationMinutes == mins
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x33B9A2F5) else Color(0x0AFFFFFF))
                            .border(1.dp, if (isSelected) EvolveAccent else EvolveLine, RoundedCornerShape(10.dp))
                            .clickable { durationMinutes = mins }
                            .padding(vertical = 10.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        Text(
                            text = "$mins min",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = if (isSelected) EvolveText else EvolveMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            EvolveField(
                label = "Target Due Date (Optional, YYYY-MM-DD)",
                value = dueDate,
                onValueChange = { dueDate = it },
                hint = "e.g. 2026-10-28",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}
