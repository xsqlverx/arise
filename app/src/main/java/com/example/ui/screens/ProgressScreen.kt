package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.StudySessionEntity
import com.example.ui.components.ContourLandscape
import com.example.ui.components.TopBar
import com.example.ui.components.TrendChart
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolveText
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun ProgressScreen(
    sessions: List<StudySessionEntity>,
    onOpenSubjectsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val totalCompletedChecks = sessions.size
    val totalCorrect = sessions.sumOf { it.score }
    val totalPossible = sessions.sumOf { it.total }
    val accuracyPct = if (totalPossible > 0) ((totalCorrect.toFloat() / totalPossible.toFloat()) * 100).toInt() else null

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
                text = "A CHANGE YOU CAN SEE",
                fontFamily = ManropeFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.8.sp,
                color = EvolveMuted
            )
            Text(
                text = "SAMPLE PROFILE",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 11.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Becoming,",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveText
        )
        Text(
            text = "day by day.",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = EvolveAccent
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Hero with Topics Reviewed counter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        ) {
            ContourLandscape(
                modifier = Modifier.matchParentSize(),
                height = 260.dp
            )

            // Overlaid metric title top left
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 16.dp, start = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "09",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 44.sp,
                        color = EvolveText
                    )
                    Text(
                        text = " / 37",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 20.sp,
                        color = EvolveDim,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Text(
                    text = "sample topics reviewed",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
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
                    text = "YOUR LANDSCAPE / SAMPLE DATA",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.1.sp,
                    color = Color(0xFFBAB3CB)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "+18%",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 26.sp,
                    color = EvolveText
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "sample weekly check trend",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
            }

            Text(
                text = "7 DAYS",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 10.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Expressive Trend Chart
        TrendChart()

        Spacer(modifier = Modifier.height(20.dp))

        // Local Practice Records Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your practice in this preview",
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = EvolveText
            )
            Text(
                text = "LOCAL RECORDS",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = EvolveDim
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3-Column Stat Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = String.format("%02d", totalCompletedChecks),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 24.sp,
                    color = EvolveText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "completed checks",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    color = EvolveMuted
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = totalCorrect.toString(),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 24.sp,
                        color = EvolveText
                    )
                    Text(
                        text = " / $totalPossible",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 14.sp,
                        color = EvolveDim,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "correct answers",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    color = EvolveMuted
                )
            }

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(
                    text = if (accuracyPct != null) "$accuracyPct%" else "—",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 24.sp,
                    color = if (accuracyPct != null) EvolveAccent else EvolveText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "check accuracy",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    color = EvolveMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(EvolveLine)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sample Subject Overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sample subject overview",
                fontFamily = ManropeFontFamily,
                fontSize = 14.sp,
                color = EvolveText
            )
            TextButton(onClick = onOpenSubjectsClick) {
                Text(
                    text = "Subjects ↗",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    color = EvolveMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        SubjectMasteryRow("Algorithmic thinking", 42)
        SubjectMasteryRow("Engineering graphics", 24)
        SubjectMasteryRow("Electrical", 18)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Landscape, trend and subject percentages are illustrative. Your completed sample quizzes above are real local records. The native app calculates progress from actual evidence.",
            fontFamily = ManropeFontFamily,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            color = EvolveDim
        )

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun SubjectMasteryRow(name: String, percentage: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontFamily = ManropeFontFamily,
            fontSize = 12.sp,
            color = EvolveText,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .width(100.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF28242F))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = percentage / 100f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFAA90DF))
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$percentage%",
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 11.sp,
            color = EvolveMuted
        )
    }
}
