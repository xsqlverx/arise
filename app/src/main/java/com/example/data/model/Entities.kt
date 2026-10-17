package com.example.data.model

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "subjects")
data class SubjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val subtitle: String,
    val icon: String,
    val orderIndex: Int
)

@Entity(tableName = "class_tips")
data class ClassTipEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val kind: String,
    val text: String,
    val pinned: Boolean,
    val date: String,
    val isSample: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val score: Int,
    val total: Int,
    val date: String,
    val isSample: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "'legacy'") val evidenceType: String = "legacy"
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val examDate: String = "",
    val collegeStart: String = "07:45",
    val collegeEnd: String = "14:30",
    val gymStart: String = "20:00",
    val gymEnd: String = "21:00",
    val callStart: String = "22:30",
    val remindersEnabled: Boolean = false,
    val reducedMotion: Boolean = false,
    val scheduledSlot: String = "15:30",
    val graphicsBrief: String = "",
    val electricalBrief: String = "",
    val activeDate: Int = 16,
    @ColumnInfo(defaultValue = "'16:00'") val studyStart: String = "16:00",
    @ColumnInfo(defaultValue = "'22:00'") val studyEnd: String = "22:00",
    @ColumnInfo(defaultValue = "''") val graphicsDueDate: String = "",
    @ColumnInfo(defaultValue = "''") val electricalDueDate: String = "",
    @ColumnInfo(defaultValue = "''") val scheduledDate: String = ""
)

@Entity(tableName = "academic_tasks")
data class AcademicTaskEntity(
    @PrimaryKey val id: String,
    val subjectId: String,
    val title: String,
    val instructions: String,
    val dueDate: String = "",
    val durationMinutes: Int = 25,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

/** One accepted, versioned semester map. Personal notes remain separate. */
@Entity(tableName = "semester")
data class SemesterEntity(
    @PrimaryKey val id: Int = 1,
    val packJson: String,
    val focusJson: String,
    val electricalMarks: Int = 20,
    val electronicsMarks: Int = 20
)

/** A small authored checkpoint, never a claim that a whole module is mastered. */
@Entity(tableName = "curriculum_attempts")
data class CurriculumAttemptEntity(
    @PrimaryKey val id: String,
    val moduleId: String,
    val answersJson: String,
    val recall: String,
    val score: Int,
    val total: Int,
    val timestamp: Long,
    val reviewDate: String
)
