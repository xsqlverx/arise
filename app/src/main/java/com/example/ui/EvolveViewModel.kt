package com.example.ui
import android.app.Application
import android.os.SystemClock
import com.example.data.WorkspaceState
import com.example.data.model.AcademicTaskEntity
import com.example.domain.QuizAttempt
import com.example.notifications.ReminderScheduler
import com.example.ui.screens.sampleQuestions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import org.json.JSONArray
import java.time.LocalDate
import java.util.UUID
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.EvolveDatabase
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import com.example.data.repository.EvolveRepository
import com.example.data.repository.Backup
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
    data class TaskEditor(val taskId: String? = null) : ActiveSheet()
    data class DeleteTip(val tip: ClassTipEntity) : ActiveSheet()
}
class EvolveViewModel(application: Application) : AndroidViewModel(application) {
    private val db = EvolveDatabase.getDatabase(application, viewModelScope)
    val repository = EvolveRepository(db)
    private val workspace = WorkspaceState(application)
    private val workspaceMutex = Mutex()
    private val revisions = mutableMapOf<String, Int>()
    private val _ready = MutableStateFlow(false)
    val ready = _ready.asStateFlow()
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate = _selectedDate.asStateFlow()
    private val _activeTaskId = MutableStateFlow<String?>(null)
    val activeTaskId = _activeTaskId.asStateFlow()
    private val _pendingBackup = MutableStateFlow<Backup?>(null)
    val pendingBackup = _pendingBackup.asStateFlow()
    private var originalCategory = "Answer writing"
    private var originalSubject = "electronics"
    private var attempt: QuizAttempt? = null
    private var completionInFlight = false
    private var endElapsed = 0L

    private fun persist(key: String, value: JSONObject?) {
        val revision = (revisions[key] ?: 0) + 1
        revisions[key] = revision
        viewModelScope.launch {
            try {
                workspaceMutex.withLock {
                    if (revisions[key] == revision) withContext(Dispatchers.IO) { workspace.write(key, value) }
                }
            } catch (e: Exception) { if (e is CancellationException) throw e; showToast("Could not preserve the draft. Keep this screen open and retry.") }
        }
    }

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
    private val _briefDueDate = MutableStateFlow("")
    val briefDueDate = _briefDueDate.asStateFlow()

    fun updateBriefDueDate(value: String) { _briefDueDate.value = value }

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
    val tasks = repository.tasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sessions: StateFlow<List<StudySessionEntity>> = repository.sessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val userProfile: StateFlow<UserProfileEntity> = repository.userProfile
        .combine(MutableStateFlow(Unit)) { profile, _ ->
            profile ?: UserProfileEntity()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserProfileEntity())

    init {
        viewModelScope.launch {
            try {
                repository.checkAndSeed()
                val draft = withContext(Dispatchers.IO) { workspace.read("draft") }
                if (draft != null) {
                    _editorTipId.value = draft.optString("id").ifBlank { null }
                    _editorSubjectId.value = draft.optString("subject", "electronics")
                    _editorCategory.value = draft.optString("category", "Answer writing")
                    _editorText.value = draft.optString("text")
                    _editorOriginalText.value = draft.optString("original")
                    originalCategory = draft.optString("originalCategory", "Answer writing")
                    originalSubject = draft.optString("originalSubject", "electronics")
                    _activeSheet.value = ActiveSheet.NoteEditor(_editorTipId.value, _editorSubjectId.value)
                }
                try { restoreStudy(withContext(Dispatchers.IO) { workspace.read("study") }) }
                catch (e: Exception) {
                    if (e is CancellationException) throw e
                    attempt = null
                    _studyStep.value = 0
                    _activeTaskId.value = null
                    _timerSeconds.value = 1500
                    persist("study", null)
                    showToast("The interrupted session could not be restored. Saved notes and results are kept.")
                }
                _ready.value = true
            } catch (e: Exception) { if (e is CancellationException) throw e; showToast("Local storage could not open. Reopen the app to retry.") }
        }
        viewModelScope.launch {
            combine(repository.userProfile.filterNotNull(), repository.tasks, repository.semester, repository.curriculumAttempts) { profile, rows, semester, attempts ->
                profile to com.example.domain.AcademicPlanner.next(profile, rows, semester?.let(com.example.domain.CurriculumCodec::accepted), attempts)
            }.collectLatest { (profile, action) -> withContext(Dispatchers.IO) {
                ReminderScheduler.sync(application, profile, action)
            } }
        }
    }

    private fun draftSnapshot() = JSONObject().apply {
        put("id", _editorTipId.value ?: ""); put("subject", _editorSubjectId.value)
        put("category", _editorCategory.value); put("text", _editorText.value)
        put("original", _editorOriginalText.value); put("originalCategory", originalCategory); put("originalSubject", originalSubject)
    }

    private fun writeDraft() = persist("draft", draftSnapshot())

    private fun studySnapshot() = JSONObject().apply {
        put("taskId", _activeTaskId.value ?: ""); put("step", _studyStep.value)
        put("seconds", _timerSeconds.value); put("running", _isTimerRunning.value)
        put("endWall", timerEndTime); put("endElapsed", endElapsed)
        put("bootOffset", System.currentTimeMillis() - SystemClock.elapsedRealtime())
        put("bootCount", workspace.bootCount)
        attempt?.let { q -> put("attempt", JSONObject().apply {
            put("id", q.id); put("index", q.index); put("selected", q.selected ?: -1)
            put("answers", JSONArray(q.answers)); put("checked", q.checked); put("complete", q.complete)
        }) }
    }

    private fun persistStudy() = persist("study", studySnapshot())

    private fun restoreStudy(state: JSONObject?) {
        if (state == null) return
        _activeTaskId.value = state.optString("taskId").ifBlank { null }
        _studyStep.value = state.optInt("step").coerceIn(0, 2)
        _timerSeconds.value = WorkspaceState.remaining(state, workspace.bootCount)
        val q = state.optJSONObject("attempt")
        if (q != null) {
            val answers = q.optJSONArray("answers") ?: JSONArray()
            attempt = QuizAttempt(q.getString("id"), sampleQuestions.map { it.correctIndex },
                q.optInt("index").coerceIn(0, sampleQuestions.lastIndex), q.optInt("selected", -1).takeIf { it in 0..2 },
                (0 until answers.length()).map { answers.getBoolean(it) }.take(sampleQuestions.size), q.optBoolean("checked"), q.optBoolean("complete"))
            syncQuiz()
        } else if (_activeTaskId.value == null) _studyStep.value = 0
        if (state.optBoolean("running") && _timerSeconds.value > 0) startTimer()
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
        pauseTimer()
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

    fun openStudyFlow() = openTaskFlow(null)

    fun openTaskFlow(taskId: String?) {
        if (_activeTaskId.value != taskId) {
            pauseTimer()
            _activeTaskId.value = taskId
            _studyStep.value = 0
            attempt = null
            _timerSeconds.value = (tasks.value.find { it.id == taskId }?.durationMinutes ?: 25) * 60
            syncQuiz()
        }
        _isStudyActive.value = true
        _activeSheet.value = null
        persistStudy()
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
        _toastMessage.value = null
        _showUnsavedDialog.value = false
        when (sheet) {
            is ActiveSheet.BriefCapture -> {
                val currentText = if (sheet.subjectKey == "graphics") {
                    userProfile.value.graphicsBrief
                } else {
                    userProfile.value.electricalBrief
                }
                _briefInputText.value = currentText
                _briefDueDate.value = if (sheet.subjectKey == "graphics") userProfile.value.graphicsDueDate else userProfile.value.electricalDueDate
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
        if (sheet is ActiveSheet.NoteEditor) {
            originalCategory = _editorCategory.value
            originalSubject = _editorSubjectId.value
            writeDraft()
        }
        _activeSheet.value = sheet
    }

    fun requestCloseSheet() {
        if (_isSaving.value) return
        val current = _activeSheet.value
        if (current is ActiveSheet.NoteEditor && (_editorText.value != _editorOriginalText.value || _editorCategory.value != originalCategory || _editorSubjectId.value != originalSubject)) {
            _showUnsavedDialog.value = true
            return
        }
        _showUnsavedDialog.value = false
        if (current is ActiveSheet.NoteEditor) persist("draft", null)
        _activeSheet.value = null
    }

    fun dismissUnsavedDialog(discardDraft: Boolean) {
        _showUnsavedDialog.value = false
        if (discardDraft && !_isSaving.value) {
            persist("draft", null)
            _activeSheet.value = null
        }
    }

    fun updateEditorText(text: String) {
        if (_isSaving.value) return
        _editorText.value = text
        writeDraft()
    }

    fun updateEditorCategory(cat: String) {
        if (_isSaving.value) return
        _editorCategory.value = cat
        writeDraft()
    }

    fun updateEditorSubject(subId: String) {
        if (_isSaving.value) return
        _editorSubjectId.value = subId
        writeDraft()
    }

    fun saveTipFromEditor() {
        if (_isSaving.value) return
        val text = _editorText.value
        if (text.isBlank()) { showToast("Add a few words before saving this tip."); return }
        val id = _editorTipId.value ?: "tip-${UUID.randomUUID()}"
        val subject = _editorSubjectId.value
        val category = _editorCategory.value
        if (subjects.value.none { it.id == subject }) { showToast("Choose a subject first."); return }
        _editorTipId.value = id
        _isSaving.value = true
        viewModelScope.launch {
            try {
                revisions["draft"] = (revisions["draft"] ?: 0) + 1
                workspaceMutex.withLock { withContext(Dispatchers.IO) { workspace.write("draft", draftSnapshot()) } }
                val existing = allTips.value.find { it.id == id }
                repository.saveTip(id, subject, category, text, existing?.pinned ?: false)
                revisions["draft"] = (revisions["draft"] ?: 0) + 1
                workspaceMutex.withLock { withContext(Dispatchers.IO) { workspace.write("draft", null) } }
                _showUnsavedDialog.value = false
                _activeSheet.value = null
                _isViewingSubjectNotes.value = true
                _activeSubjectId.value = subject
                _currentDestination.value = NavDestination.SUBJECTS
                showToast("Saved in ${subjects.value.find { it.id == subject }?.name ?: "your notebook"}.")
            } catch (e: Exception) { if (e is CancellationException) throw e; showToast("Could not save. Your draft is kept; tap Save to retry.") }
            finally { _isSaving.value = false }
        }
    }

    private fun updateRecords(block: suspend () -> Unit) {
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try { block() }
            catch (e: Exception) { if (e is CancellationException) throw e; showToast("The change could not be saved. Please retry.") }
            finally { _isSaving.value = false }
        }
    }

    fun togglePin(tip: ClassTipEntity) = updateRecords { repository.togglePin(tip) }

    fun confirmDeleteTip(tip: ClassTipEntity) = updateRecords {
        repository.deleteTip(tip.id)
        _activeSheet.value = null
        showToast("Tip deleted.")
    }

    fun updateBriefInput(text: String) {
        _briefInputText.value = text
    }

    fun saveBrief(subjectKey: String) {
        if (_isSaving.value) return
        val due = _briefDueDate.value.trim()
        if (due.isNotBlank() && PlannerEngine.date(due) == null) { showToast("Use YYYY-MM-DD, or leave the deadline unknown."); return }
        val text = _briefInputText.value
        if (text.isBlank()) {
            showToast("Add the actual assignment questions first.")
            return
        }
        _isSaving.value = true
        viewModelScope.launch {
            try {
                val profile = userProfile.value.let { if (subjectKey == "graphics") it.copy(graphicsDueDate = due) else it.copy(electricalDueDate = due) }
                repository.saveBrief(subjectKey, text, profile)
                _activeSheet.value = null
                showToast("Brief captured. Add a specific step from these questions in Plan.")
            } catch (e: Exception) { if (e is CancellationException) throw e; showToast("Could not save the brief. Your text is still here.") }
            finally { _isSaving.value = false }
        }
    }

    fun chooseRescheduleSlot(slotTime: String) = updateRecords {
        val action = com.example.domain.AcademicPlanner.next(userProfile.value, tasks.value,
            db.curriculumDao().semesterNow()?.let(com.example.domain.CurriculumCodec::accepted), db.curriculumDao().attemptsNow())
        val slot = PlannerEngine.getAvailableSlots(userProfile.value, durationMinutes = action.minutes,
            dueDate = action.dueDate).find { it.key == slotTime }
        if (slot == null) { showToast("That block is no longer available. Choose another time."); return@updateRecords }
        repository.updateScheduledSlot(slot.time24, slot.date, userProfile.value)
        _selectedDate.value = LocalDate.parse(slot.date)
        _activeSheet.value = null
        showToast("Moved to ${slot.label}. Your protected commitments stay clear.")
    }

    fun setActiveDate(day: LocalDate) { _selectedDate.value = day }

    fun updateProfile(profile: UserProfileEntity, closeSetup: Boolean = false) {
        PlannerEngine.validateProfile(profile)?.let { showToast(it); return }
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try {
                repository.updateProfile(profile)
                if (closeSetup) _isSetupScreenOpen.value = false
                showToast("Your starting point is saved.")
            } catch (e: Exception) { if (e is CancellationException) throw e; showToast("Could not save settings. Try again.") }
            finally { _isSaving.value = false }
        }
    }

    fun addCurriculumPractice(subject: String, title: String, instructions: String, onSaved: (String) -> Unit) = updateRecords {
        val id = "practice-${UUID.randomUUID()}"
        require(subjects.value.any { it.id == subject })
        repository.saveTask(AcademicTaskEntity(id,subject,title,instructions))
        showToast("Practice saved in Plan. Start with your own class question; edit the step when you have it.")
        onSaved(id)
    }

    fun clearSampleNotes() = updateRecords {
        repository.clearSampleTips()
        showToast("Example notes removed. Your own notes are kept.")
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
        updateRecords {
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



    fun saveTask(task: AcademicTaskEntity) {
        if (_isSaving.value) return
        if (task.title.isBlank() || task.instructions.isBlank()) { showToast("Add a task title and an exact first step."); return }
        if (task.durationMinutes !in 5..120 || (task.dueDate.isNotBlank() && PlannerEngine.date(task.dueDate) == null)) {
            showToast("Use 5–120 minutes and a date as YYYY-MM-DD, or leave the date blank."); return
        }
        if (subjects.value.none { it.id == task.subjectId }) { showToast("Choose a subject."); return }
        _isSaving.value = true
        viewModelScope.launch {
            try { repository.saveTask(task); _activeSheet.value = null; showToast("Your next step is ready.") }
            catch (e: Exception) { if (e is CancellationException) throw e; showToast("Could not save the task. Try again.") }
            finally { _isSaving.value = false }
        }
    }

    fun completeTask(id: String, completed: Boolean = true) {
        viewModelScope.launch {
            try { repository.completeTask(id, completed); if (completed) { exitStudyFlow(); showToast("Step completed. This records activity, not mastery.") } }
            catch (e: Exception) { if (e is CancellationException) throw e; showToast("Could not update the task. Try again.") }
        }
    }

    fun previewBackup(json: String) {
        viewModelScope.launch {
            try { _pendingBackup.value = withContext(Dispatchers.IO) { repository.readBackup(json) } }
            catch (e: Exception) { if (e is CancellationException) throw e; showToast("Backup not imported: ${e.message ?: "invalid file"}") }
        }
    }

    fun cancelImport() { if (!_isSaving.value) _pendingBackup.value = null }

    fun confirmImport() {
        val backup = _pendingBackup.value ?: return
        if (_isSaving.value) return
        _isSaving.value = true
        viewModelScope.launch {
            try { repository.importBackup(backup); _pendingBackup.value = null; showToast("Backup merged. Reminders remain off until you enable them.") }
            catch (e: Exception) { if (e is CancellationException) throw e; showToast("Nothing imported. Check the file and retry.") }
            finally { _isSaving.value = false }
        }
    }

    fun toggleTimer() { if (_isTimerRunning.value) pauseTimer() else startTimer() }

    private fun startTimer() {
        if (_timerSeconds.value <= 0) _timerSeconds.value = (tasks.value.find { it.id == _activeTaskId.value }?.durationMinutes ?: 25) * 60
        timerEndTime = System.currentTimeMillis() + _timerSeconds.value * 1000L
        endElapsed = SystemClock.elapsedRealtime() + _timerSeconds.value * 1000L
        _isTimerRunning.value = true
        persistStudy()
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                _timerSeconds.value = ((endElapsed - SystemClock.elapsedRealtime() + 999) / 1000).toInt().coerceAtLeast(0)
                if (_timerSeconds.value == 0) {
                    _isTimerRunning.value = false
                    persistStudy()
                    showToast("Focus window complete. Check your work when you’re ready.")
                    break
                }
                delay(500)
            }
        }
    }

    private fun pauseTimer() {
        if (!_isTimerRunning.value) return
        _timerSeconds.value = ((endElapsed - SystemClock.elapsedRealtime() + 999) / 1000).toInt().coerceAtLeast(0)
        _isTimerRunning.value = false
        timerJob?.cancel()
        persistStudy()
    }

    fun startQuiz() {
        if (_studyStep.value == 1 || completionInFlight || sampleQuestions.isEmpty()) return
        pauseTimer()
        attempt = QuizAttempt("attempt-${UUID.randomUUID()}", sampleQuestions.map { it.correctIndex })
        _studyStep.value = 1
        syncQuiz()
        persistStudy()
    }

    private fun syncQuiz() {
        val q = attempt
        _quizQuestionIndex.value = q?.index ?: 0
        _quizSelectedOption.value = q?.selected
        _isQuizAnswerChecked.value = q?.checked ?: false
        _quizAnswers.value = q?.answers ?: emptyList()
    }

    fun selectQuizOption(index: Int) { attempt = attempt?.select(index); syncQuiz(); persistStudy() }

    fun checkQuizAnswer(correctIndex: Int) { attempt = attempt?.check(); syncQuiz(); persistStudy() }

    fun nextQuizQuestion(totalQuestions: Int) {
        val before = attempt ?: return
        if (completionInFlight || before.complete) return
        val next = before.advance()
        if (!next.complete) { attempt = next; syncQuiz(); persistStudy(); return }
        completionInFlight = true
        _isSaving.value = true
        viewModelScope.launch {
            try {
                repository.recordSession("python", next.score, next.answerKey.size, true, next.id)
                attempt = next
                _studyStep.value = 2
                syncQuiz()
                persistStudy()
            } catch (e: Exception) { if (e is CancellationException) throw e; showToast("Result not saved. Tap See my result to retry.") }
            finally { completionInFlight = false; _isSaving.value = false }
        }
    }

    fun reviewLesson() { _studyStep.value = 0; persistStudy() }
}
