object StatsCalculator {
    fun calculateMostFrequentTime(stats: AppointmentStats): String? {
        return stats.appointmentsByHour.maxByOrNull { it.value }?.key
    }

    fun calculateDailyAverage(stats: AppointmentStats): Float {
        return if (stats.appointmentsByDay.isNotEmpty()) {
            stats.totalAppointments.toFloat() / stats.appointmentsByDay.size
        } else 0f
    }

    fun calculateBusiestDay(stats: AppointmentStats): String? {
        return stats.appointmentsByDay.maxByOrNull { it.value }?.key?.let { date ->
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormatter = SimpleDateFormat("EEEE", Locale("es"))
            formatter.parse(date)?.let { displayFormatter.format(it).capitalize() }
        }
    }

    fun calculateBusiestTimeRange(stats: AppointmentStats): Pair<String, String>? {
        val sortedHours = stats.appointmentsByHour.keys.sorted()
        if (sortedHours.size < 2) return null

        var maxCount = 0
        var bestStart = sortedHours.first()
        var currentStart = sortedHours.first()
        var currentCount = 0

        for (i in 1 until sortedHours.size) {
            val prevHour = sortedHours[i - 1]
            val currentHour = sortedHours[i]

            if (isConsecutiveHour(prevHour, currentHour)) {
                currentCount += stats.appointmentsByHour[currentHour] ?: 0
                if (currentCount > maxCount) {
                    maxCount = currentCount
                    bestStart = currentStart
                }
            } else {
                currentCount = stats.appointmentsByHour[currentHour] ?: 0
                currentStart = currentHour
            }
        }

        val endHour = getNextHour(bestStart)
        return Pair(bestStart, endHour)
    }

    private fun isConsecutiveHour(hour1: String, hour2: String): Boolean {
        val h1 = hour1.split(":")[0].toInt()
        val h2 = hour2.split(":")[0].toInt()
        return h2 - h1 == 1
    }

    private fun getNextHour(hour: String): String {
        val h = hour.split(":")[0].toInt()
        return String.format("%02d:00", h + 1)
    }
} 