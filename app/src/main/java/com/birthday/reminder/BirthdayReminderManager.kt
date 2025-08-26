package com.birthday.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object BirthdayReminderManager {
    
    fun scheduleReminder(birthday: Birthday) {
        val workManager = WorkManager.getInstance(App.instance)
        
        // 取消之前的提醒
        cancelReminder(birthday)
        
        if (!birthday.isEnabled) return
        
        val nextBirthday = birthday.getNextBirthdayDate(Calendar.getInstance().get(Calendar.YEAR))
        val reminderTime = Calendar.getInstance().apply {
            time = nextBirthday
            add(Calendar.DAY_OF_YEAR, -birthday.reminderDaysBefore)
            set(Calendar.HOUR_OF_DAY, 9) // 上午9点提醒
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }
        
        val now = Calendar.getInstance()
        if (reminderTime.before(now)) {
            // 如果提醒时间已过，设置为明年
            reminderTime.add(Calendar.YEAR, 1)
        }
        
        val delay = reminderTime.timeInMillis - now.timeInMillis
        
        // 设置约束条件，确保在设备空闲时也能工作
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<BirthdayReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .setInputData(
                Data.Builder()
                    .putLong("birthday_id", birthday.id)
                    .putString("birthday_name", birthday.name)
                    .putString("birthday_relationship", birthday.relationship)
                    .putInt("days_before", birthday.reminderDaysBefore)
                    .build()
            )
            .addTag("birthday_reminder_${birthday.id}")
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                15000L, // 15秒最小退避时间
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueue(workRequest)
        
        // 同时使用AlarmManager作为备份机制（适用于API 23+的精确闹钟）
        scheduleAlarmManagerBackup(birthday, reminderTime)
    }
    
    private fun scheduleAlarmManagerBackup(birthday: Birthday, reminderTime: Calendar) {
        val context = App.instance
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, BirthdayReminderReceiver::class.java).apply {
            putExtra("birthday_id", birthday.id)
            putExtra("birthday_name", birthday.name)
            putExtra("birthday_relationship", birthday.relationship)
            putExtra("days_before", birthday.reminderDaysBefore)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            birthday.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // 使用精确闹钟
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0+ 使用 setExactAndAllowWhileIdle 确保在Doze模式下也能触发
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                reminderTime.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                reminderTime.timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun cancelReminder(birthday: Birthday) {
        val workManager = WorkManager.getInstance(App.instance)
        workManager.cancelAllWorkByTag("birthday_reminder_${birthday.id}")
        
        // 同时取消AlarmManager的备份
        cancelAlarmManagerBackup(birthday)
    }
    
    private fun cancelAlarmManagerBackup(birthday: Birthday) {
        val context = App.instance
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        
        val intent = Intent(context, BirthdayReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            birthday.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun scheduleAllReminders() {
        // 在应用启动时调用，重新安排所有提醒
        val workManager = WorkManager.getInstance(App.instance)
        
        // 设置约束，确保即使在低电量情况下也能执行
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<ScheduleAllRemindersWorker>()
            .setConstraints(constraints)
            .build()
        
        workManager.enqueue(workRequest)
    }
    
    fun schedulePeriodicCheck() {
        // 每天检查一次是否需要重新安排提醒
        val workManager = WorkManager.getInstance(App.instance)
        
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<DailyCheckWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .addTag("daily_reminder_check")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "daily_reminder_check",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}

class BirthdayReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val birthdayId = inputData.getLong("birthday_id", 0)
        val birthdayName = inputData.getString("birthday_name") ?: ""
        val relationship = inputData.getString("birthday_relationship") ?: ""
        val daysBefore = inputData.getInt("days_before", 1)
        
        // 显示通知
        NotificationHelper.showBirthdayReminder(
            applicationContext,
            birthdayName,
            relationship,
            daysBefore
        )
        
        // 重新安排明年的提醒
        val database = BirthdayDatabase.getDatabase(applicationContext)
        val birthday = database.birthdayDao().getBirthdayById(birthdayId)
        birthday?.let {
            BirthdayReminderManager.scheduleReminder(it)
        }
        
        return Result.success()
    }
}

class ScheduleAllRemindersWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val database = BirthdayDatabase.getDatabase(applicationContext)
        val repository = BirthdayRepository(database.birthdayDao())
        val birthdays = repository.getEnabledBirthdaysSync()
        
        birthdays.forEach { birthday ->
            BirthdayReminderManager.scheduleReminder(birthday)
        }
        
        return Result.success()
    }
}

class DailyCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        // 每天检查是否有需要重新安排的提醒
        val database = BirthdayDatabase.getDatabase(applicationContext)
        val repository = BirthdayRepository(database.birthdayDao())
        val birthdays = repository.getEnabledBirthdaysSync()
        
        // 检查每个生日的提醒是否还有效
        birthdays.forEach { birthday ->
            val nextBirthday = birthday.getNextBirthdayDate(Calendar.getInstance().get(Calendar.YEAR))
            val reminderTime = Calendar.getInstance().apply {
                time = nextBirthday
                add(Calendar.DAY_OF_YEAR, -birthday.reminderDaysBefore)
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            
            val now = Calendar.getInstance()
            if (reminderTime.before(now)) {
                // 如果提醒时间已过，重新安排
                BirthdayReminderManager.scheduleReminder(birthday)
            }
        }
        
        return Result.success()
    }
}
