package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun SubjectNotesScreen(
    subject: SubjectEntity,
    tips: List<ClassTipEntity>,
    searchQuery: String,
    filterCategory: String,
    onSearchChange: (String) -> Unit,
    onFilterChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onAddTipClick: () -> Unit,
    onEditTipClick: (String) -> Unit,
    onDeleteTipClick: (ClassTipEntity) -> Unit,
    onTogglePinClick: (ClassTipEntity) -> Unit,
    onSettingsClick: () -> Unit
) {
    val categories = listOf("All", "Concept", "Formula", "Exam Tip", "Definition", "Takeaway")

    val filteredTips = tips.filter { tip ->
        val matchesCategory = filterCategory.equals("All", ignoreCase = true) ||
            tip.kind.equals(filterCategory, ignoreCase = true)
        val matchesSearch = searchQuery.isBlank() || tip.text.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }.sortedWith(compareByDescending<ClassTipEntity> { it.pinned }.thenByDescending { it.createdAt })

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
                    isSubScreen = true,
                    backTitle = "Courses",
                    onBack = onBackClick,
                    onSettingsClick = onSettingsClick
                )

                Spacer(modifier = Modifier.height(24.dp))
                Eyebrow("Notebook", accent = true)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subject.name,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 32.sp,
                    color = EvolveText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subject.subtitle.ifBlank { "Subject repository and reference notes." },
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
                    hint = "Search in ${subject.name}…",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Filter chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = (filterCategory.isBlank() && cat == "All") || filterCategory.equals(cat, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0x33B9A2F5) else Color(0x0AFFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) EvolveAccent else EvolveLine,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { onFilterChange(if (cat == "All") "" else cat) }
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

                // Add note action
                Button(
                    onClick = onAddTipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("add_note_to_subject_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EvolvePillBg,
                        contentColor = EvolvePillText
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Add note to ${subject.name}", fontFamily = ManropeFontFamily, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))
                Eyebrow("Notes (${filteredTips.size})")
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (filteredTips.isEmpty()) {
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
                            text = if (searchQuery.isNotBlank() || filterCategory.isNotBlank())
                                "No notes found matching your filter."
                            else "No notes captured yet for ${subject.name}. Tap above to add one.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            color = EvolveMuted
                        )
                    }
                }
            } else {
                items(filteredTips, key = { it.id }) { tip ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(EvolveSurface)
                            .border(
                                1.dp,
                                if (tip.pinned) Color(0x66B9A2F5) else EvolveLine,
                                RoundedCornerShape(16.dp)
                            )
                            .padding(16.dp)
                            .testTag("note_card_${tip.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .border(1.dp, Color(0x33B9A2F5), RoundedCornerShape(6.dp))
                                            .background(Color(0x12B9A2F5))
                                            .padding(horizontal = 7.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = tip.kind.uppercase(),
                                            fontFamily = ManropeFontFamily,
                                            fontSize = 9.sp,
                                            letterSpacing = 1.sp,
                                            color = EvolveAccent
                                        )
                                    }
                                    if (tip.date.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = tip.date,
                                            fontFamily = ManropeFontFamily,
                                            fontSize = 10.sp,
                                            color = EvolveDim
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onTogglePinClick(tip) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.PushPin,
                                            contentDescription = if (tip.pinned) "Unpin" else "Pin",
                                            tint = if (tip.pinned) EvolveAccent else EvolveDim,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEditTipClick(tip.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Edit,
                                            contentDescription = "Edit note",
                                            tint = EvolveDim,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteTipClick(tip) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.DeleteOutline,
                                            contentDescription = "Delete note",
                                            tint = EvolveDim,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = tip.text,
                                fontFamily = ManropeFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = EvolveText
                            )
                        }
                    }
                }
            }
        }
    }
}
