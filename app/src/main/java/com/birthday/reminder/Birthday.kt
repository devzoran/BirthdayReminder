package com.birthday.reminder

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "birthdays")
data class Birthday(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gregorianDate: String, // 格式: "yyyy-MM-dd"
    val lunarDate: String? = null, // 农历日期格式: "yyyy-MM-dd"
    val isLunar: Boolean = false, // 是否为农历生日
    val reminderDaysBefore: Int = 1, // 提前几天提醒
    val isEnabled: Boolean = true, // 是否启用提醒
    val relationship: String = "", // 关系（父亲、母亲、兄弟等）
    val notes: String = "", // 备注
    val sortOrder: Int = 0, // 排序字段
    val createdAt: Long = System.currentTimeMillis()
) {
    
    fun getAge(currentYear: Int): Int {
        val birthYear = if (isLunar && lunarDate != null) {
            lunarDate.split("-")[0].toInt()
        } else {
            gregorianDate.split("-")[0].toInt()
        }
        return currentYear - birthYear
    }
    
    fun getNextBirthdayDate(currentYear: Int): Date {
        val calendar = Calendar.getInstance()
        
        if (isLunar && lunarDate != null) {
            // 农历生日需要转换为当年的公历日期
            val lunarParts = lunarDate.split("-")
            val lunarMonth = lunarParts[1].toInt()
            val lunarDay = lunarParts[2].toInt()
            
            // 这里需要农历转公历的逻辑
            // 简化处理，实际应该使用专门的农历转换库
            calendar.set(currentYear, lunarMonth - 1, lunarDay)
        } else {
            val gregorianParts = gregorianDate.split("-")
            val month = gregorianParts[1].toInt()
            val day = gregorianParts[2].toInt()
            
            calendar.set(currentYear, month - 1, day)
        }
        
        val today = Calendar.getInstance()
        if (calendar.before(today)) {
            calendar.add(Calendar.YEAR, 1)
        }
        
        return calendar.time
    }
    
    fun getDaysUntilBirthday(): Int {
        val today = Calendar.getInstance()
        val currentYear = today.get(Calendar.YEAR)
        val nextBirthday = getNextBirthdayDate(currentYear)
        
        val diffInMillis = nextBirthday.time - today.timeInMillis
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
    }
    
    fun shouldShowReminder(): Boolean {
        if (!isEnabled) return false
        val daysUntil = getDaysUntilBirthday()
        return daysUntil <= reminderDaysBefore && daysUntil >= 0
    }
}
