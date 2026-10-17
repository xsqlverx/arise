package com.example

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.data.model.*
import com.example.domain.*
import com.example.ui.components.LocalReduceMotion
import com.example.ui.screens.ExamEditorScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.theme.EvolveTheme
import org.json.JSONArray
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File
import java.time.LocalDateTime

/** Real Compose interactions on a cloud Robolectric runner, separate from phone acceptance. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w390dp-h844dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExamExperienceTest {
    @get:Rule val compose = createComposeRule()
    private val assets = listOf(File("app/src/main/assets"),File("src/main/assets")).first { it.isDirectory }
    private val json = File(assets,"semester/s1.json").readText()
    private val pack = CurriculumCodec.decode(json)
    private val semester = CurriculumCodec.accepted(SemesterEntity(packJson = json,
        focusJson = JSONArray(pack.modules.filter { it.focus }.map { it.id }).toString()))

    private fun screenshot(name: String) {
        val directory = File("build/reports/ui").apply { mkdirs() }
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        File(directory,"$name.png").outputStream().use {
            assertTrue(bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it))
        }
    }

    @Test fun todayOffersVisibleExamSetupWithoutRepeatedInstructionParagraphs() {
        var opened = false
        val action = AcademicPlanner.next(UserProfileEntity(),emptyList(),semester,emptyList())
        compose.setContent { EvolveTheme { CompositionLocalProvider(LocalReduceMotion provides true) {
            TodayScreen(UserProfileEntity(),action,LocalDateTime.of(2026,10,17,16,0),semester,{ opened = true },{},
                {},{},{},{},{},{},{})
        } } }
        compose.onNodeWithText(action.copy).assertDoesNotExist()
        compose.onNodeWithText("Set your exam topics").performScrollTo().assertIsDisplayed()
        screenshot("today-exam-entry")
        compose.onNodeWithText("Set your exam topics").performClick()
        assertTrue(opened)
    }

    @Test fun selectingOneSyllabusTopicSavesExactlyThatPortion() {
        var saved: ExamPortion? = null
        compose.setContent { EvolveTheme { CompositionLocalProvider(LocalReduceMotion provides true) {
            ExamEditorScreen(semester,"",false,null,{}, { portion,_,_,_ -> saved = portion })
        } } }
        screenshot("exam-date")
        compose.onNodeWithText("Choose topics →").performClick()
        compose.onNodeWithText("Clear selection").performScrollTo().performClick()
        val course = pack.course("a105mat01-m3")
        compose.onNodeWithText(course.title).performScrollTo().performClick()
        compose.onNodeWithText(pack.modules.first { it.id == "a105mat01-m3" }.title).performScrollTo().performClick()
        compose.onNodeWithText("Gradient").performScrollTo().performClick()
        compose.onNodeWithText("1 topics selected").assertExists()
        screenshot("exam-topic-selection")
        compose.onNodeWithText("Save 1 topics").performClick()
        assertEquals(mapOf("a105mat01-m3" to setOf("Gradient")),saved!!.topics)
        assertFalse(saved!!.confirmed)
    }

    @Test fun aTeacherTopicCanBeAddedWithoutLosingItsSubject() {
        var saved: ExamPortion? = null
        compose.setContent { EvolveTheme { CompositionLocalProvider(LocalReduceMotion provides true) {
            ExamEditorScreen(semester,"",false,null,{}, { portion,_,_,_ -> saved = portion })
        } } }
        compose.onNodeWithText("Choose topics →").performClick()
        compose.onNodeWithText("Clear selection").performScrollTo().performClick()
        compose.onNodeWithTag("exam-custom-topic").performScrollTo().performTextInput("Teacher worksheet 7")
        compose.onNodeWithText("+ Add topic").performScrollTo().performClick()
        compose.onNodeWithText("Save 1 topics").performClick()
        assertTrue(saved!!.topics.isEmpty())
        assertEquals("Teacher worksheet 7",saved!!.extras.single().title)
        assertEquals(pack.courses.first().notebooks.first(),saved!!.extras.single().subjectId)
    }
}
