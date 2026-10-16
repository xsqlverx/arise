package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import com.example.data.model.AcademicTaskEntity
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope

@Database(
    entities = [
        SubjectEntity::class,
        ClassTipEntity::class,
        StudySessionEntity::class,
        UserProfileEntity::class,
        AcademicTaskEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class EvolveDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun classTipDao(): ClassTipDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun taskDao(): AcademicTaskDao

    companion object {
        @Volatile
        private var INSTANCE: EvolveDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): EvolveDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EvolveDatabase::class.java,
                    "evolve_academic.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // Existing installations retain their notes/profile/results. Never erase a database on upgrade.
    private object MIGRATION_1_2 : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE study_sessions ADD COLUMN evidenceType TEXT NOT NULL DEFAULT 'legacy'")
            db.execSQL("ALTER TABLE user_profile ADD COLUMN studyStart TEXT NOT NULL DEFAULT '16:00'")
            db.execSQL("ALTER TABLE user_profile ADD COLUMN studyEnd TEXT NOT NULL DEFAULT '22:00'")
            db.execSQL("ALTER TABLE user_profile ADD COLUMN graphicsDueDate TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE user_profile ADD COLUMN electricalDueDate TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE user_profile ADD COLUMN scheduledDate TEXT NOT NULL DEFAULT ''")
            db.execSQL("CREATE TABLE IF NOT EXISTS academic_tasks (id TEXT NOT NULL PRIMARY KEY, subjectId TEXT NOT NULL, title TEXT NOT NULL, instructions TEXT NOT NULL, dueDate TEXT NOT NULL, durationMinutes INTEGER NOT NULL, completed INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
        }
    }

}

suspend fun populateInitialData(database: EvolveDatabase) {
    val initialSubjects = listOf(
        SubjectEntity("electrical", "Electrical", "Foundation course", "electrical", 0),
        SubjectEntity("electronics", "Electronics", "Foundation course", "electrical", 1),
        SubjectEntity("python", "Algorithmic thinking", "With Python", "code", 2),
        SubjectEntity("graphics", "Engineering graphics", "Drawings & construction", "graphics", 3),
        SubjectEntity("chemistry", "Chemistry", "Confirm full course title", "chemistry", 4),
        SubjectEntity("maths", "Mathematics", "Steps, methods & practice", "math", 5),
        SubjectEntity("adp", "8 DP", "Course name to confirm", "notes", 6),
        SubjectEntity("life", "Life skills", "Foundation course", "spark", 7)
    )
    database.subjectDao().insertAll(initialSubjects)

    val initialProfile = UserProfileEntity(
        id = 1,
        name = "",
        examDate = "",
        collegeStart = "07:45",
        collegeEnd = "14:30",
        gymStart = "20:00",
        gymEnd = "21:00",
        callStart = "22:30",
        remindersEnabled = false,
        reducedMotion = false,
        scheduledSlot = "15:30",
        graphicsBrief = "",
        electricalBrief = "",
        activeDate = 16
    )
    database.userProfileDao().insertOrUpdate(initialProfile)
}
