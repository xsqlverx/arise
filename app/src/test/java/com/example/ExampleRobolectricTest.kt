package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.UserProfileEntity
import com.example.domain.PlannerEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Evolve", appName)
    }

    @Test
    fun `planner prioritizes graphics brief when empty`() {
        val profile = UserProfileEntity(
            graphicsBrief = "",
            electricalBrief = ""
        )
        val action = PlannerEngine.determineNextAction(profile)
        assertEquals("brief-graphics", action.actionType)
        assertEquals("Engineering graphics", action.subjectLabel)
    }

    @Test
    fun `planner prioritizes electrical brief when graphics captured`() {
        val profile = UserProfileEntity(
            graphicsBrief = "Plate 1 questions",
            electricalBrief = ""
        )
        val action = PlannerEngine.determineNextAction(profile)
        assertEquals("brief-electrical", action.actionType)
        assertEquals("Electrical & electronics", action.subjectLabel)
    }

    @Test
    fun `planner recommends study block when both briefs captured`() {
        val profile = UserProfileEntity(
            graphicsBrief = "Plate 1 questions",
            electricalBrief = "Module 1 numericals"
        )
        val action = PlannerEngine.determineNextAction(profile)
        assertEquals("open-study", action.actionType)
    }

    @Test
    fun `reschedule avoids protected commitments`() {
        val profile = UserProfileEntity(
            collegeStart = "07:45",
            collegeEnd = "14:30",
            gymStart = "20:00",
            gymEnd = "21:00",
            callStart = "22:30"
        )
        val slots = PlannerEngine.getAvailableSlots(profile)
        // 15:30 (3:30 pm), 18:30 (6:30 pm), 21:15 (9:15 pm) are outside protected times
        assertTrue(slots.any { it.time24 == "15:30" })
        assertTrue(slots.any { it.time24 == "18:30" })
        assertTrue(slots.any { it.time24 == "21:15" })
    }

    @Test
    fun `reschedule filters out slots overlapping with early gym`() {
        val profile = UserProfileEntity(
            collegeStart = "07:45",
            collegeEnd = "14:30",
            gymStart = "18:00",
            gymEnd = "19:00", // gym overlaps with 18:30!
            callStart = "22:30"
        )
        val slots = PlannerEngine.getAvailableSlots(profile)
        assertFalse(slots.any { it.time24 == "18:30" })
        assertTrue(slots.any { it.time24 == "15:30" })
    }
}
