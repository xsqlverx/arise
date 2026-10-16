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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.data.model.UserProfileEntity
import com.example.ui.components.ContourLandscape
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
fun SetupScreen(
    currentProfile: UserProfileEntity,
    onSaveProfile: (UserProfileEntity) -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    var name by rememberSaveable { mutableStateOf(currentProfile.name) }
    var examDate by rememberSaveable { mutableStateOf(currentProfile.examDate) }
    var collegeStart by rememberSaveable { mutableStateOf(currentProfile.collegeStart) }
    var collegeEnd by rememberSaveable { mutableStateOf(currentProfile.collegeEnd) }
    var gymStart by rememberSaveable { mutableStateOf(currentProfile.gymStart) }
    var gymEnd by rememberSaveable { mutableStateOf(currentProfile.gymEnd) }
    var studyStart by rememberSaveable { mutableStateOf(currentProfile.studyStart) }
    var studyEnd by rememberSaveable { mutableStateOf(currentProfile.studyEnd) }
    var callStart by rememberSaveable { mutableStateOf(currentProfile.callStart) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        TopBar(
            isSubScreen = true,
            backTitle = "Back",
            onBack = onBackClick,
            onSettingsClick = onSettingsClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "YOUR STARTING POINT",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            letterSpacing = 1.8.sp,
            color = EvolveMuted
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "A little structure.",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveText
        )
        Text(
            text = "A lot of possibility.",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveAccent
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Mini contour
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            ContourLandscape(
                modifier = Modifier.matchParentSize(),
                height = 160.dp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Set the date. Protect your commitments.\nGive the next step somewhere to land.",
            fontFamily = ManropeFontFamily,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            color = EvolveMuted
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Name Field
        FormField(
            label = "What should we call you? · optional",
            value = name,
            onValueChange = { name = it },
            placeholder = "Your name",
            testTag = "setup_name_input"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Next Exam Date Field
        FormField(
            label = "Next exam date (YYYY-MM-DD)",
            value = examDate,
            onValueChange = { examDate = it },
            placeholder = "Leave blank if unknown",
            testTag = "setup_exam_date_input"
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Protected Commitments Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Protected commitments",
                fontFamily = ManropeFontFamily,
                fontSize = 13.sp,
                color = EvolveText
            )
            Text(
                text = "EDIT ANYTIME",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 9.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // College Start / End
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FormField(
                    label = "Leave for college",
                    value = collegeStart,
                    onValueChange = { collegeStart = it },
                    placeholder = "07:45",
                    testTag = "setup_college_start_input"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FormField(
                    label = "Back from college",
                    value = collegeEnd,
                    onValueChange = { collegeEnd = it },
                    placeholder = "14:30",
                    testTag = "setup_college_end_input"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Gym Start / End
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                FormField(
                    label = "Gym starts",
                    value = gymStart,
                    onValueChange = { gymStart = it },
                    placeholder = "20:00",
                    testTag = "setup_gym_start_input"
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                FormField(
                    label = "Gym ends",
                    value = gymEnd,
                    onValueChange = { gymEnd = it },
                    placeholder = "21:00",
                    testTag = "setup_gym_end_input"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        FormField("Study window starts", studyStart, { studyStart = it }, "16:00", "study_window_start")
        Spacer(Modifier.height(14.dp))
        FormField("Study window ends", studyEnd, { studyEnd = it }, "22:00", "study_window_end")
        Spacer(Modifier.height(14.dp))
        Text("Only this window is offered for study. Include recovery and meals outside it; protected commitments are excluded automatically.", color = EvolveDim, fontSize = 11.sp)
        Spacer(Modifier.height(14.dp))
        // Evening Call Field
        FormField(
            label = "Evening call · earliest start",
            value = callStart,
            onValueChange = { callStart = it },
            placeholder = "22:30",
            testTag = "setup_call_start_input"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                onSaveProfile(
                    currentProfile.copy(
                        name = name.trim(),
                        examDate = examDate.trim(),
                        collegeStart = collegeStart.trim(),
                        collegeEnd = collegeEnd.trim(),
                        gymStart = gymStart.trim(),
                        gymEnd = gymEnd.trim(),
                        callStart = callStart.trim(),
                        studyStart = studyStart.trim(),
                        studyEnd = studyEnd.trim()
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_starting_point_button"),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EvolvePillBg,
                contentColor = EvolvePillText
            )
        ) {
            Text(
                text = "Save my starting point",
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

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "This setup defines what matters and when you are available. Everything remains editable.",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = EvolveDim
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    testTag: String
) {
    Column {
        Text(
            text = label,
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
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
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(testTag),
                singleLine = true,
                cursorBrush = SolidColor(EvolveAccent),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = ManropeFontFamily,
                    fontSize = 13.sp,
                    color = EvolveText
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            color = Color(0xFF8C8595)
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
