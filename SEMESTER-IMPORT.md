# Evolve 1.2 — semester to first study session

Build from the latest `main` in Google AI Studio. Keep the existing application ID and AI Studio signing configuration. This update is versionCode 3, versionName 1.2; it should update the existing application when signed with the same key. Do not regenerate the app from a design prompt.

## Start using it

1. Open **My semester** from Today or Settings. Preview the pack and tap **Import all 7 courses** once. The seven supplied syllabus PDFs are already bundled; no phone folder, account or upload is needed.
2. Inspect **Next IE / estimated focus**. M3–4 are selected for the four other theory courses. The combined EEE outline places Electrical in M1–2 and Electronics in M3–4, so all four are initially retained. The starting split is EE 20 + EC 20; it applies only to that combined paper. Change marks or module selections when needed. Neither the marks nor the estimated portion establish learner mastery.
3. Return to Today and start its specific concept. A short authored explanation, worked example, optional saved recall response and two checks form one starter session. An incomplete session resumes when that module is opened again.
4. After the checks, tap **Start independent practice**. The next task opens with a focus timer and concrete instructions, using your class notes/textbook for the actual question. It remains editable in Plan. You can also tap any syllabus topic to start a practice step directly.
5. Use **Original syllabus** to read the source offline. The native reader opens at the mapped page; page controls, vertical scrolling and horizontal scrolling cover the complete document.

The import maps seven courses and twenty theory modules. Separate Electrical/Electronics notebooks remain linked to their shared assessed course. Workshop and Life Skills retain their practical/activity requirements; their relative course weeks are not converted into invented deadlines. The unresolved “8 DP” notebook stays as it is.

## What is implemented

- Twelve original starter lessons and 24 short questions across the default focus: Maths M3–4, Chemistry M3–4, Graphics M3–4, Python M3–4, Electrical M1–2 and Electronics M3–4. Each lesson explicitly names its limited coverage and gives a further practice task. Online concept references are optional; lesson text, questions and source PDFs work offline.
- Recommendations share one domain function across Today, Plan, rescheduling and reminders. Dated urgent tasks/missing briefs take priority; undated missing briefs do not gate semester study. Requested independent practice precedes new starter lessons. Otherwise due starter reviews and untried concepts are considered, with recent activity used to rotate courses.
- A wrong/partial starter check returns for review the next day; a fully correct check after three days. These are simple scheduling intervals, not a validated mastery model. Repeats use the same questions and are labeled accordingly.
- Room v3 adds the semester and checkpoint attempts using an explicit 2→3 migration. The existing 1→2 migration remains available. No destructive migration is used. Import does not replace existing notes, tasks or profile settings.
- Stable checkpoint attempt IDs, guarded choices, exact recall text and serialized draft writes. A completed result is acknowledged after the database write; interrupted final saves can retry with the same ID.
- Backup v3 includes the course map, selected scope, component marks and committed checkpoint responses/results. Export takes a database transaction snapshot. Readers still accept v1/v2 backups. Drafts are interruption state on the current device, not committed backup records. The original PDFs travel with the APK, rather than being duplicated in every JSON backup.

## Boundaries

This is a prepared import of the seven supplied syllabuses, not a universal PDF/OCR ingestion engine. Arbitrary new syllabus PDFs require a reviewed course map. Syllabuses are outlines, not full lecture notes. These twelve starter concepts do not cover every listed topic, provide every assignment question, or establish exam readiness. Topics without authored lessons remain browsable and can become practice tasks from the learner's own material.

Exam dates, precise teacher portions, other papers' marks, class-selected experiments and actual assignment questions still need confirmation. The app does not silently fill those gaps. The historical exam date is not installed as a new default. Existing availability and protected windows remain editable in Setup.

## Validation and cloud/device checklist

`tools/check-source.ps1` uses cached Kotlin/JUnit tools only; it does not run Gradle or install an Android SDK. It parses Kotlin source, compiles the real pure domain/backup code for JVM tests and runs a SQLite migration fixture. PDF hashes and source page bounds are checked. These checks do not validate Compose/Android types, Room's generated schema or native rendering.

Before declaring the APK ready, build in AI Studio and test on a physical device:

- Update over 1.1 with an existing multiline/indented note, task and profile. Confirm each survives. Confirm app identity/signing and update installation.
- Import all seven once; reopen the app and confirm scope/marks persist. Existing notes remain unchanged; the workshop notebook is added without renaming “8 DP”.
- Toggle EE and EC modules separately. Today/Plan/Why this first/reschedule agree. Confirm dated urgent work overrides starter study and undated missing briefs do not block it.
- Start a lesson, type indented recall, rotate/background/kill the process, reopen that module and check recovery. Verify keyboard Back dismisses the keyboard before leaving the screen.
- Try wrong and correct answers; taps cannot change a checked answer. Complete the last check and tap repeatedly; there should be one attempt. Relaunch and verify the saved result and review date. Retake creates a new attempt, clearly using repeated questions.
- Start independent practice and verify it opens the saved task with its timer; it can be edited, completed and reopened. Test a syllabus topic without a starter lesson too.
- Open each source PDF offline, visit first/last/mapped pages, rapidly change pages and scroll in both directions. Inspect text legibility and memory behavior on the phone.
- Export v3 and merge it back; verify exact recall/notes, scope, split, tasks and attempts. Old v1/v2 backups remain readable and do not erase an installed semester. Invalid input is rejected before writing.
- Check real IME placement, text scaling, scrolling, transitions, PDF rendering and reminders on Android. Those require native evidence.
