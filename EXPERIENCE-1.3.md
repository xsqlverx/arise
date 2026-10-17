# Evolve 1.3

Today now exposes **Next exam** directly. The two-step editor collects a name and optional date, then specific syllabus topics (or whole modules). Teacher-added topics carry their own subject. The estimated portion remains editable and is only labeled confirmed when the user chooses that status. Electrical/electronics marks stay separate from other papers.

Recommendations respect the saved topics: a starter lesson needs an actual selected syllabus anchor; uncovered topics become independent practice with the learner's own class questions. Completed practice advances to another selected topic without claiming mastery. Urgent dated work retains priority. Saving uses one Room transaction for the portion/date/marks and clears stale reminder scheduling.

Today shows one action and a short preview; complete instructions and reasoning remain available under **Why & how** and in the actual study flow. Unconfirmed, empty assignment placeholders no longer occupy the home screen. The syllabus library reveals one course/module at a time. Lessons present Start, Learn, Example and Recall separately. Course context, sources and evidence limits remain available in disclosures.

Motion is finite: details expand with an arrow turn, the next action and lesson steps crossfade, saved results settle into place, and existing button press/dock feedback remains. In-app and system reduced motion are respected. No perpetual background animation or extra motion dependency was added. Matte-black surfaces, lavender contours, fonts and glass dock remain.

## Storage and build

VersionCode 5 / versionName 1.3. Room v4 adds one `examJson` column with a non-destructive 3-to-4 migration. Backup v4 reads versions 1 through 4. Existing notes, exact whitespace, tasks, attempts and profile settings stay intact. Application identity and AI Studio signing are unchanged.

The latest AI Studio commit had reintroduced duplicate shared UI declarations and a replacement sheet. This update retains the canonical shared widgets and native IME-aware sheet instead. Sync complete latest main; do not recreate UIWidgets.kt.

Local checks parse Kotlin and run the domain/backup tests and SQLite preservation fixture. The cloud workflow builds the actual APK and runs Android-aware tests, including the real Room upgrade and Compose exam-selection interactions. Native Compose captures are retained as CI artifacts for review; they are runner captures, not physical-device evidence. No Gradle/Android compilation runs on the laptop.

## Phone acceptance

- Today -> Next exam: choose a date or leave it unknown; clear the starting selection, pick one topic, add a teacher topic, save, restart, and check that both remain under the correct subjects.
- Check that the home recommendation reflects the saved portion and that an unrelated module starter is excluded.
- Edit the portion, cancel, and verify that the previous selection remains. Keep an extra topic draft and verify that Save asks you to add or clear it.
- Use the real keyboard and large text in both editor steps. Verify the save footer and Back/unsaved behavior.
- Open library course/module disclosures and a four-stage starter lesson. Verify motion, Reduce motion, full source access and saved recall.
- Complete independent practice and verify the next selected topic. Export/import a backup and verify portion/date/marks, notes and attempts.
- Verify glass rendering and notification delivery on the actual device; cloud build and runner captures do not establish those behaviors.
