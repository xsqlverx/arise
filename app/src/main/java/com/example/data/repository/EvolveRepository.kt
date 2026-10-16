package com.example.data.repository

import com.example.data.db.EvolveDatabase
import com.example.data.db.populateInitialData
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class EvolveRepository(private val db: EvolveDatabase) {

    val subjects: Flow<List<SubjectEntity>> = db.subjectDao().getAllSubjects()
    val allTips: Flow<List<ClassTipEntity>> = db.classTipDao().getAllTips()
    val sessions: Flow<List<StudySessionEntity>> = db.studySessionDao().getAllSessions()
    val userProfile: Flow<UserProfileEntity?> = db.userProfileDao().getProfile()

    suspend fun checkAndSeed() {
        if (db.subjectDao().count() == 0) {
            populateInitialData(db)
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
        val tipId = id ?: ("tip-" + UUID.randomUUID().toString())
        val tip = ClassTipEntity(
            id = tipId,
            subjectId = subjectId,
            kind = kind,
            text = text,
            pinned = pinned,
            date = date,
            isSample = false,
            createdAt = System.currentTimeMillis()
        )
        db.classTipDao().insert(tip)
    }

    suspend fun togglePin(tip: ClassTipEntity) {
        db.classTipDao().update(tip.copy(pinned = !tip.pinned))
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

    suspend fun recordSession(subjectId: String, score: Int, total: Int, isSample: Boolean = false) {
        val session = StudySessionEntity(
            id = "session-" + UUID.randomUUID().toString(),
            subjectId = subjectId,
            score = score,
            total = total,
            date = java.time.LocalDate.now().toString(),
            isSample = isSample,
            timestamp = System.currentTimeMillis()
        )
        db.studySessionDao().insert(session)
    }

    suspend fun updateProfile(profile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdate(profile)
    }

    suspend fun saveBrief(subjectKey: String, briefText: String, currentProfile: UserProfileEntity) {
        val updated = if (subjectKey == "graphics") {
            currentProfile.copy(graphicsBrief = briefText)
        } else {
            currentProfile.copy(electricalBrief = briefText)
        }
        db.userProfileDao().insertOrUpdate(updated)
    }

    suspend fun updateScheduledSlot(slot: String, currentProfile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdate(currentProfile.copy(scheduledSlot = slot))
    }

    suspend fun updateActiveDate(date: Int, currentProfile: UserProfileEntity) {
        db.userProfileDao().insertOrUpdate(currentProfile.copy(activeDate = date))
    }

    suspend fun restoreDefaults() {
        db.classTipDao().deleteSamples()
        populateInitialData(db)
    }

    fun exportBackupJson(
        subjectsList: List<SubjectEntity>,
        tipsList: List<ClassTipEntity>,
        sessionsList: List<StudySessionEntity>,
        profile: UserProfileEntity
    ): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val pObj = JSONObject().apply {
            put("name", profile.name)
            put("examDate", profile.examDate)
            put("collegeStart", profile.collegeStart)
            put("collegeEnd", profile.collegeEnd)
            put("gymStart", profile.gymStart)
            put("gymEnd", profile.gymEnd)
            put("callStart", profile.callStart)
            put("remindersEnabled", profile.remindersEnabled)
            put("reducedMotion", profile.reducedMotion)
            put("scheduledSlot", profile.scheduledSlot)
            put("graphicsBrief", profile.graphicsBrief)
            put("electricalBrief", profile.electricalBrief)
        }
        root.put("profile", pObj)

        val sArray = JSONArray()
        subjectsList.forEach { s ->
            sArray.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("subtitle", s.subtitle)
                put("icon", s.icon)
                put("orderIndex", s.orderIndex)
            })
        }
        root.put("subjects", sArray)

        val tArray = JSONArray()
        tipsList.forEach { t ->
            tArray.put(JSONObject().apply {
                put("id", t.id)
                put("subjectId", t.subjectId)
                put("kind", t.kind)
                put("text", t.text)
                put("pinned", t.pinned)
                put("date", t.date)
                put("isSample", t.isSample)
                put("createdAt", t.createdAt)
            })
        }
        root.put("notes", tArray)

        val sessArray = JSONArray()
        sessionsList.forEach { sess ->
            sessArray.put(JSONObject().apply {
                put("id", sess.id)
                put("subjectId", sess.subjectId)
                put("score", sess.score)
                put("total", sess.total)
                put("date", sess.date)
                put("isSample", sess.isSample)
                put("timestamp", sess.timestamp)
            })
        }
        root.put("sessions", sessArray)

        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            if (root.optInt("version", 0) != 1) return false

            if (root.has("profile")) {
                val p = root.getJSONObject("profile")
                val entity = UserProfileEntity(
                    id = 1,
                    name = p.optString("name", ""),
                    examDate = p.optString("examDate", "2026-11-07"),
                    collegeStart = p.optString("collegeStart", "07:45"),
                    collegeEnd = p.optString("collegeEnd", "14:30"),
                    gymStart = p.optString("gymStart", "20:00"),
                    gymEnd = p.optString("gymEnd", "21:00"),
                    callStart = p.optString("callStart", "22:30"),
                    remindersEnabled = p.optBoolean("remindersEnabled", true),
                    reducedMotion = p.optBoolean("reducedMotion", false),
                    scheduledSlot = p.optString("scheduledSlot", "15:30"),
                    graphicsBrief = p.optString("graphicsBrief", ""),
                    electricalBrief = p.optString("electricalBrief", ""),
                    activeDate = 16
                )
                db.userProfileDao().insertOrUpdate(entity)
            }

            if (root.has("subjects")) {
                val sArr = root.getJSONArray("subjects")
                val list = mutableListOf<SubjectEntity>()
                for (i in 0 until sArr.length()) {
                    val s = sArr.getJSONObject(i)
                    list.add(
                        SubjectEntity(
                            id = s.getString("id"),
                            name = s.getString("name"),
                            subtitle = s.optString("subtitle", ""),
                            icon = s.optString("icon", "notes"),
                            orderIndex = s.optInt("orderIndex", i)
                        )
                    )
                }
                db.subjectDao().insertAll(list)
            }

            if (root.has("notes")) {
                val nArr = root.getJSONArray("notes")
                val list = mutableListOf<ClassTipEntity>()
                for (i in 0 until nArr.length()) {
                    val n = nArr.getJSONObject(i)
                    list.add(
                        ClassTipEntity(
                            id = n.getString("id"),
                            subjectId = n.getString("subjectId"),
                            kind = n.optString("kind", "General"),
                            text = n.getString("text"),
                            pinned = n.optBoolean("pinned", false),
                            date = n.optString("date", "2026-10-16"),
                            isSample = n.optBoolean("isSample", false),
                            createdAt = n.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
                db.classTipDao().insertAll(list)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
