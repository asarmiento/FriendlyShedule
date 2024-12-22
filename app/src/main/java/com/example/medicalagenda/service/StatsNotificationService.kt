@AndroidEntryPoint
class StatsNotificationService : Service() {
    
    @Inject
    lateinit var statsDao: StatsDao
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            checkAndNotifyStats()
        }
        return START_NOT_STICKY
    }
    
    private suspend fun checkAndNotifyStats() {
        try {
            val today = LocalDate.now()
            val startDate = today.minusDays(7)
            
            val stats = statsDao.getAppointmentStats(
                startDate.format(DateTimeFormatter.ISO_DATE),
                today.format(DateTimeFormatter.ISO_DATE)
            )
            
            if (stats.total > 0 && stats.attended.toFloat() / stats.total < 0.7f) {
                showLowAttendanceNotification(stats)
            }
            
            stopSelf()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking stats", e)
            stopSelf()
        }
    }
    
    private fun showLowAttendanceNotification(stats: AppointmentStatsEntity) {
        val attendanceRate = (stats.attended.toFloat() / stats.total * 100).roundToInt()
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stats)
            .setContentTitle("Alerta de asistencia")
            .setContentText("La tasa de asistencia es del $attendanceRate%")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("La tasa de asistencia de la última semana es del $attendanceRate%. " +
                        "Total de citas: ${stats.total}, Atendidas: ${stats.attended}"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val TAG = "StatsNotificationService"
        private const val CHANNEL_ID = "stats_notifications"
        private const val NOTIFICATION_ID = 2
        
        fun schedule(context: Context) {
            val work = PeriodicWorkRequestBuilder<StatsNotificationWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    "stats_notification",
                    ExistingPeriodicWorkPolicy.KEEP,
                    work
                )
        }
    }
} 