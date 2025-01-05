@Dao
interface StatsDao {
    @Query("""
        SELECT 
            COUNT(*) as total,
            SUM(CASE WHEN attendance = 1 THEN 1 ELSE 0 END) as attended,
            SUM(CASE WHEN attendance = 0 AND init_date >= date('now') THEN 1 ELSE 0 END) as pending,
            SUM(CASE WHEN absent = 1 THEN 1 ELSE 0 END) as absent
        FROM appointments
        WHERE init_date BETWEEN :startDate AND :endDate
    """)
    suspend fun getAppointmentStats(startDate: String, endDate: String): AppointmentStatsEntity

    @Query("""
        SELECT init_date as date, COUNT(*) as count
        FROM appointments
        WHERE init_date BETWEEN :startDate AND :endDate
        GROUP BY init_date
    """)
    suspend fun getAppointmentsByDay(startDate: String, endDate: String): List<DailyStatsEntity>

    @Query("""
        SELECT substr(init_time, 1, 5) as hour, COUNT(*) as count
        FROM appointments
        WHERE init_date BETWEEN :startDate AND :endDate
        GROUP BY substr(init_time, 1, 5)
    """)
    suspend fun getAppointmentsByHour(startDate: String, endDate: String): List<HourlyStatsEntity>
}

data class AppointmentStatsEntity(
    val total: Int,
    val attended: Int,
    val pending: Int,
    val absent: Int
)

data class DailyStatsEntity(
    val date: String,
    val count: Int
)

data class HourlyStatsEntity(
    val hour: String,
    val count: Int
) 