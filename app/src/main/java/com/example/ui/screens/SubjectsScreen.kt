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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassTipEntity
import com.example.data.model.SubjectEntity
import com.example.ui.components.TopBar
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun SubjectsScreen(
    subjects: List<SubjectEntity>,
    allTips: List<ClassTipEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSubjectClick: (String) -> Unit,
    onAddSubjectClick: () -> Unit,
    onCaptureTipClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val q = searchQuery.trim().lowercase()
    val filteredSubjects = subjects.filter { s ->
        s.name.lowercase().contains(q) || allTips.any { it.subjectId == s.id && it.text.lowercase().contains(q) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .padding(horizontal = 24.dp)
    ) {
        TopBar(onSettingsClick = onSettingsClick)

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "YOUR POCKET NOTEBOOK",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.8.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Keep what",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    color = EvolveText
                )
                Text(
                    text = "class gives you.",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    color = EvolveAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Teacher tips. Exam advice. The little details\nthat make a difference when you write.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Search field
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(1.dp, EvolveLine, RoundedCornerShape(13.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = EvolveDim,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("subject_search_input"),
                            singleLine = true,
                            cursorBrush = SolidColor(EvolveAccent),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = ManropeFontFamily,
                                fontSize = 12.sp,
                                color = EvolveText
                            ),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Find a subject or a note",
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 12.sp,
                                        color = EvolveDim
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBJECTS",
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.6.sp,
                        color = EvolveMuted
                    )
                    TextButton(onClick = onAddSubjectClick, modifier = Modifier.testTag("add_subject_button")) {
                        Text(
                            text = "+ Add subject",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = EvolveAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            if (filteredSubjects.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null,
                            tint = EvolveAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No matches yet.",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            color = EvolveText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try a subject name or a word from a note.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            color = EvolveMuted
                        )
                    }
                }
            } else {
                itemsIndexed(filteredSubjects) { index, sub ->
                    val noteCount = allTips.count { it.subjectId == sub.id }
                    SubjectRowItem(
                        index = index + 1,
                        subject = sub,
                        noteCount = noteCount,
                        onClick = { onSubjectClick(sub.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onCaptureTipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("capture_tip_button"),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EvolvePillBg,
                        contentColor = EvolvePillText
                    )
                ) {
                    Text(
                        text = "+ Capture a class tip",
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Example notes illustrate your requested writing conventions. Replace them with your teachers’ actual advice.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    color = EvolveDim
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
fun SubjectRowItem(
    index: Int,
    subject: SubjectEntity,
    noteCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp)
            .testTag("subject_row_${subject.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = String.format("%02d", index),
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            color = EvolveDim,
            modifier = Modifier.width(28.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subject.name,
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 18.sp,
                color = EvolveText
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subject.subtitle,
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = EvolveMuted
            )
        }

        if (noteCount > 0) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0x33BBA5F5), RoundedCornerShape(6.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = noteCount.toString(),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 11.sp,
                    color = EvolveAccent
                )
            }
        } else {
            Text(
                text = "—",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 12.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            tint = EvolveDim,
            modifier = Modifier.size(16.dp)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(EvolveLine)
    )
}
