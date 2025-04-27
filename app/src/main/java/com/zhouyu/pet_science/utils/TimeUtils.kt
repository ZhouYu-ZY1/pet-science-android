package com.zhouyu.pet_science.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeUtils {
    /**
     * 将时间戳转换为友好显示的时间字符串。
     */
    @JvmStatic
    fun getMessageTime(timestamp: Long?): String {
        if (timestamp == null) {
            return "未知时间"
        }
        val now = System.currentTimeMillis()
        val diff = now - timestamp
//        if (diff < 0) {
//            return "未知时间";
//        }
        val diffSeconds = diff / 1000
        val diffMinutes = diff / (60 * 1000)
        val diffHours = diff / (60 * 60 * 1000)
        val diffDays = TimeUnit.MILLISECONDS.toDays(diff)
        return if (diffDays > 0) {
            diffDays.toString() + "天前"
        } else if (diffHours > 0) {
            val remainingMinutes = diffMinutes % 60
            diffHours.toString() + "小时" + remainingMinutes + "分钟前"
        } else if (diffMinutes > 0) {
            diffMinutes.toString() + "分钟前"
        } else if (diffSeconds > 0) {
            diffSeconds.toString() + "秒前"
        } else {
            "刚刚"
        }
    }

    fun getNotificationTime(time: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(time)
    }

    /**
     * 将时间戳转换为年龄。
     *
     * @param timestamp 时间戳
     * @return 年龄
     */
    fun calculateAge(timestamp: Long): Int {
        // 创建 Calendar 实例并设置为出生日期
        val birthDate = Calendar.getInstance()
        birthDate.timeInMillis = timestamp

        // 创建 Calendar 实例并设置为当前日期
        val currentDate = Calendar.getInstance()
        var age = currentDate[Calendar.YEAR] - birthDate[Calendar.YEAR]
        // 如果生日还没到，则减一岁
        if (birthDate[Calendar.DAY_OF_YEAR] > currentDate[Calendar.DAY_OF_YEAR]) {
            age--
        }
        return age
    }
}
