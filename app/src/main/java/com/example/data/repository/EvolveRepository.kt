package com.example.data.repository

import com.example.data.db.EvolveDatabase
import com.example.data.db.populateInitialData
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.AcademicTaskEntity
import com.example.domain.PlannerEngine
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class EvolveRepository(private val db: EvolveDatabase) {

    val subjects: Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()
    val allTips: Flow<List<ClassTipEntity>> = db.classTipDao().getAllTips()
    val sessions: Flow<List<StudySessionEntity>> = db.studySessionDao().getAllSessions()
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getProfile()
    val tasks: Flow<List<AcademicTaskEntity>> = db.taskDao().observeAll()

    suspend fun checkAndSeed() {
        db.withTransaction {
            if (db.subjectDao().count() == 0) populateInitialData(db)
        }
    }

    suspend fun saveTip(
        id: String?,
        subjectId: String,
        kind: String,
        text: String,
        pinned: Boolean = false,
        date: String = java.time.LocalDate.now().toString()
    ) {
        require(text.isNotBlank())
        val tipId = id ?: ("tip-" + UUID.randomUUID().toString())
        val existing = id?.let { db.classTipDao().getById(it) }
        val tip = ClassTipEntity(
            id = tipId,
            subjectId = subjectId,
            kind = kind,
            text = text,
            pinned = pinned,
            date = existing?.date ?: date,
            isSample = false,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )
        db.classTipDao().insert(tip)
    }

    suspend fun togglePin(tip: ClassTipEntity) {
        db.classTipDao().togglePin(tip.id)
    }

    suspend fun deleteTip(tipId: String) {
        db.classTipDao().deleteById(tipId)
    }

    suspend fun clearSampleTips() {
        db.classTipDao().deleteSamples()
    }

    suspend fun addSubject(name: String, subtitle: String = "Your class notebook", icon: String = "notes") {
        val newSubject = SubjectEntity(
            id = "sub-" + UUID.randomUUID().toString().take(8),
            name = name,
            subtitle = subtitle,
            icon = icon,
            orderIndex = db.subjectDao().count()
        )
        db.subjectDao().insert(newSubject)
    }

    suspend fun recordSession(subjectId: String, score: Int, total: Int, isSample: Boolean = false,
        attemptId: String = UUID.randomUUID().toString(), evidenceType: String = "sample_quiz") {
        require(total > 0 && score in 0..total)
        val session = StudySessionEntity(
            id = attemptId,
            subjectId = subjectId,
            score = score,
            total = total,
            date = java.time.LocalDate.now().toString(),
            isSample = isSample,
            timestamp = System.currentTimeMillis(),
            evidenceType = evidenceType
        )
        db.studySessionDao().insertOnce(session)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        require(PlannerEngine.validateProfile(profile) == null)
        db.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun saveTask(task: AcademicTaskEntity) = db.taskDao().save(task)
    suspend fun completeTask(id: String, completed: Boolean) = db.taskDao().setCompleted(id, completed)

    suspend fun saveBrief(subjectKey: String, briefText: String, currentProfile: UserProfileEntity) {
        val updated = if (subjectKey == "graphics") {
            currentProfile.copy(graphicsBrief = briefText)
        } else {
            currentProfile.copy(electricalBrief = briefText)
        }
        db.userProfileDao().insertOrUpdate(updated)
    }

    suspend fun updateScheduledSlot(slot: String, date: String, currentProfile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdate(currentProfile.copy(scheduledSlot = slot, scheduledDate = date))
    }

    fun exportBackupJson(subjectsList: List<SubjectEntity>, tipsList: List<ClassTipEntity>,
        sessionsList: List<StudySessionEntity>, profile: UserProfileEntity,
        taskList: List<AcademicTaskEntity> = emptyList()) = BackupCodec.encode(subjectsList, tipsList, sessionsList, profile, taskList)

    fun readBackup(json: String) = BackupCodec.decode(json)

    suspend fun importBackup(backup: Backup) {
        db.withTransaction {
            db.subjectDao().insertAll(backup.subjects)
            db.classTipDao().insertAll(backup.notes)
            db.studySessionDao().insertAll(backup.sessions)
            db.taskDao().saveAll(backup.tasks)
            db.userProfileDao().insertOrUpdate(backup.profile.copy(remindersEnabled = false))
        }
    }
}
