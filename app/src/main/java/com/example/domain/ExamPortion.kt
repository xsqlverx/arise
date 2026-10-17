package com.example.domain

import org.json.JSONArray
import org.json.JSONObject

data class ExtraExamTopic(val id: String, val subjectId: String, val title: String)
data class ExamPracticeTopic(val id: String, val subjectId: String, val title: String) {
    val instructions get() = "Open your class notes or textbook for: $title. Work through one solved example. Close the solution, attempt a fresh question on paper, then compare each step. Record any teacher advice in class notes."
}

/** One upcoming assessment. Exact syllabus topics and teacher-added topics stay distinct. */
data class ExamPortion(val name: String, val confirmed: Boolean,
    val topics: Map<String, Set<String>>, val extras: List<ExtraExamTopic> = emptyList()) {
    val topicCount get() = topics.values.sumOf { it.size } + extras.size
    fun practiceTopics(pack: SemesterPack): List<ExamPracticeTopic> = pack.modules.flatMap { m ->
        m.topics.mapIndexedNotNull { index, text -> if (text in topics[m.id].orEmpty())
            ExamPracticeTopic("exam:${m.id}:$index", m.component, text) else null }
    } + extras.map { ExamPracticeTopic("extra:${it.id}", it.subjectId, it.title) }

    fun encode(): String = JSONObject().apply {
        put("version", 1); put("name", name); put("confirmed", confirmed)
        put("topics", JSONObject().apply { topics.toSortedMap().forEach { (id, values) -> put(id, JSONArray(values.sorted())) } })
        put("extras", JSONArray().apply { extras.forEach { put(JSONObject().put("id",it.id).put("subjectId",it.subjectId).put("title",it.title)) } })
    }.toString()

    companion object {
        fun initial(semester: AcceptedSemester) = semester.exam ?: ExamPortion("Next internal", false,
            semester.pack.modules.filter { it.id in semester.focus }.associate { it.id to it.topics.toSet() })

        fun decode(json: String, pack: SemesterPack): ExamPortion {
            require(json.length <= 200_000) { "Exam portion is too large" }
            val root = JSONObject(json)
            require(root.get("version") is Number && root.get("version").toString() == "1")
            require(root.get("name") is String && root.getString("name").isNotBlank() && root.getString("name").length <= 100)
            require(root.get("confirmed") is Boolean)
            val selections = root.getJSONObject("topics")
            require(selections.length() <= pack.modules.size)
            val topics = selections.keys().asSequence().associateWith { id ->
                val module = pack.modules.firstOrNull { it.id == id } ?: error("Unknown exam module")
                val array = selections.getJSONArray(id)
                require(array.length() in 1..module.topics.size)
                val values = (0 until array.length()).map { i ->
                    val value = array.get(i); require(value is String && value in module.topics); value
                }
                require(values.distinct().size == values.size)
                values.toSet()
            }
            val array = root.getJSONArray("extras"); require(array.length() <= 100)
            val subjects = pack.courses.flatMap { it.notebooks }.toSet()
            val extras = (0 until array.length()).map { i ->
                val value = array.getJSONObject(i)
                require(listOf("id","subjectId","title").all { value.get(it) is String })
                ExtraExamTopic(value.getString("id"), value.getString("subjectId"), value.getString("title")).also {
                    require(it.id.matches(Regex("[a-zA-Z0-9-]{1,100}")) && it.subjectId in subjects)
                    require(it.title.isNotBlank() && it.title.length <= 300)
                }
            }
            require(extras.map { it.id }.distinct().size == extras.size)
            return ExamPortion(root.getString("name"), root.getBoolean("confirmed"), topics, extras).also {
                require(it.topicCount > 0) { "Select at least one topic" }
            }
        }
    }
}
