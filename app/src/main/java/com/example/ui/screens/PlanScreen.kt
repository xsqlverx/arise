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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.domain.PlannerEngine
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
fun PlanScreen(
    userProfile: UserProfileEntity,
    activeDate: Int,
    onSelectDate: (Int) -> Unit,
    onRescheduleClick: () -> Unit,
    onOpenBriefClick: (String) -> Unit,
    onOpenStudyClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isFriday16 = activeDate == 16
    val daysToExam = PlannerEngine.calculateDaysToExam(userProfile.examDate)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        TopBar(onSettingsClick = onSettingsClick)

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR TIME, CONSIDERED",
                fontFamily = ManropeFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                color = EvolveMuted
            )
            Text(
                text = "OCT 2026",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Make space",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveText
        )
        Text(
            text = "for progress.",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveAccent
        )

        Spacer(modifier = Modifier.height(18.dp))

        // 7-day compact date strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dayLetters = listOf("M", "T", "W", "T", "F", "S", "S")
            for (i in 0 until 7) {
                val dayNum = 12 + i
                val isSelected = activeDate == dayNum

                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(if (isSelected) EvolvePillBg else Color.Transparent)
                        .clickable { onSelectDate(dayNum) }
                        .padding(horizontal = 11.dp, vertical = 10.dp)
                        .testTag("date_strip_day_$dayNum"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLetters[i],
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        color = if (isSelected) EvolvePillText else EvolveDim
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dayNum.toString(),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 14.sp,
                        color = if (isSelected) EvolvePillText else EvolveDim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isFriday16) "Today · Friday 16" else "Preview · $activeDate October",
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = EvolveMuted
            )
            Text(
                text = "Proposed schedule",
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isFriday16) {
            // Empty state for other days
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = EvolveAccent,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A little breathing room.",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 20.sp,
                    color = EvolveText
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "No study blocks on this preview day.\nChoose Friday 16 to explore the sample plan.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = EvolveMuted
                )
            }
        } else {
            // Full chronological timeline
            Column(modifier = Modifier.fillMaxWidth()) {
                // College
                TimelineSlot(
                    time = userProfile.collegeStart,
                    title = "College",
                    subtitle = "Until ${PlannerEngine.formatTime12h(userProfile.collegeEnd)} · protected",
                    isLocked = true,
                    tag = null
                )

                // Buffer
                TimelineSlot(
                    time = userProfile.collegeEnd,
                    title = "Land. Eat. Reset.",
                    subtitle = "A realistic buffer after college.",
                    isLocked = false,
                    tag = "OPEN SPACE"
                )

                // Current Priority Slot
                TimelineSlot(
                    time = userProfile.scheduledSlot,
                    title = "Get the assignment questions",
                    subtitle = "Capture both briefs. Find out what the work actually involves.",
                    isLocked = false,
                    isCurrent = true,
                    tag = "FIRST PRIORITY",
                    actionLabel = "Open this step ↗",
                    onActionClick = { onOpenBriefClick("graphics") }
                )

                // Study Block
                TimelineSlot(
                    time = "16:00",
                    title = "Python · one concept",
                    subtitle = "25 min guided practice + a short check.",
                    isLocked = false,
                    tag = "SAMPLE STUDY BLOCK",
                    actionLabel = "Open this step ↗",
                    onActionClick = onOpenStudyClick
                )

                // Assignment Work
                TimelineSlot(
                    time = "17:30",
                    title = "Assignment work",
                    subtitle = "Confirm questions and scope before estimating this block.",
                    isLocked = false,
                    tag = "NEEDS THE BRIEF"
                )

                // Gym
                TimelineSlot(
                    time = userProfile.gymStart,
                    title = "Gym",
                    subtitle = "Until ${PlannerEngine.formatTime12h(userProfile.gymEnd)} · protected",
                    isLocked = true,
                    tag = null
                )

                // Evening Call
                TimelineSlot(
                    time = userProfile.callStart,
                    title = "Your evening call",
                    subtitle = "Flexible start · protected time",
                    isLocked = true,
                    tag = null,
                    isLast = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onRescheduleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("move_task_button"),
                shape = RoundedCornerShape(15.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, EvolveLine),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EvolveText
                )
            ) {
                Text(
                    text = "Move the first task",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        // Coming up section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Coming up",
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = EvolveText
            )
            Text(
                text = "$daysToExam days to the exam",
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        DeadlineRow(
            title = "Engineering graphics",
            subtitle = if (userProfile.graphicsBrief.isNotBlank()) "Assignment · Questions captured" else "Assignment · Questions needed",
            tag = "18 Oct · confirm",
            icon = Icons.Outlined.Architecture,
            onClick = { onOpenBriefClick("graphics") }
        )

        DeadlineRow(
            title = "Electrical & electronics",
            subtitle = if (userProfile.electricalBrief.isNotBlank()) "Assignment · Questions captured" else "Assignment · Questions needed",
            tag = "18 Oct · confirm",
            icon = Icons.Outlined.ElectricBolt,
            onClick = { onOpenBriefClick("electrical") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Friday is an illustrative plan. Dates, durations and available slots need confirmation before a real schedule is generated.",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = EvolveDim
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun TimelineSlot(
    time: String,
    title: String,
    subtitle: String,
    isLocked: Boolean,
    tag: String?,
    isCurrent: Boolean = false,
    isLast: Boolean = false,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 8.dp else 18.dp)
    ) {
        // Time label
        Text(
            text = time,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            color = EvolveDim,
            modifier = Modifier
                .width(46.dp)
                .padding(top = 2.dp)
        )

        // Dot & connecting line
        Box(
            modifier = Modifier
                .width(20.dp)
                .padding(top = 6.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(if (isCurrent) 8.dp else 6.dp)
                    .background(if (isCurrent) EvolveAccent else Color(0xFF55515F), CircleShape)
            )
        }

        // Slot content
        if (isCurrent) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(17.dp))
                    .background(Color(0x0EB79FE9))
                    .border(1.dp, Color(0x33B79FE9), RoundedCornerShape(17.dp))
                    .padding(14.dp)
            ) {
                Column {
                    if (tag != null) {
                        Text(
                            text = tag,
                            fontFamily = ManropeFontFamily,
                            fontSize = 8.sp,
                            letterSpacing = 1.sp,
                            color = EvolveAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = title,
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp,
                        color = EvolveText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 16.sp,
                        color = EvolveMuted
                    )
                    if (actionLabel != null && onActionClick != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = onActionClick,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(
                                text = actionLabel,
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                color = EvolveAccent
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp)
            ) {
                if (tag != null) {
                    Text(
                        text = tag,
                        fontFamily = ManropeFontFamily,
                        fontSize = 8.sp,
                        letterSpacing = 1.sp,
                        color = EvolveAccent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLocked) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Protected",
                            tint = EvolveDim,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = title,
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp,
                        color = if (isLocked) EvolveMuted else EvolveText
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 15.sp,
                    color = EvolveMuted
                )
                if (actionLabel != null && onActionClick != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    TextButton(
                        onClick = onActionClick,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = EvolveAccent
                        )
                    }
                }
            }
        }
    }
}
