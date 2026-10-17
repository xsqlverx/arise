package com.example

import android.content.Intent
import android.Manifest
import android.os.Build
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import com.example.ui.components.LocalReduceMotion
import com.example.ui.components.LocalSheetFeedback
import com.example.ui.screens.TaskFocusScreen
import com.example.ui.sheets.TaskEditorSheet
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import android.provider.Settings
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
import com.example.ui.CurriculumViewModel
import com.example.ui.screens.CurriculumScreen
import com.example.domain.AcademicPlanner
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
    private val incomingAction = MutableStateFlow<String?>(null)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingAction.value = intent.getStringExtra("evolve_action")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        incomingAction.value = intent.getStringExtra("evolve_action")
        setContent {
            EvolveTheme {
                val incoming by incomingAction.collectAsStateWithLifecycle()
                EvolveApp(incomingAction = incoming, onIncomingHandled = { incomingAction.value = null })
            }
        }
    }
}

@Composable
fun EvolveApp(
    viewModel: EvolveViewModel = viewModel(),
    curriculum: CurriculumViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    incomingAction: String? = null,
    onIncomingHandled: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val now by produceState(LocalDateTime.now()) {
        while (true) { value = LocalDateTime.now(); delay(30_000) }
    }
    val backdrop = rememberGraphicsLayer()
    var backdropOrigin by remember { mutableStateOf(Offset.Zero) }
    val savedScreens = rememberSaveableStateHolder()
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0

    val currentDest by viewModel.currentDestination.collectAsStateWithLifecycle()
    val isViewingNotes by viewModel.isViewingSubjectNotes.collectAsStateWithLifecycle()
    val activeSubjectId by viewModel.activeSubjectId.collectAsStateWithLifecycle()
    val isStudyActive by viewModel.isStudyActive.collectAsStateWithLifecycle()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsStateWithLifecycle()
    val isSetupScreenOpen by viewModel.isSetupScreenOpen.collectAsStateWithLifecycle()
    val activeSheet by viewModel.activeSheet.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val showUnsavedDialog by viewModel.showUnsavedDialog.collectAsStateWithLifecycle()

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val allTips by viewModel.allTips.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    val subjectSearch by viewModel.subjectSearch.collectAsStateWithLifecycle()
    val noteSearch by viewModel.noteSearch.collectAsStateWithLifecycle()
    val noteFilter by viewModel.noteFilter.collectAsStateWithLifecycle()

    val editorTipId by viewModel.editorTipId.collectAsStateWithLifecycle()
    val editorSubjectId by viewModel.editorSubjectId.collectAsStateWithLifecycle()
    val editorCategory by viewModel.editorCategory.collectAsStateWithLifecycle()
    val editorText by viewModel.editorText.collectAsStateWithLifecycle()
    val briefInputText by viewModel.briefInputText.collectAsStateWithLifecycle()

    val studyStep by viewModel.studyStep.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val quizIndex by viewModel.quizQuestionIndex.collectAsStateWithLifecycle()
    val selectedOption by viewModel.quizSelectedOption.collectAsStateWithLifecycle()
    val isAnswerChecked by viewModel.isQuizAnswerChecked.collectAsStateWithLifecycle()
    val quizAnswers by viewModel.quizAnswers.collectAsStateWithLifecycle()

    val ready by viewModel.ready.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val activeTaskId by viewModel.activeTaskId.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()
    val briefDueDate by viewModel.briefDueDate.collectAsStateWithLifecycle()
    val pendingBackup by viewModel.pendingBackup.collectAsStateWithLifecycle()
    val semester by curriculum.semester.collectAsStateWithLifecycle()
    val curriculumAttempts by curriculum.attempts.collectAsStateWithLifecycle()
    val curriculumOpen by curriculum.open.collectAsStateWithLifecycle()
    val curriculumFeedback by curriculum.feedback.collectAsStateWithLifecycle()
    LaunchedEffect(curriculumOpen,curriculumFeedback) {
        if (!curriculumOpen) curriculumFeedback?.let { viewModel.showToast(it) }
    }
    val nextAction = AcademicPlanner.next(userProfile, tasks, semester, curriculumAttempts, now.toLocalDate())
    fun openSemester() {
        viewModel.exitStudyFlow()
        viewModel.closeSettings()
        viewModel.requestCloseSheet()
        curriculum.show()
    }
    val reducedMotion = userProfile.reducedMotion || Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    val route = when {
        curriculumOpen -> "curriculum"
        isSetupScreenOpen -> "setup"
        isStudyActive -> "study:${activeTaskId ?: "sample"}"
        isViewingNotes -> "notes:$activeSubjectId"
        else -> currentDest.name
    }
    fun openAction(type: String, id: String = nextAction.id) {
        when (type) {
            "semester" -> openSemester()
            "exam-practice" -> semester?.exam?.practiceTopics(semester!!.pack)?.find { it.id == id }?.let { topic ->
                viewModel.addCurriculumPractice(topic.subjectId,"Practise: ${topic.title}",topic.instructions,
                    dueDate = userProfile.examDate.takeIf { PlannerEngine.date(it)?.let { day -> day >= now.toLocalDate() } == true } ?: "") { taskId ->
                    curriculum.close(); viewModel.openTaskFlow(taskId)
                }
            }
            "curriculum" -> { openSemester(); curriculum.startModule(id, review = true) }
            "brief-graphics" -> viewModel.openSheet(ActiveSheet.BriefCapture("graphics"))
            "brief-electrical" -> viewModel.openSheet(ActiveSheet.BriefCapture("electrical"))
            "task" -> viewModel.openTaskFlow(id)
            "add-task" -> viewModel.openSheet(ActiveSheet.TaskEditor())
            "reschedule" -> viewModel.openSheet(ActiveSheet.Reschedule)
            else -> viewModel.openStudyFlow()
        }
    }
    LaunchedEffect(now.toLocalDate()) { if (selectedDate < now.toLocalDate()) viewModel.setActiveDate(now.toLocalDate()) }
    LaunchedEffect(incomingAction, ready) {
        if (incomingAction != null && ready) {
            if (incomingAction.startsWith("exam-practice:")) openAction("exam-practice", incomingAction.removePrefix("exam-practice:"))
            else if (incomingAction.startsWith("curriculum:")) openAction("curriculum", incomingAction.removePrefix("curriculum:"))
            else if (incomingAction.startsWith("task:")) openAction("task", incomingAction.removePrefix("task:"))
            else openAction(incomingAction)
            onIncomingHandled()
        }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.updateProfile(userProfile.copy(remindersEnabled = true))
        else viewModel.showToast("Notifications are off. You can enable permission in Android settings.")
    }
    val exportFile = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) scope.launch {
            try {
                val json = withContext(Dispatchers.IO) { viewModel.repository.exportCurrentBackup() }
                withContext(Dispatchers.IO) { context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray(Charsets.UTF_8)) } ?: error("File could not open") }
                viewModel.showToast("Backup saved.")
            } catch (e: Exception) { viewModel.showToast("Backup could not be saved. Try another location.") }
        }
    }
    val importFile = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.reader(Charsets.UTF_8)?.use { reader ->
                        val result = StringBuilder()
                        val buffer = CharArray(4096)
                        var count = reader.read(buffer)
                        while (count >= 0) {
                            result.append(buffer, 0, count)
                            require(result.length <= 5_000_000) { "Backup is too large" }
                            count = reader.read(buffer)
                        }
                        result.toString()
                    } ?: error("File could not open")
                }
                viewModel.previewBackup(text)
            } catch (e: Exception) { viewModel.showToast("Backup file could not be read.") }
        }
    }
    val availableSlots = PlannerEngine.getAvailableSlots(userProfile, now = now, durationMinutes = nextAction.minutes, dueDate = nextAction.dueDate)

    // Hardware Back Handler Navigation
    BackHandler(enabled = !imeVisible) {
        when {
            curriculumOpen -> curriculum.back()
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

    CompositionLocalProvider(LocalReduceMotion provides reducedMotion, LocalSheetFeedback provides toastMessage) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = EvolveBlack
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Capture screen content once; the dock samples this layer, never itself.
            AnimatedContent(targetState = if (ready) route else "loading", label = "screen",
                modifier = Modifier.fillMaxSize().onGloballyPositioned { backdropOrigin = it.positionInRoot() }
                    .drawWithContent { backdrop.record { this@drawWithContent.drawContent() }; drawLayer(backdrop) },
                transitionSpec = {
                    val duration = if (reducedMotion) 0 else 260
                    (fadeIn(tween(duration)) + slideInHorizontally(tween(duration)) { it / 18 }) togetherWith
                        (fadeOut(tween(duration)) + slideOutHorizontally(tween(duration)) { -it / 24 })
                }) { screen ->
            savedScreens.SaveableStateProvider(screen) {
            when {
                screen == "loading" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = EvolveAccent) }
                screen == "curriculum" -> CurriculumScreen(curriculum, userProfile.examDate, { curriculum.back() },
                    onNotebook = { id -> curriculum.back(); viewModel.openSubjectNotebook(id) },
                    onPractice = { subject, title, instructions -> viewModel.addCurriculumPractice(subject,title,instructions) { id ->
                        curriculum.close(); viewModel.openTaskFlow(id)
                    } })
                screen == "setup" -> {
                    SetupScreen(
                        currentProfile = userProfile,
                        onSaveProfile = { updated ->
                            viewModel.updateProfile(updated, closeSetup = true)
                        },
                        onBackClick = { viewModel.closeSetupScreen() },
                        onSettingsClick = { viewModel.openSettings() }
                    )
                }
                screen.startsWith("study:") -> {
                    val task = tasks.find { it.id == activeTaskId }
                    if (activeTaskId != null && task != null) TaskFocusScreen(
                        task, allTips.filter { it.subjectId == task.subjectId }, timerSeconds, isTimerRunning,
                        { viewModel.toggleTimer() }, { viewModel.completeTask(task.id, !task.completed) },
                        { viewModel.openSheet(ActiveSheet.TaskEditor(task.id)) }, { viewModel.exitStudyFlow() })
                    else if (activeTaskId != null) TextButton(onClick = { viewModel.exitStudyFlow() }) { Text("Task unavailable. Return to your plan.", color = EvolveText) }
                    else StudyScreen(
                        studyStep = studyStep,
                        timerSeconds = timerSeconds,
                        isTimerRunning = isTimerRunning,
                        quizIndex = quizIndex,
                        selectedOption = selectedOption,
                        isAnswerChecked = isAnswerChecked,
                        quizAnswers = quizAnswers,
                        onToggleTimer = { viewModel.toggleTimer() },
                        onStartQuiz = { viewModel.startQuiz() },
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
                screen.startsWith("notes:") -> {
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
                    when (NavDestination.valueOf(screen)) {
                        NavDestination.TODAY -> {
                            TodayScreen(
                                semester = semester,
                                onOpenExam = { curriculum.editExam() },
                                userProfile = userProfile,
                                now = now,
                                nextAction = nextAction,
                                onOpenSemester = { openSemester() },
                                onNextActionClick = { openAction(it) },
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
                                onOpenExam = { curriculum.editExam() },
                                userProfile = userProfile,
                                tasks = tasks,
                                now = now,
                                activeDate = selectedDate,
                                nextAction = nextAction,
                                onOpenAction = { openAction(nextAction.actionType) },
                                onOpenSemester = { openSemester() },
                                onSelectDate = { viewModel.setActiveDate(it) },
                                onRescheduleClick = { viewModel.openSheet(ActiveSheet.Reschedule) },
                                onOpenBriefClick = { key -> viewModel.openSheet(ActiveSheet.BriefCapture(key)) },
                                onOpenStudyClick = { viewModel.openStudyFlow() },
                                onTaskClick = { viewModel.openTaskFlow(it) },
                                onEditTask = { viewModel.openSheet(ActiveSheet.TaskEditor(it)) },
                                onAddTask = { viewModel.openSheet(ActiveSheet.TaskEditor()) },
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
                                curriculumAttempts = curriculumAttempts,
                                onOpenSemester = { openSemester() },
                                sessions = sessions,
                                tasks = tasks,
                                onOpenSample = { viewModel.openStudyFlow() },
                                onOpenSubjectsClick = { viewModel.navigateTo(NavDestination.SUBJECTS) },
                                onSettingsClick = { viewModel.openSettings() }
                            )
                        }
                    }
                }
            }

            }
            }

            // Floating Smoked-Glass Capsule Navigation Dock
            // Hides when sheets, study session, or settings are active!
            val isDockVisible = ready && !curriculumOpen && activeSheet == null && !isStudyActive && !isSettingsOpen && !isSetupScreenOpen && !imeVisible
            GlassDock(
                currentDestination = currentDest,
                onNavigate = { dest ->
                    viewModel.navigateTo(dest)
                },
                isVisible = isDockVisible,
                backdrop = backdrop,
                backdropOrigin = backdropOrigin,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Animated Toast Notification Banner
            AnimatedVisibility(
                visible = toastMessage != null,
                enter = if (reducedMotion) androidx.compose.animation.EnterTransition.None else slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = if (reducedMotion) androidx.compose.animation.ExitTransition.None else slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
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
                        isSaving = isSaving,
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
                        action = nextAction,
                        onGetFirstBrief = { openAction(nextAction.actionType) },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.Reschedule -> {
                    RescheduleSheet(
                        currentScheduledSlot = "${userProfile.scheduledDate}|${userProfile.scheduledSlot}",
                        action = nextAction,
                        availableSlots = availableSlots,
                        onSelectSlot = { slotTime ->
                            viewModel.chooseRescheduleSlot(slotTime)
                        },
                        onOpenSetup = {
                            viewModel.requestCloseSheet()
                            viewModel.openSetupScreen()
                        },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.BriefCapture -> {
                    BriefCaptureSheet(
                        subjectKey = sheet.subjectKey,
                        briefText = briefInputText,
                        dueDate = briefDueDate,
                        onDueDateChange = { viewModel.updateBriefDueDate(it) },
                        onTextChange = { viewModel.updateBriefInput(it) },
                        onSaveClick = { viewModel.saveBrief(sheet.subjectKey) },
                        onClose = { viewModel.requestCloseSheet() }
                    )
                }
                is ActiveSheet.TaskEditor -> {
                    TaskEditorSheet(tasks.find { it.id == sheet.taskId }, subjects, isSaving,
                        { viewModel.saveTask(it) }, { viewModel.requestCloseSheet() })
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
                    onUpdateProfile = { updated ->
                        if (updated.remindersEnabled && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                            permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        else viewModel.updateProfile(updated)
                    },
                    onOpenSemester = { openSemester() },
                    onExportBackup = { exportFile.launch("Evolve-backup-${LocalDate.now()}.json") },
                    onImportBackup = { importFile.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                    onClearSamples = { viewModel.clearSampleNotes() },
                    onOpenSetup = {
                        viewModel.openSetupScreen()
                    },
                    onClose = { viewModel.closeSettings() }
                )
            }

            pendingBackup?.let { backup ->
                AlertDialog(onDismissRequest = { viewModel.cancelImport() }, containerColor = com.example.ui.theme.EvolveSurface,
                    title = { Text("Merge this backup?", color = EvolveText) },
                    text = { Text(backup.summary + " Reminders will be off after import.", color = EvolveText) },
                    confirmButton = { TextButton(enabled = !isSaving, onClick = { viewModel.confirmImport() }) { Text(if (isSaving) "Importing…" else "Merge records") } },
                    dismissButton = { TextButton(onClick = { viewModel.cancelImport() }) { Text("Cancel") } })
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
}
