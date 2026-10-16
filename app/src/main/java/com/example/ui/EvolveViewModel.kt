package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.EvolveDatabase
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.EvolveRepository
import com.example.domain.NextAction
import com.example.domain.PlannerEngine
import com.example.domain.RescheduleSlot
import com.example.ui.components.NavDestination
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ActiveSheet {
    data object WhyFirst : ActiveSheet()
    data object Reschedule : ActiveSheet()
    data class NoteEditor(val tipId: String?, val subjectId: String) : ActiveSheet()
    data class BriefCapture(val subjectKey: String) : ActiveSheet()
    data object AddSubject : ActiveSheet()
    data class DeleteTip(val tip: ClassTipEntity) : ActiveSheet()
}

class EvolveViewModel(application: Application) : AndroidViewModel(application) {

    private val db = EvolveDatabase.getDatabase(application, viewModelScope)
    val repository = EvolveRepository(db)

    // Navigation and screen states
    private val _currentDestination = MutableStateFlow(NavDestination.TODAY)
    val currentDestination: StateFlow<NavDestination> = _currentDestination.asStateFlow()

    private val _activeSubjectId = MutableStateFlow<String?>("electronics")
    val activeSubjectId: StateFlow<String?> = _activeSubjectId.asStateFlow()

    private val _isViewingSubjectNotes = MutableStateFlow(false)
    val isViewingSubjectNotes: StateFlow<Boolean> = _isViewingSubjectNotes.asStateFlow()

    private val _isStudyActive = MutableStateFlow(false)
    val isStudyActive: StateFlow<Boolean> = _isStudyActive.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isSetupScreenOpen = MutableStateFlow(false)
    val isSetupScreenOpen: StateFlow<Boolean> = _isSetupScreenOpen.asStateFlow()

    private val _activeSheet = MutableStateFlow<ActiveSheet?>(null)
    val activeSheet: StateFlow<ActiveSheet?> = _activeSheet.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Search and filters
    private val _subjectSearch = MutableStateFlow("")
    val subjectSearch: StateFlow<String> = _subjectSearch.asStateFlow()

    private val _noteSearch = MutableStateFlow("")
    val noteSearch: StateFlow<String> = _noteSearch.asStateFlow()

    private val _noteFilter = MutableStateFlow("All")
    val noteFilter: StateFlow<String> = _noteFilter.asStateFlow()

    // Note Editor draft state
    private val _editorTipId = MutableStateFlow<String?>(null)
    val editorTipId: StateFlow<String?> = _editorTipId.asStateFlow()

    private val _editorSubjectId = MutableStateFlow("electronics")
    val editorSubjectId: StateFlow<String> = _editorSubjectId.asStateFlow()

    private val _editorCategory = MutableStateFlow("Answer writing")
    val editorCategory: StateFlow<String> = _editorCategory.asStateFlow()

    private val _editorText = MutableStateFlow("")
    val editorText: StateFlow<String> = _editorText.asStateFlow()

    private val _editorOriginalText = MutableStateFlow("")
    private val _showUnsavedDialog = MutableStateFlow(false)
    val showUnsavedDialog: StateFlow<Boolean> = _showUnsavedDialog.asStateFlow()

    // Brief capture state
    private val _briefInputText = MutableStateFlow("")
    val briefInputText: StateFlow<String> = _briefInputText.asStateFlow()

    // Study session state
    private val _studyStep = MutableStateFlow(0) // 0: Concept & timer, 1: Quiz, 2: Complete
    val studyStep: StateFlow<Int> = _studyStep.asStateFlow()

    private val _timerSeconds = MutableStateFlow(1500)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private var timerJob: Job? = null
    private var timerEndTime = 0L

    // Quiz state
    private val _quizQuestionIndex = MutableStateFlow(0)
    val quizQuestionIndex: StateFlow<Int> = _quizQuestionIndex.asStateFlow()

    private val _quizSelectedOption = MutableStateFlow<Int?>(null)
    val quizSelectedOption: StateFlow<Int?> = _quizSelectedOption.asStateFlow()

    private val _isQuizAnswerChecked = MutableStateFlow(false)
    val isQuizAnswerChecked: StateFlow<Boolean> = _isQuizAnswerChecked.asStateFlow()

    private val _quizAnswers = MutableStateFlow<List<Boolean>>(emptyList())
    val quizAnswers: StateFlow<List<Boolean>> = _quizAnswers.asStateFlow()

    // DB flows
    val subjects: StateFlow<List<SubjectEntity>> = repository.subjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTips: StateFlow<List<ClassTipEntity>> = repository.allTips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<StudySessionEntity>> = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .combine(MutableStateFlow(Unit)) { profile, _ ->
            profile ?: UserProfileEntity()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    init {
        viewModelScope.launch {
            repository.checkAndSeed()
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
        viewModelScope.launch {
            delay(3500)
            if (_toastMessage.value == msg) {
                _toastMessage.value = null
            }
        }
    }

    fun navigateTo(dest: NavDestination) {
        _isStudyActive.value = false
        _isSettingsOpen.value = false
        _activeSheet.value = null
        if (dest != NavDestination.SUBJECTS) {
            _isViewingSubjectNotes.value = false
        }
        _currentDestination.value = dest
    }

    fun openSubjectNotebook(subjectId: String) {
        _activeSubjectId.value = subjectId
        _isViewingSubjectNotes.value = true
        _currentDestination.value = NavDestination.SUBJECTS
        _noteFilter.value = "All"
        _noteSearch.value = ""
    }

    fun closeSubjectNotebook() {
        _isViewingSubjectNotes.value = false
    }

    fun openStudyFlow() {
        _isStudyActive.value = true
        _activeSheet.value = null
        _studyStep.value = 0
        _quizQuestionIndex.value = 0
        _quizSelectedOption.value = null
        _isQuizAnswerChecked.value = false
        _quizAnswers.value = emptyList()
    }

    fun exitStudyFlow() {
        pauseTimer()
        _isStudyActive.value = false
    }

    fun openSettings() {
        _isSettingsOpen.value = true
    }

    fun closeSettings() {
        _isSettingsOpen.value = false
    }

    fun openSetupScreen() {
        _isSettingsOpen.value = false
        _isSetupScreenOpen.value = true
    }

    fun closeSetupScreen() {
        _isSetupScreenOpen.value = false
    }

    fun openSheet(sheet: ActiveSheet) {
        when (sheet) {
            is ActiveSheet.BriefCapture -> {
                val currentText = if (sheet.subjectKey == "graphics") {
                    userProfile.value.graphicsBrief
                } else {
                    userProfile.value.electricalBrief
                }
                _briefInputText.value = currentText
            }
            is ActiveSheet.NoteEditor -> {
                _editorTipId.value = sheet.tipId
                _editorSubjectId.value = sheet.subjectId
                if (sheet.tipId != null) {
                    val existing = allTips.value.find { it.id == sheet.tipId }
                    _editorText.value = existing?.text ?: ""
                    _editorOriginalText.value = existing?.text ?: ""
                    _editorCategory.value = existing?.kind ?: "Answer writing"
                } else {
                    _editorText.value = ""
                    _editorOriginalText.value = ""
                    _editorCategory.value = "Answer writing"
                }
            }
            else -> {}
        }
        _activeSheet.value = sheet
    }

    fun requestCloseSheet() {
        val current = _activeSheet.value
        if (current is ActiveSheet.NoteEditor && _editorText.value != _editorOriginalText.value && _editorText.value.isNotBlank()) {
            _showUnsavedDialog.value = true
            return
        }
        _showUnsavedDialog.value = false
        _activeSheet.value = null
    }

    fun dismissUnsavedDialog(discardDraft: Boolean) {
        _showUnsavedDialog.value = false
        if (discardDraft) {
            _activeSheet.value = null
        }
    }

    fun updateEditorText(text: String) {
        _editorText.value = text
    }

    fun updateEditorCategory(cat: String) {
        _editorCategory.value = cat
    }

    fun updateEditorSubject(subId: String) {
        _editorSubjectId.value = subId
    }

    fun saveTipFromEditor() {
        val text = _editorText.value.trim()
        if (text.isEmpty()) {
            showToast("Add a few words before saving this tip.")
            return
        }

        viewModelScope.launch {
            val tipId = _editorTipId.value
            val existing = allTips.value.find { it.id == tipId }
            repository.saveTip(
                id = tipId,
                subjectId = _editorSubjectId.value,
                kind = _editorCategory.value,
                text = text,
                pinned = existing?.pinned ?: false
            )
            val subName = subjects.value.find { it.id == _editorSubjectId.value }?.name ?: "Subject"
            showToast("Saved in $subName.")
            _activeSheet.value = null
            _isViewingSubjectNotes.value = true
            _activeSubjectId.value = _editorSubjectId.value
            _currentDestination.value = NavDestination.SUBJECTS
        }
    }

    fun togglePin(tip: ClassTipEntity) {
        viewModelScope.launch {
            repository.togglePin(tip)
        }
    }

    fun confirmDeleteTip(tip: ClassTipEntity) {
        viewModelScope.launch {
            repository.deleteTip(tip.id)
            _activeSheet.value = null
            showToast("Tip deleted.")
        }
    }

    fun updateBriefInput(text: String) {
        _briefInputText.value = text
    }

    fun saveBrief(subjectKey: String) {
        val text = _briefInputText.value.trim()
        if (text.isEmpty()) {
            showToast("Add the actual assignment questions first.")
            return
        }
        viewModelScope.launch {
            repository.saveBrief(subjectKey, text, userProfile.value)
            _activeSheet.value = null
            showToast("Brief captured. The next action is clearer.")
        }
    }

    fun chooseRescheduleSlot(slotTime: String) {
        viewModelScope.launch {
            repository.updateScheduledSlot(slotTime, userProfile.value)
            _activeSheet.value = null
            showToast("Moved to ${PlannerEngine.formatTime12h(slotTime)}. Your protected commitments stay clear.")
        }
    }

    fun setActiveDate(day: Int) {
        viewModelScope.launch {
            repository.updateActiveDate(day, userProfile.value)
        }
    }

    fun updateProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            repository.updateProfile(profile)
            showToast("Your starting point is saved.")
        }
    }

    fun clearSampleNotes() {
        viewModelScope.launch {
            repository.clearSampleTips()
            showToast("Example class notes removed. Your own notes are kept.")
        }
    }

    fun addSubject(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            showToast("Enter a subject name.")
            return
        }
        if (subjects.value.any { it.name.equals(trimmed, ignoreCase = true) }) {
            showToast("That subject already has a notebook.")
            return
        }
        viewModelScope.launch {
            repository.addSubject(trimmed)
            _activeSheet.value = null
            showToast("Subject added.")
        }
    }

    fun setSubjectSearch(query: String) {
        _subjectSearch.value = query
    }

    fun setNoteSearch(query: String) {
        _noteSearch.value = query
    }

    fun setNoteFilter(filter: String) {
        _noteFilter.value = filter
    }

    // Timer controls
    fun toggleTimer() {
        if (_isTimerRunning.value) {
            pauseTimer()
        } else {
            startTimer()
        }
    }

    private fun startTimer() {
        if (_timerSeconds.value <= 0) _timerSeconds.value = 1500
        timerEndTime = System.currentTimeMillis() + _timerSeconds.value * 1000L
        _isTimerRunning.value = true

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value && System.currentTimeMillis() < timerEndTime) {
                val remaining = ((timerEndTime - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
                _timerSeconds.value = remaining
                delay(500)
            }
            if (_isTimerRunning.value) {
                _timerSeconds.value = 0
                _isTimerRunning.value = false
                showToast("Focus window complete. Check your understanding when you’re ready.")
            }
        }
    }

    private fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        val remaining = ((timerEndTime - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
        _timerSeconds.value = remaining
    }

    // Quiz controls
    fun selectQuizOption(index: Int) {
        if (!_isQuizAnswerChecked.value) {
            _quizSelectedOption.value = index
        }
    }

    fun checkQuizAnswer(correctIndex: Int) {
        if (_quizSelectedOption.value != null && !_isQuizAnswerChecked.value) {
            _isQuizAnswerChecked.value = true
            val isCorrect = (_quizSelectedOption.value == correctIndex)
            _quizAnswers.value = _quizAnswers.value + isCorrect
        }
    }

    fun nextQuizQuestion(totalQuestions: Int) {
        if (_quizQuestionIndex.value < totalQuestions - 1) {
            _quizQuestionIndex.value += 1
            _quizSelectedOption.value = null
            _isQuizAnswerChecked.value = false
        } else {
            // Completed quiz! Record session locally
            val correctCount = _quizAnswers.value.count { it }
            viewModelScope.launch {
                repository.recordSession(
                    subjectId = "python",
                    score = correctCount,
                    total = totalQuestions,
                    isSample = false
                )
            }
            _studyStep.value = 2 // Result screen
        }
    }

    fun reviewLesson() {
        _studyStep.value = 0
    }
}
