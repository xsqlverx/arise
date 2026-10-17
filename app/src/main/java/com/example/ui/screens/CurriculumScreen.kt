package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
fun CurriculumScreen(vm: CurriculumViewModel, examDate: String, onClose: () -> Unit, onNotebook: (String) -> Unit,
    onPractice: (String, String, String) -> Unit) {
    val semester by vm.semester.collectAsState()
    val preview by vm.preview.collectAsState()
    val attempts by vm.attempts.collectAsState()
    val draft by vm.draft.collectAsState()
    val document by vm.document.collectAsState()
    val busy by vm.busy.collectAsState()
    val feedback by vm.feedback.collectAsState()
    val examOpen by vm.examOpen.collectAsState()
    val examSession by vm.examSession.collectAsState()
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
    if (examOpen && semester != null) {
        key(examSession) { ExamEditorScreen(semester!!,examDate,busy,feedback,{ vm.closeExam() },{ portion,date,ee,ec -> vm.saveExam(portion,date,ee,ec) }) }
        return
    }
    draft?.let { LessonScreen(pack,it,attempts.find { a -> a.id == it.id },busy,feedback,vm,onPractice); return }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    var moduleOpen by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var details by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(EvolveBlack).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
        TextButton(onClick = onClose,enabled = !busy) { Text("← Back",color = EvolveMuted) }
        Spacer(Modifier.height(20.dp)); Eyebrow("Your semester",true); Spacer(Modifier.height(12.dp))
        Text("Room to\nlearn.",fontFamily = SpaceGroteskFontFamily,fontSize = 40.sp,lineHeight = 45.sp,color = EvolveText)
        Spacer(Modifier.height(12.dp)); Reading("7 courses · 20 modules · offline PDFs",true)
        if (semester == null) {
            ContourLandscape(height = 110.dp)
            Reading(if (examOpen) "Import your syllabus once to choose exam topics." else "Your syllabuses are ready together.")
            Spacer(Modifier.height(16.dp))
            EvolveAction(if (busy) "Importing…" else "Import all 7 courses",{ vm.importSemester() },enabled = !busy)
        } else {
            Spacer(Modifier.height(20.dp))
            TextButton(onClick = { vm.editExam() },enabled = !busy) {
                Text(semester!!.exam?.let { "${it.name} · ${it.topicCount} topics · Edit ↗" } ?: "Set next exam topics ↗",color = EvolveAccent)
            }
        }
        feedback?.let { Spacer(Modifier.height(12.dp)); Text(it,color = EvolveWarm,fontSize = 13.sp,lineHeight = 20.sp) }
        Spacer(Modifier.height(16.dp))
        EvolveField("Find a subject or topic",query,{ query = it },hint = "Gradient, Python, water…")
        val courses = pack.courses.filter { c -> query.isBlank() ||
            (listOf(c.title,c.code) + c.modules.flatMap { listOf(it.title) + it.topics }).any { it.contains(query,true) } }
        if (courses.isEmpty()) Reading("No matches. Try another subject or topic.",true)
        courses.forEach { course ->
            EvolveDisclosure(course.title,expanded == course.id || query.isNotBlank(),
                { expanded = if (expanded == course.id) null else course.id },
                if (course.modules.isEmpty()) "Activities & practical work" else "${course.modules.size} modules") {
                TextButton(onClick = { vm.openDocument(course.source,1) }) { Text("Original syllabus ↗",color = EvolveAccent) }
                if (semester != null) Row(Modifier.horizontalScroll(rememberScrollState())) {
                    course.notebooks.forEach { id -> TextButton(onClick = { onNotebook(id) }) {
                        Text("${id.replaceFirstChar { it.uppercase() }} notes ↗",color = EvolveMuted,fontSize = 12.sp)
                    } }
                }
                course.activities.forEach { activity ->
                    Spacer(Modifier.height(12.dp)); Reading(activity.title)
                    TextButton(onClick = { vm.openDocument(course.source,activity.pages.first()) }) { Text("Activity details ↗",color = EvolveAccent) }
                    if (semester != null) TextButton(enabled = !busy,onClick = {
                        onPractice(course.notebooks.first(),activity.title,"Open the ${course.code} syllabus on page ${activity.pages.first()}. Check the teacher's selected activity, required record and deadline for: ${activity.title}.")
                    }) { Text("Plan this activity +",color = EvolveMuted) }
                }
                course.modules.forEach { module ->
                    val last = attempts.filter { it.moduleId == module.id }.maxByOrNull { it.timestamp }
                    val inFocus = module.id in semester?.focus.orEmpty()
                    EvolveDisclosure("M${module.number} · ${module.title}",moduleOpen == module.id || query.isNotBlank(),
                        { moduleOpen = if (moduleOpen == module.id) null else module.id },
                        if (last != null) "Starter check ${last.score}/${last.total}" else if (inFocus) "In exam focus" else null) {
                        if (last != null) Reading("Review ${last.reviewDate} · starter concept only",true)
                        if (semester != null && StarterLessons.forModule(module.id) != null) {
                            Spacer(Modifier.height(10.dp))
                            EvolveAction(if (last == null) "Start one concept · 15 min" else "Open concept / resume",{ vm.startModule(module.id) },enabled = !busy)
                        }
                        Spacer(Modifier.height(16.dp)); Eyebrow("Topics · practise from your class material")
                        module.topics.forEach { topic ->
                            Text(topic,color = EvolveMuted,fontSize = 13.sp,lineHeight = 22.sp,
                                modifier = Modifier.fillMaxWidth().clickable(enabled = semester != null && !busy) {
                                    onPractice(module.component,"Practise: $topic",ExamPracticeTopic("",module.component,topic).instructions)
                                }.padding(vertical = 12.dp))
                        }
                        TextButton(onClick = { vm.openDocument(course.source,module.pages.first()) }) {
                            Text("Syllabus · page ${module.pages.joinToString(", ")} ↗",color = EvolveAccent)
                        }
                        if (module.notes.isNotEmpty()) {
                            var notesOpen by rememberSaveable(module.id) { mutableStateOf(false) }
                            EvolveDisclosure("Syllabus notes",notesOpen,{ notesOpen = !notesOpen }) {
                                module.notes.forEach { Reading(it,true); Spacer(Modifier.height(10.dp)) }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    HorizontalDivider(color = EvolveLine)
                }
                if (course.notes.isNotEmpty()) {
                    var courseNotes by rememberSaveable(course.id) { mutableStateOf(false) }
                    EvolveDisclosure("Course notes",courseNotes,{ courseNotes = !courseNotes }) {
                        course.notes.forEach { Reading(it,true); Spacer(Modifier.height(10.dp)) }
                    }
                }
            }
            HorizontalDivider(color = EvolveLine)
        }
        Spacer(Modifier.height(20.dp))
        EvolveDisclosure("About these materials",details,{ details = !details }) {
            Reading("Twelve short lessons cover starter concepts. Other topics use your class notes and textbooks; checks do not certify whole-module readiness.",true)
            Spacer(Modifier.height(10.dp)); Reading(pack.scopeStatus,true)
            Spacer(Modifier.height(10.dp)); Reading("Workshop and life skills use practical/activity assessment. The unresolved 8 DP notebook stays separate.",true)
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun LessonScreen(pack: SemesterPack, d: CheckpointDraft, result: com.example.data.model.CurriculumAttemptEntity?,
    busy: Boolean, feedback: String?, vm: CurriculumViewModel, onPractice: (String,String,String) -> Unit) {
    val m = pack.modules.first { it.id == d.moduleId }; val c = pack.course(m.id)
    val lesson = StarterLessons.forModule(m.id)!!
    val reduced = LocalReduceMotion.current
    val scroll = rememberScrollState()
    var page by rememberSaveable(d.id) { mutableIntStateOf(0) }
    val ime = WindowInsets.ime.getBottom(androidx.compose.ui.platform.LocalDensity.current) > 0
    BackHandler(enabled = !ime && d.step == 0 && page > 0) { page-- }
    LaunchedEffect(d.moduleId,d.step,d.choices.size,page) { scroll.scrollTo(0) }
    Column(Modifier.fillMaxSize().background(EvolveBlack).imePadding().verticalScroll(scroll).padding(horizontal = 24.dp)) {
        TextButton(onClick = { if (d.step == 0 && page > 0) page-- else vm.back() },enabled = !busy) { Text("← Back",color = EvolveMuted) }
        Spacer(Modifier.height(18.dp)); Eyebrow("${c.code} · M${m.number} · One concept",true)
        Spacer(Modifier.height(14.dp)); Text(lesson.title,color = EvolveText,fontFamily = SpaceGroteskFontFamily,fontSize = 34.sp,lineHeight = 40.sp)
        feedback?.let { Spacer(Modifier.height(12.dp)); Text(it,color = EvolveWarm) }
        when (d.step) {
            0 -> {
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Start","Learn","Example","Recall").forEachIndexed { index,label ->
                        TextButton(onClick = { page = index },enabled = !busy,modifier = Modifier.weight(1f),contentPadding = PaddingValues(horizontal = 4.dp)) {
                            Text(label,color = if (page == index) EvolveAccent else EvolveDim,fontSize = 11.sp)
                        }
                    }
                }
                AnimatedContent(page,label = "lesson page",transitionSpec = {
                    val ms = if (reduced) 0 else 180
                    fadeIn(tween(ms)) togetherWith fadeOut(tween(ms))
                }) { active ->
                    Column {
                        when (active) {
                            0 -> { SectionTitle("Start on paper"); Reading(lesson.firstStep) }
                            1 -> { SectionTitle("Understand"); Reading(lesson.explanation) }
                            2 -> {
                                SectionTitle("Work the example")
                                Text(lesson.worked,color = EvolveText,fontFamily = FontFamily.Monospace,fontSize = 14.sp,lineHeight = 24.sp,
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(EvolveSurface).padding(18.dp))
                            }
                            3 -> {
                                SectionTitle("Close the example"); Reading(lesson.recall)
                                Spacer(Modifier.height(14.dp))
                                EvolveField("Your recall / working",d.recall,{ vm.recall(it) },multiline = true,
                                    hint = "Optional · saved as your own response",enabled = !busy)
                            }
                        }
                        Spacer(Modifier.height(28.dp))
                        EvolveAction(if (active == 3) "Try the two checks →" else "Continue →",{
                            if (active == 3) vm.beginCheck() else page = active + 1
                        },enabled = !busy)
                    }
                }
                Spacer(Modifier.height(20.dp))
                var sources by rememberSaveable { mutableStateOf(false) }
                EvolveDisclosure("Sources & coverage",sources,{ sources = !sources }) {
                    Reading("This authored starter covers one concept. The two checks are not teacher questions or a whole-module assessment.",true)
                    TextButton(onClick = { vm.openDocument(c.source,m.pages.first()) }) { Text("Original syllabus ↗",color = EvolveAccent) }
                    val uri = androidx.compose.ui.platform.LocalUriHandler.current
                    TextButton(onClick = { runCatching { uri.openUri(lesson.reference) } }) { Text("Concept reference online ↗",color = EvolveMuted) }
                }
            }
            1 -> {
                val check = lesson.checks[d.choices.size]
                SectionTitle("Check ${d.choices.size+1} / ${lesson.checks.size}"); Reading(check.prompt)
                Spacer(Modifier.height(16.dp))
                check.options.forEachIndexed { index,text ->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(if (d.selected == index) EvolveRaised else EvolveSurface)
                        .clickable(enabled = !busy && !d.checked) { vm.select(index) }.padding(12.dp)) {
                        RadioButton(selected = d.selected == index,onClick = null)
                        Text(text,color = EvolveText,modifier = Modifier.weight(1f).padding(start = 10.dp,top = 9.dp),fontSize = 14.sp)
                    }; Spacer(Modifier.height(10.dp))
                }
                if (d.checked) {
                    Reading(if (d.selected == check.answer) "Correct. ${check.explanation}" else "Revisit this. ${check.explanation}")
                    Spacer(Modifier.height(18.dp)); EvolveAction(if (busy) "Saving…" else if (d.choices.size+1 == lesson.checks.size) "Save check" else "Next check →",{ vm.next() },enabled = !busy)
                } else EvolveAction("Check my answer",{ vm.checkAnswer() },enabled = !busy && d.selected >= 0)
            }
            2 -> {
                Spacer(Modifier.height(24.dp)); SavedMark()
                SectionTitle("Starter check saved")
                Text("${lesson.score(d.choices)} / ${lesson.checks.size}",fontFamily = SpaceGroteskFontFamily,fontSize = 56.sp,color = EvolveAccent)
                Reading(result?.let { "Review on ${it.reviewDate}" } ?: "Your result is saved.",true)
                SectionTitle("Try it independently"); Reading(lesson.practice)
                Spacer(Modifier.height(18.dp))
                EvolveAction("Start independent practice",{ onPractice(m.component,"Practise: ${m.title}",lesson.practice) },enabled = !busy)
                TextButton(onClick = { vm.back() }) { Text("Choose another topic ↗",color = EvolveAccent) }
                TextButton(onClick = { vm.retry() },enabled = !busy) { Text("Review lesson & repeat checks",color = EvolveMuted) }
                var evidence by rememberSaveable { mutableStateOf(false) }
                EvolveDisclosure("What this result shows",evidence,{ evidence = !evidence }) {
                    Reading("This is evidence for one starter concept, not whole-module exam readiness. Repeated checks use the same two questions.",true)
                }
            }
        }
        Spacer(Modifier.height(40.dp))
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
