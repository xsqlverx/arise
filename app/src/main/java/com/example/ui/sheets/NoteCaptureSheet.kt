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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@Composable
fun NoteCaptureSheet(
    isEditing: Boolean,
    isSaving: Boolean,
    tipText: String,
    category: String,
    selectedSubjectId: String,
    subjects: List<SubjectEntity>,
    onTextChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    val categories = listOf("Concept", "Formula", "Exam Tip", "Definition", "Takeaway")

    EvolveSheet(
        title = if (isEditing) "Edit note" else "Capture note",
        onClose = onCloseClick,
        footer = {
            Button(
                onClick = onSaveClick,
                enabled = !isSaving && tipText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_note_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvolvePillBg,
                    contentColor = EvolvePillText,
                    disabledContainerColor = Color(0x33E8E2F3),
                    disabledContentColor = Color(0x5517131D)
                )
            ) {
                Text(
                    text = if (isSaving) "Saving…" else if (isEditing) "Update note" else "Save to notebook",
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
            Eyebrow("Subject Notebook")
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
                            .clickable { onSubjectChange(sub.id) }
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
            Eyebrow("Note Category")
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0x33B9A2F5) else Color(0x0AFFFFFF))
                            .border(1.dp, if (isSelected) EvolveAccent else EvolveLine, RoundedCornerShape(10.dp))
                            .clickable { onCategoryChange(cat) }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = cat,
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = if (isSelected) EvolveText else EvolveMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            EvolveField(
                label = "Note Content",
                value = tipText,
                onValueChange = onTextChange,
                hint = "Type key formula, exam takeaway, theorem, or definition…",
                multiline = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
