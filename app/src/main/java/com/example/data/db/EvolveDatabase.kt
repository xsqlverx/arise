package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SubjectEntity::class,
        ClassTipEntity::class,
        StudySessionEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EvolveDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun classTipDao(): ClassTipDao
    abstract fun studySessionDao(): StudySessionDao
    abstract fun userProfileDao(): UserProfileDao

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
                    .addCallback(EvolveDatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class EvolveDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
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

    val initialTips = listOf(
        ClassTipEntity(
            id = "sample-electronics-1",
            subjectId = "electronics",
            kind = "Answer writing",
            text = "Write your answer as clear, separate points. Give each idea its own line.",
            pinned = true,
            date = "2026-10-16",
            isSample = true
        ),
        ClassTipEntity(
            id = "sample-python-1",
            subjectId = "python",
            kind = "Answer writing",
            text = "Show indentation clearly when writing Python by hand. Keep every statement in a block aligned.",
            pinned = true,
            date = "2026-10-16",
            isSample = true
        ),
        ClassTipEntity(
            id = "sample-maths-1",
            subjectId = "maths",
            kind = "Answer writing",
            text = "Show each step of the working, including substitutions. Make the final answer easy to identify.",
            pinned = true,
            date = "2026-10-16",
            isSample = true
        )
    )
    database.classTipDao().insertAll(initialTips)

    val initialProfile = UserProfileEntity(
        id = 1,
        name = "",
        examDate = "2026-11-07",
        collegeStart = "07:45",
        collegeEnd = "14:30",
        gymStart = "20:00",
        gymEnd = "21:00",
        callStart = "22:30",
        remindersEnabled = true,
        reducedMotion = false,
        scheduledSlot = "15:30",
        graphicsBrief = "",
        electricalBrief = "",
        activeDate = 16
    )
    database.userProfileDao().insertOrUpdate(initialProfile)
}
