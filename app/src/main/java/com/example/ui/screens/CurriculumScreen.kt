package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.*
import com.example.ui.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

@Composable
private fun Reading(text: String, muted: Boolean = false) {
    Text(text, color = if (muted) EvolveMuted else EvolveText, fontFamily = ManropeFontFamily,
        fontSize = 14.sp, lineHeight = 23.sp)
}
@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(24.dp)); Eyebrow(text, true); Spacer(Modifier.height(12.dp))
}

@Composable
fun CurriculumScreen(vm: CurriculumViewModel, onClose: () -> Unit, onNotebook: (String) -> Unit,
    onPractice: (String, String, String) -> Unit) {
    val semester by vm.semester.collectAsState()
    val preview by vm.preview.collectAsState()
    val attempts by vm.attempts.collectAsState()
    val draft by vm.draft.collectAsState()
    val document by vm.document.collectAsState()
    val busy by vm.busy.collectAsState()
    val feedback by vm.feedback.collectAsState()
    val pack = semester?.pack ?: preview
    val imeVisible = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    BackHandler(enabled = !imeVisible) { vm.back() }
    document?.let { SyllabusReader(it) { vm.back() }; return }
    if (pack == null) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            TextButton(onClick = onClose) { Text("← Back") }
            Reading(feedback ?: "Opening your semester…")
        }; return
    }
    val d = draft
    if (d != null) {
        LessonScreen(pack, d, attempts.find { it.id == d.id }, busy, feedback, vm, onPractice); return
    }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var ee by rememberSaveable(semester?.electricalMarks) { mutableStateOf((semester?.electricalMarks ?: 20).toString()) }
    var ec by rememberSaveable(semester?.electronicsMarks) { mutableStateOf((semester?.electronicsMarks ?: 20).toString()) }
    Column(Modifier.fillMaxSize().background(EvolveBlack).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        TextButton(onClick = onClose, enabled = !busy) { Text("← Back to your day", color = EvolveMuted) }
        Spacer(Modifier.height(20.dp)); Eyebrow("S1 / Your actual syllabus", true)
        Spacer(Modifier.height(12.dp))
        Text("A place\nto start.", fontFamily = SpaceGroteskFontFamily, fontSize = 44.sp, lineHeight = 48.sp, color = EvolveText)
        ContourLandscape(height = 140.dp)
        Reading("7 courses · 20 modules · 7 original PDFs")
        Spacer(Modifier.height(8.dp))
        Reading("The supplied syllabuses map what to study. Twelve short lessons cover starter concepts in the likely IE focus. Full teacher notes, textbooks and assignment questions are still separate material.", true)
        if (semester == null) {
            SectionTitle("One import. No retyping.")
            Reading("Preview the courses below, then import them together. Your saved class notes, tasks and settings stay in place.")
            Spacer(Modifier.height(16.dp))
            EvolveAction(if (busy) "Importing…" else "Import all 7 courses", { vm.importSemester() }, enabled = !busy)
        }
        feedback?.let { Spacer(Modifier.height(16.dp)); Text(it, color = EvolveWarm, fontSize = 13.sp, lineHeight = 21.sp) }
        SectionTitle("Next IE · estimated focus")
        Reading(pack.scopeStatus, true)
        Spacer(Modifier.height(12.dp))
        Reading("EC 20 + EE 20 = 40 is the starting split. It applies to the combined electrical/electronics paper only. Other paper marks and exact exam dates need confirmation.", true)
        if (semester != null) {
            Spacer(Modifier.height(14.dp))
            Reading("Current split: EE ${semester!!.electricalMarks} + EC ${semester!!.electronicsMarks} = ${semester!!.electricalMarks + semester!!.electronicsMarks} marks")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f)) { EvolveField("EE marks", ee, { ee = it }, enabled = !busy) }
                Column(Modifier.weight(1f)) { EvolveField("EC marks", ec, { ec = it }, enabled = !busy) }
            }
            TextButton(onClick = { vm.saveMarks(ee, ec) }, enabled = !busy) { Text("Save split") }
            Reading("Toggle modules below as the teacher confirms the portion. Earlier modules stay available for prerequisite revision. Marks are recorded for context; they do not estimate mastery or allocate study time.", true)
        }
        SectionTitle("Course map")
        EvolveField("Search courses or topics", query, { query = it }, hint = "Gradient, Python, water…")
        Spacer(Modifier.height(12.dp))
        pack.courses.filter { c -> query.isBlank() || (listOf(c.title,c.code) + c.modules.flatMap { it.topics }).any { it.contains(query,true) } }.forEach { c ->
            val selected = expanded == c.id || query.isNotBlank()
            Column(Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
                TextButton(onClick = { expanded = if (expanded == c.id) null else c.id }, contentPadding = PaddingValues(0.dp)) {
                    Column(Modifier.fillMaxWidth()) {
                        Eyebrow("${c.code} / ${if (c.modules.isEmpty()) "Activities & practical work" else "${c.modules.size} modules"}")
                        Spacer(Modifier.height(8.dp))
                        Text(c.title + if (selected) " −" else " +", color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 23.sp, lineHeight = 29.sp)
                    }
                }
                if (selected) {
                    TextButton(onClick = { vm.openDocument(c.source, 1) }) { Text("Open original syllabus · ${c.source.pages} pages ↗", color = EvolveAccent) }
                    c.notes.forEach { Reading(it, true); Spacer(Modifier.height(10.dp)) }
                    if (semester != null) c.notebooks.forEach { id -> TextButton(onClick = { onNotebook(id) }) {
                        Text("${id.replaceFirstChar { it.uppercase() }} class notes ↗", color = EvolveAccent)
                    } }
                    c.activities.forEach { a ->
                        Spacer(Modifier.height(12.dp)); Reading(a.title)
                        TextButton(onClick = { vm.openDocument(c.source,a.pages.first()) }) { Text("Activity details · page ${a.pages.joinToString(", ")} ↗",color=EvolveAccent) }
                        if (semester != null) TextButton(enabled = !busy, onClick = {
                            onPractice(c.notebooks.first(),a.title,"Open the ${c.code} syllabus on page ${a.pages.first()} and check the teacher's actual instructions for: ${a.title}. Confirm the selected activity/experiment, required record and deadline. The class's current selection and completion are unknown.")
                        }) { Text("Plan this activity +", color=EvolveMuted) }
                    }
                    c.modules.forEach { m ->
                        val last = attempts.filter { it.moduleId == m.id }.maxByOrNull { it.timestamp }
                        Spacer(Modifier.height(12.dp)); HorizontalDivider(color = EvolveLine); Spacer(Modifier.height(16.dp))
                        if (semester != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Eyebrow("MODULE ${m.number} / ${m.component}", true)
                                Text(if (m.id in semester!!.focus) "In estimated IE focus" else "Reference / prerequisite", color = EvolveMuted, fontSize = 12.sp)
                            }
                            Checkbox(checked = m.id in semester!!.focus, onCheckedChange = { vm.setFocus(m.id,it) }, enabled = !busy)
                        } else Eyebrow("MODULE ${m.number}${if (m.focus) " / Estimated focus" else " / Prerequisite"}", m.focus)
                        Spacer(Modifier.height(8.dp)); Reading(m.title)
                        Spacer(Modifier.height(10.dp))
                        if (semester != null) Reading("Tap a topic to practise it with your own class material.", true)
                        m.topics.forEach { topic -> Text("· $topic", color = EvolveMuted, fontSize = 13.sp, lineHeight = 22.sp,
                            modifier = Modifier.fillMaxWidth().clickable(enabled = semester != null && !busy) {
                                onPractice(m.component,"Practise: $topic",
                                    "Open your class notes or textbook for ${c.code}, module ${m.number}: $topic. Work through one example with its solution. Close the solution and attempt a second question on paper. Compare every step, write what went wrong and save any teacher advice in class notes. This app has the syllabus outline; the actual class question has not been supplied.")
                            }.padding(vertical = 12.dp)) }
                        m.notes.forEach { Spacer(Modifier.height(8.dp)); Text(it, color = EvolveWarm, fontSize = 12.sp, lineHeight = 20.sp) }
                        TextButton(onClick = { vm.openDocument(c.source, m.pages.first()) }) { Text("Syllabus · page ${m.pages.joinToString(", ")}", color = EvolveAccent) }
                        if (semester != null) {
                            last?.let { Text("Starter check ${it.score}/${it.total} · review ${it.reviewDate}. Remaining module topics are untested.", color = EvolveWarm, fontSize = 12.sp, lineHeight = 20.sp) }
                            if (StarterLessons.forModule(m.id) != null) TextButton(enabled = !busy, onClick = { vm.startModule(m.id) }) {
                                Text(if (last == null) "Start one concept · 15 min →" else "Open concept / resume →", color = EvolveAccent)
                            } else Reading("No authored lesson yet. Use the outline to select a teacher example and add a practice step.", true)
                            TextButton(enabled = !busy, onClick = { onPractice(m.component, "Practise: ${m.title}",
                                "Open your class material for ${c.code}, module ${m.number}. Choose one topic: ${m.topics.joinToString("; ")}. Work through one teacher example, close the solution, then attempt another question. Check each step. Actual questions have not been supplied.") }) {
                                Text("Add a practice step to my plan +", color = EvolveMuted)
                            }
                        }
                    }
                }
            }
            HorizontalDivider(color = EvolveLine)
        }
        Spacer(Modifier.height(24.dp)); Reading("Life skills and the workshop keep their activity/practical assessment. “8 DP” remains an unresolved notebook; it has not been renamed.", true)
        Spacer(Modifier.height(60.dp))
    }
}

@Composable
private fun LessonScreen(pack: SemesterPack, d: CheckpointDraft, result: com.example.data.model.CurriculumAttemptEntity?,
    busy: Boolean, feedback: String?, vm: CurriculumViewModel, onPractice: (String,String,String) -> Unit) {
    val m = pack.modules.first { it.id == d.moduleId }; val c = pack.course(m.id)
    val lesson = StarterLessons.forModule(m.id)!!
    val scroll = rememberScrollState()
    LaunchedEffect(d.moduleId,d.step,d.choices.size) { scroll.scrollTo(0) }
    Column(Modifier.fillMaxSize().background(EvolveBlack).imePadding().verticalScroll(scroll).padding(horizontal = 24.dp)) {
        TextButton(onClick = { vm.back() }, enabled = !busy) { Text("← Syllabus / draft kept", color = EvolveMuted) }
        Spacer(Modifier.height(18.dp)); Eyebrow("${c.code} / Module ${m.number} / 15 min", true)
        Spacer(Modifier.height(14.dp)); Text(lesson.title, color = EvolveText, fontFamily = SpaceGroteskFontFamily, fontSize = 36.sp, lineHeight = 41.sp)
        Spacer(Modifier.height(12.dp)); Reading("Authored starter · one concept from the supplied outline. These checks are not teacher questions or a whole-module assessment.", true)
        feedback?.let { Spacer(Modifier.height(12.dp)); Text(it, color = EvolveWarm) }
        when (d.step) {
            0 -> {
                SectionTitle("01 / Start on paper"); Reading(lesson.firstStep)
                SectionTitle("02 / Understand"); Reading(lesson.explanation)
                SectionTitle("03 / Work the example")
                Text(lesson.worked, color = EvolveText, fontFamily = FontFamily.Monospace, fontSize = 14.sp, lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(EvolveSurface).padding(18.dp))
                SectionTitle("04 / Close the example"); Reading(lesson.recall)
                Spacer(Modifier.height(12.dp))
                EvolveField("Your recall / working (optional)", d.recall, { vm.recall(it) }, multiline = true,
                    hint = "This is saved as your own response, not automatically graded.", enabled = !busy)
                Spacer(Modifier.height(20.dp)); EvolveAction("Try the two checks →", { vm.beginCheck() }, enabled = !busy)
                TextButton(onClick = { vm.openDocument(c.source,m.pages.first()) }) { Text("See where this sits in the syllabus ↗", color = EvolveAccent) }
                val uri = androidx.compose.ui.platform.LocalUriHandler.current
                TextButton(onClick = { runCatching { uri.openUri(lesson.reference) } }) { Text("Concept reference online ↗", color = EvolveMuted) }
            }
            1 -> {
                val check = lesson.checks[d.choices.size]
                SectionTitle("Check ${d.choices.size+1} / ${lesson.checks.size}"); Reading(check.prompt)
                Spacer(Modifier.height(16.dp))
                check.options.forEachIndexed { index, text ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (d.selected == index) EvolveRaised else EvolveSurface)
                        .clickable(enabled = !busy && !d.checked) { vm.select(index) }.padding(12.dp)) {
                        RadioButton(selected = d.selected == index, onClick = null)
                        Text(text, color = EvolveText, modifier = Modifier.weight(1f).padding(start = 10.dp,top = 9.dp), fontSize = 14.sp)
                    }; Spacer(Modifier.height(10.dp))
                }
                if (d.checked) {
                    Reading(if (d.selected == check.answer) "Correct. ${check.explanation}" else "Revisit this. ${check.explanation}")
                    Spacer(Modifier.height(18.dp)); EvolveAction(if (busy) "Saving…" else if (d.choices.size+1 == lesson.checks.size) "Save check & review date" else "Next check →", { vm.next() }, enabled = !busy)
                } else EvolveAction("Check my answer", { vm.checkAnswer() }, enabled = !busy && d.selected >= 0)
            }
            2 -> {
                SectionTitle("Saved / starter evidence")
                Text("${lesson.score(d.choices)} / ${lesson.checks.size}", fontFamily = SpaceGroteskFontFamily, fontSize = 56.sp, color = EvolveAccent)
                Reading("${result?.reviewDate?.let { "Review on $it. " } ?: "Your result is saved. "}A correct answer here does not establish exam readiness. Repeated checks use the same two questions.", true)
                SectionTitle("Now practise independently"); Reading(lesson.practice)
                Spacer(Modifier.height(18.dp))
                EvolveAction("Start independent practice", { onPractice(m.component, "Practise: ${m.title}",lesson.practice) }, enabled = !busy)
                TextButton(onClick = { vm.back() }) { Text("Back to syllabus / choose another module →", color = EvolveAccent) }
                TextButton(onClick = { vm.retry() }, enabled = !busy) { Text("Review lesson & repeat these checks", color = EvolveMuted) }
            }
        }
        Spacer(Modifier.height(60.dp))
    }
}

/** Native offline reader. Only a single bounded-resolution page is rendered at a time. */
private val syllabusCacheLock = Any()

@Composable
private fun SyllabusReader(document: SyllabusPage, onBack: () -> Unit) {
    val context = LocalContext.current
    var page by rememberSaveable(document.source.asset,document.page) { mutableIntStateOf(document.page) }
    var error by remember(document.source.asset,page) { mutableStateOf<String?>(null) }
    val bitmap by produceState<Bitmap?>(null, document.source.asset,page) {
        value = null
        try {
            value = withContext(Dispatchers.IO) {
                val bytes = context.assets.open(document.source.asset).use { it.readBytes() }
                val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
                check(hash == document.source.sha256)
                val cache = File(context.cacheDir,"syllabus-${document.source.sha256}.pdf")
                synchronized(syllabusCacheLock) {
                    if (!cache.exists() || cache.length() != bytes.size.toLong()) {
                        val pending = File(context.cacheDir,"syllabus-pending.pdf")
                        pending.writeBytes(bytes)
                        check(pending.renameTo(cache))
                    }
                }
                ParcelFileDescriptor.open(cache,ParcelFileDescriptor.MODE_READ_ONLY).use { descriptor ->
                    PdfRenderer(descriptor).use { pdf ->
                        pdf.openPage(page-1).use { p ->
                            val width = 1440; val height = (width.toDouble()*p.height/p.width).toInt().coerceIn(1,2400)
                            Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888).also { image ->
                                image.eraseColor(android.graphics.Color.WHITE)
                                p.render(image,null,null,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; error = "This source page could not open. Return and retry." }
    }
    Column(Modifier.fillMaxSize().background(EvolveBlack).padding(16.dp)) {
        TextButton(onClick = onBack) { Text("← Return to study",color=EvolveAccent) }
        Reading("Original syllabus · page $page / ${document.source.pages}")
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick = { page-- },enabled=page>1) { Text("← Previous") }
            TextButton(onClick = { page++ },enabled=page<document.source.pages) { Text("Next →") }
        }
        Reading("Swipe sideways to read the full-size page; scroll to reach the bottom.", true)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).horizontalScroll(rememberScrollState())) {
            bitmap?.let { Image(it.asImageBitmap(),contentDescription="Original syllabus page $page",modifier=Modifier.width(720.dp).height((720f*it.height/it.width).dp)) }
                ?: Reading(error ?: "Rendering page…",true)
        }
    }
}
