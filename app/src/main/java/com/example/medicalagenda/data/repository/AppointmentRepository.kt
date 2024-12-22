@Singleton
class AppointmentRepository @Inject constructor(
    private val apiService: ApiService,
    private val appointmentDao: AppointmentDao,
    private val preferencesManager: PreferencesManager,
    private val networkManager: NetworkManager,
    private val appointmentCache: AppointmentCache
) {
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            preferencesManager.saveToken(response.access_token)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAppointments(date: String): List<Appointment> {
        // Intentar obtener del caché primero
        appointmentCache.get(date)?.let { return it }

        return if (networkManager.isNetworkAvailable()) {
            try {
                val token = "Bearer ${preferencesManager.getToken()}"
                val remoteAppointments = apiService.getAppointments(token)
                
                // Guardar en caché y base de datos local
                appointmentCache.put(date, remoteAppointments)
                appointmentDao.insertAll(remoteAppointments.map { it.toEntity() })
                
                remoteAppointments
            } catch (e: Exception) {
                // Si falla la red, usar datos locales
                appointmentDao.getAppointmentsByDate(date).map { it.toDomain() }
            }
        } else {
            appointmentDao.getAppointmentsByDate(date).map { it.toDomain() }
        }
    }

    suspend fun createAppointment(appointment: AppointmentRequest): Result<Appointment> {
        return try {
            val token = "Bearer ${preferencesManager.getToken()}"
            val response = apiService.createAppointment(token, appointment)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateCustomer(identification: String): Result<CustomerValidationResponse> {
        return try {
            val response = apiService.validateCustomer(identification)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAppointmentStatus(appointmentId: Int, attended: Boolean) {
        appointmentDao.updateAttendanceStatus(appointmentId, attended)
        // TODO: Implementar sincronización con el servidor
    }

    suspend fun syncAppointment(appointment: Appointment) {
        if (!networkManager.isNetworkAvailable()) {
            appointmentDao.markForSync(appointment.id)
            return
        }

        _syncState.value = SyncState.Syncing
        try {
            val token = "Bearer ${preferencesManager.getToken()}"
            apiService.updateAppointment(token, appointment.id, appointment)
            appointmentDao.clearSyncFlag(appointment.id)
            _syncState.value = SyncState.Success
        } catch (e: Exception) {
            _syncState.value = SyncState.Error(e.message ?: "Error de sincronización")
            appointmentDao.markForSync(appointment.id)
        }
    }

    suspend fun getPendingSyncAppointments(): List<Appointment> {
        return appointmentDao.getPendingSyncAppointments().map { it.toDomain() }
    }
}

sealed class SyncState {
    object Idle : SyncState()
    object Syncing : SyncState()
    object Success : SyncState()
    data class Error(val message: String) : SyncState()
} 