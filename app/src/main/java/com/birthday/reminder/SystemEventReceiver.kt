package com.birthday.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SystemEventReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // 设备重启后重新安排所有提醒
                CoroutineScope(Dispatchers.IO).launch {
                    BirthdayReminderManager.scheduleAllReminders()
                }
            }
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // 应用更新后重新安排提醒
                if (intent.dataString?.contains(context.packageName) == true) {
                    CoroutineScope(Dispatchers.IO).launch {
                        BirthdayReminderManager.scheduleAllReminders()
                    }
                }
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> {
                // 时间或时区改变后重新安排提醒
                CoroutineScope(Dispatchers.IO).launch {
                    BirthdayReminderManager.scheduleAllReminders()
                }
            }
            "android.intent.action.QUICKBOOT_POWERON" -> {
                // 小米等设备的快速启动
                CoroutineScope(Dispatchers.IO).launch {
                    BirthdayReminderManager.scheduleAllReminders()
                }
            }
        }
    }
    
    companion object {
        fun registerReceiver(context: Context) {
            val receiver = SystemEventReceiver()
            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_BOOT_COMPLETED)
                addAction(Intent.ACTION_MY_PACKAGE_REPLACED)
                addAction(Intent.ACTION_PACKAGE_REPLACED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
                addAction("android.intent.action.QUICKBOOT_POWERON")
                addDataScheme("package")
            }
            
            try {
                context.registerReceiver(receiver, filter)
            } catch (e: Exception) {
                // 某些情况下可能注册失败，但不影响其他功能
                e.printStackTrace()
            }
        }
    }
}
