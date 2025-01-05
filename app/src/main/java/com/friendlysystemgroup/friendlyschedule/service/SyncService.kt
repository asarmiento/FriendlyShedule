@AndroidEntryPoint
class SyncService : Service() {
    
    @Inject
    lateinit var repository: AppointmentRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        
        serviceScope.launch {
            try {
                syncData()
            } finally {
                stopSelf()
            }
        }
        
        return START_NOT_STICKY
    }
    
    private suspend fun syncData() {
        try {
            // Sincronizar citas locales pendientes
            val localAppointments = repository.getPendingSyncAppointments()
            localAppointments.forEach { appointment ->
                repository.syncAppointment(appointment)
            }
            
            // Actualizar datos desde el servidor
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.getAppointments(today)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing data", e)
        }
    }
    
    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Sincronización",
            NotificationManager.IMPORTANCE_LOW
        )
        
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sincronizando datos")
            .setSmallIcon(R.drawable.ic_sync)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    companion object {
        private const val TAG = "SyncService"
        private const val CHANNEL_ID = "sync_channel"
        private const val NOTIFICATION_ID = 1
        
        fun startSync(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(Intent(context, SyncService::class.java))
            } else {
                context.startService(Intent(context, SyncService::class.java))
            }
        }
    }
} 