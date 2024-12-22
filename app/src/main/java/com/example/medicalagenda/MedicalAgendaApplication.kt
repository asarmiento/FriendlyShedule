@HiltAndroidApp
class MedicalAgendaApplication : Application() {

    @Inject
    lateinit var notificationChannelManager: NotificationChannelManager

    override fun onCreate() {
        super.onCreate()
        initializeWorkManager()
        initializeSync()
        initializeNotifications()
    }

    private fun initializeWorkManager() {
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.INFO)
                .build()
        )
        WidgetUpdateWorker.enqueuePeriodicWork()
    }

    private fun initializeSync() {
        SyncWorker.schedulePeriodic()
    }

    private fun initializeNotifications() {
        notificationChannelManager.createChannels()
        StatsNotificationService.schedule(this)
    }
} 