package com.example

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.EvolveDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Real Room opens an original-install schema and validates both production migrations. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DatabaseUpgradeTest {
    @Test fun upgradePreservesExistingRecordsAndValidatesTheNewSchema() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val path = context.getDatabasePath("evolve_academic.db")
        context.deleteDatabase(path.name)
        path.parentFile?.mkdirs()
        val exactText = "    if ready:\n        print(\"✓\")\n\n"
        SQLiteDatabase.openOrCreateDatabase(path,null).use { legacy ->
            // Schema shipped before evidenceType, study-window/deadline columns and tasks.
            legacy.execSQL("CREATE TABLE subjects (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, subtitle TEXT NOT NULL, icon TEXT NOT NULL, orderIndex INTEGER NOT NULL)")
            legacy.execSQL("CREATE TABLE class_tips (id TEXT NOT NULL PRIMARY KEY, subjectId TEXT NOT NULL, kind TEXT NOT NULL, text TEXT NOT NULL, pinned INTEGER NOT NULL, date TEXT NOT NULL, isSample INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
            legacy.execSQL("CREATE TABLE study_sessions (id TEXT NOT NULL PRIMARY KEY, subjectId TEXT NOT NULL, score INTEGER NOT NULL, total INTEGER NOT NULL, date TEXT NOT NULL, isSample INTEGER NOT NULL, timestamp INTEGER NOT NULL)")
            legacy.execSQL("CREATE TABLE user_profile (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL, examDate TEXT NOT NULL, collegeStart TEXT NOT NULL, collegeEnd TEXT NOT NULL, gymStart TEXT NOT NULL, gymEnd TEXT NOT NULL, callStart TEXT NOT NULL, remindersEnabled INTEGER NOT NULL, reducedMotion INTEGER NOT NULL, scheduledSlot TEXT NOT NULL, graphicsBrief TEXT NOT NULL, electricalBrief TEXT NOT NULL, activeDate INTEGER NOT NULL)")
            legacy.insertOrThrow("subjects",null,ContentValues().apply {
                put("id","python"); put("name","My Python class"); put("subtitle","Keep this title")
                put("icon","code"); put("orderIndex",0)
            })
            legacy.insertOrThrow("class_tips",null,ContentValues().apply {
                put("id","existing-note"); put("subjectId","python"); put("kind","Answer writing")
                put("text",exactText); put("pinned",1); put("date","2026-10-16")
                put("isSample",0); put("createdAt",123L)
            })
            legacy.insertOrThrow("study_sessions",null,ContentValues().apply {
                put("id","existing-result"); put("subjectId","python"); put("score",2); put("total",3)
                put("date","2026-10-16"); put("isSample",0); put("timestamp",456L)
            })
            legacy.insertOrThrow("user_profile",null,ContentValues().apply {
                put("id",1); put("name","Keep my profile"); put("examDate","")
                put("collegeStart","07:45"); put("collegeEnd","14:30")
                put("gymStart","20:00"); put("gymEnd","21:00"); put("callStart","22:30")
                put("remindersEnabled",0); put("reducedMotion",1); put("scheduledSlot","16:30")
                put("graphicsBrief","Actual drawing questions"); put("electricalBrief",""); put("activeDate",16)
            })
            legacy.version = 1
        }
        val upgraded = EvolveDatabase.getDatabase(context,CoroutineScope(Dispatchers.IO))
        try {
            assertEquals(4,upgraded.openHelper.writableDatabase.version)
            val note = upgraded.classTipDao().getById("existing-note")!!
            assertEquals(exactText,note.text)
            assertTrue(note.pinned)
            assertEquals("Answer writing",note.kind)
            assertEquals(123L,note.createdAt)
            val profile = upgraded.userProfileDao().getNow()!!
            assertEquals("Keep my profile",profile.name)
            assertTrue(profile.reducedMotion)
            assertEquals("Actual drawing questions",profile.graphicsBrief)
            assertEquals("16:00",profile.studyStart)
            assertEquals("",profile.scheduledDate)
            val result = upgraded.studySessionDao().getNow().single()
            assertEquals(2,result.score); assertEquals(3,result.total)
            assertEquals("legacy",result.evidenceType)
            assertEquals("My Python class",upgraded.subjectDao().getNow().single().name)
            assertTrue(upgraded.taskDao().getNow().isEmpty())
            assertNull(upgraded.curriculumDao().semesterNow())
            assertTrue(upgraded.curriculumDao().attemptsNow().isEmpty())
            val repo = com.example.data.repository.EvolveRepository(upgraded)
            val packJson = context.assets.open("semester/s1.json").bufferedReader().use { it.readText() }
            repo.acceptSemester(packJson)
            assertEquals("",upgraded.curriculumDao().semesterNow()!!.examJson)
            val portion = com.example.domain.ExamPortion("Internal 2",true,mapOf("a105mat01-m3" to setOf("Gradient")))
            repo.saveExam(portion,"2026-11-07",20,20)
            val saved = upgraded.curriculumDao().semesterNow()!!
            assertEquals(portion,com.example.domain.CurriculumCodec.accepted(saved).exam)
            assertEquals("2026-11-07",upgraded.userProfileDao().getNow()!!.examDate)
            assertTrue(upgraded.userProfileDao().getNow()!!.reducedMotion)
            assertEquals(exactText,upgraded.classTipDao().getById("existing-note")!!.text)
            val exported = com.example.data.repository.BackupCodec.decode(repo.exportCurrentBackup())
            assertEquals(portion,com.example.domain.CurriculumCodec.accepted(exported.semester!!).exam)
            try {
                repo.saveExam(portion.copy(topics = mapOf("missing" to setOf("Unknown"))),"2026-11-09",10,30)
                fail("Invalid portion must not change either the exam or profile")
            } catch (_: Exception) { /* Transaction must keep both old values. */ }
            assertEquals(saved,upgraded.curriculumDao().semesterNow())
            assertEquals("2026-11-07",upgraded.userProfileDao().getNow()!!.examDate)
        } finally { upgraded.close() }
    }
}
