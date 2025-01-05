@AndroidEntryPoint
class DetailedStatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailedStatsBinding
    private val viewModel: StatsViewModel by viewModels()

    @Inject
    lateinit var statsSharer: StatsSharer

    @Inject
    lateinit var statsAnalyzer: StatsAnalyzer

    @Inject
    lateinit var statsExportManager: StatsExportManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.toolbar.inflateMenu(R.menu.menu_detailed_stats)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export_pdf -> {
                    exportStats(ExportFormat.PDF)
                    true
                }
                R.id.action_export_excel -> {
                    exportStats(ExportFormat.EXCEL)
                    true
                }
                R.id.action_share_image -> {
                    shareAsImage()
                    true
                }
                else -> false
            }
        }

        binding.timeDistributionView.setOnBarClickListener { timeOfDay, details ->
            showTimeDistributionDetails(timeOfDay, details)
        }

        binding.activeFiltersView.setOnFilterRemovedListener { filterId ->
            viewModel.removeFilter(filterId)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                stats?.let { updateStats(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.filter.collect { filter ->
                updateActiveFilters(filter)
            }
        }
    }

    private fun updateStats(stats: AppointmentStats) {
        with(binding) {
            // Resumen general con animaciones
            StatsAnimator.animateNumber(tvTotalAppointments, stats.totalAppointments)
            StatsAnimator.animatePercentage(tvAttendanceRate, stats.attendanceRate)
            StatsAnimator.animatePercentage(tvAbsentRate, stats.absentRate)

            // Estadísticas por día
            val dailyStats = stats.appointmentsByDay.entries.sortedBy { it.key }
            updateDailyStats(dailyStats)
            StatsAnimator.animateRecyclerView(rvDailyStats)

            // Estadísticas por hora
            val hourlyStats = stats.appointmentsByHour.entries.sortedBy { it.key }
            updateHourlyStats(hourlyStats)
            StatsAnimator.animateRecyclerView(rvHourlyStats)

            // Análisis de estadísticas
            val analysis = statsAnalyzer.analyzeStats(stats)
            
            // Actualizar distribución horaria
            timeDistributionView.setData(analysis.timeDistribution)
            
            // Actualizar tendencias
            updateTrends(analysis.trends)
        }
    }

    private fun updateDailyStats(dailyStats: List<Map.Entry<String, Int>>) {
        binding.rvDailyStats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = DailyStatsAdapter(dailyStats)
        }
    }

    private fun updateHourlyStats(hourlyStats: List<Map.Entry<String, Int>>) {
        binding.rvHourlyStats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = HourlyStatsAdapter(hourlyStats)
        }
    }

    private fun updateTrends(trends: List<Trend>) {
        binding.rvTrends.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TrendAdapter(trends)
        }
        StatsAnimator.animateRecyclerView(binding.rvTrends)
    }

    private fun exportStats(format: ExportFormat) {
        viewModel.stats.value?.let { stats ->
            lifecycleScope.launch {
                try {
                    showLoading(true)
                    val uri = statsExportManager.exportDetailedStats(stats, format)
                    shareFile(uri, format.mimeType)
                } catch (e: Exception) {
                    showError("Error al exportar: ${e.message}")
                } finally {
                    showLoading(false)
                }
            }
        }
    }

    private fun shareFile(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartir estadísticas"))
    }

    private fun shareAsImage() {
        viewModel.stats.value?.let { stats ->
            lifecycleScope.launch {
                try {
                    showLoading(true)
                    val uri = statsSharer.shareAsImage(stats)
                    shareFile(uri, "image/png")
                } catch (e: Exception) {
                    showError("Error al compartir: ${e.message}")
                } finally {
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.toolbar.menu.forEach { it.isEnabled = !show }
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showTimeDistributionDetails(timeOfDay: TimeOfDay, details: TimeDistributionDetails) {
        TimeDistributionDetailsDialog(timeOfDay, details)
            .show(supportFragmentManager, TimeDistributionDetailsDialog.TAG)
    }

    private fun updateActiveFilters(filter: StatsFilterState) {
        val activeFilters = mutableListOf<FilterItem>()

        // Filtros de tipo
        AppointmentType.values().forEach { type ->
            if (!filter.showTypes.contains(type)) {
                activeFilters.add(FilterItem(
                    id = "type_${type.name}",
                    displayText = "Sin ${type.name.lowercase()}"
                ))
            }
        }

        // Filtros de estado
        AppointmentStatus.values().forEach { status ->
            if (!filter.showStatuses.contains(status)) {
                activeFilters.add(FilterItem(
                    id = "status_${status.name}",
                    displayText = "Sin ${status.name.lowercase()}"
                ))
            }
        }

        // Filtro de tasa de asistencia
        if (filter.minAttendanceRate != null || filter.maxAttendanceRate != null) {
            activeFilters.add(FilterItem(
                id = "attendance_rate",
                displayText = "Asistencia: ${filter.minAttendanceRate?.times(100)?.roundToInt()}% - " +
                        "${filter.maxAttendanceRate?.times(100)?.roundToInt()}%"
            ))
        }

        binding.activeFiltersView.isVisible = activeFilters.isNotEmpty()
        binding.activeFiltersView.setFilters(activeFilters)
    }
}

enum class ExportFormat(val mimeType: String) {
    PDF("application/pdf"),
    EXCEL("application/vnd.ms-excel")
} 