@Singleton
class PreferencesManager @Inject constructor(
    private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun setWeekendEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WEEKEND_ENABLED, enabled).apply()
    }

    fun isWeekendEnabled(): Boolean {
        return prefs.getBoolean(KEY_WEEKEND_ENABLED, false)
    }

    fun setBlockedTimes(times: Set<String>) {
        prefs.edit().putStringSet(KEY_BLOCKED_TIMES, times).apply()
    }

    fun getBlockedTimes(): Set<String> {
        return prefs.getStringSet(KEY_BLOCKED_TIMES, emptySet()) ?: emptySet()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "MedicalAgendaPrefs"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_WEEKEND_ENABLED = "weekend_enabled"
        private const val KEY_BLOCKED_TIMES = "blocked_times"
    }
} 