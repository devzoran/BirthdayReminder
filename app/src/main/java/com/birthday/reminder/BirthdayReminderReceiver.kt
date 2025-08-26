package com.birthday.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BirthdayReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 设备重启后重新安排提醒
                BirthdayReminderManager.scheduleAllReminders()
            }
            
            "BIRTHDAY_REMINDER" -> {
                val name = intent.getStringExtra("name") ?: ""
                val relationship = intent.getStringExtra("relationship") ?: ""
                val daysBefore = intent.getIntExtra("days_before", 1)
                
                NotificationHelper.showBirthdayReminder(
                    context,
                    name,
                    relationship,
                    daysBefore
                )
            }
        }
    }
}
