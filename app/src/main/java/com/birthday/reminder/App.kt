package com.birthday.reminder

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.Constraints
import androidx.work.NetworkType

class App : Application() {
    
    companion object {
        lateinit var instance: App
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // 创建通知渠道
        NotificationHelper.createNotificationChannel(this)
        
        // 安排所有提醒
        BirthdayReminderManager.scheduleAllReminders()
        
        // 启动每日检查
        BirthdayReminderManager.schedulePeriodicCheck()
        
        // 注册系统事件监听器
        SystemEventReceiver.registerReceiver(this)
    }
}
