package com.example.data.repository

import com.example.data.model.*
import com.example.domain.PlannerEngine
import org.json.JSONArray
import org.json.JSONObject

data class Backup(val profile: UserProfileEntity, val subjects: List<SubjectEntity>,
        val notes: List<ClassTipEntity>, val sessions: List<StudySessionEntity>, val tasks: List<AcademicTaskEntity>) {
        val summary: String get() = "${subjects.size} subjects, ${notes.size} notes, ${sessions.size} checks and ${tasks.size} tasks. Matching IDs are replaced; other records are kept. Settings are restored."
    }


object BackupCodec {
    fun encode(
        subjectsList: List<SubjectEntity>,
        tipsList: List<ClassTipEntity>,
        sessionsList: List<StudySessionEntity>,
        profile: UserProfileEntity,
        taskList: List<AcademicTaskEntity> = emptyList()
    ): String {
        val root = JSONObject()
        root.put("version", 2)
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
            put("studyStart", profile.studyStart)
            put("studyEnd", profile.studyEnd)
            put("graphicsDueDate", profile.graphicsDueDate)
            put("electricalDueDate", profile.electricalDueDate)
            put("scheduledDate", profile.scheduledDate)
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
                put("evidenceType", sess.evidenceType)
            })
        }
        root.put("sessions", sessArray)
        root.put("tasks", JSONArray().apply {
            taskList.forEach { task -> put(JSONObject().apply {
                put("id", task.id); put("subjectId", task.subjectId); put("title", task.title)
                put("instructions", task.instructions); put("dueDate", task.dueDate)
                put("durationMinutes", task.durationMinutes); put("completed", task.completed); put("createdAt", task.createdAt)
            }) }
        })

        return root.toString(2)
    }

    fun decode(json: String): Backup {
        require(json.length <= 5_000_000) { "Backup is too large." }
        val root = JSONObject(json)
        require(root.get("version") is Number && root.getInt("version") in 1..2) { "Unsupported backup version." }
        fun JSONObject.text(key: String, fallback: String? = null): String {
            if (!has(key) && fallback != null) return fallback
            require(get(key) is String) { "Invalid $key" }
            return getString(key)
        }
        fun JSONObject.flag(key: String, fallback: Boolean = false): Boolean {
            if (!has(key)) return fallback
            require(get(key) is Boolean) { "Invalid $key" }
            return getBoolean(key)
        }
        fun JSONObject.number(key: String, fallback: Long = 0): Long {
            if (!has(key)) return fallback
            val value = get(key)
            require(value is Number && value.toDouble() == value.toLong().toDouble()) { "Invalid $key" }
            require(value.toLong() in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong() || key in listOf("createdAt", "timestamp")) { "Number out of range: $key" }
            return value.toLong()
        }
        fun <T> rows(key: String, parse: (JSONObject) -> T): List<T> {
            val array = root.getJSONArray(key)
            require(array.length() <= 20000)
            return (0 until array.length()).map { parse(array.getJSONObject(it)) }
        }
        val p = root.getJSONObject("profile")
        val profile = UserProfileEntity(name=p.text("name", ""), examDate=p.text("examDate", ""),
            collegeStart=p.text("collegeStart", "07:45"), collegeEnd=p.text("collegeEnd", "14:30"),
            gymStart=p.text("gymStart", "20:00"), gymEnd=p.text("gymEnd", "21:00"), callStart=p.text("callStart", "22:30"),
            remindersEnabled=p.flag("remindersEnabled"), reducedMotion=p.flag("reducedMotion"), scheduledSlot=p.text("scheduledSlot", "15:30"),
            graphicsBrief=p.text("graphicsBrief", ""), electricalBrief=p.text("electricalBrief", ""),
            studyStart=p.text("studyStart", "16:00"), studyEnd=p.text("studyEnd", "22:00"),
            graphicsDueDate=p.text("graphicsDueDate", ""), electricalDueDate=p.text("electricalDueDate", ""),
            scheduledDate=p.text("scheduledDate", ""))
        require(PlannerEngine.validateProfile(profile) == null) { "Invalid dates or protected times." }
        val subjects = rows("subjects") { SubjectEntity(it.text("id"), it.text("name"), it.text("subtitle", ""), it.text("icon", "notes"), it.number("orderIndex").toInt()) }
        val notes = rows("notes") { ClassTipEntity(it.text("id"), it.text("subjectId"), it.text("kind", "General"), it.text("text"), it.flag("pinned"), it.text("date"), it.flag("isSample"), it.number("createdAt")) }
        val sessions = rows("sessions") { StudySessionEntity(it.text("id"), it.text("subjectId"), it.number("score").toInt(), it.number("total").toInt(), it.text("date"), it.flag("isSample"), it.number("timestamp"), it.text("evidenceType", "legacy")) }
        val tasks = if (root.getInt("version") == 2) rows("tasks") { AcademicTaskEntity(it.text("id"), it.text("subjectId"), it.text("title"), it.text("instructions"), it.text("dueDate", ""), it.number("durationMinutes", 25).toInt(), it.flag("completed"), it.number("createdAt")) } else emptyList()
        fun ids(values: List<String>) { require(values.all { it.isNotBlank() && it.length <= 200 } && values.distinct().size == values.size) { "Invalid or duplicate IDs." } }
        ids(subjects.map { it.id }); ids(notes.map { it.id }); ids(sessions.map { it.id }); ids(tasks.map { it.id })
        require(subjects.isNotEmpty()) { "No subject records in backup." }
        require(profile.scheduledDate.isBlank() || PlannerEngine.date(profile.scheduledDate) != null)
        val subjectIds = subjects.map { it.id }.toSet()
        require(subjects.all { it.name.isNotBlank() })
        require(notes.all { it.subjectId in subjectIds && it.text.isNotBlank() && PlannerEngine.date(it.date) != null })
        require(sessions.all { it.subjectId in subjectIds && it.total in 0..10000 && it.score in 0..it.total && PlannerEngine.date(it.date) != null })
        require(tasks.all { it.subjectId in subjectIds && it.title.isNotBlank() && it.durationMinutes in 5..120 && (it.dueDate.isBlank() || PlannerEngine.date(it.dueDate) != null) })
        return Backup(profile, subjects, notes, sessions, tasks)
    }

}
