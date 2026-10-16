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
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.ui.components.Eyebrow
import com.example.domain.NextAction
import com.example.domain.PlannerEngine
import com.example.ui.components.ContourLandscape
import com.example.ui.components.TopBar
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveCardGradientEnd
import com.example.ui.theme.EvolveCardGradientStart
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveText
import com.example.ui.theme.EvolveWarm
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun TodayScreen(
    userProfile: UserProfileEntity,
    nextAction: NextAction,
    now: LocalDateTime,
    onNextActionClick: (String) -> Unit,
    onWhyFirstClick: () -> Unit,
    onRescheduleClick: () -> Unit,
    onOpenPlanClick: () -> Unit,
    onOpenSubjectsClick: () -> Unit,
    onOpenBriefClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val daysToExam = PlannerEngine.calculateDaysToExam(userProfile.examDate)
    val examDateFormatted = PlannerEngine.formatExamDate(userProfile.examDate)

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
                text = now.toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)).uppercase(),
                fontFamily = ManropeFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                color = EvolveMuted
            )
            Text(
                text = "S1 / CSE",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A clear path.",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveText
        )
        Text(
            text = "From here.",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveAccent
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Contour Hero Area with Exam Counter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(205.dp)
        ) {
            ContourLandscape(
                modifier = Modifier.matchParentSize(),
                height = 205.dp
            )

            // Exam countdown top right
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 6.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = daysToExam?.let { if (it >= 0) it.toString().padStart(2, '0') else "Past" } ?: "—",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 36.sp,
                    color = EvolveText
                )
                Text(
                    text = if (daysToExam == null) "Confirm your\nnext exam date" else if (daysToExam < 0) "Review your\nexam date" else "days to $examDateFormatted\nyour next exam",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    color = EvolveMuted
                )
            }

            // Terrain label bottom left
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(EvolveAccent, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "01 / FIND YOUR FOOTING",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFFBAB3CB)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Next Action Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(EvolveCardGradientStart, EvolveCardGradientEnd)
                    )
                )
                .border(1.dp, Color(0x33BCA1FF), RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("next_action_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR NEXT MOVE",
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.2.sp,
                        color = EvolveAccent
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = EvolveMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = nextAction.duration,
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = EvolveMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = nextAction.title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    color = EvolveText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = nextAction.copy,
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFB8B2C4)
                )

                Spacer(modifier = Modifier.height(18.dp))
                Eyebrow("Start here", accent = true)
                Spacer(modifier = Modifier.height(7.dp))
                Text(nextAction.firstStep, fontFamily = ManropeFontFamily, fontSize = 13.sp,
                    lineHeight = 21.sp, color = EvolveText)
                Spacer(modifier = Modifier.height(16.dp))
                Text(PlannerEngine.plannedSlot(userProfile, nextAction, now)?.let { "Next free block · ${it.label}" }
                    ?: "No free block fits. Edit availability or choose a smaller step.",
                    fontSize = 11.sp, lineHeight = 17.sp, color = EvolveMuted)
                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { onNextActionClick(nextAction.actionType) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_action_button"),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EvolvePillBg,
                        contentColor = EvolvePillText
                    )
                ) {
                    Text(
                        text = nextAction.cta,
                        fontFamily = ManropeFontFamily,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Action links
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onWhyFirstClick, modifier = Modifier.testTag("why_first_button")) {
                Text(
                    text = "Why this first?",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
            }
            TextButton(onClick = onRescheduleClick, modifier = Modifier.testTag("find_another_time_button")) {
                Text(
                    text = "Find another time ↗",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveAccent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // On the horizon section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "On the horizon",
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = EvolveText
            )
            TextButton(onClick = onOpenPlanClick) {
                Text(
                    text = "See plan ↗",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    color = EvolveMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Deadlines list
        DeadlineRow(
            title = "Engineering graphics",
            subtitle = if (userProfile.graphicsBrief.isNotBlank()) "Assignment · Questions captured" else "Assignment · Questions needed",
            tag = if (userProfile.graphicsDueDate.isBlank()) "Date needed" else PlannerEngine.formatExamDate(userProfile.graphicsDueDate),
            icon = Icons.Outlined.Architecture,
            onClick = { onOpenBriefClick("graphics") }
        )

        DeadlineRow(
            title = "Electrical & electronics",
            subtitle = if (userProfile.electricalBrief.isNotBlank()) "Assignment · Questions captured" else "Assignment · Questions needed",
            tag = if (userProfile.electricalDueDate.isBlank()) "Date needed" else PlannerEngine.formatExamDate(userProfile.electricalDueDate),
            icon = Icons.Outlined.ElectricBolt,
            onClick = { onOpenBriefClick("electrical") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Keep the small things
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Keep the small things",
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = EvolveText
            )
            TextButton(onClick = onOpenSubjectsClick) {
                Text(
                    text = "Class notes ↗",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    color = EvolveAccent
                )
            }
        }

        Text(
            text = "Something your teacher said today? Give it a place before it slips away.",
            fontFamily = ManropeFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = EvolveDim
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your plan uses the work and dates you enter. Unknown information stays visible.",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = EvolveDim
        )

        // Bottom space so content is fully scrollable above the 67dp floating glass dock
        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun DeadlineRow(
    title: String,
    subtitle: String,
    tag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0x0DB39BDF))
                .border(1.dp, Color(0x1DBDA6E4), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EvolveAccent,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
                color = EvolveText
            )
            Text(
                text = subtitle,
                fontFamily = ManropeFontFamily,
                fontSize = 11.sp,
                color = EvolveMuted
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0x1AD6B081))
                .padding(horizontal = 8.dp, vertical = 5.dp)
        ) {
            Text(
                text = tag,
                fontFamily = ManropeFontFamily,
                fontSize = 9.sp,
                color = EvolveWarm
            )
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(EvolveLine)
    )
}
