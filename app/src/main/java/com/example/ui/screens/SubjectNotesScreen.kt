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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val q = searchQuery.trim().lowercase()
    val filteredTips = tips.filter { t ->
        val matchesFilter = filterCategory == "All" || t.kind.equals(filterCategory, ignoreCase = true)
        val matchesQuery = q.isEmpty() || t.text.lowercase().contains(q)
        matchesFilter && matchesQuery
    }.sortedWith(compareByDescending<ClassTipEntity> { it.pinned }.thenByDescending { it.createdAt })

    val categories = listOf("All", "Answer writing", "Exam tips", "Concepts", "General")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .padding(horizontal = 24.dp)
    ) {
        TopBar(
            isSubScreen = true,
            backTitle = "Subjects",
            onBack = onBackClick,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                // Subject Mark
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1CBBA2ED))
                        .border(1.dp, Color(0x33BBA2ED), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getSubjectIcon(subject.icon),
                        contentDescription = null,
                        tint = EvolveAccent,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CLASS NOTES / ${subject.subtitle.uppercase()}",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = subject.name,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 34.sp,
                    color = EvolveText
                )

                Text(
                    text = "Your teacher’s advice, kept close.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tips.size} saved ${if (tips.size == 1) "note" else "notes"}",
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        color = EvolveMuted
                    )
                    TextButton(onClick = onAddTipClick, modifier = Modifier.testTag("add_tip_header_button")) {
                        Text(
                            text = "+ Add a tip",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = EvolveAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Category chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = filterCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(25.dp))
                                .background(if (isSelected) Color(0x2BD9C6FF) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0x55BBA2ED) else EvolveLine,
                                    RoundedCornerShape(25.dp)
                                )
                                .clickable { onFilterChange(cat) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("filter_chip_$cat")
                        ) {
                            Text(
                                text = cat,
                                fontFamily = ManropeFontFamily,
                                fontSize = 10.sp,
                                color = if (isSelected) Color(0xFFD5C2FC) else EvolveMuted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notebook Search
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
                                .testTag("note_search_input"),
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
                                        text = "Search this notebook",
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

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (filteredTips.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.StickyNote2,
                            contentDescription = null,
                            tint = EvolveAccent,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || filterCategory != "All") "No matching notes." else "A place for the details.",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            color = EvolveText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (searchQuery.isNotBlank() || filterCategory != "All") "Try another filter or search." else "Capture the next useful thing your teacher says.\nA sentence is a perfectly good start.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            color = EvolveMuted
                        )
                    }
                }
            } else {
                items(filteredTips, key = { it.id }) { tip ->
                    ClassTipCard(
                        tip = tip,
                        onPinClick = { onTogglePinClick(tip) },
                        onEditClick = { onEditTipClick(tip.id) },
                        onDeleteClick = { onDeleteTipClick(tip) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onAddTipClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("capture_tip_notebook_button"),
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
                    text = "Short notes are enough. Keep line breaks, code indentation, or the teacher’s exact wording.",
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
fun ClassTipCard(
    tip: ClassTipEntity,
    onPinClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left border line with dot
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .padding(top = 4.dp),
                contentAlignment = Alignment.TopStart
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(EvolveAccent, CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        width = 1.dp,
                        color = Color(0x33B9A2F5),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color(0x08FFFFFF), RoundedCornerShape(12.dp))
                    .padding(14.dp)
                    .testTag("tip_item_${tip.id}")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = tip.kind.uppercase(),
                        fontFamily = ManropeFontFamily,
                        fontSize = 9.sp,
                        letterSpacing = 1.2.sp,
                        color = EvolveAccent
                    )

                    Row(
                        modifier = Modifier
                            .clickable { onPinClick() }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (tip.pinned) {
                            Icon(
                                imageVector = Icons.Outlined.PushPin,
                                contentDescription = "Pinned",
                                tint = EvolveAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = "Pin",
                                fontFamily = ManropeFontFamily,
                                fontSize = 10.sp,
                                color = EvolveDim
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Preserves exact whitespace and indentation!
                Text(
                    text = tip.text,
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 22.sp,
                    color = EvolveText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${tip.date} · ${if (tip.isSample) "Example note" else "Your note"}",
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        color = EvolveDim
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(28.dp).testTag("edit_tip_${tip.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                tint = EvolveMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(28.dp).testTag("delete_tip_${tip.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = "Delete",
                                tint = EvolveMuted,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getSubjectIcon(name: String): ImageVector {
    return when (name) {
        "electrical" -> Icons.Outlined.ElectricBolt
        "code" -> Icons.Outlined.Code
        "graphics" -> Icons.Outlined.Architecture
        "chemistry" -> Icons.Outlined.Science
        "math" -> Icons.Outlined.Functions
        else -> Icons.Outlined.StickyNote2
    }
}
