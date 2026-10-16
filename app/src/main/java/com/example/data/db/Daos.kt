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
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY orderIndex ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

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
    @Query("SELECT * FROM class_tips ORDER BY pinned DESC, createdAt DESC")
    fun getAllTips(): Flow<List<ClassTipEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: StudySessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<StudySessionEntity>)

    @Query("DELETE FROM study_sessions")
    suspend fun clearAll()
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: UserProfileEntity)
}
