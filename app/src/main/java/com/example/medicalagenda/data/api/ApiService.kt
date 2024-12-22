interface ApiService {
    @POST("login")
    suspend fun login(
        @Body credentials: LoginRequest
    ): LoginResponse

    @GET("histories-schedule")
    suspend fun getAppointments(
        @Header("Authorization") token: String
    ): List<AppointmentResponse>

    @POST("store-shedule")
    suspend fun createAppointment(
        @Header("Authorization") token: String,
        @Body appointment: AppointmentRequest
    ): AppointmentResponse

    @GET("api/ae")
    suspend fun validateCustomer(
        @Query("identificacion") id: String
    ): CustomerValidationResponse
} 