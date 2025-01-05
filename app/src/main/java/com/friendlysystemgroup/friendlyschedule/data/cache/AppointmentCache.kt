@Singleton
class AppointmentCache @Inject constructor() {
    private val cache = LruCache<String, List<Appointment>>(CACHE_SIZE)
    private val timestamps = mutableMapOf<String, Long>()

    fun put(date: String, appointments: List<Appointment>) {
        cache.put(date, appointments)
        timestamps[date] = System.currentTimeMillis()
    }

    fun get(date: String): List<Appointment>? {
        val timestamp = timestamps[date] ?: return null
        val isExpired = System.currentTimeMillis() - timestamp > CACHE_EXPIRATION
        return if (isExpired) {
            cache.remove(date)
            timestamps.remove(date)
            null
        } else {
            cache.get(date)
        }
    }

    fun clear() {
        cache.evictAll()
        timestamps.clear()
    }

    companion object {
        private const val CACHE_SIZE = 7 // Una semana de citas
        private const val CACHE_EXPIRATION = 30 * 60 * 1000L // 30 minutos
    }
} 