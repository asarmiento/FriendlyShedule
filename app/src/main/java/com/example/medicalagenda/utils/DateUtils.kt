object DateUtils {
    fun generateTimeSlots(startHour: Int = 7, endHour: Int = 17, intervalMinutes: Int = 30): List<String> {
        val slots = mutableListOf<String>()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, startHour)
            set(Calendar.MINUTE, 0)
        }

        while (calendar.get(Calendar.HOUR_OF_DAY) < endHour) {
            slots.add(formatTime(calendar.time))
            calendar.add(Calendar.MINUTE, intervalMinutes)
        }

        return slots
    }

    fun formatTime(date: Date): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
    }

    fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    fun formatDateForDisplay(date: Date): String {
        return SimpleDateFormat("dd MMM, yyyy", Locale("es")).format(date)
    }

    fun isWeekend(calendar: Calendar): Boolean {
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
    }
} 