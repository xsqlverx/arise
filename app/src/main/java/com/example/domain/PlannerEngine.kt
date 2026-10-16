package com.example.domain

import com.example.data.model.UserProfileEntity
import java.time.LocalDate
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
    val actionType: String // "brief-graphics", "brief-electrical", "open-study"
)

data class RescheduleSlot(
    val time24: String,
    val label: String,
    val copy: String
)

object PlannerEngine {

    fun calculateDaysToExam(examDateStr: String): Long {
        return try {
            val examDate = LocalDate.parse(examDateStr)
            val today = LocalDate.now()
            val diff = ChronoUnit.DAYS.between(today, examDate)
            if (diff >= 0) diff else 0
        } catch (e: Exception) {
            22L
        }
    }

    fun formatExamDate(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
            date.format(formatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun determineNextAction(profile: UserProfileEntity): NextAction {
        return when {
            profile.graphicsBrief.isBlank() -> NextAction(
                id = "graphics",
                subjectLabel = "Engineering graphics",
                title = "Get the graphics\nassignment brief.",
                copy = "Due soon. The questions are your first missing piece. Get them before planning the work.",
                cta = "Get the brief",
                duration = "10 min",
                actionType = "brief-graphics"
            )
            profile.electricalBrief.isBlank() -> NextAction(
                id = "electrical",
                subjectLabel = "Electrical & electronics",
                title = "Get the second\nassignment brief.",
                copy = "One brief captured. Get the electrical & electronics questions so both assignments can be broken down.",
                cta = "Capture the questions",
                duration = "10 min",
                actionType = "brief-electrical"
            )
            else -> NextAction(
                id = "python",
                subjectLabel = "Your next study block",
                title = "Make one concept\nclick today.",
                copy = "Your assignment briefs are captured. Try a guided sample session while the full plan takes shape.",
                cta = "Try the study flow",
                duration = "25 min",
                actionType = "open-study"
            )
        }
    }

    fun getAvailableSlots(profile: UserProfileEntity): List<RescheduleSlot> {
        fun toMins(timeStr: String): Int {
            val parts = timeStr.split(":")
            val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
            val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
            return h * 60 + m
        }

        val collegeStart = toMins(profile.collegeStart)
        val collegeEnd = toMins(profile.collegeEnd)
        val gymStart = toMins(profile.gymStart)
        val gymEnd = toMins(profile.gymEnd)
        val callStart = toMins(profile.callStart)

        val candidateSlots = listOf(
            Triple("15:30", "3:30–3:40 pm", "Before the sample 4 pm study block."),
            Triple("18:30", "6:30–6:40 pm", "Before your gym session."),
            Triple("21:15", "9:15–9:25 pm", "After the gym, before your evening call.")
        )

        val blockedRanges = listOf(
            collegeStart to collegeEnd,
            gymStart to gymEnd,
            callStart to 1440
        )

        return candidateSlots.filter { (time24, _, _) ->
            val slotMins = toMins(time24)
            val slotEnd = slotMins + 10
            blockedRanges.none { (bStart, bEnd) ->
                slotMins < bEnd && slotEnd > bStart
            }
        }.map { (time24, label, copy) ->
            RescheduleSlot(time24, label, copy)
        }
    }

    fun formatTime12h(time24: String): String {
        val parts = time24.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val minuteStr = String.format(Locale.US, "%02d", m)
        val period = if (h >= 12) "pm" else "am"
        val hour12 = if (h % 12 == 0) 12 else h % 12
        return "$hour12:$minuteStr $period"
    }
}
