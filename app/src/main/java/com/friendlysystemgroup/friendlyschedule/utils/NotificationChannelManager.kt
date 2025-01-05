@Singleton
class NotificationChannelManager @Inject constructor(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createAppointmentChannel()
            createStatsChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createAppointmentChannel() {
        val channel = NotificationChannel(
            CHANNEL_APPOINTMENTS,
            "Recordatorios de citas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Canal para recordatorios de citas médicas"
            enableLights(true)
            lightColor = Color.BLUE
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createStatsChannel() {
        val channel = NotificationChannel(
            CHANNEL_STATS,
            "Estadísticas",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Canal para notificaciones de estadísticas"
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_APPOINTMENTS = "appointment_reminders"
        const val CHANNEL_STATS = "stats_notifications"
    }
} 