package com.example.ui.sheets

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubjectEntity
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun NoteCaptureSheet(isEditing: Boolean, tipText: String, category: String, selectedSubjectId: String,
    subjects: List<SubjectEntity>, onTextChange: (String) -> Unit, onCategoryChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit, onSaveClick: () -> Unit, onCloseClick: () -> Unit,
    isSaving: Boolean = false, modifier: Modifier = Modifier) {
    EvolveSheet(if (isEditing) "Edit class tip" else "Keep this detail", onCloseClick,
        footer = { EvolveAction(if (isSaving) "Saving…" else "Save class tip", onSaveClick,
            Modifier.testTag("save_tip_button"), !isSaving) }) {
        Column(modifier.verticalScroll(rememberScrollState())) {
            Text("Your teacher’s exact words. Line breaks, spaces and indentation stay as you wrote them.",
                color = EvolveMuted, fontFamily = ManropeFontFamily, fontSize = 13.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(24.dp))
            ChoiceStrip("Subject", subjects.map { it.id to it.name }, selectedSubjectId, { if (!isSaving) onSubjectChange(it) })
            Spacer(Modifier.height(18.dp))
            EvolveField("What do you want to remember?", tipText, onTextChange, multiline = true,
                hint = "Write the tip here…", enabled = !isSaving)
            Eyebrow("${tipText.length} characters · draft kept on this device")
            Spacer(Modifier.height(24.dp))
            ChoiceStrip("Keep it under", (listOf("Answer writing", "Exam tips", "Concepts", "General") + category).distinct().map { it to it }, category,
                { if (!isSaving) onCategoryChange(it) })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun ChoiceStrip(label: String, choices: List<Pair<String, String>>, selected: String, onSelect: (String) -> Unit) {
    Eyebrow(label)
    Spacer(Modifier.height(9.dp))
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEach { (id, name) ->
            val active = id == selected
            Text(name, color = if (active) EvolveBlack else EvolveMuted, fontFamily = ManropeFontFamily, fontSize = 12.sp,
                modifier = Modifier.clip(RoundedCornerShape(24.dp)).background(if (active) EvolvePillBg else EvolveRaised)
                    .clickable { onSelect(id) }.defaultMinSize(minHeight = 48.dp).padding(horizontal = 16.dp, vertical = 15.dp))
        }
    }
}
