package com.example.domain

import com.example.data.model.AcademicTaskEntity
import com.example.data.model.UserProfileEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class NextAction(val id: String, val subjectLabel: String, val title: String, val copy: String,
    val cta: String, val duration: String, val actionType: String, val minutes: Int = 10,
    val firstStep: String = "", val dueDate: String = "")

data class RescheduleSlot(val time24: String, val label: String, val copy: String,
    val date: String = LocalDate.now().toString()) {
    val key: String get() = "$date|$time24"
}

object PlannerEngine {
    fun date(value: String): LocalDate? = runCatching { LocalDate.parse(value) }.getOrNull()
    fun time(value: String): LocalTime? = runCatching { LocalTime.parse(value) }.getOrNull()
    fun calculateDaysToExam(value: String): Long? = date(value)?.let { ChronoUnit.DAYS.between(LocalDate.now(), it) }
    fun formatExamDate(value: String): String = date(value)?.format(DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)) ?: "Date to confirm"
    fun formatTime12h(value: String): String = time(value)?.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))?.lowercase(Locale.ENGLISH) ?: "Time to confirm"

    fun validateProfile(p: UserProfileEntity): String? {
        if (p.examDate.isNotBlank() && date(p.examDate) == null) return "Use YYYY-MM-DD for the exam, or leave it blank."
        for (value in listOf(p.graphicsDueDate, p.electricalDueDate)) {
            if (value.isNotBlank() && date(value) == null) return "Use YYYY-MM-DD for deadlines, or leave them unknown."
        }
        if (listOf(p.collegeStart, p.collegeEnd, p.gymStart, p.gymEnd, p.callStart, p.studyStart, p.studyEnd).any { time(it) == null })
            return "Enter valid times as HH:MM (24-hour clock)."
        if (time(p.collegeStart)!! >= time(p.collegeEnd)!! || time(p.gymStart)!! >= time(p.gymEnd)!!)
            return "College and gym must end after they start."
        if (time(p.studyStart)!! >= time(p.studyEnd)!!) return "Your study window must end after it starts, on the same day."
        return null
    }

    fun determineNextAction(p: UserProfileEntity, tasks: List<AcademicTaskEntity> = emptyList()): NextAction {
        val missing = listOf(Triple("graphics", p.graphicsBrief, p.graphicsDueDate),
            Triple("electrical", p.electricalBrief, p.electricalDueDate))
            .filter { it.second.isBlank() }.sortedBy { date(it.third) ?: LocalDate.MAX }
        val task = tasks.filter { !it.completed }.sortedWith(
            compareBy<AcademicTaskEntity> { date(it.dueDate) ?: LocalDate.MAX }.thenBy { it.createdAt }).firstOrNull()
        val brief = missing.firstOrNull()
        if (brief != null && (task == null || (date(brief.third) ?: LocalDate.MIN) <= (date(task.dueDate) ?: LocalDate.MAX))) {
            val name = if (brief.first == "graphics") "Engineering graphics" else "Electrical & electronics"
            return NextAction(brief.first, name, "Get the assignment\nquestions.",
                if (brief.third.isBlank()) "The scope and deadline still need confirmation. Get the actual questions before estimating the work."
                else "Due ${formatExamDate(brief.third)}. The missing questions are the first prerequisite.",
                "Capture the brief", "10 min", "brief-${brief.first}", firstStep =
                "Open your class group, notebook or portal. Copy the exact assignment questions and confirm the deadline.", dueDate = brief.third)
        }
        if (task != null) return NextAction(task.id, "Your next study block", task.title,
            if (task.dueDate.isBlank()) "Your oldest unfinished step. No deadline has been confirmed."
            else "Due ${formatExamDate(task.dueDate)}. This is your earliest dated unfinished task.",
            "Start this step", "${task.durationMinutes} min", "task", task.durationMinutes,
            task.instructions.ifBlank { "Write down the first question or material you need, then start there." }, task.dueDate)
        return NextAction("add-task", "Build your next step", "Give the work\na starting point.",
            "Your briefs are captured. Add one specific step from the actual questions or your confirmed exam coverage.",
            "Add a study task", "2 min", "add-task", 2,
            "Choose a subject, name one small piece of work and write how to start. Unknown dates can stay blank.")
    }

    fun isProtected(p: UserProfileEntity, start: LocalTime, minutes: Int): Boolean {
        fun mins(t: LocalTime) = t.hour * 60 + t.minute
        val begin = mins(start)
        val end = begin + minutes
        if (end > 1440) return true
        return listOf(p.collegeStart to p.collegeEnd, p.gymStart to p.gymEnd, p.callStart to "23:59").any { (a, b) ->
            val from = time(a) ?: return true
            val to = time(b) ?: return true
            begin < (if (b == "23:59") 1440 else mins(to)) && end > mins(from)
        }
    }

    fun getAvailableSlots(p: UserProfileEntity, now: LocalDateTime = LocalDateTime.now(),
        durationMinutes: Int = 10, dueDate: String = "", days: Int = 7): List<RescheduleSlot> {
        if (validateProfile(p) != null || durationMinutes !in 1..180) return emptyList()
        val start = time(p.studyStart)!!
        val end = time(p.studyEnd)!!
        val deadline = date(dueDate)
        val result = mutableListOf<RescheduleSlot>()
        for (offset in 0 until days) {
            val day = now.toLocalDate().plusDays(offset.toLong())
            if (deadline != null && day > deadline) break
            var candidate = day.atTime(start)
            while (candidate.plusMinutes(durationMinutes.toLong()).toLocalDate() == day
                && candidate.plusMinutes(durationMinutes.toLong()).toLocalTime() <= end) {
                val t = candidate.toLocalTime()
                if (candidate >= now.plusMinutes(1) && !isProtected(p, t, durationMinutes)) {
                    result.add(RescheduleSlot(t.toString(),
                        "${day.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH))} · ${formatTime12h(t.toString())}",
                        "$durationMinutes min inside your chosen study window. Protected commitments stay clear.", day.toString()))
                    candidate = candidate.plusMinutes(maxOf(30, durationMinutes + 10).toLong())
                } else candidate = candidate.plusMinutes(5)
                if (result.size == 8) return result
            }
        }
        return result
    }

    fun plannedSlot(p: UserProfileEntity, action: NextAction, now: LocalDateTime = LocalDateTime.now()): RescheduleSlot? {
        val saved = runCatching { LocalDateTime.of(LocalDate.parse(p.scheduledDate), LocalTime.parse(p.scheduledSlot)) }.getOrNull()
        val due = date(action.dueDate)
        if (saved != null && saved >= now.plusMinutes(1) && (due == null || saved.toLocalDate() <= due)
            && saved.toLocalTime() >= (time(p.studyStart) ?: LocalTime.MAX)
            && saved.plusMinutes(action.minutes.toLong()).toLocalDate() == saved.toLocalDate()
            && saved.plusMinutes(action.minutes.toLong()).toLocalTime() <= (time(p.studyEnd) ?: LocalTime.MIN)
            && !isProtected(p, saved.toLocalTime(), action.minutes))
            return RescheduleSlot(p.scheduledSlot, "${formatExamDate(p.scheduledDate)} · ${formatTime12h(p.scheduledSlot)}", "Your chosen block", p.scheduledDate)
        return getAvailableSlots(p, now, action.minutes, action.dueDate).firstOrNull()
    }
}
