package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.WorkspaceState
import com.example.data.db.EvolveDatabase
import com.example.data.model.*
import com.example.data.repository.EvolveRepository
import com.example.domain.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID

data class CheckpointDraft(val moduleId: String, val id: String = UUID.randomUUID().toString(),
    val step: Int = 0, val recall: String = "", val choices: List<Int> = emptyList(),
    val selected: Int = -1, val checked: Boolean = false)
data class SyllabusPage(val source: CourseSource, val page: Int)

class CurriculumViewModel(application: Application) : AndroidViewModel(application) {
    private val db = EvolveDatabase.getDatabase(application, viewModelScope)
    private val repo = EvolveRepository(db)
    private val workspace = WorkspaceState(application)
    private val writes = Mutex()
    private val latestDrafts = mutableMapOf<String, CheckpointDraft>()
    private val _open = MutableStateFlow(false)
    val open = _open.asStateFlow()
    private val _draft = MutableStateFlow<CheckpointDraft?>(null)
    val draft = _draft.asStateFlow()
    private val _document = MutableStateFlow<SyllabusPage?>(null)
    val document = _document.asStateFlow()
    private val _preview = MutableStateFlow<SemesterPack?>(null)
    val preview = _preview.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _feedback = MutableStateFlow<String?>(null)
    val feedback = _feedback.asStateFlow()
    val row = repo.semester.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val semester = repo.semester.map { it?.let(CurriculumCodec::accepted) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val attempts = repo.curriculumAttempts.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch {
            try { _preview.value = withContext(Dispatchers.IO) { CurriculumCodec.decode(assetJson()) } }
            catch (e: Exception) { if (e is CancellationException) throw e; _feedback.value = "The semester pack could not open. Reopen the app to retry." }
        }
    }
    private fun assetJson() = getApplication<Application>().assets.open("semester/s1.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
    fun show() { _open.value = true; _feedback.value = null }
    fun close() { _document.value = null; _draft.value = null; _open.value = false; focus(false) }
    fun back() {
        if (_busy.value) return
        when {
            _document.value != null -> _document.value = null
            _draft.value != null -> { _draft.value = null; focus(false) }
            else -> _open.value = false
        }
    }
    fun openDocument(source: CourseSource, page: Int) { _document.value = SyllabusPage(source, page.coerceIn(1, source.pages)) }
    private fun action(block: suspend () -> Unit) {
        if (_busy.value) return
        _busy.value = true; _feedback.value = null
        viewModelScope.launch {
            try { block() }
            catch (e: Exception) { if (e is CancellationException) throw e; _feedback.value = "Could not save this change. Your saved work is kept; retry here." }
            finally { _busy.value = false }
        }
    }
    fun importSemester() = action {
        val json = withContext(Dispatchers.IO) {
            val text = assetJson(); val pack = CurriculumCodec.decode(text)
            pack.courses.forEach { course ->
                val bytes = getApplication<Application>().assets.open(course.source.asset).use { it.readBytes() }
                val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
                check(hash == course.source.sha256) { "Source mismatch" }
            }; text
        }
        repo.acceptSemester(json)
        _feedback.value = "Seven courses imported. Your notes are kept. Choose Start from Today, or open a module here."
    }
    fun setFocus(id: String, enabled: Boolean) = action {
        val current = db.curriculumDao().semesterNow() ?: return@action
        val accepted = CurriculumCodec.accepted(current)
        val next = if (enabled) accepted.focus + id else accepted.focus - id
        repo.saveSemester(current.copy(focusJson = JSONArray(next.sorted()).toString()))
    }
    fun saveMarks(ee: String, ec: String) {
        val electrical = ee.toIntOrNull(); val electronics = ec.toIntOrNull()
        if (electrical == null || electronics == null || electrical !in 0..100 || electronics !in 0..100) {
            _feedback.value = "Use whole marks between 0 and 100."; return
        }
        action { val current = db.curriculumDao().semesterNow() ?: return@action
            repo.saveSemester(current.copy(electricalMarks = electrical, electronicsMarks = electronics))
            _feedback.value = "Marks saved. Module selections control the focus; neither component is silently removed."
        }
    }
    fun startModule(id: String, review: Boolean = false) {
        show()
        if (_busy.value) return
        action {
            check(db.curriculumDao().semesterNow()?.let(CurriculumCodec::accepted)?.pack?.modules?.any { it.id == id } == true)
            check(StarterLessons.forModule(id) != null)
            val state = withContext(Dispatchers.IO) { workspace.read("checkpoint-$id") }
            val loaded = runCatching { state?.let { s ->
                val choices = s.getJSONArray("choices").let { a -> (0 until a.length()).map { a.getInt(it) } }
                CheckpointDraft(id, s.getString("id"), s.getInt("step"), s.getString("recall"), choices, s.getInt("selected"), s.getBoolean("checked"))
            }?.also { validateDraft(it) } }.getOrNull() ?: CheckpointDraft(id)
            val resumed = if (review && loaded.step == 2) CheckpointDraft(id) else loaded
            _draft.value = resumed; latestDrafts[id] = resumed; saveDraft(resumed); focus(resumed.step != 2)
        }
    }
    private fun validateDraft(d: CheckpointDraft) {
        val lesson = StarterLessons.forModule(d.moduleId)!!
        require(d.step in 0..2 && d.choices.size <= lesson.checks.size && d.id.isNotBlank())
        require(d.choices.indices.all { d.choices[it] in lesson.checks[it].options.indices })
        require(d.step != 1 || d.choices.size < lesson.checks.size)
        require(d.step != 2 || d.choices.size == lesson.checks.size)
        if (d.step == 1) require(d.selected == -1 || d.selected in lesson.checks[d.choices.size].options.indices)
        require(!d.checked || d.selected >= 0)
    }
    private fun focus(active: Boolean) {
        viewModelScope.launch {
            try { writes.withLock { withContext(Dispatchers.IO) {
                workspace.write("curriculum-focus", if (active) JSONObject().put("until", System.currentTimeMillis()+15*60_000) else null)
            } } } catch (e: Exception) { if (e is CancellationException) throw e; _feedback.value = "Focus state could not be saved. Reminder suppression may be unavailable." }
        }
    }
    private suspend fun saveDraft(d: CheckpointDraft) = writes.withLock {
        // Skip an obsolete queued keystroke. The latest snapshot wins.
        if (latestDrafts[d.moduleId] != d) return@withLock
        withContext(Dispatchers.IO) { workspace.write("checkpoint-${d.moduleId}", JSONObject().apply {
            put("id", d.id); put("step", d.step); put("recall", d.recall); put("choices", JSONArray(d.choices))
            put("selected", d.selected); put("checked", d.checked)
        }) }
    }
    private fun change(transform: (CheckpointDraft) -> CheckpointDraft) {
        if (_busy.value) return
        val next = _draft.value?.let(transform) ?: return
        validateDraft(next); _draft.value = next; latestDrafts[next.moduleId] = next
        viewModelScope.launch { try { saveDraft(next) } catch (e: Exception) {
            if (e is CancellationException) throw e; _feedback.value = "Draft could not be preserved. Keep this session open and retry."
        } }
    }
    fun recall(text: String) { if (text.length <= 30000) change { if (it.step == 0) it.copy(recall = text) else it } }
    fun beginCheck() = change { if (it.step == 0) it.copy(step = 1) else it }
    fun select(index: Int) = change { if (it.step == 1 && !it.checked) it.copy(selected = index) else it }
    fun checkAnswer() = change { if (it.step == 1 && it.selected >= 0) it.copy(checked = true) else it }
    fun next() {
        val d = _draft.value ?: return
        if (d.step != 1 || !d.checked || _busy.value) return
        val lesson = StarterLessons.forModule(d.moduleId)!!
        val choices = d.choices + d.selected
        if (choices.size < lesson.checks.size) change { it.copy(choices = choices, selected = -1, checked = false) }
        else action {
            val timestamp = System.currentTimeMillis(); val score = lesson.score(choices)
            val a = CurriculumAttemptEntity(d.id, d.moduleId, JSONArray(choices).toString(), d.recall, score,
                choices.size, timestamp, CurriculumCodec.reviewDate(timestamp, score == choices.size))
            repo.recordCurriculumAttempt(a)
            val complete = d.copy(step = 2, choices = choices, selected = -1, checked = false)
            _draft.value = complete; latestDrafts[complete.moduleId] = complete; saveDraft(complete); focus(false)
        }
    }
    fun retry() { change { CheckpointDraft(it.moduleId, recall = it.recall) }; focus(true) }
}
