package com.birthday.reminder

import android.app.Service
import android.content.Intent
import android.os.IBinder

class BirthdayReminderService : Service() {
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 在后台服务中处理生日提醒逻辑
        return START_STICKY
    }
}
