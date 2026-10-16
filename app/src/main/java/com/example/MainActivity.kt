package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.domain.PlannerEngine
import com.example.ui.ActiveSheet
import com.example.ui.EvolveViewModel
import com.example.ui.components.GlassDock
import com.example.ui.components.NavDestination
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.StudyScreen
import com.example.ui.screens.SubjectNotesScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.sheets.AddSubjectDialog
import com.example.ui.sheets.BriefCaptureSheet
import com.example.ui.sheets.DeleteTipDialog
import com.example.ui.sheets.NoteCaptureSheet
import com.example.ui.sheets.RescheduleSheet
import com.example.ui.sheets.SettingsSheet
import com.example.ui.sheets.UnsavedDraftDialog
import com.example.ui.sheets.WhyFirstSheet
import com.example.ui.theme.EvolveAccent
import com.example.ui.theme.EvolveBlack
import com.example.ui.theme.EvolveText
import com.example.ui.theme.EvolveTheme
import com.example.ui.theme.ManropeFontFamily

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EvolveTheme {
                EvolveApp()
            }
        }
    }
}

@Composable
fun EvolveApp(
    viewModel: EvolveViewModel = viewModel()
) {
    val context = LocalContext.current

    val currentDest by viewModel.currentDestination.collectAsState()
    val isViewingNotes by viewModel.isViewingSubjectNotes.collectAsState()
    val activeSubjectId by viewModel.activeSubjectId.collectAsState()
    val isStudyActive by viewModel.isStudyActive.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isSetupScreenOpen by viewModel.isSetupScreenOpen.collectAsState()
    val activeSheet by viewModel.activeSheet.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val showUnsavedDialog by viewModel.showUnsavedDialog.collectAsState()

    val subjects by viewModel.subjects.collectAsState()
    val allTips by viewModel.allTips.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val subjectSearch by viewModel.subjectSearch.collectAsState()
    val noteSearch by viewModel.noteSearch.collectAsState()
    val noteFilter by viewModel.noteFilter.collectAsState()

    val editorTipId by viewModel.editorTipId.collectAsState()
    val editorSubjectId by viewModel.editorSubjectId.collectAsState()
    val editorCategory by viewModel.editorCategory.collectAsState()
    val editorText by viewModel.editorText.collectAsState()
    val briefInputText by viewModel.briefInputText.collectAsState()

    val studyStep by viewModel.studyStep.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val quizIndex by viewModel.quizQuestionIndex.collectAsState()
    val selectedOption by viewModel.quizSelectedOption.collectAsState()
    val isAnswerChecked by viewModel.isQuizAnswerChecked.collectAsState()
    val quizAnswers by viewModel.quizAnswers.collectAsState()

    val nextAction = PlannerEngine.determineNextAction(userProfile)
    val availableSlots = PlannerEngine.getAvailableSlots(userProfile)

    // Hardware Back Handler Navigation
    BackHandler {
        when {
            showUnsavedDialog -> viewModel.dismissUnsavedDialog(discardDraft = false)
            activeSheet != null -> viewModel.requestCloseSheet()
            isStudyActive -> viewModel.exitStudyFlow()
            isSetupScreenOpen -> viewModel.closeSetupScreen()
            isSettingsOpen -> viewModel.closeSettings()
            isViewingNotes -> viewModel.closeSubjectNotebook()
            currentDest != NavDestination.TODAY -> viewModel.navigateTo(NavDestination.TODAY)
            else -> {
                (context as? ComponentActivity)?.finish()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = EvolveBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen Content
            when {
                isSetupScreenOpen -> {
                    SetupScreen(
                        currentProfile = userProfile,
                        onSaveProfile = { updated ->
                            viewModel.updateProfile(updated)
                            viewModel.closeSetupScreen()
                        },
                        onBackClick = { viewModel.closeSetupScreen() },
                        onSettingsClick = { viewModel.openSettings() }
                    )
                }
                isStudyActive -> {
                    StudyScreen(
                        studyStep = studyStep,
                        timerSeconds = timerSeconds,
                        isTimerRunning = isTimerRunning,
                        quizIndex = quizIndex,
                        selectedOption = selectedOption,
                        isAnswerChecked = isAnswerChecked,
                        quizAnswers = quizAnswers,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onStartQuiz = { viewModel.openStudyFlow(); viewModel.nextQuizQuestion(0) },
                        onSelectOption = { viewModel.selectQuizOption(it) },
                        onCheckAnswer = { viewModel.checkQuizAnswer(it) },
                        onNextQuestion = { viewModel.nextQuizQuestion(it) },
                        onReviewLesson = { viewModel.reviewLesson() },
                        onSeeProgress = {
                            viewModel.exitStudyFlow()
                            viewModel.navigateTo(NavDestination.PROGRESS)
                        },
                        onBackClick = { viewModel.exitStudyFlow() }
                    )
                }
                isViewingNotes -> {
                    val activeSubject = subjects.find { it.id == activeSubjectId } ?: subjects.firstOrNull()
                    if (activeSubject != null) {
                        SubjectNotesScreen(
                            subject = activeSubject,
                            tips = allTips.filter { it.subjectId == activeSubject.id },
                            searchQuery = noteSearch,
                            filterCategory = noteFilter,
                            onSearchChange = { viewModel.setNoteSearch(it) },
                            onFilterChange = { viewModel.setNoteFilter(it) },
                            onBackClick = { viewModel.closeSubjectNotebook() },
                            onAddTipClick = {
                                viewModel.openSheet(ActiveSheet.NoteEditor(null, activeSubject.id))
                            },
                            onEditTipClick = { tipId ->
                                viewModel.openSheet(ActiveSheet.NoteEditor(tipId, activeSubject.id))
                            },
                            onDeleteTipClick = { tip ->
                                viewModel.openSheet(ActiveSheet.DeleteTip(tip))
                            },
                            onTogglePinClick = { tip ->
                                viewModel.togglePin(tip)
                            },
                            onSettingsClick = { viewModel.openSettings() }
                        )
                    }
                }
                else -> {
                    when (currentDest) {
                        NavDestination.TODAY -> {
                            TodayScreen(
                                userProfile = userProfile,
                                nextAction = nextAction,
                                onNextActionClick = { actionType ->
                                    when (actionType) {
                                        "brief-graphics" -> viewModel.openSheet(ActiveSheet.BriefCapture("graphics"))
                                        "brief-electrical" -> viewModel.openSheet(ActiveSheet.BriefCapture("electrical"))
                                        "open-study" -> viewModel.openStudyFlow()
                                    }
                                },
                                onWhyFirstClick = { viewModel.openSheet(ActiveSheet.WhyFirst) },
                                onRescheduleClick = { viewModel.openSheet(ActiveSheet.Reschedule) },
                                onOpenPlanClick = { viewModel.navigateTo(NavDestination.PLAN) },
                                onOpenSubjectsClick = { viewModel.navigateTo(NavDestination.SUBJECTS) },
                                onOpenBriefClick = { key -> viewModel.openSheet(ActiveSheet.BriefCapture(key)) },
                                onSettingsClick = { viewModel.openSettings() }
                            )
                        }
                        NavDestination.PLAN -> {
                            PlanScreen(
                                userProfile = userProfile,
                                activeDate = userProfile.activeDate,
                                onSelectDate = { viewModel.setActiveDate(it) },
                                onRescheduleClick = { viewModel.openSheet(ActiveSheet.Reschedule) },
                                onOpenBriefClick = { key -> viewModel.openSheet(ActiveSheet.BriefCapture(key)) },
                                onOpenStudyClick = { viewModel.openStudyFlow() },
                                onSettingsClick = { viewModel.openSettings() }
                            )
                        }
                        NavDestination.SUBJECTS -> {
                            SubjectsScreen(
                                subjects = subjects,
                                allTips = allTips,
                                searchQuery = subjectSearch,
                                onSearchChange = { viewModel.setSubjectSearch(it) },
                                onSubjectClick = { subId -> viewModel.openSubjectNotebook(subId) },
                                onAddSubjectClick = { viewModel.openSheet(ActiveSheet.AddSubject) },
                                onCaptureTipClick = {
                                    val fallbackId = subjects.firstOrNull()?.id ?: "electronics"
                                    viewModel.openSheet(ActiveSheet.NoteEditor(null, fallbackId))
                                },
                                onSettingsClick = { viewModel.openSettings() }
                            )
                        }
                        NavDestination.PROGRESS -> {
                            ProgressScreen(
                                sessions = sessions,
                                onOpenSubjectsClick = { viewModel.navigateTo(NavDestination.SUBJECTS) },
                                onSettingsClick = { viewModel.openSettings() }
                            )
                        }
                    }
                }
            }

            // Floating Smoked-Glass Capsule Navigation Dock
            // Hides when sheets, study session, or settings are active!
            val isDockVisible = activeSheet == null && !isStudyActive && !isSettingsOpen && !isSetupScreenOpen
            GlassDock(
                currentDestination = currentDest,
                onNavigate = { dest ->
                    viewModel.navigateTo(dest)
                },
                isVisible = isDockVisible,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Animated Toast Notification Banner
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                toastMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xF026212F))
                            .border(1.dp, Color(0x55B9A2F5), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = msg,
                            fontFamily = ManropeFontFamily,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            color = EvolveText
                        )
                    }
                }
            }

            // Modal Sheets and Overlays
            when (val sheet = activeSheet) {
                is ActiveSheet.NoteEditor -> {
                    NoteCaptureSheet(
                        isEditing = editorTipId != null,
                        tipText = editorText,
                        category = editorCategory,
                        selectedSubjectId = editorSubjectId,
                        subjects = subjects,
                        onTextChange = { viewModel.updateEditorText(it) },
                        onCategoryChange = { viewModel.updateEditorCategory(it) },
                        onSubjectChange = { viewModel.updateEditorSubject(it) },
                        onSaveClick = { viewModel.saveTipFromEditor() },
                        onCloseClick = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.WhyFirst -> {
                    WhyFirstSheet(
                        onGetFirstBrief = {
                            viewModel.openSheet(ActiveSheet.BriefCapture("graphics"))
                        },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.Reschedule -> {
                    RescheduleSheet(
                        currentScheduledSlot = userProfile.scheduledSlot,
                        availableSlots = availableSlots,
                        onSelectSlot = { slotTime ->
                            viewModel.chooseRescheduleSlot(slotTime)
                        },
                        onOpenSetup = {
                            viewModel.requestCloseSheet()
                            viewModel.openSettings()
                        },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.BriefCapture -> {
                    BriefCaptureSheet(
                        subjectKey = sheet.subjectKey,
                        briefText = briefInputText,
                        onTextChange = { viewModel.updateBriefInput(it) },
                        onSaveClick = { viewModel.saveBrief(sheet.subjectKey) },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.AddSubject -> {
                    AddSubjectDialog(
                        onAddSubject = { name -> viewModel.addSubject(name) },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.DeleteTip -> {
                    DeleteTipDialog(
                        tip = sheet.tip,
                        onConfirmDelete = { viewModel.confirmDeleteTip(sheet.tip) },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                null -> {}
            }

            // Settings Sheet Overlay
            if (isSettingsOpen) {
                SettingsSheet(
                    profile = userProfile,
                    onUpdateProfile = { viewModel.updateProfile(it) },
                    onExportBackup = {
                        val json = viewModel.repository.exportBackupJson(
                            subjectsList = subjects,
                            tipsList = allTips,
                            sessionsList = sessions,
                            profile = userProfile
                        )
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, json)
                            type = "application/json"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export Evolve Backup"))
                    },
                    onClearSamples = { viewModel.clearSampleNotes() },
                    onOpenSetup = {
                        viewModel.openSetupScreen()
                    },
                    onClose = { viewModel.closeSettings() }
                )
            }

            // Unsaved Draft Dialog
            if (showUnsavedDialog) {
                UnsavedDraftDialog(
                    onSaveDraft = { viewModel.saveTipFromEditor() },
                    onKeepEditing = { viewModel.dismissUnsavedDialog(discardDraft = false) },
                    onDiscardDraft = { viewModel.dismissUnsavedDialog(discardDraft = true) }
                )
            }
        }
    }
}
