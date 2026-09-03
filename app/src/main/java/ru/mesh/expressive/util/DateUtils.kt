package ru.mesh.expressive.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    /**
     * Преобразует дату в относительный формат:
     * 0 дней -> "Сегодня, 3 сентября"
     * 1 день -> "Вчера, 2 сентября"
     * 2 дня -> "Позавчера, 1 сентября"
     * > 2 дней -> "31 августа 2026"
     */
    fun formatRelativeDate(isoDateOrDateStr: String?): String {
        if (isoDateOrDateStr.isNullOrBlank()) return ""
        return try {
            val dateOnly = isoDateOrDateStr.substringBefore("T").substringBefore(" ").trim()
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateOnly) ?: return isoDateOrDateStr

            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetCal = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = todayCal.timeInMillis - targetCal.timeInMillis
            val diffDays = TimeUnit.MILLISECONDS.toDays(diffMillis)

            val dayMonthFormat = SimpleDateFormat("d MMMM", Locale("ru"))
            val fullFormat = SimpleDateFormat("d MMMM yyyy", Locale("ru"))

            when (diffDays) {
                0L -> "Сегодня, ${dayMonthFormat.format(date)}"
                1L -> "Вчера, ${dayMonthFormat.format(date)}"
                2L -> "Позавчера, ${dayMonthFormat.format(date)}"
                -1L -> "Завтра, ${dayMonthFormat.format(date)}"
                -2L -> "Послезавтра, ${dayMonthFormat.format(date)}"
                else -> fullFormat.format(date)
            }
        } catch (_: Exception) {
            isoDateOrDateStr
        }
    }

    /**
     * Преобразует полную дату со временем в относительный формат с секундами:
     * Например: "Позавчера, 1 сентября в 10:19:00"
     */
    fun formatRelativeDateTime(isoDateTimeStr: String?): String {
        if (isoDateTimeStr.isNullOrBlank()) return ""
        return try {
            val clean = isoDateTimeStr.substringBefore("+").substringBefore("Z").replace(" ", "T")
            val parts = clean.split("T")
            val datePart = parts[0]
            val timePart = if (parts.size > 1) parts[1] else ""

            val relativeDate = formatRelativeDate(datePart)
            if (timePart.isNotBlank()) {
                val timeSub = timePart.split(":")
                val h = timeSub.getOrNull(0) ?: "00"
                val m = timeSub.getOrNull(1) ?: "00"
                val s = timeSub.getOrNull(2) ?: "00"
                "$relativeDate в $h:$m:$s"
            } else {
                relativeDate
            }
        } catch (_: Exception) {
            isoDateTimeStr
        }
    }
}
