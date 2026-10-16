"""Exercise the migration SQL against SQLite. This does not run Room's schema validator."""
import re
import sqlite3
from pathlib import Path

root = Path(__file__).resolve().parent.parent
source = (root / 'app/src/main/java/com/example/data/db/EvolveDatabase.kt').read_text(encoding='utf-8')
sql = re.findall(r'db\.execSQL\("([^"\n]+)"\)', source)
assert len(sql) == 7, f'Unexpected migration statements: {len(sql)}'
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
    for statement in sql:
        db.execute(statement)
    assert db.execute('SELECT text FROM class_tips').fetchone()[0] == exact
    assert db.execute('SELECT score, total, evidenceType FROM study_sessions').fetchone() == (2, 3, 'legacy')
    assert db.execute('SELECT name, studyStart, studyEnd, scheduledDate FROM user_profile').fetchone() == ('Keep me', '16:00', '22:00', '')
    assert [r[1] for r in db.execute('PRAGMA table_info(academic_tasks)')] == [
        'id', 'subjectId', 'title', 'instructions', 'dueDate', 'durationMinutes', 'completed', 'createdAt']
print('Migration SQL passed: existing note text, scores and profile preserved; task schema created.')
