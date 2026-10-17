package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.time.LocalDate
import java.util.UUID

@Composable
fun ExamEditorScreen(semester: AcceptedSemester, initialDate: String, busy: Boolean, feedback: String?,
    onClose: () -> Unit, onSave: (ExamPortion, String, String, String) -> Unit) {
    val context = LocalContext.current
    val reduced = LocalReduceMotion.current
    val initial = remember(semester) { ExamPortion.initial(semester) }
    var name by rememberSaveable { mutableStateOf(initial.name) }
    var date by rememberSaveable { mutableStateOf(initialDate) }
    var confirmed by rememberSaveable { mutableStateOf(initial.confirmed) }
    // JSON is saveable across rotation/process restoration; committed state is written transactionally.
    var selectionJson by rememberSaveable { mutableStateOf(initial.encode()) }
    val selected = remember(selectionJson) {
        // An empty draft is allowed while selecting; the decoder only accepts complete, nonempty portions.
        val json = org.json.JSONObject(selectionJson)
        val rows = json.getJSONObject("topics")
        rows.keys().asSequence().associateWith { key -> rows.getJSONArray(key).let { a ->
            (0 until a.length()).map { a.getString(it) }.toSet() } }
    }
    val extras = remember(selectionJson) { org.json.JSONObject(selectionJson).getJSONArray("extras").let { a ->
        (0 until a.length()).map { a.getJSONObject(it).let { v -> ExtraExamTopic(v.getString("id"),v.getString("subjectId"),v.getString("title")) } }
    } }
    fun setTopics(topics: Map<String,Set<String>>, extra: List<ExtraExamTopic> = extras) {
        selectionJson = ExamPortion(name, confirmed, topics.filterValues { it.isNotEmpty() }, extra).encode()
    }
    var ee by rememberSaveable { mutableStateOf(semester.electricalMarks.toString()) }
    var ec by rememberSaveable { mutableStateOf(semester.electronicsMarks.toString()) }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var expandedCourse by rememberSaveable { mutableStateOf<String?>(null) }
    var expandedModule by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var customText by rememberSaveable { mutableStateOf("") }
    var subject by rememberSaveable { mutableStateOf(semester.pack.courses.first().notebooks.first()) }
    var subjectMenu by remember { mutableStateOf(false) }
    var discard by remember { mutableStateOf(false) }
    val dirty = name != initial.name || date != initialDate || confirmed != initial.confirmed ||
        selected != initial.topics || extras != initial.extras || ee != semester.electricalMarks.toString() || ec != semester.electronicsMarks.toString()
    fun leave() { if (!busy) { if (dirty || customText.isNotBlank()) discard = true else onClose() } }
    val ime = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    BackHandler(enabled = !ime) { if (step > 0) step-- else leave() }
    val count = selected.values.sumOf { it.size } + extras.size
    val validDate = date.isEmpty() || PlannerEngine.date(date)?.let { it >= LocalDate.now() } == true
    val validMarks = ee.toIntOrNull()?.let { it in 0..100 } == true && ec.toIntOrNull()?.let { it in 0..100 } == true
    Column(Modifier.fillMaxSize().background(EvolveBlack).imePadding().padding(horizontal = 24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { if (step > 0) step-- else leave() }, enabled = !busy) { Text("← Back", color = EvolveMuted) }
            Text("${step + 1} / 2", color = EvolveDim, fontSize = 12.sp)
        }
        AnimatedContent(step, transitionSpec = {
            val ms = if (reduced) 0 else 180
            fadeIn(tween(ms)) togetherWith fadeOut(tween(ms))
        }, modifier = Modifier.weight(1f), label = "exam setup step") { page ->
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(20.dp)); Eyebrow("Next exam", true); Spacer(Modifier.height(10.dp))
                Text(if (page == 0) "Give it\na place." else "What's in\nthe paper?", color = EvolveText,
                    fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp, lineHeight = 41.sp)
                Spacer(Modifier.height(24.dp))
                if (page == 0) {
                    EvolveField("Exam name", name, { if (it.length <= 100) name = it }, enabled = !busy)
                    Text("Date", color = EvolveMuted, fontSize = 12.sp)
                    TextButton(enabled = !busy, onClick = {
                        val day = PlannerEngine.date(date)?.takeIf { it >= LocalDate.now() } ?: LocalDate.now()
                        DatePickerDialog(context, { _, y, m, d -> date = LocalDate.of(y,m+1,d).toString() },
                            day.year,day.monthValue-1,day.dayOfMonth).apply {
                            datePicker.minDate = LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                        }.show()
                    }) { Text(if (date.isEmpty()) "Choose a date" else PlannerEngine.formatExamDate(date), color = EvolveAccent, fontSize = 20.sp) }
                    if (date.isNotEmpty()) TextButton(enabled = !busy, onClick = { date = "" }) { Text("Date isn't confirmed yet", color = EvolveMuted) }
                    if (!validDate) Text("Choose an upcoming date or leave it unknown.", color = EvolveWarm)
                    Spacer(Modifier.height(20.dp))
                    Text("The date can stay unknown. Choose the portion next.", color = EvolveMuted, fontSize = 13.sp, lineHeight = 21.sp)
                } else {
                    Text("$count topics selected", color = EvolveAccent, fontSize = 14.sp)
                    TextButton(onClick = { setTopics(emptyMap(),emptyList()) }, enabled = !busy && count > 0) {
                        Text("Clear selection", color = EvolveMuted, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    EvolveField("Find a subject or topic", query, { query = it }, hint = "Gradient, Python…", enabled = !busy)
                    if (semester.exam == null) Text("Your current module focus is preselected. Adjust it to the teacher's portion.", color = EvolveMuted, fontSize = 12.sp, lineHeight = 19.sp)
                    val courses = semester.pack.courses.filter { c -> c.modules.isNotEmpty() && (query.isBlank() ||
                        (listOf(c.title,c.code) + c.modules.flatMap { listOf(it.title) + it.topics }).any { it.contains(query,true) }) }
                    if (courses.isEmpty()) Text("No matching syllabus topic. You can add a teacher topic below.", color = EvolveMuted, fontSize = 13.sp)
                    courses.forEach { course ->
                        EvolveDisclosure(course.title, expandedCourse == course.id || query.isNotBlank(),
                            { expandedCourse = if (expandedCourse == course.id) null else course.id },
                            "${course.modules.sumOf { selected[it.id].orEmpty().size }} selected") {
                            course.modules.filter { m -> query.isBlank() || course.title.contains(query,true) || course.code.contains(query,true) ||
                                (listOf(m.title) + m.topics).any { it.contains(query,true) } }.forEach { module ->
                                val picked = selected[module.id].orEmpty()
                                val all = picked.size == module.topics.size
                                Row(Modifier.fillMaxWidth().toggleable(all, enabled = !busy, role = Role.Checkbox,
                                    onValueChange = { checked -> setTopics(selected + (module.id to if (checked) module.topics.toSet() else emptySet())) }),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(all, null, enabled = !busy)
                                    Text("Module ${module.number} · All topics", color = EvolveText, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                }
                                EvolveDisclosure(module.title, expandedModule == module.id || query.isNotBlank(),
                                    { expandedModule = if (expandedModule == module.id) null else module.id }, "${picked.size} / ${module.topics.size} topics") {
                                    module.topics.filter { query.isBlank() || course.title.contains(query,true) || course.code.contains(query,true) ||
                                        module.title.contains(query,true) || it.contains(query,true) }.forEach { topic ->
                                        Row(Modifier.fillMaxWidth().toggleable(topic in picked, enabled = !busy, role = Role.Checkbox,
                                            onValueChange = { checked -> setTopics(selected + (module.id to if (checked) picked + topic else picked - topic)) })
                                            .heightIn(min = 48.dp).padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(topic in picked, null, enabled = !busy)
                                            Text(topic, color = EvolveMuted, fontSize = 13.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(color = EvolveLine)
                    }
                    Spacer(Modifier.height(24.dp)); Eyebrow("Teacher added something?", true); Spacer(Modifier.height(10.dp))
                    Box {
                        TextButton(onClick = { subjectMenu = true }, enabled = !busy) { Text("${subject.replaceFirstChar { it.uppercase() }} ▾", color = EvolveAccent) }
                        DropdownMenu(expanded = subjectMenu, onDismissRequest = { subjectMenu = false }) {
                            semester.pack.courses.flatMap { it.notebooks }.distinct().forEach { id ->
                                DropdownMenuItem(text = { Text(id.replaceFirstChar { it.uppercase() }) }, onClick = { subject = id; subjectMenu = false })
                            }
                        }
                    }
                    EvolveField("Extra topic", customText, { if (it.length <= 300) customText = it }, hint = "Use your teacher's wording", enabled = !busy,
                        modifier = Modifier.testTag("exam-custom-topic"))
                    TextButton(enabled = !busy && customText.isNotBlank() && extras.size < 100, onClick = {
                        setTopics(selected, extras + ExtraExamTopic(UUID.randomUUID().toString(),subject,customText.trim())); customText = ""
                    }) { Text("+ Add topic", color = EvolveAccent) }
                    extras.forEach { extra ->
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("${extra.subjectId.replaceFirstChar { it.uppercase() }} · ${extra.title}", color = EvolveText, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            TextButton(enabled = !busy, onClick = { setTopics(selected,extras.filterNot { it.id == extra.id }) }) { Text("Remove", color = EvolveMuted) }
                        }
                    }
                    Row(Modifier.fillMaxWidth().toggleable(confirmed, enabled = !busy, role = Role.Checkbox, onValueChange = { confirmed = it }),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(confirmed, null, enabled = !busy)
                        Text("Teacher-confirmed portion", color = EvolveText, fontSize = 13.sp)
                    }
                    Text(if (confirmed) "Saved as confirmed by you." else "Saved as an estimate. You can change it anytime.", color = EvolveMuted, fontSize = 12.sp)
                    var marksExpanded by rememberSaveable { mutableStateOf(false) }
                    EvolveDisclosure("Electrical + Electronics marks", marksExpanded, { marksExpanded = !marksExpanded }, "EE $ee + EC $ec") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f)) { EvolveField("EE marks", ee, { ee = it }, enabled = !busy) }
                            Column(Modifier.weight(1f)) { EvolveField("EC marks", ec, { ec = it }, enabled = !busy) }
                        }
                        Text("This split applies only to the combined EE/EC paper.", color = EvolveMuted, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
        feedback?.let { Text(it, color = EvolveWarm, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp)) }
        if (step == 1 && customText.isNotBlank()) Text("Add or clear the extra topic before saving.", color = EvolveWarm, fontSize = 12.sp)
        if (step == 1 && !validMarks) Text("Marks must be whole numbers from 0 to 100.", color = EvolveWarm, fontSize = 12.sp)
        EvolveAction(if (busy) "Saving…" else if (step == 0) "Choose topics →" else "Save $count topics", {
            if (step == 0) step = 1 else onSave(ExamPortion(name.trim(),confirmed,selected,extras),date,ee,ec)
        }, enabled = !busy && name.isNotBlank() && validDate && (step == 0 || (count > 0 && validMarks && customText.isBlank())))
        Spacer(Modifier.height(16.dp))
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("Leave without saving?") },
        text = { Text("Your previous exam portion will stay in place.") },
        confirmButton = { TextButton(onClick = { discard = false; onClose() }) { Text("Discard changes") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("Keep editing") } })
}
