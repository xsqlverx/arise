package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClassTipEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.UserProfileEntity
import com.example.data.model.AcademicTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    suspend fun getNow(): List<SubjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<SubjectEntity>)

    @Update
    suspend fun update(subject: SubjectEntity)

    @Delete
    suspend fun delete(subject: SubjectEntity)

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun count(): Int
}

@Dao
interface ClassTipDao {
    @Query("SELECT * FROM class_tips WHERE id = :id")
    suspend fun getById(id: String): ClassTipEntity?

    @Query("UPDATE class_tips SET pinned = NOT pinned WHERE id = :id")
    suspend fun togglePin(id: String)
    @Query("SELECT * FROM class_tips ORDER BY pinned DESC, createdAt DESC")
    fun getAllTips(): Flow<List<ClassTipEntity>>
    @Query("SELECT * FROM class_tips ORDER BY pinned DESC, createdAt DESC")
    suspend fun getNow(): List<ClassTipEntity>

    @Query("SELECT * FROM class_tips WHERE subjectId = :subjectId ORDER BY pinned DESC, createdAt DESC")
    fun getTipsBySubject(subjectId: String): Flow<List<ClassTipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tip: ClassTipEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tips: List<ClassTipEntity>)

    @Update
    suspend fun update(tip: ClassTipEntity)

    @Query("DELETE FROM class_tips WHERE id = :tipId")
    suspend fun deleteById(tipId: String)

    @Query("DELETE FROM class_tips WHERE isSample = 1")
    suspend fun deleteSamples()

    @Query("SELECT COUNT(*) FROM class_tips")
    suspend fun count(): Int
}

@Dao
interface StudySessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    suspend fun getNow(): List<StudySessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<StudySessionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOnce(session: StudySessionEntity)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getNow(): UserProfileEntity?
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)
}

@Dao
interface AcademicTaskDao {
    @Query("SELECT * FROM academic_tasks ORDER BY completed ASC, createdAt ASC")
    fun observeAll(): Flow<List<AcademicTaskEntity>>

    @Query("SELECT * FROM academic_tasks ORDER BY createdAt ASC")
    suspend fun getNow(): List<AcademicTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(task: AcademicTaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAll(tasks: List<AcademicTaskEntity>)

    @Query("UPDATE academic_tasks SET completed = :completed WHERE id = :id")
    suspend fun setCompleted(id: String, completed: Boolean)
}

@Dao
interface CurriculumDao {
    @Query("SELECT * FROM semester WHERE id = 1")
    fun observeSemester(): Flow<com.example.data.model.SemesterEntity?>
    @Query("SELECT * FROM semester WHERE id = 1")
    suspend fun semesterNow(): com.example.data.model.SemesterEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSemester(row: com.example.data.model.SemesterEntity)
    @Query("SELECT * FROM curriculum_attempts ORDER BY timestamp DESC")
    fun observeAttempts(): Flow<List<com.example.data.model.CurriculumAttemptEntity>>
    @Query("SELECT * FROM curriculum_attempts ORDER BY timestamp DESC")
    suspend fun attemptsNow(): List<com.example.data.model.CurriculumAttemptEntity>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAttempt(row: com.example.data.model.CurriculumAttemptEntity)
}
