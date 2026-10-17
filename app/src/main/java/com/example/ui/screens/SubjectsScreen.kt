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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ClassTipEntity
import com.example.data.model.SubjectEntity
import com.example.ui.components.EvolveField
import com.example.ui.components.Eyebrow
import com.example.ui.components.TopBar
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveSurface
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
    onSettingsClick: () -> Unit
) {
    val filteredSubjects = subjects.filter { sub ->
        searchQuery.isBlank() ||
            sub.name.contains(searchQuery, ignoreCase = true) ||
            sub.subtitle.contains(searchQuery, ignoreCase = true) ||
            allTips.any { it.subjectId == sub.id && it.text.contains(searchQuery, ignoreCase = true) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                TopBar(
                    isSubScreen = false,
                    onSettingsClick = onSettingsClick
                )

                Spacer(modifier = Modifier.height(24.dp))
                Eyebrow("Course Notebooks", accent = true)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Class notes",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 32.sp,
                    color = EvolveText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Structured knowledge base for college lectures, exam formulas, definitions, and class takeaways.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Search field
                EvolveField(
                    label = "",
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    hint = "Search topics, formulas, subjects…",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onCaptureTipClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("capture_tip_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EvolvePillBg,
                            contentColor = EvolvePillText
                        )
                    ) {
                        Icon(imageVector = Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Capture note", fontFamily = ManropeFontFamily, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onAddSubjectClick,
                        modifier = Modifier
                            .height(44.dp)
                            .testTag("add_subject_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EvolveLine)
                    ) {
                        Icon(imageVector = Icons.Outlined.Add, contentDescription = null, tint = EvolveMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "New course", color = EvolveText, fontFamily = ManropeFontFamily, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Eyebrow("Enrolled Courses (${filteredSubjects.size})")
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredSubjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(EvolveSurface)
                            .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No notes or subjects matched \"$searchQuery\"." else "No subjects registered.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            color = EvolveMuted
                        )
                    }
                }
            } else {
                items(filteredSubjects, key = { it.id }) { subject ->
                    val tipsForSubject = allTips.filter { it.subjectId == subject.id }
                    val pinnedCount = tipsForSubject.count { it.pinned }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EvolveSurface)
                            .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                            .clickable { onSubjectClick(subject.id) }
                            .padding(16.dp)
                            .testTag("subject_card_${subject.id}")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0x18B9A2F5))
                                        .border(1.dp, Color(0x33B9A2F5), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = subject.name.take(2).uppercase(),
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 14.sp,
                                        color = EvolveAccent
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = subject.name,
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 17.sp,
                                        color = EvolveText
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = subject.subtitle.ifBlank { "Course notes and takeaways" },
                                        fontFamily = ManropeFontFamily,
                                        fontSize = 11.sp,
                                        color = EvolveMuted
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (pinnedCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color(0x44B9A2F5), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$pinnedCount pinned",
                                            fontFamily = ManropeFontFamily,
                                            fontSize = 9.sp,
                                            color = EvolveAccent
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                Text(
                                    text = "${tipsForSubject.size} notes",
                                    fontFamily = ManropeFontFamily,
                                    fontSize = 11.sp,
                                    color = EvolveDim
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Outlined.ChevronRight,
                                    contentDescription = null,
                                    tint = EvolveDim,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
