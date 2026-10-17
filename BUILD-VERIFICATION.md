# Evolve 1.3 — build verification

This repair retains the complete semester import, seven offline PDFs, twenty-module map, twelve starter lessons, saved recall/checkpoint evidence, reviews, independent practice, class notes, backups and Room v4 migrations. It restores the planner, actual-data chart, glass backdrop, reduced motion and native keyboard-aware sheets after the broad AI Studio rewrite. Existing note categories remain visible, including kinds saved while testing that rewrite.

The application ID and AI Studio signing configuration remain unchanged. VersionCode is 5 and versionName is 1.3. The next-exam editor saves individual syllabus topics, teacher-added topics, the exam name/date and confirmation status; see EXPERIENCE-1.3.md.

## Required checks

- `python tools/check-project.py`: fast completeness/feature-preservation checks, one canonical set of UI declarations, compatible declared tool versions, manifest reminder recovery, PDF hashes and source page bounds.
- `tools/check-source.ps1`: the lightweight cached Kotlin/JVM suite and migration fixture on Windows. No local Gradle/Android build or SDK installation.
- **Android compile and regression checks** in GitHub Actions: the actual Gradle 9.3.1 / JDK 17 Android build, KSP/Room generation, resources/manifest, Compose/Kotlin types, APK assembly and Android-aware/JVM unit tests. This runs on pushes to main, pull requests and manual dispatch.

The CI build uses a disposable debug certificate only for compilation validation. Its APK is deliberately not published for installation; it does not replace the AI Studio-managed update certificate. Continue compiling/installing update APKs through the existing AI Studio project with its original signing credentials. A green cloud build verifies compilation and those tests, not physical-device keyboard, rendering, notification-delivery or every runtime path.

## Paste into the existing AI Studio project

```text
Use the latest complete xsqlverx/arise main branch, including the repair commit, as the source of truth. Preserve all relative file paths and verify app/src/main/AndroidManifest.xml, all shared components/theme/domain files and all seven syllabus PDFs exist in the actual cloud workspace before compiling.

Preserve the semester import, twenty modules, twelve starter lessons/checks, saved recall/progress, reviews, independent practice, exact class notes/drafts, native editor sheets, protected-time scheduling, factual progress chart, glass backdrop, reduced motion, next-exam topics/date, reminders, transactional backups and non-destructive Room migrations. Keep applicationId com.aistudio.evolve.acadsq and the current AI Studio-managed signing credentials.

Do not replace the app with an older/simple version, remove features, fabricate sample progress or weaken tests to resolve an error. Fix the precise failing source/API/configuration instead. Keep one canonical LocalReduceMotion/LocalSheetFeedback and one set of shared widgets; do not create replacement UIWidgets copies. Gradle must remain compatible with AGP 9.1.1 (Gradle 9.3.1 minimum).

Run python3 tools/check-project.py and compile the entire Android application. Inspect the GitHub Android compile and regression checks for the same commit. Report the actual build result and exact first compiler errors if it still fails; provide an update APK only after successful compilation with the existing signing identity.
```

After installing, verify note whitespace/draft recovery, import persistence, lesson completion/retake, independent practice, protected scheduling, offline PDF navigation and reminders on the phone. The full flow checklist remains in SEMESTER-IMPORT.md.
