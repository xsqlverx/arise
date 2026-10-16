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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveDim
import com.example.ui.theme.EvolveGreen
import com.example.ui.theme.EvolveLine
import com.example.ui.theme.EvolveMuted
import com.example.ui.theme.EvolvePillBg
import com.example.ui.theme.EvolvePillText
import com.example.ui.theme.EvolveText
import com.example.ui.theme.EvolveWarm
import com.example.ui.theme.ManropeFontFamily
import com.example.ui.theme.SpaceGroteskFontFamily
import java.util.Locale

data class QuizQuestion(
    val title: String,
    val code: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

val sampleQuestions = listOf(
    QuizQuestion(
        title = "What prints when score is 40?",
        code = "score = 40\nif score >= 50:\n    print(\"Passed\")\nprint(\"Checked\")",
        options = listOf("Passed, then Checked", "Checked only", "Nothing prints"),
        correctIndex = 1,
        explanation = "The condition is false, so Python skips the indented line. The final print is outside the block and still runs."
    ),
    QuizQuestion(
        title = "Which line belongs to the if block?",
        code = "if ready:\n    begin()\nfinish()",
        options = listOf("begin() only", "begin() and finish()", "finish() only"),
        correctIndex = 0,
        explanation = "Only begin() is indented under the if statement. finish() is outside that block."
    ),
    QuizQuestion(
        title = "What tells Python a block is starting?",
        code = "if score >= 50:\n    print(\"Passed\")",
        options = listOf("A semicolon and braces", "A colon followed by an indented body", "An uppercase condition"),
        correctIndex = 1,
        explanation = "The if header ends with a colon. Consistent indentation then marks the statements in its body."
    )
)

@Composable
fun StudyScreen(
    studyStep: Int,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    quizIndex: Int,
    selectedOption: Int?,
    isAnswerChecked: Boolean,
    quizAnswers: List<Boolean>,
    onToggleTimer: () -> Unit,
    onStartQuiz: () -> Unit,
    onSelectOption: (Int) -> Unit,
    onCheckAnswer: (Int) -> Unit,
    onNextQuestion: (Int) -> Unit,
    onReviewLesson: () -> Unit,
    onSeeProgress: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(studyStep, quizIndex) { scrollState.scrollTo(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EvolveBlack)
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState)
    ) {
        // Study Header with Back Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onBackClick() }
                    .padding(vertical = 4.dp, horizontal = 2.dp)
                    .testTag("study_back_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = EvolveMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Algorithmic thinking / Python",
                    fontFamily = ManropeFontFamily,
                    fontSize = 11.sp,
                    color = EvolveMuted
                )
            }

            Text(
                text = "SAMPLE LESSON",
                fontFamily = ManropeFontFamily,
                fontSize = 9.sp,
                letterSpacing = 1.2.sp,
                color = EvolveAccent
            )
        }

        // 3-Step Progress Indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (step in 0..2) {
                val isDone = step <= studyStep
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDone) EvolveAccent else Color(0xFF242228))
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (studyStep) {
            0 -> {
                // Step 0: Understand the idea & Focus Timer
                Text(
                    text = "01 / UNDERSTAND THE IDEA",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "One block.",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    color = EvolveText
                )
                Text(
                    text = "One clear structure.",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    color = EvolveAccent
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Learn how indentation tells Python\nwhich statements belong together.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Timer Wrap
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EvolveLine, RoundedCornerShape(16.dp))
                        .background(Color(0x08FFFFFF), RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "YOUR FOCUS WINDOW",
                                fontFamily = ManropeFontFamily,
                                fontSize = 9.sp,
                                letterSpacing = 1.4.sp,
                                color = EvolveDim
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val minutes = timerSeconds / 60
                            val seconds = timerSeconds % 60
                            Text(
                                text = String.format(Locale.US, "%02d:%02d", minutes, seconds),
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 48.sp,
                                color = EvolveText
                            )
                        }

                        IconButton(
                            onClick = onToggleTimer,
                            modifier = Modifier
                                .size(50.dp)
                                .border(1.dp, EvolveLine, CircleShape)
                                .testTag("timer_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isTimerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (isTimerRunning) "Pause" else "Start",
                                tint = EvolveAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Lesson Step 1
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "01",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = EvolveAccent,
                        modifier = Modifier.width(28.dp)
                    )
                    Column {
                        Text(
                            text = "Read the concept",
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            color = EvolveText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Indentation groups statements into a block. After an if statement ending in a colon, indent its body consistently.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            color = EvolveMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Code block with monospace typography
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131216))
                        .border(1.dp, EvolveLine, RoundedCornerShape(14.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "score = 72\nif score >= 50:\n    print(\"Passed\")\nprint(\"Checked\")",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 22.sp,
                        color = Color(0xFFD6C9F0)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Lesson Step 2
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "02",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = EvolveAccent,
                        modifier = Modifier.width(28.dp)
                    )
                    Column {
                        Text(
                            text = "Explain what runs",
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            color = EvolveText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Which line runs only when the condition is true? Which runs regardless? Try score = 40 on paper.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            lineHeight = 18.sp,
                            color = EvolveMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Inline notebook tip callout
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x0EB9A2F5))
                        .border(
                            1.dp,
                            Color(0x33B9A2F5),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "FROM YOUR NOTEBOOK · EXAMPLE",
                            fontFamily = ManropeFontFamily,
                            fontSize = 9.sp,
                            letterSpacing = 1.sp,
                            color = EvolveAccent
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keep every statement in the same Python block aligned.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color(0xFFBBB3CC)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onStartQuiz,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_quiz_button"),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EvolvePillBg,
                        contentColor = EvolvePillText
                    )
                ) {
                    Text(
                        text = "Check my understanding",
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
                    text = "A sample concept and quiz. Your real study steps will cite your own materials and verified page references.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    lineHeight = 16.sp,
                    color = EvolveDim
                )
            }

            1 -> {
                // Step 1: Knowledge Check Quiz
                val currentQ = sampleQuestions[quizIndex]

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "02 / KNOWLEDGE CHECK",
                        fontFamily = ManropeFontFamily,
                        fontSize = 10.sp,
                        letterSpacing = 1.4.sp,
                        color = EvolveMuted
                    )
                    Text(
                        text = "${quizIndex + 1} / ${sampleQuestions.size}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 11.sp,
                        color = EvolveDim
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = currentQ.title,
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    color = EvolveText
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131216))
                        .border(1.dp, EvolveLine, RoundedCornerShape(14.dp))
                        .padding(14.dp)
                ) {
                    Text(
                        text = currentQ.code,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFFD6C9F0)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options A, B, C
                currentQ.options.forEachIndexed { idx, opt ->
                    val letter = ('A' + idx).toString()
                    val isSelected = selectedOption == idx
                    val isCorrect = idx == currentQ.correctIndex

                    val borderColor = when {
                        isAnswerChecked && isCorrect -> Color(0x99B4D4BF)
                        isAnswerChecked && isSelected && !isCorrect -> Color(0x99D9B384)
                        isSelected -> Color(0x88B9A2F5)
                        else -> EvolveLine
                    }

                    val bgColor = when {
                        isAnswerChecked && isCorrect -> Color(0x18B4D4BF)
                        isAnswerChecked && isSelected && !isCorrect -> Color(0x18D9B384)
                        isSelected -> Color(0x18B9A2F5)
                        else -> Color(0x08FFFFFF)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bgColor)
                            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                            .clickable(enabled = !isAnswerChecked) { onSelectOption(idx) }
                            .padding(14.dp)
                            .testTag("quiz_option_$idx"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, EvolveLine, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letter,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 11.sp,
                                color = if (isSelected) EvolveAccent else EvolveDim
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = opt,
                            fontFamily = ManropeFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = EvolveText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Explanation text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                ) {
                    if (isAnswerChecked) {
                        val wasCorrect = selectedOption == currentQ.correctIndex
                        Column {
                            Text(
                                text = if (wasCorrect) "That’s right. " else "Worth revisiting. ",
                                fontFamily = ManropeFontFamily,
                                fontSize = 12.sp,
                                color = if (wasCorrect) EvolveAccent else EvolveWarm
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentQ.explanation,
                                fontFamily = ManropeFontFamily,
                                fontSize = 11.sp,
                                lineHeight = 17.sp,
                                color = EvolveMuted
                            )
                        }
                    } else {
                        Text(
                            text = "Think it through. There’s no timer on understanding.",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = EvolveDim
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isAnswerChecked) {
                    Button(
                        onClick = { onNextQuestion(sampleQuestions.size) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("next_question_button"),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EvolvePillBg,
                            contentColor = EvolvePillText
                        )
                    ) {
                        Text(
                            text = if (quizIndex == sampleQuestions.size - 1) "See my result" else "Next question",
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
                } else {
                    Button(
                        onClick = { onCheckAnswer(currentQ.correctIndex) },
                        enabled = selectedOption != null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("check_answer_button"),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EvolvePillBg,
                            contentColor = EvolvePillText,
                            disabledContainerColor = Color(0x33E8E2F3),
                            disabledContentColor = Color(0x5517131D)
                        )
                    ) {
                        Text(
                            text = "Check answer",
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

            2 -> {
                // Step 2: Session Complete Results Orbit
                val score = quizAnswers.count { it }

                Text(
                    text = "03 / SESSION COMPLETE",
                    fontFamily = ManropeFontFamily,
                    fontSize = 10.sp,
                    letterSpacing = 1.4.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Orbital score graphic
                Box(
                    modifier = Modifier
                        .size(174.dp)
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .border(1.dp, Color(0x44B9A2F5), CircleShape)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score / ${sampleQuestions.size}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 38.sp,
                            color = EvolveText
                        )
                        Text(
                            text = "answers correct",
                            fontFamily = ManropeFontFamily,
                            fontSize = 11.sp,
                            color = EvolveMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (score == sampleQuestions.size) "A little clearer.\nA step forward." else "You found what\nto work on next.",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 28.sp,
                    lineHeight = 34.sp,
                    color = EvolveText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (score == sampleQuestions.size)
                        "You correctly traced the sample blocks.\nRevisit the idea later to check retention."
                    else
                        "Review the explanations, then try again.\nA mistake gives your next session direction.",
                    fontFamily = ManropeFontFamily,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = EvolveMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0DFFFFFF))
                        .padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = EvolveGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "This practice result is saved locally. One short check is evidence of practice, rather than a mastery certificate.",
                        fontFamily = ManropeFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = EvolveMuted
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onSeeProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("see_progress_button"),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EvolvePillBg,
                        contentColor = EvolvePillText
                    )
                ) {
                    Text(
                        text = "See your progress",
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onReviewLesson,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("review_lesson_button"),
                    shape = RoundedCornerShape(15.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EvolveLine),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EvolveText
                    )
                ) {
                    Text(
                        text = "Review the lesson",
                        fontFamily = ManropeFontFamily,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
