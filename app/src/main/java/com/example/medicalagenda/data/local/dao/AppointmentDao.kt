@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE initDate = :date")
    suspend fun getAppointmentsByDate(date: String): List<AppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Query("UPDATE appointments SET attendance = :attended WHERE id = :appointmentId")
    suspend fun updateAttendanceStatus(appointmentId: Int, attended: Boolean)

    @Query("DELETE FROM appointments WHERE initDate < :date")
    suspend fun deleteOldAppointments(date: String)

    @Query("SELECT * FROM appointments WHERE needsSync = 1")
    suspend fun getPendingSyncAppointments(): List<AppointmentEntity>

    @Query("UPDATE appointments SET needsSync = 1 WHERE id = :appointmentId")
    suspend fun markForSync(appointmentId: Int)

    @Query("UPDATE appointments SET needsSync = 0 WHERE id = :appointmentId")
    suspend fun clearSyncFlag(appointmentId: Int)
} 