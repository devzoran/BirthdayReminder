package com.birthday.reminder

import android.content.Context
import androidx.work.*
import java.util.*
import java.util.concurrent.TimeUnit

object NotificationTestHelper {
    
    /**
     * 立即测试通知显示
     */
    fun testImmediateNotification(context: Context) {
        NotificationHelper.showBirthdayReminder(
            context,
            "张三",
            "朋友", 
            1 // 明天生日
        )
    }
    
    /**
     * 测试今天生日通知
     */
    fun testTodayBirthdayNotification(context: Context) {
        NotificationHelper.showBirthdayReminder(
            context,
            "李四",
            "同事",
            0 // 今天生日
        )
    }
    
    /**
     * 测试一周前通知
     */
    fun testWeekBeforeNotification(context: Context) {
        NotificationHelper.showBirthdayReminder(
            context,
            "王五",
            "家人",
            7 // 7天后生日
        )
    }
    
    /**
     * 测试10秒后的延迟通知
     */
    fun testDelayedNotification(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<TestNotificationWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .setInputData(
                Data.Builder()
                    .putString("test_name", "测试延迟通知")
                    .putString("test_relationship", "测试")
                    .putInt("test_days_before", 3)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    /**
     * 测试所有通知功能的综合测试
     */
    fun runFullNotificationTest(context: Context): String {
        val results = mutableListOf<String>()
        
        try {
            // 1. 测试立即通知
            testImmediateNotification(context)
            results.add("✅ 立即通知测试")
            
            // 2. 测试今天生日通知
            testTodayBirthdayNotification(context)
            results.add("✅ 今天生日通知测试")
            
            // 3. 测试一周前通知
            testWeekBeforeNotification(context)
            results.add("✅ 一周前通知测试")
            
            // 4. 测试延迟通知
            testDelayedNotification(context)
            results.add("✅ 延迟通知测试（10秒后）")
            
            // 5. 检查通知渠道
            if (NotificationHelper.isNotificationChannelEnabled(context)) {
                results.add("✅ 通知渠道已启用")
            } else {
                results.add("❌ 通知渠道未启用")
            }
            
        } catch (e: Exception) {
            results.add("❌ 测试过程中出现错误: ${e.message}")
        }
        
        return results.joinToString("\n")
    }
}

/**
 * 用于测试延迟通知的Worker
 */
class TestNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val name = inputData.getString("test_name") ?: "测试用户"
        val relationship = inputData.getString("test_relationship") ?: ""
        val daysBefore = inputData.getInt("test_days_before", 1)
        
        NotificationHelper.showBirthdayReminder(
            applicationContext,
            name,
            relationship,
            daysBefore
        )
        
        return Result.success()
    }
}
