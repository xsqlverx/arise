package com.example.ui.sheets

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassTipEntity
import com.example.data.model.UserProfileEntity
import com.example.domain.PlannerEngine
import com.example.domain.NextAction
import com.example.ui.components.EvolveField
import com.example.domain.RescheduleSlot
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveSurface
import com.example.ui.theme.EvolveText
import com.example.ui.theme.EvolveWarm
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun WhyFirstSheet(
    action: NextAction,
    onGetFirstBrief: () -> Unit,
    onClose: () -> Unit
) {
    BaseSheet(title = "Why this first?", onClose = onClose) {
        ReasonStep(number = "01", title = "The deciding factor", desc = action.copy)
        Spacer(modifier = Modifier.height(14.dp))
        ReasonStep(number = "02", title = "How to begin", desc = action.firstStep)
        Spacer(modifier = Modifier.height(14.dp))
        ReasonStep(number = "03", title = "Time to set aside", desc = action.duration)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onGetFirstBrief,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("get_first_brief_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EvolvePillBg, contentColor = EvolvePillText)
        ) {
            Text(text = action.cta, fontFamily = ManropeFontFamily, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun ReasonStep(number: String, title: String, desc: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = number, fontFamily = SpaceGroteskFontFamily, fontSize = 11.sp, color = EvolveAccent, modifier = Modifier.width(28.dp))
        Column {
            Text(text = title, fontFamily = ManropeFontFamily, fontSize = 13.sp, color = EvolveText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = desc, fontFamily = ManropeFontFamily, fontSize = 11.sp, lineHeight = 17.sp, color = EvolveMuted)
        }
    }
}

@Composable
fun RescheduleSheet(
    currentScheduledSlot: String,
    action: NextAction,
    availableSlots: List<RescheduleSlot>,
    onSelectSlot: (String) -> Unit,
    onOpenSetup: () -> Unit,
    onClose: () -> Unit
) {
    BaseSheet(title = "Give it a real time.", onClose = onClose) {
        Text(
            text = "${action.title.replace("\n", " ")} · ${action.minutes} minutes. Choose a future block in your study window.",
            fontFamily = ManropeFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = EvolveMuted
        )

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x0EB9A2F5))
                .border(1.dp, Color(0x33B9A2F5), RoundedCornerShape(10.dp))
                .padding(14.dp)
        ) {
            Column {
                Text(text = "THE TRADE-OFF", fontFamily = ManropeFontFamily, fontSize = 9.sp, letterSpacing = 1.sp, color = EvolveAccent)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (action.dueDate.isBlank()) "No confirmed deadline is available. Moving this step delays the next action; an exam consequence cannot be estimated yet." else "This step is due ${PlannerEngine.formatExamDate(action.dueDate)}. Only blocks that finish within that date are offered.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 17.sp,
                    color = Color(0xFFBBB3CC)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (availableSlots.isEmpty()) {
            Text(
                text = "No available block fits before your protected commitments.\nTry adjusting your availability.",
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                color = EvolveWarm
            )
            Spacer(modifier = Modifier.height(14.dp))
            OutlinedButton(
                onClick = onOpenSetup,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(15.dp)
            ) {
                Text(text = "Edit protected times", fontFamily = ManropeFontFamily, fontSize = 12.sp)
            }
        } else {
            availableSlots.forEach { slot ->
                val isSelected = currentScheduledSlot == slot.key
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectSlot(slot.key) }
                        .padding(vertical = 12.dp)
                        .testTag("slot_option_${slot.time24}"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = slot.label,
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            color = if (isSelected) EvolveAccent else EvolveText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = slot.copy, fontFamily = ManropeFontFamily, fontSize = 11.sp, color = EvolveMuted)
                    }
                    Icon(
                        imageVector = if (isSelected) Icons.Outlined.Check else Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        tint = if (isSelected) EvolveAccent else EvolveDim,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(EvolveLine))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Blocks use your study window, current time, duration, confirmed deadline and protected college/gym/call windows. One study block is planned at a time.",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = EvolveDim
        )
    }
}

@Composable
fun BriefCaptureSheet(
    subjectKey: String,
    briefText: String,
    dueDate: String,
    onDueDateChange: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onClose: () -> Unit
) {
    val title = if (subjectKey == "graphics") "Engineering graphics" else "Electrical & electronics"

    BaseSheet(title = "First, get the questions.", onClose = onClose) {
        Text(text = title, fontFamily = SpaceGroteskFontFamily, fontSize = 15.sp, color = EvolveAccent)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Check your class group, notebook or learning portal for the actual assignment sheet. Confirm the due date with it.",
            fontFamily = ManropeFontFamily,
            fontSize = 11.sp,
            lineHeight = 17.sp,
            color = EvolveMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x0EB9A2F5))
                .border(1.dp, Color(0x33B9A2F5), RoundedCornerShape(10.dp))
                .padding(12.dp)
        ) {
            Column {
                Text(text = "WHY THIS IS THE NEXT MOVE", fontFamily = ManropeFontFamily, fontSize = 9.sp, letterSpacing = 1.sp, color = EvolveAccent)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Until you have the questions, the scope is unknown. Capturing the brief makes the next study block specific.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = Color(0xFFBBB3CC)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(text = "Paste or type the assignment questions", fontFamily = ManropeFontFamily, fontSize = 11.sp, color = EvolveMuted)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0AFFFFFF))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            BasicTextField(
                value = briefText,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxSize().testTag("brief_text_input"),
                cursorBrush = SolidColor(EvolveAccent),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                    color = EvolveText
                ),
                decorationBox = { innerTextField ->
                    if (briefText.isEmpty()) {
                        Text(text = "The actual questions go here…", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = Color(0xFF8C8595))
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        EvolveField("Confirmed due date · YYYY-MM-DD · optional", dueDate, onDueDateChange, hint = "Leave blank if unknown")

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_brief_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EvolvePillBg, contentColor = EvolvePillText)
        ) {
            Text(text = "Save the brief", fontFamily = ManropeFontFamily, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun AddSubjectDialog(
    onAddSubject: (String) -> Unit,
    onClose: () -> Unit
) {
    var subjectName by remember { mutableStateOf("") }

    BaseSheet(title = "One more subject.", onClose = onClose) {
        Text(
            text = "Course name",
            fontFamily = ManropeFontFamily,
            fontSize = 11.sp,
            color = EvolveMuted
        )
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0AFFFFFF))
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = subjectName,
                onValueChange = { subjectName = it },
                modifier = Modifier.fillMaxWidth().testTag("add_subject_input"),
                singleLine = true,
                cursorBrush = SolidColor(EvolveAccent),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    color = EvolveText
                ),
                decorationBox = { innerTextField ->
                    if (subjectName.isEmpty()) {
                        Text(text = "Use the name on your timetable", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = Color(0xFF8C8595))
                    }
                    innerTextField()
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onAddSubject(subjectName) },
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_add_subject_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EvolvePillBg, contentColor = EvolvePillText)
        ) {
            Text(text = "+ Add subject", fontFamily = ManropeFontFamily, fontSize = 13.sp)
        }
    }
}

@Composable
fun DeleteTipDialog(
    tip: ClassTipEntity,
    onConfirmDelete: () -> Unit,
    onClose: () -> Unit
) {
    BaseSheet(title = "Delete this tip?", onClose = onClose) {
        Text(
            text = tip.text.take(150),
            fontFamily = ManropeFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = EvolveMuted
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirmDelete,
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("confirm_delete_tip_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF993333), contentColor = Color.White)
        ) {
            Text(text = "Delete tip", fontFamily = ManropeFontFamily, fontSize = 13.sp)
        }
    }
}

@Composable
fun UnsavedDraftDialog(
    onSaveDraft: () -> Unit,
    onKeepEditing: () -> Unit,
    onDiscardDraft: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        containerColor = EvolveSurface,
        title = {
            Text(text = "Keep your words?", fontFamily = SpaceGroteskFontFamily, fontSize = 20.sp, color = EvolveText)
        },
        text = {
            Text(
                text = "This tip has unsaved changes. Save it to its subject, keep editing, or discard this draft.",
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = EvolveMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onSaveDraft,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EvolvePillBg, contentColor = EvolvePillText)
            ) {
                Text(text = "Save this tip", fontFamily = ManropeFontFamily, fontSize = 12.sp)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDiscardDraft) {
                    Text(text = "Discard", fontFamily = ManropeFontFamily, fontSize = 12.sp, color = EvolveWarm)
                }
                TextButton(onClick = onKeepEditing) {
                    Text(text = "Keep editing", fontFamily = ManropeFontFamily, fontSize = 12.sp, color = EvolveMuted)
                }
            }
        }
    )
}

@Composable
fun SettingsSheet(
    profile: UserProfileEntity,
    onUpdateProfile: (UserProfileEntity) -> Unit,
    onOpenSemester: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onClearSamples: () -> Unit,
    onOpenSetup: () -> Unit,
    onClose: () -> Unit
) {
    BaseSheet(title = "Your space. Your rules.", onClose = onClose) {
        Text(
            text = "A few preferences for this native academic companion.",
            fontFamily = ManropeFontFamily,
            fontSize = 11.sp,
            color = EvolveMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onOpenSemester) { Text("My semester / import syllabus ↗", color = EvolveAccent) }
        // Reminders Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Study reminders", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = EvolveText)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Timely accountability without catch-up floods.", fontFamily = ManropeFontFamily, fontSize = 10.sp, color = EvolveMuted)
            }
            Switch(
                checked = profile.remindersEnabled,
                onCheckedChange = { onUpdateProfile(profile.copy(remindersEnabled = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = EvolveAccent,
                    checkedTrackColor = Color(0x33B9A2F5),
                    uncheckedThumbColor = Color(0xFF706A7B),
                    uncheckedTrackColor = Color(0xFF29262F)
                ),
                modifier = Modifier.testTag("reminders_toggle")
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(EvolveLine))

        // Reduce Motion Toggle
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Reduce motion", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = EvolveText)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Keep transitions quiet and immediate.", fontFamily = ManropeFontFamily, fontSize = 10.sp, color = EvolveMuted)
            }
            Switch(
                checked = profile.reducedMotion,
                onCheckedChange = { onUpdateProfile(profile.copy(reducedMotion = it)) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = EvolveAccent,
                    checkedTrackColor = Color(0x33B9A2F5),
                    uncheckedThumbColor = Color(0xFF706A7B),
                    uncheckedTrackColor = Color(0xFF29262F)
                ),
                modifier = Modifier.testTag("reduce_motion_toggle")
            )
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(EvolveLine))

        Spacer(modifier = Modifier.height(10.dp))

        // Your Starting Point (Setup)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenSetup() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Your starting point", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = EvolveText)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Exam date & protected commitments", fontFamily = ManropeFontFamily, fontSize = 10.sp, color = EvolveMuted)
            }
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = EvolveDim, modifier = Modifier.size(16.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(EvolveLine))

        // Export Local Backup
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExportBackup() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Export a local backup", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = EvolveText)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Your notes, profile & preview results (JSON)", fontFamily = ManropeFontFamily, fontSize = 10.sp, color = EvolveMuted)
            }
            Icon(imageVector = Icons.Outlined.Download, contentDescription = null, tint = EvolveDim, modifier = Modifier.size(16.dp))
        }

        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(EvolveLine))

        TextButton(onClick = onImportBackup) {
            Text("Import a JSON backup ↗", color = EvolveAccent, fontFamily = ManropeFontFamily)
        }
        Text("Choose a block with Find another time to schedule reminders. Delivery is approximate; permission, battery settings and protected windows affect it.",
            color = EvolveDim, fontSize = 11.sp, lineHeight = 18.sp)
        Spacer(Modifier.height(16.dp))

        // Clear Example Notes
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClearSamples() }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Remove example class notes", fontFamily = ManropeFontFamily, fontSize = 13.sp, color = EvolveText)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Keep only the notes you wrote yourself", fontFamily = ManropeFontFamily, fontSize = 10.sp, color = EvolveMuted)
            }
            Icon(imageVector = Icons.Outlined.StickyNote2, contentDescription = null, tint = EvolveDim, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Evolve Academic Companion. All data stays strictly on-device.",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            color = EvolveDim
        )
    }
}

@Composable
fun BaseSheet(
    title: String,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    EvolveSheet(title, onClose) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) { content() }
    }
}
