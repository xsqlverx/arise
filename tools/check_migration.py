"""Exercise the migration SQL against SQLite. This does not run Room's schema validator."""
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parent.parent
source = (root / 'app/src/main/java/com/example/data/db/EvolveDatabase.kt').read_text(encoding='utf-8')
migrations = {}
for start,end,body in re.findall(r'private object MIGRATION_(\d)_(\d).*?override fun migrate\(db: SupportSQLiteDatabase\) \{(.*?)\n        \}', source, re.S):
    migrations[(int(start),int(end))] = re.findall(r'db\.execSQL\("([^"\n]+)"\)', body)
assert set(migrations) == {(1,2),(2,3),(3,4)}
assert len(migrations[(1,2)]) == 7 and len(migrations[(2,3)]) == 2 and len(migrations[(3,4)]) == 1
with sqlite3.connect(':memory:') as db:
    db.executescript('''
        CREATE TABLE subjects (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL);
        CREATE TABLE class_tips (id TEXT NOT NULL PRIMARY KEY, text TEXT NOT NULL);
        CREATE TABLE study_sessions (id TEXT NOT NULL PRIMARY KEY, score INTEGER NOT NULL, total INTEGER NOT NULL);
        CREATE TABLE user_profile (id INTEGER NOT NULL PRIMARY KEY, name TEXT NOT NULL);
    ''')
    exact = '    if ready:\n        print("✓")\n\n'
    db.execute('INSERT INTO class_tips VALUES (?, ?)', ('tip', exact))
    db.execute('INSERT INTO study_sessions VALUES (?, ?, ?)', ('attempt', 2, 3))
    db.execute('INSERT INTO user_profile VALUES (?, ?)', (1, 'Keep me'))
    for statement in migrations[(1,2)]:
        db.execute(statement)
    # A v2 installation already contains real tasks before its upgrade to v3.
    db.execute('INSERT INTO academic_tasks VALUES (?,?,?,?,?,?,?,?)',('task','python','Keep task','Exact first step','',25,0,123))
    for statement in migrations[(2,3)]:
        db.execute(statement)
    db.execute('INSERT INTO semester VALUES (?,?,?,?,?)',(1,'original pack','["existing-module"]',20,20))
    for statement in migrations[(3,4)]:
        db.execute(statement)
    assert db.execute('SELECT packJson,focusJson,examJson,electricalMarks,electronicsMarks FROM semester').fetchone() == ('original pack','["existing-module"]','',20,20)
    assert db.execute('SELECT title,instructions FROM academic_tasks').fetchone() == ('Keep task','Exact first step')
    assert db.execute('SELECT text FROM class_tips').fetchone()[0] == exact
    assert db.execute('SELECT score, total, evidenceType FROM study_sessions').fetchone() == (2, 3, 'legacy')
    assert db.execute('SELECT name, studyStart, studyEnd, scheduledDate FROM user_profile').fetchone() == ('Keep me', '16:00', '22:00', '')
    assert [r[1] for r in db.execute('PRAGMA table_info(academic_tasks)')] == [
        'id', 'subjectId', 'title', 'instructions', 'dueDate', 'durationMinutes', 'completed', 'createdAt']
    assert [r[1] for r in db.execute('PRAGMA table_info(semester)')] == ['id','packJson','focusJson','electricalMarks','electronicsMarks','examJson']
    assert len(list(db.execute('PRAGMA table_info(curriculum_attempts)'))) == 8
print('Migration SQL passed through v4: note text, scores, profile, tasks and existing semester scope preserved; exam portion starts unset.')
