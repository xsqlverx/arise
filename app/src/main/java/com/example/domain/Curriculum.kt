package com.example.domain

import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class CourseSource(val asset: String, val sha256: String, val pages: Int)
data class CourseModule(val id: String, val number: Int, val title: String, val pages: List<Int>,
    val topics: List<String>, val notes: List<String>, val component: String, val focus: Boolean)
data class SemesterCourse(val id: String, val code: String, val title: String, val kind: String,
    val notebooks: List<String>, val source: CourseSource, val notes: List<String>,
    val modules: List<CourseModule>, val activities: List<CourseActivity> = emptyList())
data class CourseActivity(val title: String, val pages: List<Int>)
data class SemesterPack(val id: String, val title: String, val scopeStatus: String, val courses: List<SemesterCourse>) {
    val modules get() = courses.flatMap { it.modules }
    fun course(moduleId: String) = courses.first { c -> c.modules.any { it.id == moduleId } }
}
data class AcceptedSemester(val pack: SemesterPack, val focus: Set<String>, val electricalMarks: Int,
    val electronicsMarks: Int, val exam: ExamPortion? = null)

/** Strict even for backup input. PDF assets are bundled, never arbitrary file paths/URLs. */
object CurriculumCodec {
    private fun JSONObject.string(key: String): String = get(key).let {
        require(it is String && it.length <= 20000) { "Invalid $key" }; it
    }
    private fun JSONObject.integer(key: String, range: IntRange): Int = get(key).let {
        require(it is Number && it.toDouble() == it.toLong().toDouble() && it.toLong() in range.first.toLong()..range.last.toLong()) { "Invalid $key" }; it.toInt()
    }
    private fun JSONArray.strings() = (0 until length()).map {
        val value = get(it); require(value is String && value.length <= 20000); value
    }.also { require(it.size <= 500) }
    fun decode(json: String): SemesterPack {
        require(json.length <= 500000)
        val root = JSONObject(json)
        require(root.string("format") == "evolve.semester" && root.integer("version", 1..1) == 1)
        val rows = root.getJSONArray("courses"); require(rows.length() in 1..30)
        val courses = (0 until rows.length()).map { index ->
            val c = rows.getJSONObject(index); val s = c.getJSONObject("source")
            val source = CourseSource(s.string("asset"), s.string("sha256"), s.integer("pages", 1..500))
            require(source.asset.matches(Regex("semester/sources/[A-Za-z0-9-]+\\.pdf")))
            require(source.sha256.matches(Regex("[a-f0-9]{64}")))
            val ms = c.getJSONArray("modules"); require(ms.length() <= 50)
            val modules = (0 until ms.length()).map { mi ->
                val m = ms.getJSONObject(mi); val ps = m.getJSONArray("pages")
                val pages = (0 until ps.length()).map { pi ->
                    val v = ps.get(pi); require(v is Number && v.toDouble() == v.toInt().toDouble() && v.toInt() in 1..source.pages); v.toInt()
                }; require(pages.isNotEmpty())
                require(m.get("focus") is Boolean)
                CourseModule(m.string("id"), m.integer("number", 1..50), m.string("title"), pages,
                    m.getJSONArray("topics").strings(), m.getJSONArray("notes").strings(), m.string("component"), m.getBoolean("focus"))
            }
            val activities = c.optJSONArray("activities")?.let { array ->
                require(array.length() <= 100)
                (0 until array.length()).map { ai ->
                    val a = array.getJSONObject(ai)
                    val title = if (a.has("title")) a.string("title") else "${a.string("id").replaceFirstChar { it.uppercase() }}: at least ${a.integer("minimumCount", 1..100)} of ${a.integer("listedCount", 1..100)} listed experiments"
                    val ps = a.getJSONArray("sourcePages")
                    val pages = (0 until ps.length()).map { pi ->
                        val v=ps.get(pi); require(v is Number && v.toDouble()==v.toInt().toDouble() && v.toInt() in 1..source.pages); v.toInt()
                    }; require(pages.isNotEmpty())
                    CourseActivity(title,pages)
                }
            } ?: emptyList()
            SemesterCourse(c.string("id"), c.string("code"), c.string("title"), c.string("kind"),
                c.getJSONArray("notebooks").strings(), source, c.getJSONArray("notes").strings(), modules, activities).also { course ->
                require(course.notebooks.all { it.matches(Regex("[a-z0-9-]{1,100}")) })
                require(course.modules.map { it.number }.distinct().size == course.modules.size)
                require(course.modules.all { it.id == "${course.id}-m${it.number}" && it.component in course.notebooks })
            }
        }
        val ids = courses.map { it.id } + courses.flatMap { c -> c.modules.map { it.id } }
        require(ids.distinct().size == ids.size && ids.all { it.matches(Regex("[a-z0-9-]{1,100}")) })
        require(courses.all { it.title.isNotBlank() && it.notebooks.isNotEmpty() })
        return SemesterPack(root.string("id"), root.string("title"), root.string("scopeStatus"), courses)
    }
    fun accepted(row: SemesterEntity): AcceptedSemester {
        require(row.id == 1 && row.electricalMarks in 0..100 && row.electronicsMarks in 0..100)
        val pack = decode(row.packJson)
        val focus = JSONArray(row.focusJson).strings()
        require(focus.distinct().size == focus.size && focus.all { id -> pack.modules.any { it.id == id } })
        val exam = row.examJson.takeIf { it.isNotEmpty() }?.let { ExamPortion.decode(it, pack) }
        require(exam == null || focus.toSet() == exam.topics.keys) { "Exam topics and module focus disagree" }
        return AcceptedSemester(pack, focus.toSet(), row.electricalMarks, row.electronicsMarks, exam)
    }
    fun validateAttempt(a: CurriculumAttemptEntity, semester: AcceptedSemester) {
        require(a.id.isNotBlank() && a.id.length <= 200 && a.recall.length <= 30000 && a.timestamp >= 0)
        val lesson = StarterLessons.forModule(a.moduleId) ?: error("Unknown checkpoint")
        require(semester.pack.modules.any { it.id == a.moduleId })
        val answers = JSONArray(a.answersJson)
        require(answers.length() == lesson.checks.size && a.total == lesson.checks.size)
        val choices = (0 until answers.length()).map { i ->
            val v = answers.get(i); require(v is Number && v.toDouble() == v.toInt().toDouble() && v.toInt() in lesson.checks[i].options.indices); v.toInt()
        }
        require(a.score == lesson.score(choices) && PlannerEngine.date(a.reviewDate) != null)
    }
    fun reviewDate(timestamp: Long, passed: Boolean): String = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault()).toLocalDate().plusDays(if (passed) 3 else 1).toString()
}

/** Shared by Today, Plan and background reminders. Scope is provisional; evidence is narrow. */
object AcademicPlanner {
    fun next(p: UserProfileEntity, tasks: List<AcademicTaskEntity>, semester: AcceptedSemester?,
        attempts: List<CurriculumAttemptEntity>, today: LocalDate = LocalDate.now()): NextAction {
        val urgentTasks = tasks.filter { !it.completed && PlannerEngine.date(it.dueDate)?.let { day -> day <= today.plusDays(2) } == true }
        val urgentBrief = (p.graphicsBrief.isBlank() && PlannerEngine.date(p.graphicsDueDate)?.let { it <= today.plusDays(2) } == true) ||
            (p.electricalBrief.isBlank() && PlannerEngine.date(p.electricalDueDate)?.let { it <= today.plusDays(2) } == true)
        if (urgentTasks.isNotEmpty() || urgentBrief) {
            // Unknown dates cannot outrank the actual dated work.
            val datedProfile = p.copy(graphicsBrief = p.graphicsBrief.ifBlank { if (PlannerEngine.date(p.graphicsDueDate) == null) "Unknown scope" else "" },
                electricalBrief = p.electricalBrief.ifBlank { if (PlannerEngine.date(p.electricalDueDate) == null) "Unknown scope" else "" })
            return PlannerEngine.determineNextAction(datedProfile, urgentTasks)
        }
        if (semester == null) return NextAction("semester", "Your semester", "Bring the semester\ninto focus.",
            "Seven supplied syllabuses are ready together. Preview the courses once, then start with a specific concept.",
            "Import my semester", "2 min", "semester", 2, "Preview the seven-course pack. Your class notes and existing work stay in place.")
        val queued = tasks.filter { !it.completed }
        if (queued.isNotEmpty()) return PlannerEngine.determineNextAction(p.copy(graphicsBrief = "Scope pending", electricalBrief = "Scope pending"), queued)
        val latest = attempts.groupBy { it.moduleId }.mapValues { (_, rows) -> rows.maxBy { it.timestamp } }
        val eligible = semester.pack.modules.filter { it.id in semester.focus && StarterLessons.forModule(it.id) != null &&
            (semester.exam == null || StarterLessons.scopeTopic(it)?.let { topic -> topic in semester.exam.topics[it.id].orEmpty() } == true) }
        val candidate = eligible.filter { latest[it.id] == null || PlannerEngine.date(latest[it.id]!!.reviewDate)?.let { date -> date <= today } == true }
            .sortedWith(compareBy<CourseModule> { if (latest[it.id] != null) 0 else 1 }
                .thenBy { module -> semester.pack.course(module.id).modules.mapNotNull { latest[it.id]?.timestamp }.maxOrNull() ?: 0L }
                .thenBy { it.number }).firstOrNull()
        if (candidate != null) {
            val course = semester.pack.course(candidate.id); val lesson = StarterLessons.forModule(candidate.id)!!
            val previous = latest[candidate.id]
            val reason = if (previous == null) "No starter-check evidence yet for this module."
                else "Last starter check: ${previous.score}/${previous.total}; the ${previous.reviewDate} review is due."
            val component = if (course.id == "a105est04") " ${candidate.component.replaceFirstChar { it.uppercase() }} is retained for its separate marks." else ""
            val scope = if (semester.exam == null) "estimated IE focus" else "saved exam portion"
            return NextAction(candidate.id, "${course.code} · M${candidate.number}", lesson.title,
                "$reason This concept is in your $scope.$component Two checks cover this concept only.",
                if (previous == null) "Start this concept" else "Review this concept", "15 min", "curriculum", 15,
                "Take a blank page. ${lesson.firstStep}", p.examDate.takeIf { PlannerEngine.date(it)?.let { day -> day >= today } == true } ?: "")
        }
        val practice = semester.exam?.practiceTopics(semester.pack)?.firstOrNull { topic ->
            tasks.none { it.completed && it.subjectId == topic.subjectId && it.title == "Practise: ${topic.title}" }
        }
        if (practice != null) return NextAction(practice.id, "Your exam portion", practice.title,
            "You selected this topic for ${semester.exam!!.name}. Use your own class questions; starter checks do not cover every topic.",
            "Practise this topic", "25 min", "exam-practice", 25, practice.instructions,
            p.examDate.takeIf { PlannerEngine.date(it)?.let { day -> day >= today } == true } ?: "")
        return NextAction("semester", "Your next layer", "Go beyond the\nstarter checks.",
            "The selected starter concepts have no review due today. This does not mean the modules are exam-ready. Choose another syllabus topic and practise from your class material.",
            "Open my syllabus", "15 min", "semester", 15, "Open a module, pick an uncovered topic and add one concrete practice task. Adjust focus when your teacher confirms the portion.")
    }
}
