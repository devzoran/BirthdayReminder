package com.birthday.reminder

import java.util.*

/**
 * 简单的农历转换工具类
 * 注意：这是一个简化版本，实际应用中建议使用专业的农历库
 */
object LunarCalendarUtil {
    
    // 农历月份名称
    private val lunarMonths = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )
    
    // 农历日期名称
    private val lunarDays = arrayOf(
        "", "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )
    
    /**
     * 将公历日期转换为农历日期字符串
     * 注意：这是一个简化实现，实际应用中需要更精确的转换算法
     */
    fun solarToLunar(solarDate: String): String {
        // 简化处理：直接解析输入日期并返回格式化的农历日期
        // 实际应用中应该使用专业的农历转换算法
        
        val parts = solarDate.split("-")
        if (parts.size != 3) return solarDate
        
        val year = parts[0].toIntOrNull() ?: return solarDate
        val month = parts[1].toIntOrNull() ?: return solarDate
        val day = parts[2].toIntOrNull() ?: return solarDate
        
        // 简化的转换逻辑（仅作示例）
        // 实际应用中需要复杂的天干地支和农历计算
        val lunarMonth = if (month > 1) month - 1 else 12
        val lunarDay = if (day > 15) day - 15 else day + 15
        
        val monthName = if (lunarMonth in 1..12) lunarMonths[lunarMonth - 1] else "正月"
        val dayName = if (lunarDay in 1..30) lunarDays[lunarDay] else "初一"
        
        return "${year}年${monthName}${dayName}"
    }
    
    /**
     * 将农历日期转换为公历日期字符串
     * 注意：这是一个简化实现
     */
    fun lunarToSolar(lunarDate: String): String {
        // 简化处理，实际应用中需要复杂的转换算法
        return lunarDate // 临时返回原值
    }
    
    /**
     * 获取农历日期的显示名称
     */
    fun getLunarDisplayName(month: Int, day: Int): String {
        val monthName = if (month in 1..12) lunarMonths[month - 1] else "正月"
        val dayName = if (day in 1..30) lunarDays[day] else "初一"
        return "${monthName}${dayName}"
    }
    
    /**
     * 检查是否为有效的农历日期
     */
    fun isValidLunarDate(month: Int, day: Int): Boolean {
        return month in 1..12 && day in 1..30
    }
}
