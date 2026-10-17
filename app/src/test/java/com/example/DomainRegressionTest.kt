package com.example

import com.example.data.model.*
import com.example.domain.*
import java.time.*
import org.junit.Assert.*
import org.junit.Test

class DomainRegressionTest {
    private val now = LocalDateTime.of(2026, 10, 30, 18, 30)
    private val profile = UserProfileEntity(graphicsBrief = "actual questions", electricalBrief = "actual questions")

    @Test fun quizRequiresEveryAnswerAndPreservesMixedScore() {
        var q = QuizAttempt("attempt", listOf(1, 0, 1))
        assertEquals(q, q.advance())
        q = q.select(1).check()
        assertEquals(q, q.check())
        q = q.advance().select(2).check().advance().select(1).check().advance()
        assertTrue(q.complete)
        assertEquals(2, q.score)
        assertEquals(3, q.answers.size)
        assertEquals(q, q.advance())
    }
    @Test fun emptyQuizCannotComplete() { assertFalse(QuizAttempt("empty", emptyList()).check().advance().complete) }
    @Test fun checkedAnswerCannotChange() {
        val checked = QuizAttempt("attempt", listOf(1)).select(1).check()
        assertEquals(checked, checked.select(0))
    }
    @Test fun slotsAreFutureAndNeverCrossProtectedWindows() {
        val slots = PlannerEngine.getAvailableSlots(profile, now, 45)
        assertTrue(slots.isNotEmpty())
        slots.forEach {
            val start = LocalDate.parse(it.date).atTime(LocalTime.parse(it.time24))
            assertTrue(start > now)
            assertFalse(PlannerEngine.isProtected(profile, start.toLocalTime(), 45))
            assertTrue(start.plusMinutes(45).toLocalTime() <= LocalTime.parse(profile.studyEnd))
        }
    }
    @Test fun noSlotIsInventedBeforeAnExpiredDeadline() {
        assertTrue(PlannerEngine.getAvailableSlots(profile, now, 25, "2026-10-29").isEmpty())
    }
    @Test fun narrowProtectedWindowHasNoSlots() {
        assertTrue(PlannerEngine.getAvailableSlots(profile.copy(studyStart = "20:00", studyEnd = "21:00"), now, 25).isEmpty())
    }
    @Test fun alternativesCarryFullDatesAcrossMonthBoundaries() {
        val slots = PlannerEngine.getAvailableSlots(profile, LocalDateTime.of(2026, 10, 31, 23, 0))
        assertEquals("2026-11-01", slots.first().date)
    }
    @Test fun realEarlierTaskOutranksLaterMissingBrief() {
        val p = profile.copy(graphicsBrief = "", graphicsDueDate = "2026-11-06")
        val task = AcademicTaskEntity("real", "python", "Trace question 1", "Open my actual worksheet", "2026-11-01")
        assertEquals("real", PlannerEngine.determineNextAction(p, listOf(task)).id)
    }
    @Test fun completedTaskIsExcludedAndSampleIsNeverRecommended() {
        val task = AcademicTaskEntity("done", "python", "Trace", "Open worksheet", completed = true)
        assertEquals("add-task", PlannerEngine.determineNextAction(profile, listOf(task)).actionType)
    }
    @Test fun malformedAndReversedTimesAreRejected() {
        assertNotNull(PlannerEngine.validateProfile(profile.copy(gymStart = "25:99")))
        assertNotNull(PlannerEngine.validateProfile(profile.copy(studyStart = "22:00", studyEnd = "16:00")))
    }
    @Test fun savedSlotIsRecheckedAfterChangingCommitments() {
        val p = profile.copy(scheduledDate = "2026-10-30", scheduledSlot = "21:15", gymStart = "21:00", gymEnd = "22:00")
        assertNotEquals("21:15", PlannerEngine.plannedSlot(p, PlannerEngine.determineNextAction(p), now)?.time24)
    }
    @Test fun missingExamDoesNotInventTwentyTwoDays() {
        assertNull(PlannerEngine.calculateDaysToExam(""))
        assertNull(PlannerEngine.calculateDaysToExam("invalid"))
    }
    @Test fun earlierDeadlineWinsEvenWhenTheTaskListIsNotSorted() {
        val later = AcademicTaskEntity("later", "python", "Later", "Open worksheet", "2026-11-05")
        val earlier = later.copy(id="earlier", dueDate="2026-11-01")
        assertEquals("earlier", PlannerEngine.determineNextAction(profile, listOf(later,earlier)).id)
    }
    @Test fun expiredExamStaysInThePast() {
        assertEquals(-1L,PlannerEngine.calculateDaysToExam(LocalDate.now().minusDays(1).toString()))
    }
    @Test fun aBlockEndingAtGymStartFitsButCrossingItDoesNot() {
        assertFalse(PlannerEngine.isProtected(profile,LocalTime.of(19,30),30))
        assertTrue(PlannerEngine.isProtected(profile,LocalTime.of(19,30),31))
    }
    @Test fun crossingTheCallOrMidnightIsProtected() {
        assertTrue(PlannerEngine.isProtected(profile,LocalTime.of(22,15),30))
        assertTrue(PlannerEngine.isProtected(profile,LocalTime.of(23,50),25))
    }
    @Test fun aPastSavedSlotCannotRemainThePlannedSlot() {
        val p=profile.copy(scheduledDate="2026-10-29",scheduledSlot="16:00")
        val slot=PlannerEngine.plannedSlot(p,PlannerEngine.determineNextAction(p),now)
        assertNotNull(slot)
        assertTrue(LocalDate.parse(slot!!.date).atTime(LocalTime.parse(slot.time24)) > now)
    }
    @Test fun noSavedOrSuggestedSlotIsCreatedWhenTheWholeWindowIsProtected() {
        val p=profile.copy(studyStart="20:00",studyEnd="21:00",scheduledDate="2026-10-30",scheduledSlot="20:15")
        assertNull(PlannerEngine.plannedSlot(p,PlannerEngine.determineNextAction(p),now))
    }
    @Test fun invalidDurationsCannotCreateCandidates() {
        assertTrue(PlannerEngine.getAvailableSlots(profile,now,durationMinutes=0).isEmpty())
        assertTrue(PlannerEngine.getAvailableSlots(profile,now,durationMinutes=181).isEmpty())
    }
    @Test fun anEmptyQuizCannotRecordACheckedAnswer() {
        val q=QuizAttempt("empty",emptyList()).select(1).check().advance()
        assertFalse(q.complete)
        assertTrue(q.answers.isEmpty())
    }
}
