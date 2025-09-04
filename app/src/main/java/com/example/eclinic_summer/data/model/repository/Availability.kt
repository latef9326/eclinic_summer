package com.example.eclinic_summer.data.model.repository

import java.text.SimpleDateFormat
import java.util.*

data class Availability(
    val id: String = "",
    val date: String = "",
    val dayOfWeek: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val isBooked: Boolean = false
) {
    fun getStatus(): String {
        val currentTime = System.currentTimeMillis()
        val slotTime = parseDateAndTime(this.date, this.startTime)

        // Jeśli nie udało się sparsować czasu, traktuj jako dostępny
        if (slotTime == 0L) return "available"

        return when {
            slotTime < currentTime && this.isBooked -> "completed"
            slotTime < currentTime && !this.isBooked -> "expired"
            this.isBooked -> "scheduled"
            else -> "available"
        }
    }

    private fun parseDateAndTime(dateStr: String, timeStr: String): Long {
        return try {
            // Najpierw spróbuj sparsować format 24-godzinny (np. "14", "15")
            if (timeStr.length <= 2 && timeStr.all { it.isDigit() }) {
                val hour = timeStr.toInt()
                if (hour in 0..23) {
                    val timeWithMinutes = if (timeStr.length == 1) "0$timeStr:00" else "$timeStr:00"
                    val dateTimeStr = "$dateStr $timeWithMinutes"
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    dateFormat.parse(dateTimeStr)?.time ?: 0L
                } else {
                    0L
                }
            }
            // Spróbuj sparsować format 12-godzinny z "am/pm" (np. "3pm", "10am")
            else if (timeStr.contains("am", ignoreCase = true) || timeStr.contains("pm", ignoreCase = true)) {
                parse12HourFormat(dateStr, timeStr)
            }
            // Domyślnie zwróć 0 jeśli format jest nieznany
            else {
                0L
            }
        } catch (e: Exception) {
            0L // Zwróć 0 w przypadku błędu parsowania
        }
    }

    private fun parse12HourFormat(dateStr: String, timeStr: String): Long {
        return try {
            val cleanTime = timeStr.replace("am", "").replace("pm", "").replace("AM", "").replace("PM", "").trim()
            var hour = cleanTime.toInt()

            if (timeStr.contains("pm", ignoreCase = true) && hour < 12) {
                hour += 12
            } else if (timeStr.contains("am", ignoreCase = true) && hour == 12) {
                hour = 0
            }

            val formattedTime = String.format("%02d:00", hour)
            val dateTimeStr = "$dateStr $formattedTime"
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            dateFormat.parse(dateTimeStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}