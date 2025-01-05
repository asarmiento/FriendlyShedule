@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }

    @Provides
    fun provideWorkerFactory(
        repository: AppointmentRepository
    ): WorkerFactory {
        return object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return when (workerClassName) {
                    WidgetUpdateWorker::class.java.name ->
                        WidgetUpdateWorker(appContext, workerParameters)
                    else -> null
                }
            }
        }
    }
} 