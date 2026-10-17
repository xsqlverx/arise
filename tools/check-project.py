"""Fast source-preservation check; the cloud job separately compiles the actual Android app."""
from pathlib import Path
import hashlib
import json
import re
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent.parent
MAIN = ROOT / 'app/src/main'
KOTLIN = MAIN / 'java/com/example'

required = [
    'MainActivity.kt', 'domain/PlannerEngine.kt', 'domain/QuizAttempt.kt',
    'domain/Curriculum.kt', 'domain/ExamPortion.kt', 'domain/StarterLessons.kt', 'data/WorkspaceState.kt',
    'data/db/EvolveDatabase.kt', 'data/db/Daos.kt', 'data/model/Entities.kt',
    'data/repository/EvolveRepository.kt', 'data/repository/BackupCodec.kt',
    'ui/EvolveViewModel.kt', 'ui/CurriculumViewModel.kt',
    'ui/screens/CurriculumScreen.kt', 'ui/screens/ExamEditorScreen.kt', 'ui/screens/TaskFocusScreen.kt',
    'ui/components/GlassDock.kt', 'ui/components/EvolveUi.kt',
    'ui/components/TrendChart.kt', 'ui/sheets/EvolveSheet.kt',
    'notifications/ReminderScheduler.kt', 'ui/theme/Color.kt',
    'ui/theme/Type.kt', 'ui/theme/Theme.kt',
]
for name in required:
    assert (KOTLIN / name).is_file(), f'Missing source: {name}'

# The failed AI Studio rewrite duplicated these same-package symbols.
components = '\n'.join(p.read_text(encoding='utf-8') for p in (KOTLIN / 'ui/components').glob('*.kt'))
for name in ['LocalReduceMotion', 'LocalSheetFeedback']:
    assert len(re.findall(r'^val\s+' + name + r'\b', components, re.M)) == 1, f'Duplicate/missing {name}'
for name in ['Eyebrow', 'EvolveAction', 'EvolveField']:
    assert len(re.findall(r'^fun\s+' + name + r'\s*\(', components, re.M)) == 1, f'Duplicate/missing UI contract: {name}'
for name in ['NoteCaptureSheet.kt', 'TaskEditorSheet.kt', 'AuxiliarySheets.kt']:
    source = (KOTLIN / 'ui/sheets' / name).read_text(encoding='utf-8')
    assert 'import com.example.ui.components.EvolveSheet' not in source, 'Editor no longer uses its native modal'

xml_files = list(MAIN.rglob('*.xml'))
for path in xml_files:
    ET.parse(path)
manifest = ET.parse(MAIN / 'AndroidManifest.xml').getroot()
android = '{http://schemas.android.com/apk/res/android}'
permissions = {p.get(android+'name') for p in manifest.findall('uses-permission')}
assert 'android.permission.RECEIVE_BOOT_COMPLETED' in permissions
receiver = next(r for r in manifest.findall('application/receiver') if r.get(android+'name') == '.notifications.ReminderReceiver')
actions = {a.get(android+'name') for a in receiver.findall('intent-filter/action')}
assert {'android.intent.action.BOOT_COMPLETED', 'android.intent.action.TIME_SET',
        'android.intent.action.TIMEZONE_CHANGED', 'android.intent.action.MY_PACKAGE_REPLACED'} <= actions

catalog = (ROOT / 'gradle/libs.versions.toml').read_text(encoding='utf-8')
wrapper = (ROOT / 'gradle/wrapper/gradle-wrapper.properties').read_text(encoding='utf-8')
assert re.search(r'^agp\s*=\s*"9\.1\.1"', catalog, re.M)
assert 'gradle-9.3.1-bin.zip' in wrapper, 'AGP 9.1.1 requires Gradle 9.3.1'
build = (ROOT / 'app/build.gradle.kts').read_text(encoding='utf-8')
assert 'applicationId = "com.aistudio.evolve.acadsq"' in build
database = (KOTLIN / 'data/db/EvolveDatabase.kt').read_text(encoding='utf-8')
assert 'MIGRATION_1_2' in database and 'MIGRATION_2_3' in database
assert 'MIGRATION_3_4' in database
assert 'fallbackToDestructiveMigration' not in database

pack = json.loads((MAIN / 'assets/semester/s1.json').read_text(encoding='utf-8'))
assert len(pack['courses']) == 7 and sum(len(c['modules']) for c in pack['courses']) == 20
for course in pack['courses']:
    source = course['source']
    document = MAIN / 'assets' / source['asset']
    assert document.is_file() and hashlib.sha256(document.read_bytes()).hexdigest() == source['sha256']
    assert all(1 <= page <= source['pages'] for m in course['modules'] for page in m['pages'])
eee = next(c for c in pack['courses'] if c['id'] == 'a105est04')
assert eee['notebooks'] == ['electrical', 'electronics']
assert all(m['focus'] for m in eee['modules']), 'Keep both components of the provisional 20+20 focus'
print(f'Project contracts passed: canonical UI, native editors, reminder recovery, compatible tool versions, {len(xml_files)} XML files, seven verified PDFs and twenty modules.')
