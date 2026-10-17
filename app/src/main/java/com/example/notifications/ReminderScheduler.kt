package com.example.notifications

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.WorkspaceState
import com.example.data.db.EvolveDatabase
import com.example.data.model.UserProfileEntity
import com.example.domain.NextAction
import com.example.domain.PlannerEngine
import kotlinx.coroutines.*
import java.time.*

/** Two ordinary inexact alarms, replaced as one plan changes. No exact-alarm permission. */
object ReminderScheduler {
    private const val CHANNEL = "study_blocks"
    private const val NOTICE = 410
    private fun prefs(c: Context) = c.getSharedPreferences("evolve_reminders_v2", Context.MODE_PRIVATE)
    private fun pending(c: Context, stage: Int, token: String): PendingIntent = PendingIntent.getBroadcast(c, 410 + stage,
        Intent(c, ReminderReceiver::class.java).setAction("evolve.remind").putExtra("token", token).putExtra("stage", stage),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    @Synchronized
    fun sync(c: Context, profile: UserProfileEntity, action: NextAction, force: Boolean = false) {
        val settings = prefs(c)
        val manager = c.getSystemService(AlarmManager::class.java)
        val allowed = profile.remindersEnabled && NotificationManagerCompat.from(c).areNotificationsEnabled()
        val slot = if (allowed && action.actionType != "add-task" && profile.scheduledDate.isNotBlank())
            PlannerEngine.plannedSlot(profile, action)?.takeIf { it.key == "${profile.scheduledDate}|${profile.scheduledSlot}" } else null
        val snooze = settings.getLong("snoozeUntil", 0).takeIf { settings.getString("action", "") == action.id }
        val start = if (snooze != null && snooze > System.currentTimeMillis()) Instant.ofEpochMilli(snooze).atZone(ZoneId.systemDefault()).toLocalDateTime()
            else slot?.let { LocalDate.parse(it.date).atTime(LocalTime.parse(it.time24)) }
        val token = start?.let { "${action.id}|$it|${action.minutes}|${profile.studyStart}|${profile.studyEnd}|${profile.collegeStart}|${profile.collegeEnd}|${profile.gymStart}|${profile.gymEnd}|${profile.callStart}" }
        if (!force && settings.getString("token", null) == token) return
        manager.cancel(pending(c, 0, "")); manager.cancel(pending(c, 1, ""))
        NotificationManagerCompat.from(c).cancel(NOTICE)
        settings.edit().remove("token").apply()
        if (!allowed || token == null || start == null) return
        val millis = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        settings.edit().putString("token", token).putString("action", action.id).putLong("start", millis).putInt("delivered", -1).apply()
        manager.setWindow(AlarmManager.RTC_WAKEUP, millis, 10 * 60_000L, pending(c, 0, token))
        manager.setWindow(AlarmManager.RTC_WAKEUP, millis + 15 * 60_000L, 10 * 60_000L, pending(c, 1, token))
    }

    fun deliver(c: Context, profile: UserProfileEntity, action: NextAction, intent: Intent) {
        val settings = prefs(c)
        val token = intent.getStringExtra("token") ?: return
        if (!profile.remindersEnabled || token != settings.getString("token", null) || action.id != settings.getString("action", null)) return
        val now = LocalDateTime.now()
        if (intent.action == "evolve.snooze") {
            val next = PlannerEngine.getAvailableSlots(profile, now.plusMinutes(15), action.minutes, action.dueDate).firstOrNull()
            NotificationManagerCompat.from(c).cancel(NOTICE)
            val alarms = c.getSystemService(AlarmManager::class.java)
            alarms.cancel(pending(c, 0, "")); alarms.cancel(pending(c, 1, ""))
            if (next != null) {
                val start = LocalDate.parse(next.date).atTime(LocalTime.parse(next.time24))
                settings.edit().putLong("snoozeUntil", start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()).apply()
                sync(c, profile, action, true)
            } else settings.edit().remove("token").apply()
            return
        }
        val stage = intent.getIntExtra("stage", 0)
        if (stage <= settings.getInt("delivered", -1)) return
        // Late alarms cannot flood the user or interrupt protected time/focus.
        if (System.currentTimeMillis() - settings.getLong("start", 0) > 45 * 60_000L
            || PlannerEngine.isProtected(profile, now.toLocalTime(), action.minutes)
            || now.toLocalTime() < (PlannerEngine.time(profile.studyStart) ?: LocalTime.MAX)
            || now.plusMinutes(action.minutes.toLong()).toLocalTime() > (PlannerEngine.time(profile.studyEnd) ?: LocalTime.MIN)
            || WorkspaceState(c).focusActive()) return
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        if (!NotificationManagerCompat.from(c).areNotificationsEnabled()) return
        if (Build.VERSION.SDK_INT >= 26) c.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(NotificationChannel(CHANNEL, "Study blocks", NotificationManager.IMPORTANCE_DEFAULT))
        fun launch(value: String, code: Int) = PendingIntent.getActivity(c, code,
            Intent(c, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("evolve_action", value),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val open = launch(if (action.actionType in listOf("task", "curriculum", "exam-practice")) "${action.actionType}:${action.id}" else action.actionType, 420)
        val busy = PendingIntent.getBroadcast(c, 422, Intent(c, ReminderReceiver::class.java)
            .setAction("evolve.snooze").putExtra("token", token), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(c, CHANNEL).setSmallIcon(R.drawable.ic_stat_evolve)
            .setContentTitle(action.title.replace("\n", " ")).setContentText(action.firstStep)
            .setStyle(NotificationCompat.BigTextStyle().bigText(action.firstStep))
            .setContentIntent(open).setAutoCancel(true).setOnlyAlertOnce(false)
            .addAction(0, "Start", open).addAction(0, "Another time", launch("reschedule", 421))
            .addAction(0, "Busy · 15 min", busy).build()
        NotificationManagerCompat.from(c).notify(NOTICE, notification)
        settings.edit().putInt("delivered", stage).apply()
    }
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val result = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val db = EvolveDatabase.getDatabase(context, this)
                val profile = db.userProfileDao().getNow() ?: return@launch
                val action = com.example.domain.AcademicPlanner.next(profile, db.taskDao().getNow(),
                    db.curriculumDao().semesterNow()?.let(com.example.domain.CurriculumCodec::accepted), db.curriculumDao().attemptsNow())
                if (intent.action == "evolve.remind" || intent.action == "evolve.snooze") ReminderScheduler.deliver(context, profile, action, intent)
                else ReminderScheduler.sync(context, profile, action, true)
            } catch (_: Exception) { /* A denied/revoked permission must not crash a background receiver. */ }
            finally { result.finish() }
        }
    }
}
