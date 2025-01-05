@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return Retrofit.Builder()
            .baseUrl("https://friendlypos.net/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build())
            .build()
            .create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "medical_agenda_db"
        ).build()
    }

    @Provides
    fun provideAppointmentDao(database: AppDatabase): AppointmentDao {
        return database.appointmentDao()
    }

    @Provides
    fun provideCustomerDao(database: AppDatabase): CustomerDao {
        return database.customerDao()
    }

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager {
        return PreferencesManager(context)
    }

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
                    SyncWorker::class.java.name ->
                        SyncWorker(appContext, workerParameters)
                    else -> null
                }
            }
        }
    }
} 