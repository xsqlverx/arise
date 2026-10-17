package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.ElectricBolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.domain.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TodayScreen(userProfile: UserProfileEntity, nextAction: NextAction, now: LocalDateTime,
    semester: AcceptedSemester?, onOpenExam: () -> Unit, onOpenSemester: () -> Unit,
    onNextActionClick: (String) -> Unit, onWhyFirstClick: () -> Unit, onRescheduleClick: () -> Unit,
    onOpenPlanClick: () -> Unit, onOpenSubjectsClick: () -> Unit, onOpenBriefClick: (String) -> Unit,
    onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    val reduced = LocalReduceMotion.current
    val days = PlannerEngine.calculateDaysToExam(userProfile.examDate)
    val portion = semester?.exam
    val slot = PlannerEngine.plannedSlot(userProfile,nextAction,now)
    Column(modifier.fillMaxSize().background(EvolveBlack).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        TopBar(onSettingsClick = onSettingsClick)
        Spacer(Modifier.height(12.dp))
        Eyebrow(now.toLocalDate().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)))
        Spacer(Modifier.height(10.dp))
        Text("A clear path.", fontFamily = SpaceGroteskFontFamily, fontSize = 34.sp, color = EvolveText)
        Text("From here.", fontFamily = SpaceGroteskFontFamily, fontSize = 34.sp, color = EvolveAccent)
        Box(Modifier.fillMaxWidth().height(152.dp)) {
            ContourLandscape(Modifier.matchParentSize(),height = 152.dp)
            Column(Modifier.align(Alignment.TopEnd).padding(top = 12.dp), horizontalAlignment = Alignment.End) {
                Text(days?.let { if (it >= 0) it.toString().padStart(2,'0') else "Past" } ?: "—",
                    fontFamily = SpaceGroteskFontFamily, fontSize = 32.sp, color = EvolveText)
                Text(if (days == null) "Exam date unknown" else if (days < 0) "Update exam date" else "days to ${PlannerEngine.formatExamDate(userProfile.examDate)}",
                    color = EvolveMuted, fontSize = 10.sp)
            }
        }
        AnimatedContent(nextAction, label = "next study step", transitionSpec = {
            val ms = if (reduced) 0 else 180
            fadeIn(tween(ms)) togetherWith fadeOut(tween(ms))
        }) { action ->
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(EvolveCardGradientStart,EvolveCardGradientEnd)))
                .border(1.dp,Color(0x33BCA1FF),RoundedCornerShape(24.dp)).padding(20.dp).testTag("next_action_card")) {
                Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                    Eyebrow("Your next move",true)
                    Text(action.duration,color = EvolveMuted,fontSize = 11.sp)
                }
                Spacer(Modifier.height(12.dp))
                Text(action.title,color = EvolveText,fontFamily = SpaceGroteskFontFamily,fontSize = 25.sp,lineHeight = 31.sp)
                Spacer(Modifier.height(12.dp))
                Text(action.firstStep,color = EvolveMuted,fontFamily = ManropeFontFamily,fontSize = 13.sp,lineHeight = 21.sp,
                    maxLines = 3,overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(16.dp))
                EvolveAction(action.cta + " →",{ onNextActionClick(action.actionType) },Modifier.testTag("next_action_button"))
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onWhyFirstClick,modifier = Modifier.testTag("why_first_button")) { Text("Why & how",color = EvolveMuted,fontSize = 12.sp) }
            TextButton(onClick = onRescheduleClick,modifier = Modifier.testTag("find_another_time_button")) {
                Text(slot?.let { PlannerEngine.formatTime12h(it.time24) + " · Change" } ?: "Choose a time",color = EvolveAccent,fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth().clickable(onClick = onOpenExam).heightIn(min = 76.dp).padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Eyebrow("Next exam",true); Spacer(Modifier.height(7.dp))
                Text(portion?.name ?: "Set your exam topics",color = EvolveText,fontFamily = SpaceGroteskFontFamily,fontSize = 20.sp)
                Spacer(Modifier.height(5.dp))
                Text(portion?.let { "${it.topicCount} topics · ${if (it.confirmed) "Confirmed portion" else "Estimated portion"}" }
                    ?: "Choose the date and what's in the paper",color = EvolveMuted,fontSize = 12.sp)
            }
            Text("↗",color = EvolveAccent,fontSize = 23.sp,modifier = Modifier.padding(start = 12.dp))
        }
        HorizontalDivider(color = EvolveLine)
        Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onOpenSemester) { Text("Syllabus library ↗",color = EvolveAccent,fontSize = 12.sp) }
            TextButton(onClick = onOpenSubjectsClick) { Text("Class notes ↗",color = EvolveMuted,fontSize = 12.sp) }
        }
        val graphics = userProfile.graphicsDueDate.isNotBlank() || userProfile.graphicsBrief.isNotBlank()
        val electrical = userProfile.electricalDueDate.isNotBlank() || userProfile.electricalBrief.isNotBlank()
        if (graphics || electrical) {
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween,verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Assignments")
                TextButton(onClick = onOpenPlanClick) { Text("Your plan ↗",color = EvolveMuted,fontSize = 12.sp) }
            }
            if (graphics) DeadlineRow("Engineering graphics",if (userProfile.graphicsBrief.isBlank()) "Questions needed" else "Questions captured",
                PlannerEngine.formatExamDate(userProfile.graphicsDueDate),Icons.Outlined.Architecture,{ onOpenBriefClick("graphics") })
            if (electrical) DeadlineRow("Electrical & electronics",if (userProfile.electricalBrief.isBlank()) "Questions needed" else "Questions captured",
                PlannerEngine.formatExamDate(userProfile.electricalDueDate),Icons.Outlined.ElectricBolt,{ onOpenBriefClick("electrical") })
        }
        Spacer(Modifier.height(120.dp))
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
