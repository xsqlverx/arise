# Evolve prototype 2 — cloud build and device check

This revision develops the existing Kotlin/Compose app. Application ID and AI Studio signing remain intact. Version code is 2 (version name 1.1), with an explicit Room 1→2 migration preserving existing records. No Android SDK, emulator, Gradle build or APK installation was run locally.

## Changes

- Connected screen transitions, saved screen state, animated modal windows, larger controls, scrollable notebook filters and in-app reduced motion.
- The floating dock samples the actual screen layer. Its background is blurred on Android 12+, with sharp icons/selection lens and a translucent fallback on earlier versions. This is a native approximation, not proprietary optical refraction.
- Real editable tasks with a subject, exact first step, duration, optional confirmed deadline and completion state. Focus sessions show the actual instructions and pinned personal notes.
- Today and Plan use current dates. The next-action explanation uses the same action as its button. The planner fits one next block inside the editable study window, excludes protected commitments, offers future alternatives and sorts the timeline chronologically. It does not schedule the entire task queue or assume unknown curriculum coverage.
- Notes retain exact whitespace, line breaks and Unicode. Draft snapshots, stable IDs, save guards, retry feedback and a separate modal editor protect capture. Save has a separate footer above the IME. Edits preserve original creation order.
- The sample quiz starts at question 1, requires checked answers, records once per attempt ID and preserves interrupted state. Retakes use separate IDs. Samples and legacy checks are separated from personal task activity; invalid 0/0 records are excluded without erasing them.
- Timer restoration uses durable endpoints and monotonic time within a boot. A paused timer no longer loses time when another screen is opened. Timer completion awards no mastery.
- JSON export writes an actual file. Import validates the full payload and shows a merge preview before a Room transaction, including sessions and tasks. Matching IDs are replaced, other records remain, and reminders are off after import.
- Opt-in reminders request Android permission. Choose a dated block through **Find another time** to schedule them. Delivery uses ordinary inexact alarms, one reminder plus at most one follow-up, focus/protected-time suppression, task links and Busy/snooze. Schedules are reconsidered on reboot/time changes. Android battery policies still govern delivery.

Fresh notebooks start without sample teacher claims. Existing notes and older results remain intact. Sample Python practice is available explicitly from Plan or Progress.

## Verification performed locally

- Kotlin PSI parsed all 37 Kotlin source/test files with zero syntax errors. This is syntax validation, not Android type checking or compilation.
- 17 JUnit tests passed for planner/quiz transitions and backup validation/round-tripping, using actual production models/domain/codec on the JVM. Only Room annotation metadata was replaced by small stand-ins for this lightweight run.
- Migration SQL was executed against a SQLite v1 fixture: note text, scores and profile survived, defaults were applied and the task table was created. Room's Android schema validation still needs the cloud build/device upgrade.
- XML parsing and `git diff --check` are checked before commit.

`tools/check-source.ps1` reproduces the lightweight checks using an existing cached Kotlin compiler and small local test jars. It installs nothing and does not invoke Gradle. Android compilation and runtime checks belong in the cloud/device workflow.

## Compile and test

Compile this source in the existing AI Studio Android project. Keep its signing configuration and application identity; install the update over the first prototype to exercise migration.

1. Open Settings → starting point. Confirm the exam date (or leave it unknown), protected times and a realistic study window. Save, restart and check persistence.
2. Capture a multiline class tip with an indented first line, blank lines and Unicode. Save with the real keyboard open; restart, edit and compare exact text. Also interrupt an unsaved draft and reopen the app. Check long notes, landscape, large text and Back behavior.
3. Add a real study task in Plan. Enter an exact starting instruction, open it, pause/resume focus, mark it complete and check the activity count. Completion is self-reported, not a learning score.
4. Open the sample Python lesson. “Check my understanding” must show question 1/3. Answer B, C, B for 2/3. Rapid repeated taps must produce one result. Review and retake for a distinct attempt; restart and check the records.
5. Change protected times, choose another block and check the full date and chronological timeline. Test an expired deadline and an availability window entirely inside a protected commitment.
6. Export JSON, inspect the file and import it through the preview. Check notes, tasks, results and settings. Invalid files must leave records unchanged.
7. Enable reminders, grant permission and explicitly choose a near-future block. Test background delivery, task links, Busy, denial, cancellation and protected/focus suppression. Delivery is approximate; this has not yet been tested on Android.
8. Inspect dock blur, screen/sheet transitions and reduced motion on the actual phone. Report native screenshots and compiler/device errors so the next revision can use direct evidence.

Native compilation, visual fidelity, IME placement, process-death recovery, real Room migration and reminder delivery remain pending until these cloud/device checks are performed.
