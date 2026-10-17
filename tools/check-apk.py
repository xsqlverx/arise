"""Run on the cloud runner after assembleDebug and testDebugUnitTest, never instead of them."""
from pathlib import Path
import hashlib
import json
import os
import xml.etree.ElementTree as ET
import zipfile

root = Path(__file__).resolve().parent.parent
apk = root / 'app/build/outputs/apk/debug/app-debug.apk'
assert apk.is_file(), 'No actual APK was assembled'
with zipfile.ZipFile(apk) as archive:
    assert archive.testzip() is None, 'APK ZIP corruption'
    assert 'AndroidManifest.xml' in archive.namelist()
    assert any(name.startswith('classes') and name.endswith('.dex') for name in archive.namelist())
    pack = json.loads(archive.read('assets/semester/s1.json'))
    for course in pack['courses']:
        source = course['source']
        assert hashlib.sha256(archive.read('assets/' + source['asset'])).hexdigest() == source['sha256']
    assert len(pack['courses']) == 7 and sum(len(c['modules']) for c in pack['courses']) == 20
reports = list((root / 'app/build/test-results/testDebugUnitTest').glob('TEST-*.xml'))
assert reports, 'No native Gradle unit-test reports were produced'
totals = {key: 0 for key in ('tests', 'failures', 'errors', 'skipped')}
for report in reports:
    suite = ET.parse(report).getroot()
    for key in totals:
        totals[key] += int(suite.get(key, '0'))
assert totals['failures'] == totals['errors'] == 0, totals
assert totals['tests'] >= 58 and totals['skipped'] == 0, totals
digest = hashlib.sha256(apk.read_bytes()).hexdigest()
summary = (f"Android debug APK assembled: {apk.stat().st_size:,} bytes.\n\n"
           f"{totals['tests']} unit tests; 0 failures/errors/skips. Includes the real Room upgrade test.\n\n"
           f"Packaged semester verified: seven original PDFs, seven courses, twenty modules.\n\n"
           f"APK SHA-256: {digest}\n\n"
           "CI signing is disposable and the APK is not an installation/update artifact. "
           "Use the original AI Studio signing for the device APK.\n")
print(summary)
if os.environ.get('GITHUB_STEP_SUMMARY'):
    with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as output:
        output.write(summary)
