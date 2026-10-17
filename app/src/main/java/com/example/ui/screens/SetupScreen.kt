package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfileEntity
import com.example.domain.PlannerEngine
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
fun SetupScreen(
    currentProfile: UserProfileEntity,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var examDate by remember { mutableStateOf(currentProfile.examDate) }
    var collegeStart by remember { mutableStateOf(currentProfile.collegeStart) }
    var collegeEnd by remember { mutableStateOf(currentProfile.collegeEnd) }
    var gymStart by remember { mutableStateOf(currentProfile.gymStart) }
    var gymEnd by remember { mutableStateOf(currentProfile.gymEnd) }
    var callStart by remember { mutableStateOf(currentProfile.callStart) }
    var studyStart by remember { mutableStateOf(currentProfile.studyStart) }
    var studyEnd by remember { mutableStateOf(currentProfile.studyEnd) }
    var remindersEnabled by remember { mutableStateOf(currentProfile.remindersEnabled) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .statusBarsPadding()
            .imePadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            TopBar(
                isSubScreen = true,
                backTitle = "Back",
                onBack = onBackClick,
                onSettingsClick = onSettingsClick
            )

            Spacer(modifier = Modifier.height(24.dp))
            Eyebrow("Commitments & Routine", accent = true)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Protected Windows",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 28.sp,
                color = EvolveText
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Evolve plans around these blocks and will never schedule study sessions or alarms that overlap them.",
                fontFamily = ManropeFontFamily,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = EvolveMuted
            )

            Spacer(modifier = Modifier.height(24.dp))

            // College block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EvolveSurface)
                    .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Eyebrow("College Hours")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EvolveField(
                            label = "Start (24h)",
                            value = collegeStart,
                            onValueChange = { collegeStart = it; errorMessage = null },
                            hint = "07:45",
                            modifier = Modifier.weight(1f)
                        )
                        EvolveField(
                            label = "End (24h)",
                            value = collegeEnd,
                            onValueChange = { collegeEnd = it; errorMessage = null },
                            hint = "14:30",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gym & evening commitments
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EvolveSurface)
                    .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Eyebrow("Gym & Personal Time")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EvolveField(
                            label = "Gym Start",
                            value = gymStart,
                            onValueChange = { gymStart = it; errorMessage = null },
                            hint = "20:00",
                            modifier = Modifier.weight(1f)
                        )
                        EvolveField(
                            label = "Gym End",
                            value = gymEnd,
                            onValueChange = { gymEnd = it; errorMessage = null },
                            hint = "21:00",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    EvolveField(
                        label = "Evening Call Time",
                        value = callStart,
                        onValueChange = { callStart = it; errorMessage = null },
                        hint = "22:30",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Study availability window
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EvolveSurface)
                    .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Eyebrow("Available Study Window")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        EvolveField(
                            label = "Earliest Study",
                            value = studyStart,
                            onValueChange = { studyStart = it; errorMessage = null },
                            hint = "16:00",
                            modifier = Modifier.weight(1f)
                        )
                        EvolveField(
                            label = "Latest Study",
                            value = studyEnd,
                            onValueChange = { studyEnd = it; errorMessage = null },
                            hint = "22:00",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Exam target date
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EvolveSurface)
                    .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Eyebrow("Exam Horizon")
                    Spacer(modifier = Modifier.height(10.dp))
                    EvolveField(
                        label = "Target Exam Date (YYYY-MM-DD)",
                        value = examDate,
                        onValueChange = { examDate = it; errorMessage = null },
                        hint = "e.g. 2026-11-20",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reminders toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EvolveSurface)
                    .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Reminders",
                        fontFamily = ManropeFontFamily,
                        fontSize = 14.sp,
                        color = EvolveText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Inexact system alarms at planned focus windows",
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        color = EvolveMuted
                    )
                }
                Switch(
                    checked = remindersEnabled,
                    onCheckedChange = { remindersEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EvolvePillText,
                        checkedTrackColor = EvolveAccent,
                        uncheckedThumbColor = EvolveMuted,
                        uncheckedTrackColor = Color(0x22FFFFFF)
                    )
                )
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage.orEmpty(),
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    color = Color(0xFFFF6B6B)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val updated = currentProfile.copy(
                        collegeStart = collegeStart.trim(),
                        collegeEnd = collegeEnd.trim(),
                        gymStart = gymStart.trim(),
                        gymEnd = gymEnd.trim(),
                        callStart = callStart.trim(),
                        studyStart = studyStart.trim(),
                        studyEnd = studyEnd.trim(),
                        examDate = examDate.trim(),
                        remindersEnabled = remindersEnabled
                    )
                    val error = PlannerEngine.validateProfile(updated)
                    if (error != null) {
                        errorMessage = error
                    } else {
                        onSaveProfile(updated)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EvolvePillBg,
                    contentColor = EvolvePillText
                )
            ) {
                Text(
                    text = "Save commitments",
                    fontFamily = ManropeFontFamily,
                    fontSize = 14.sp
                )
            }
        }
    }
}
