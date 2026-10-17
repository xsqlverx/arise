package com.example.domain

import com.example.data.model.AcademicTaskEntity
import com.example.data.model.UserProfileEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class NextAction(
    val id: String,
    val subjectLabel: String,
    val title: String,
    val copy: String,
    val cta: String,
    val duration: String,
    val actionType: String,
    val minutes: Int = 10,
    val firstStep: String = "",
    val dueDate: String = ""
)

data class RescheduleSlot(
    val date: String,
    val time24: String,
    val label: String,
    val copy: String,
    val minutes: Int = 25
) {
    val key: String get() = "$date|$time24"
}

object PlannerEngine {

    fun date(s: String?): LocalDate? {
        if (s.isNullOrBlank()) return null
        return runCatching { LocalDate.parse(s.trim()) }.getOrNull()
    }

    fun time(s: String?): LocalTime? {
        if (s.isNullOrBlank()) return null
        return runCatching { LocalTime.parse(s.trim()) }.getOrNull()
    }

    fun validateProfile(profile: UserProfileEntity): String? {
        val cs = time(profile.collegeStart) ?: return "Invalid college start time"
        val ce = time(profile.collegeEnd) ?: return "Invalid college end time"
        if (!ce.isAfter(cs)) return "College end must be after start"

        val gs = time(profile.gymStart) ?: return "Invalid gym start time"
        val ge = time(profile.gymEnd) ?: return "Invalid gym end time"
        if (!ge.isAfter(gs)) return "Gym end must be after start"

        time(profile.callStart) ?: return "Invalid call start time"

        val ss = time(profile.studyStart) ?: return "Invalid study start time"
        val se = time(profile.studyEnd) ?: return "Invalid study end time"
        if (!se.isAfter(ss)) return "Study end must be after start"

        if (profile.examDate.isNotBlank() && date(profile.examDate) == null) return "Invalid exam date"
        if (profile.scheduledDate.isNotBlank() && date(profile.scheduledDate) == null) return "Invalid scheduled date"
        if (profile.graphicsDueDate.isNotBlank() && date(profile.graphicsDueDate) == null) return "Invalid graphics due date"
        if (profile.electricalDueDate.isNotBlank() && date(profile.electricalDueDate) == null) return "Invalid electrical due date"

        return null
    }

    fun calculateDaysToExam(examDateStr: String): Long {
        val exam = date(examDateStr) ?: return 22L
        val today = LocalDate.now()
        val diff = ChronoUnit.DAYS.between(today, exam)
        return if (diff >= 0) diff else 0L
    }

    fun formatExamDate(dateStr: String): String {
        val d = date(dateStr) ?: return dateStr
        return d.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH))
    }

    fun formatTime12h(time24: String): String {
        val t = time(time24) ?: return time24
        val minuteStr = String.format(Locale.US, "%02d", t.minute)
        val period = if (t.hour >= 12) "pm" else "am"
        val hour12 = if (t.hour % 12 == 0) 12 else t.hour % 12
        return "$hour12:$minuteStr $period"
    }

    fun plannedSlot(profile: UserProfileEntity, action: NextAction, now: LocalDateTime = LocalDateTime.now()): RescheduleSlot? {
        val dateStr = profile.scheduledDate.ifBlank { now.toLocalDate().toString() }
        val timeStr = profile.scheduledSlot.ifBlank { "15:30" }
        return RescheduleSlot(
            date = dateStr,
            time24 = timeStr,
            label = "${formatTime12h(timeStr)} · Planned",
            copy = "Scheduled focus block",
            minutes = action.minutes
        )
    }

    fun determineNextAction(profile: UserProfileEntity, tasks: List<AcademicTaskEntity> = emptyList()): NextAction {
        val uncompletedTask = tasks.firstOrNull { !it.completed }
        if (uncompletedTask != null) {
            return NextAction(
                id = uncompletedTask.id,
                subjectLabel = uncompletedTask.subjectId.replaceFirstChar { it.uppercase() },
                title = uncompletedTask.title,
                copy = uncompletedTask.instructions.ifBlank { "Independent practice step for your notebook." },
                cta = "Start practice",
                duration = "${uncompletedTask.durationMinutes} min",
                actionType = "task",
                minutes = uncompletedTask.durationMinutes,
                firstStep = "Open your material: ${uncompletedTask.title}",
                dueDate = uncompletedTask.dueDate
            )
        }

        return when {
            profile.graphicsBrief.isBlank() -> NextAction(
                id = "graphics",
                subjectLabel = "Engineering graphics",
                title = "Get the graphics\nassignment brief.",
                copy = "Due soon. The questions are your first missing piece. Get them before planning the work.",
                cta = "Get the brief",
                duration = "10 min",
                actionType = "brief-graphics",
                minutes = 10,
                firstStep = "Find the plate instructions in your class group.",
                dueDate = profile.graphicsDueDate
            )
            profile.electricalBrief.isBlank() -> NextAction(
                id = "electrical",
                subjectLabel = "Electrical & electronics",
                title = "Get the second\nassignment brief.",
                copy = "One brief captured. Get the electrical & electronics questions so both assignments can be broken down.",
                cta = "Capture the questions",
                duration = "10 min",
                actionType = "brief-electrical",
                minutes = 10,
                firstStep = "Capture the questions from the teacher's assignment sheet.",
                dueDate = profile.electricalDueDate
            )
            else -> NextAction(
                id = "python",
                subjectLabel = "Your next study block",
                title = "Make one concept\nclick today.",
                copy = "Your assignment briefs are captured. Try a guided sample session while the full plan takes shape.",
                cta = "Try the study flow",
                duration = "25 min",
                actionType = "open-study",
                minutes = 25,
                firstStep = "Read the concept and trace the example.",
                dueDate = ""
            )
        }
    }

    fun isProtected(profile: UserProfileEntity, time: LocalTime, durationMinutes: Int = 0): Boolean {
        val endTime = time.plusMinutes(durationMinutes.toLong())
        val collegeStart = time(profile.collegeStart) ?: LocalTime.of(7, 45)
        val collegeEnd = time(profile.collegeEnd) ?: LocalTime.of(14, 30)
        val gymStart = time(profile.gymStart) ?: LocalTime.of(20, 0)
        val gymEnd = time(profile.gymEnd) ?: LocalTime.of(21, 0)
        val callStart = time(profile.callStart) ?: LocalTime.of(22, 30)

        if (time < collegeEnd && endTime > collegeStart) return true
        if (time < gymEnd && endTime > gymStart) return true
        if (endTime > callStart) return true
        return false
    }

    fun getAvailableSlots(
        profile: UserProfileEntity,
        from: LocalDateTime = LocalDateTime.now(),
        durationMinutes: Int = 25,
        dueDateStr: String = "",
        now: LocalDateTime = from,
        dueDate: String = dueDateStr
    ): List<RescheduleSlot> {
        val slots = mutableListOf<RescheduleSlot>()
        val startDate = from.toLocalDate()
        val limitDays = 3L

        val collegeStart = time(profile.collegeStart) ?: LocalTime.of(7, 45)
        val collegeEnd = time(profile.collegeEnd) ?: LocalTime.of(14, 30)
        val gymStart = time(profile.gymStart) ?: LocalTime.of(20, 0)
        val gymEnd = time(profile.gymEnd) ?: LocalTime.of(21, 0)
        val callStart = time(profile.callStart) ?: LocalTime.of(22, 30)

        val candidateTimes = listOf(
            LocalTime.of(15, 30) to "Before late afternoon work",
            LocalTime.of(16, 30) to "Afternoon study window",
            LocalTime.of(18, 30) to "Before gym session",
            LocalTime.of(21, 15) to "After gym, before evening call"
        )

        for (dayOffset in 0L until limitDays) {
            val d = startDate.plusDays(dayOffset)
            val dStr = d.toString()

            for ((t, copy) in candidateTimes) {
                val candidateDt = d.atTime(t)
                if (candidateDt.isBefore(from)) continue

                // Check conflict with college
                if (!t.isBefore(collegeStart) && t.isBefore(collegeEnd)) continue
                // Check conflict with gym
                if (!t.isBefore(gymStart) && t.isBefore(gymEnd)) continue
                // Check conflict with call
                if (!t.isBefore(callStart)) continue

                val tStr = String.format(Locale.US, "%02d:%02d", t.hour, t.minute)
                val label = "${formatTime12h(tStr)} · ${if (dayOffset == 0L) "Today" else d.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))}"
                slots.add(
                    RescheduleSlot(
                        date = dStr,
                        time24 = tStr,
                        label = label,
                        copy = copy,
                        minutes = durationMinutes
                    )
                )
            }
        }
        return slots
    }
}
