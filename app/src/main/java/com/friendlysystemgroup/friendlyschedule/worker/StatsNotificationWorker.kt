@HiltWorker
class StatsNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val statsDao: StatsDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val intent = Intent(applicationContext, StatsNotificationService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationContext.startForegroundService(intent)
            } else {
                applicationContext.startService(intent)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 