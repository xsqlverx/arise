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
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.example.data.model.SubjectEntity
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveRaised
import com.example.ui.theme.EvolveSurface
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun NoteCaptureSheet(
    isEditing: Boolean,
    tipText: String,
    category: String,
    selectedSubjectId: String,
    subjects: List<SubjectEntity>,
    onTextChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var isSubjectDropdownOpen by remember { mutableStateOf(false) }
    var isCategoryDropdownOpen by remember { mutableStateOf(false) }

    val currentSubject = subjects.find { it.id == selectedSubjectId } ?: subjects.firstOrNull()
    val categories = listOf("Answer writing", "Exam tips", "Concepts", "General")

    // Full screen overlay that hugs keyboard with imePadding and status/nav insets
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(onClick = onCloseClick)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(EvolveSurface)
                .border(1.dp, EvolveLine, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .clickable(enabled = false) {} // Prevent backdrop click through
                .statusBarsPadding()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Sheet Handle
            Box(
                modifier = Modifier
                    .size(width = 34.dp, height = 3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF48434F))
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Sheet Heading
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit class tip" else "Keep this detail",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 24.sp,
                    color = EvolveText
                )

                IconButton(
                    onClick = onCloseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, EvolveLine, CircleShape)
                        .testTag("close_note_editor_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = EvolveMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = "A teacher’s tip, an answer-writing rule, a reminder for the exam. Your exact words are enough.",
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                lineHeight = 17.sp,
                color = EvolveMuted
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
            ) {
                // Subject Dropdown
                Text(
                    text = "Subject",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .clickable { isSubjectDropdownOpen = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = currentSubject?.name ?: "Select subject",
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp,
                        color = EvolveText
                    )

                    DropdownMenu(
                        expanded = isSubjectDropdownOpen,
                        onDismissRequest = { isSubjectDropdownOpen = false },
                        modifier = Modifier.background(EvolveRaised)
                    ) {
                        subjects.forEach { s ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = s.name,
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 13.sp,
                                        color = EvolveText
                                    )
                                },
                                onClick = {
                                    onSubjectChange(s.id)
                                    isSubjectDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Input area (preserving exact newlines, spaces, indentation)
                Text(
                    text = "What do you want to remember?",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    BasicTextField(
                        value = tipText,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("note_text_input"),
                        cursorBrush = SolidColor(EvolveAccent),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = EvolveText
                        ),
                        decorationBox = { innerTextField ->
                            if (tipText.isEmpty()) {
                                Text(
                                    text = "Write the tip here. Line breaks and indentation are preserved.",
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF8C8595)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Plain text · Up to 5,000 characters",
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        color = EvolveDim
                    )
                    Text(
                        text = "${tipText.length} / 5000",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 10.sp,
                        color = EvolveDim
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = "Keep it under",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                        .clickable { isCategoryDropdownOpen = true }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = category,
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp,
                        color = EvolveText
                    )

                    DropdownMenu(
                        expanded = isCategoryDropdownOpen,
                        onDismissRequest = { isCategoryDropdownOpen = false },
                        modifier = Modifier.background(EvolveRaised)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = cat,
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 13.sp,
                                        color = EvolveText
                                    )
                                },
                                onClick = {
                                    onCategoryChange(cat)
                                    isCategoryDropdownOpen = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Pinned Footer above keyboard
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_tip_button"),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvolvePillBg,
                    contentColor = EvolvePillText
                )
            ) {
                Text(
                    text = "Save class tip",
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
