package com.example

import com.example.data.model.*
import com.example.data.repository.BackupCodec
import com.example.domain.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.time.LocalDate

class ExamPortionTest {
    private val assets = listOf(File("app/src/main/assets"),File("src/main/assets")).first { it.isDirectory }
    private val json = File(assets,"semester/s1.json").readText()
    private val pack = CurriculumCodec.decode(json)
    private val module = pack.modules.first { it.id == "a105mat01-m3" }
    private val today = LocalDate.of(2026,10,17)
    private fun portion(topic: String = "Gradient") = ExamPortion("Internal 2",true,mapOf(module.id to setOf(topic)))
    private fun row(p: ExamPortion) = SemesterEntity(packJson = json, focusJson = JSONArray(p.topics.keys.toList()).toString(), examJson = p.encode())
    private fun next(p: ExamPortion, tasks: List<AcademicTaskEntity> = emptyList()) = AcademicPlanner.next(
        UserProfileEntity(examDate = "2026-11-07"),tasks,CurriculumCodec.accepted(row(p)),emptyList(),today)

    @Test fun exactSelectedTopicsAndTeacherExtrasRoundTrip() {
        val p = portion().copy(extras = listOf(ExtraExamTopic("teacher-1","python","Teacher worksheet 7")))
        assertEquals(p,ExamPortion.decode(p.encode(),pack))
        assertEquals(2,p.topicCount)
        assertEquals(p,CurriculumCodec.accepted(row(p)).exam)
    }
    @Test fun selectingAnUnrelatedTopicDoesNotRecommendTheModulesGradientLesson() {
        val action = next(portion("Critical and saddle points"))
        assertEquals("exam-practice",action.actionType)
        assertEquals("Critical and saddle points",action.title)
        assertEquals("2026-11-07",action.dueDate)
    }
    @Test fun selectedGradientCanStillUseTheAuthoredStarter() {
        val action = next(portion())
        assertEquals("curriculum",action.actionType)
        assertEquals(module.id,action.id)
        assertEquals("2026-11-07",action.dueDate)
        assertFalse(action.copy.contains("estimated IE focus"))
    }
    @Test fun teacherOnlyPortionWorksWithoutInventingASyllabusModule() {
        val p = ExamPortion("Python internal",false,emptyMap(),listOf(ExtraExamTopic("teacher-1","python","Trace class worksheet 7")))
        val action = next(p)
        assertEquals("exam-practice",action.actionType)
        assertEquals("extra:teacher-1",action.id)
        assertTrue(CurriculumCodec.accepted(row(p)).focus.isEmpty())
    }
    @Test fun completedTopicPracticeMovesToTheNextSelectedTopic() {
        val p = portion("Critical and saddle points").copy(extras = listOf(ExtraExamTopic("teacher-1","python","Worksheet 7")))
        val complete = AcademicTaskEntity("done","maths","Practise: Critical and saddle points","Actual work",completed = true)
        assertEquals("Worksheet 7",next(p,listOf(complete)).title)
    }
    @Test fun urgentWorkStillTakesPriorityOverTheExamPortion() {
        val urgent = AcademicTaskEntity("due","graphics","Actual question 2","Open the brief",today.toString())
        assertEquals("due",next(portion(),listOf(urgent)).id)
    }
    @Test fun newBackupPreservesPortionDateAndMarks() {
        val subjects = pack.courses.flatMap { it.notebooks }.distinct().mapIndexed { i,id -> SubjectEntity(id,id,"","notes",i) }
        val encoded = BackupCodec.encode(subjects,emptyList(),emptyList(),UserProfileEntity(examDate = "2026-11-07"),semester = row(portion()))
        assertEquals(4,JSONObject(encoded).getInt("version"))
        val backup = BackupCodec.decode(encoded)
        assertEquals(portion(),CurriculumCodec.accepted(backup.semester!!).exam)
        assertEquals("2026-11-07",backup.profile.examDate)
        assertEquals(20,backup.semester!!.electronicsMarks)
    }
    @Test fun v3BackupsAndExistingSemesterRowsHaveNoFalselyConfirmedPortion() {
        val subjects = pack.courses.flatMap { it.notebooks }.distinct().mapIndexed { i,id -> SubjectEntity(id,id,"","notes",i) }
        val old = row(portion()).copy(examJson = "")
        val backup = JSONObject(BackupCodec.encode(subjects,emptyList(),emptyList(),UserProfileEntity(),semester = old)).put("version",3)
        val restored = CurriculumCodec.accepted(BackupCodec.decode(backup.toString()).semester!!)
        assertNull(restored.exam)
        assertEquals(setOf(module.id),restored.focus)
        assertFalse(ExamPortion.initial(restored).confirmed)
    }
    @Test fun invalidOrDuplicateTopicsCannotSilentlyAlterThePortion() {
        assertThrows(Exception::class.java) { ExamPortion.decode(portion("Invented topic").encode(),pack) }
        val bad = JSONObject(portion().encode())
        bad.getJSONObject("topics").put(module.id,JSONArray(listOf("Gradient","Gradient")))
        assertThrows(Exception::class.java) { ExamPortion.decode(bad.toString(),pack) }
        assertThrows(Exception::class.java) { ExamPortion.decode(portion().copy(topics = emptyMap()).encode(),pack) }
        assertThrows(Exception::class.java) { CurriculumCodec.accepted(row(portion()).copy(focusJson = "[]")) }
    }
    @Test fun everyStarterAnchorBelongsToItsActualSyllabus() {
        StarterLessons.moduleIds.forEach { id -> assertNotNull(StarterLessons.scopeTopic(pack.modules.first { it.id == id })) }
    }
}
