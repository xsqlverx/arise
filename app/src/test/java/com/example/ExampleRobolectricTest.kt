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
    fun `planner requests a real task when both briefs captured`() {
        val profile = UserProfileEntity(
            graphicsBrief = "Plate 1 questions",
            electricalBrief = "Module 1 numericals"
        )
        val action = PlannerEngine.determineNextAction(profile)
        assertEquals("add-task", action.actionType)
    }

}
