package com.example.data.model

import androidx.room.Entity
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
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val examDate: String = "2026-11-07",
    val collegeStart: String = "07:45",
    val collegeEnd: String = "14:30",
    val gymStart: String = "20:00",
    val gymEnd: String = "21:00",
    val callStart: String = "22:30",
    val remindersEnabled: Boolean = true,
    val reducedMotion: Boolean = false,
    val scheduledSlot: String = "15:30",
    val graphicsBrief: String = "",
    val electricalBrief: String = "",
    val activeDate: Int = 16
)
