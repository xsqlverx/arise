package com.example

import com.example.data.model.*
import com.example.data.repository.BackupCodec
import com.example.domain.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId

class CurriculumTest {
    private val assets = listOf(File("app/src/main/assets"),File("src/main/assets")).first { it.isDirectory }
    private val json = File(assets,"semester/s1.json").readText(Charsets.UTF_8)
    private val pack = CurriculumCodec.decode(json)
    private val row = SemesterEntity(packJson = json, focusJson = JSONArray(pack.modules.filter { it.focus }.map { it.id }).toString())
    private val accepted = CurriculumCodec.accepted(row)
    private val today = LocalDate.of(2026,10,17)
    private val timestamp = today.atTime(12,0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private fun attempt(id: String, correct: Boolean = true): CurriculumAttemptEntity {
        val lesson = StarterLessons.forModule(id)!!
        val choices = lesson.checks.map { if (correct) it.answer else (it.answer+1)%it.options.size }
        val score = lesson.score(choices)
        return CurriculumAttemptEntity("attempt-$id",id,JSONArray(choices).toString(),"    exact\n\n",score,choices.size,timestamp,
            CurriculumCodec.reviewDate(timestamp,score==choices.size))
    }
    @Test fun allSevenDocumentsMatchTheirRecordedHashesAndPageCounts() {
        assertEquals(7,pack.courses.size); assertEquals(20,pack.modules.size)
        assertEquals(40,pack.courses.sumOf { it.source.pages })
        pack.courses.forEach { c ->
            val bytes=File(assets,c.source.asset).readBytes()
            assertEquals(c.source.sha256,MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) })
            assertTrue(c.modules.all { m -> m.pages.all { it in 1..c.source.pages } })
        }
    }
    @Test fun electricalAndElectronicsRemainOneCourseWithBothMarkComponentsInFocus() {
        val eee=pack.courses.single { it.id=="a105est04" }
        assertEquals(listOf("electrical","electronics"),eee.notebooks)
        assertEquals(20,accepted.electricalMarks); assertEquals(20,accepted.electronicsMarks)
        assertTrue(eee.modules.all { it.id in accepted.focus })
        assertEquals(listOf("electrical","electrical","electronics","electronics"),eee.modules.map { it.component })
    }
    @Test fun activitiesNeverAcquireInventedTheoryModules() {
        assertTrue(pack.courses.single { it.id=="a105esl07" }.modules.isEmpty())
        assertTrue(pack.courses.single { it.id=="a105hwt06" }.modules.isEmpty())
        assertFalse(pack.courses.any { "adp" in it.notebooks })
    }
    @Test fun firstUseGivesOneImportInsteadOfAnUndatedBriefGate() {
        assertEquals("semester",AcademicPlanner.next(UserProfileEntity(),emptyList(),null,emptyList(),today).actionType)
    }
    @Test fun twelveConceptsAdvanceWithoutImmediateRepetition() {
        val records=mutableListOf<CurriculumAttemptEntity>()
        repeat(12) {
            val action=AcademicPlanner.next(UserProfileEntity(),emptyList(),accepted,records,today)
            assertEquals("curriculum",action.actionType)
            assertFalse(records.any { it.moduleId==action.id })
            records += attempt(action.id)
        }
        assertEquals(StarterLessons.moduleIds,records.map { it.moduleId }.toSet())
        assertEquals("semester",AcademicPlanner.next(UserProfileEntity(),emptyList(),accepted,records,today).actionType)
    }
    @Test fun datedUrgentTaskOverridesTheCourseQueue() {
        val task=AcademicTaskEntity("due","graphics","Draw question 2","Open the actual brief",today.toString())
        val action=AcademicPlanner.next(UserProfileEntity(),listOf(task),accepted,emptyList(),today)
        assertEquals("task",action.actionType); assertEquals("due",action.id)
    }
    @Test fun requestedIndependentPracticeDoesNotSitBehindEveryStarterLesson() {
        val task=AcademicTaskEntity("practice-1","maths","Practise gradients","Open my worksheet")
        assertEquals("practice-1",AcademicPlanner.next(UserProfileEntity(),listOf(task),accepted,emptyList(),today).id)
    }
    @Test fun urgentDatedMissingBriefRemainsActionable() {
        val p=UserProfileEntity(graphicsDueDate=today.toString())
        assertEquals("brief-graphics",AcademicPlanner.next(p,emptyList(),accepted,emptyList(),today).actionType)
    }
    @Test fun changingFocusActuallyRemovesAConceptFromRecommendations() {
        val focused=accepted.copy(focus=setOf("a105est05-m3"))
        assertEquals("a105est05-m3",AcademicPlanner.next(UserProfileEntity(),emptyList(),focused,emptyList(),today).id)
    }
    @Test fun weakEvidenceReturnsTheNextDayAndCorrectEvidenceAfterThreeDays() {
        val one=accepted.copy(focus=setOf("a105mat01-m3"))
        val weak=attempt("a105mat01-m3",false)
        assertEquals("2026-10-18",weak.reviewDate)
        assertEquals("semester",AcademicPlanner.next(UserProfileEntity(),emptyList(),one,listOf(weak),today).actionType)
        assertEquals("curriculum",AcademicPlanner.next(UserProfileEntity(),emptyList(),one,listOf(weak),today.plusDays(1)).actionType)
        assertEquals("2026-10-20",attempt("a105mat01-m3").reviewDate)
    }
    @Test fun questionKeysAndScoresAreValidatedAgainstActualChoices() {
        StarterLessons.moduleIds.forEach { id ->
            val lesson=StarterLessons.forModule(id)!!
            assertEquals(2,lesson.checks.size)
            assertEquals(2,lesson.score(lesson.checks.map { it.answer }))
            assertEquals(0,lesson.score(lesson.checks.map { (it.answer+1)%it.options.size }))
            CurriculumCodec.validateAttempt(attempt(id),accepted)
        }
        assertThrows(IllegalArgumentException::class.java) {
            CurriculumCodec.validateAttempt(attempt("a105mat01-m3").copy(score=0),accepted)
        }
    }
    @Test fun semesterScopeMarksAndExactResponsesSurviveBackup() {
        val subjects=pack.courses.flatMap { it.notebooks }.distinct().mapIndexed { i,id -> SubjectEntity(id,id,"","notes",i) }
        val edited=row.copy(focusJson="[\"a105est05-m3\"]",electricalMarks=18,electronicsMarks=22)
        val attempt=attempt("a105est05-m3")
        val backup=BackupCodec.decode(BackupCodec.encode(subjects,emptyList(),emptyList(),UserProfileEntity(),emptyList(),edited,listOf(attempt)))
        val restored=backup.semester!!
        assertTrue(JSONObject(edited.packJson).similar(JSONObject(restored.packJson)))
        assertEquals(edited.copy(packJson=restored.packJson),restored)
        assertEquals(listOf(attempt),backup.curriculumAttempts)
    }
    @Test fun unsafePathsDuplicatesAndUnknownFocusAreRejectedBeforeImport() {
        val bad=JSONObject(json)
        bad.getJSONArray("courses").getJSONObject(0).getJSONObject("source").put("asset","../../secret.pdf")
        assertThrows(IllegalArgumentException::class.java) { CurriculumCodec.decode(bad.toString()) }
        assertThrows(IllegalArgumentException::class.java) { CurriculumCodec.accepted(row.copy(focusJson="[\"fake-module\"]")) }
        val duplicate=JSONObject(json); val courses=duplicate.getJSONArray("courses"); courses.put(courses.getJSONObject(0))
        assertThrows(IllegalArgumentException::class.java) { CurriculumCodec.decode(duplicate.toString()) }
    }
    @Test fun fractionalVersionsCannotMasqueradeAsAnAcceptedVersion() {
        val bad=JSONObject(json).put("version",1.5)
        assertThrows(IllegalArgumentException::class.java) { CurriculumCodec.decode(bad.toString()) }
    }
}
