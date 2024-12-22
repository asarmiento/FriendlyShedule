@Singleton
class StatsAnalyzer @Inject constructor() {
    fun analyzeStats(stats: AppointmentStats): StatsAnalysis {
        return StatsAnalysis(
            totalAppointments = stats.totalAppointments,
            attendanceRate = stats.attendanceRate,
            absentRate = stats.absentRate,
            busiestDay = findBusiestDay(stats.appointmentsByDay),
            busiestTime = findBusiestTime(stats.appointmentsByHour),
            timeDistribution = analyzeTimeDistribution(stats.appointmentsByHour),
            weekdayDistribution = analyzeWeekdayDistribution(stats.appointmentsByDay),
            trends = analyzeTrends(stats)
        )
    }

    private fun findBusiestDay(dailyStats: Map<String, Int>): DayStats {
        val entry = dailyStats.maxByOrNull { it.value } ?: return DayStats("", 0)
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(entry.key)
        val dayName = date?.let {
            SimpleDateFormat("EEEE", Locale("es")).format(it).capitalize()
        } ?: entry.key
        return DayStats(dayName, entry.value)
    }

    private fun findBusiestTime(hourlyStats: Map<String, Int>): TimeStats {
        val entry = hourlyStats.maxByOrNull { it.value } ?: return TimeStats("", 0)
        return TimeStats(entry.key, entry.value)
    }

    private fun analyzeTimeDistribution(hourlyStats: Map<String, Int>): TimeDistribution {
        val morning = hourlyStats.filter { it.key.split(":")[0].toInt() in 7..11 }
            .values.sum()
        val afternoon = hourlyStats.filter { it.key.split(":")[0].toInt() in 12..16 }
            .values.sum()
        val evening = hourlyStats.filter { it.key.split(":")[0].toInt() in 17..21 }
            .values.sum()

        return TimeDistribution(morning, afternoon, evening)
    }

    private fun analyzeWeekdayDistribution(dailyStats: Map<String, Int>): Map<String, Int> {
        return dailyStats.entries.groupBy { entry ->
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(entry.key)
            date?.let {
                SimpleDateFormat("EEEE", Locale("es")).format(it).capitalize()
            } ?: "Desconocido"
        }.mapValues { it.value.sumOf { entry -> entry.value } }
    }

    private fun analyzeTrends(stats: AppointmentStats): List<Trend> {
        val trends = mutableListOf<Trend>()

        // Analizar tendencia de asistencia
        if (stats.attendanceRate > 0.8f) {
            trends.add(Trend(
                "Alta tasa de asistencia",
                "La tasa de asistencia es superior al 80%",
                TrendType.POSITIVE
            ))
        } else if (stats.attendanceRate < 0.6f) {
            trends.add(Trend(
                "Baja tasa de asistencia",
                "La tasa de asistencia es inferior al 60%",
                TrendType.NEGATIVE
            ))
        }

        // Analizar distribución horaria
        val timeDistribution = analyzeTimeDistribution(stats.appointmentsByHour)
        val total = timeDistribution.morning + timeDistribution.afternoon + timeDistribution.evening
        if (total > 0) {
            val morningPercentage = timeDistribution.morning.toFloat() / total
            if (morningPercentage > 0.5f) {
                trends.add(Trend(
                    "Preferencia matutina",
                    "Más del 50% de las citas son en la mañana",
                    TrendType.NEUTRAL
                ))
            }
        }

        return trends
    }
}

data class StatsAnalysis(
    val totalAppointments: Int,
    val attendanceRate: Float,
    val absentRate: Float,
    val busiestDay: DayStats,
    val busiestTime: TimeStats,
    val timeDistribution: TimeDistribution,
    val weekdayDistribution: Map<String, Int>,
    val trends: List<Trend>
)

data class DayStats(val name: String, val count: Int)
data class TimeStats(val time: String, val count: Int)
data class TimeDistribution(val morning: Int, val afternoon: Int, val evening: Int)
data class Trend(val title: String, val description: String, val type: TrendType)

enum class TrendType {
    POSITIVE, NEGATIVE, NEUTRAL
} 