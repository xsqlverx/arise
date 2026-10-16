package com.example

import com.example.data.model.*
import com.example.data.repository.BackupCodec
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class BackupCodecTest {
    private val profile = UserProfileEntity(name = "Test", graphicsBrief = "Actual brief", studyStart = "17:00", reducedMotion = true)
    private val subjects = listOf(SubjectEntity("python", "Python", "Course", "code", 0))
    private val notes = listOf(ClassTipEntity("tip", "python", "Concepts", "    if ready:\n        print(\"✓\")\n\n", true, "2026-10-16", false, 123))
    private val sessions = listOf(StudySessionEntity("attempt", "python", 2, 3, "2026-10-16", true, 456, "sample_quiz"))
    private val tasks = listOf(AcademicTaskEntity("task", "python", "Trace my worksheet", "Open question 1", "", 25, false, 789))
    private fun encoded() = BackupCodec.encode(subjects, notes, sessions, profile, tasks)

    @Test fun allRecordsAndExactTextRoundTrip() {
        val backup = BackupCodec.decode(encoded())
        assertEquals(profile, backup.profile)
        assertEquals(subjects, backup.subjects)
        assertEquals(notes, backup.notes)
        assertEquals(sessions, backup.sessions)
        assertEquals(tasks, backup.tasks)
    }
    @Test fun olderBackupKeepsSessionsWithUnknownEvidenceSource() {
        val json = JSONObject(encoded()).put("version", 1)
        json.getJSONArray("sessions").getJSONObject(0).remove("evidenceType")
        val backup = BackupCodec.decode(json.toString())
        assertEquals("legacy", backup.sessions.single().evidenceType)
        assertTrue(backup.tasks.isEmpty())
    }
    @Test fun invalidNotePreventsProducingAnImportableBackup() {
        val json = JSONObject(encoded())
        json.getJSONArray("notes").getJSONObject(0).put("subjectId", "missing")
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode(json.toString()) }
    }
    @Test fun duplicateIdsAndWrongTypesAreRejected() {
        val json = JSONObject(encoded())
        json.getJSONArray("notes").put(json.getJSONArray("notes").getJSONObject(0))
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode(json.toString()) }
        val types = JSONObject(encoded())
        types.getJSONObject("profile").put("reducedMotion", "yes")
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode(types.toString()) }
    }
    @Test fun overflowingNumbersCannotWrapToValidDurations() {
        val json = JSONObject(encoded())
        json.getJSONArray("tasks").getJSONObject(0).put("durationMinutes", 4294967321L)
        assertThrows(IllegalArgumentException::class.java) { BackupCodec.decode(json.toString()) }
    }
}
