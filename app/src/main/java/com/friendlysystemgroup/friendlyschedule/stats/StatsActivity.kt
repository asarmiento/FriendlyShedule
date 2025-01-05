@AndroidEntryPoint
class StatsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStatsBinding
    private val viewModel: StatsViewModel by viewModels()
    private lateinit var pieChart: PieChart
    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart

    @Inject
    lateinit var reportGenerator: ReportGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupCharts()
        observeViewModel()
        setupMenu()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }
        
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Semana"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Mes"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Año"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Personalizado"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> updateFilter(StatsFilter(dateRange = DateRange.WEEK))
                    1 -> updateFilter(StatsFilter(dateRange = DateRange.MONTH))
                    2 -> updateFilter(StatsFilter(dateRange = DateRange.YEAR))
                    3 -> showDateRangeDialog()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {
                if (tab?.position == 3) showDateRangeDialog()
            }
        })

        binding.fabFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun setupCharts() {
        setupPieChart()
        setupLineChart()
        setupBarChart()
    }

    private fun setupPieChart() {
        pieChart = binding.pieChart
        pieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setDrawEntryLabels(false)
            legend.apply {
                isEnabled = true
                orientation = Legend.LegendOrientation.VERTICAL
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                setDrawInside(false)
                textSize = 12f
                formSize = 12f
                formToTextSpace = 8f
            }
            setExtraOffsets(8f, 8f, 8f, 8f)
        }
    }

    private fun setupLineChart() {
        lineChart = binding.lineChart
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textSize = 10f
            }
            axisLeft.apply {
                setDrawGridLines(true)
                textSize = 10f
            }
            axisRight.isEnabled = false
            legend.apply {
                textSize = 12f
                formSize = 12f
                formToTextSpace = 8f
            }
        }
    }

    private fun setupBarChart() {
        barChart = binding.barChart
        barChart.apply {
            description.isEnabled = false
            setDrawValueAboveBar(true)
            setDrawGridBackground(false)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 1f
            axisRight.isEnabled = false
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.stats.collect { stats ->
                stats?.let { updateCharts(it) }
            }
        }
    }

    private fun updateCharts(stats: AppointmentStats) {
        updatePieChart(stats)
        updateLineChart(stats)
        updateBarChart(stats)
    }

    private fun updatePieChart(stats: AppointmentStats) {
        val entries = listOf(
            PieEntry(stats.attendedAppointments.toFloat(), "Atendidos"),
            PieEntry(stats.pendingAppointments.toFloat(), "Pendientes"),
            PieEntry(stats.absentAppointments.toFloat(), "Ausentes")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(
                getColor(R.color.success),
                getColor(R.color.warning),
                getColor(R.color.error)
            )
            valueTextSize = 14f
            valueFormatter = PercentFormatter(pieChart)
        }

        pieChart.data = PieData(dataSet)
        pieChart.invalidate()
        ChartAnimator.animatePieChart(pieChart)
    }

    private fun updateLineChart(stats: AppointmentStats) {
        val dates = stats.appointmentsByDay.keys.toList()
        val entries = dates.mapIndexed { index, date ->
            Entry(index.toFloat(), stats.appointmentsByDay[date]?.toFloat() ?: 0f)
        }

        val dataSet = LineDataSet(entries, "Citas por día").apply {
            color = getColor(R.color.primary)
            setCircleColor(getColor(R.color.primary))
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
        }

        lineChart.apply {
            xAxis.valueFormatter = DateAxisFormatter(dates)
            data = LineData(dataSet)
            ChartAnimator.animateLineChart(this)
        }
    }

    private fun updateBarChart(stats: AppointmentStats) {
        val entries = stats.appointmentsByHour.map { (hour, count) ->
            BarEntry(hour.toFloat(), count.toFloat())
        }

        val dataSet = BarDataSet(entries, "Citas por hora").apply {
            color = getColor(R.color.accent)
            valueTextSize = 10f
        }

        barChart.data = BarData(dataSet)
        barChart.invalidate()
    }

    private fun setupMenu() {
        binding.toolbar.inflateMenu(R.menu.menu_stats)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_export -> {
                    exportReport()
                    true
                }
                else -> false
            }
        }
    }

    private fun exportReport() {
        viewModel.stats.value?.let { stats ->
            lifecycleScope.launch {
                try {
                    showLoading(true)
                    val uri = reportGenerator.generatePdfReport(stats, viewModel.dateRange.value)
                    shareReport(uri)
                } catch (e: Exception) {
                    showError("Error al generar el reporte: ${e.message}")
                } finally {
                    showLoading(false)
                }
            }
        }
    }

    private fun shareReport(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(intent, "Compartir reporte"))
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
        binding.toolbar.menu.findItem(R.id.action_export)?.isEnabled = !show
    }

    private fun showDateRangeDialog() {
        DateRangeDialog(
            currentRange = viewModel.filter.value.customDateRange,
            onRangeSelected = { range ->
                updateFilter(viewModel.filter.value.copy(
                    dateRange = DateRange.CUSTOM,
                    customDateRange = range
                ))
            }
        ).show(supportFragmentManager, DateRangeDialog.TAG)
    }

    private fun showFilterDialog() {
        StatsFilterDialog(
            currentFilter = viewModel.filter.value,
            onFilterApplied = { filter ->
                updateFilter(filter)
            }
        ).show(supportFragmentManager, StatsFilterDialog.TAG)
    }

    private fun updateFilter(filter: StatsFilter) {
        viewModel.updateFilter(filter)
    }

    private fun updateStats(stats: AppointmentStats) {
        with(binding) {
            tvTotalAppointments.text = stats.totalAppointments.toString()
            tvAttendanceRate.text = "${(stats.attendanceRate * 100).roundToInt()}%"
            tvAbsentRate.text = "${(stats.absentRate * 100).roundToInt()}%"
            
            StatsCalculator.calculateMostFrequentTime(stats)?.let { time ->
                tvMostFrequentTime.text = time
            }

            StatsCalculator.calculateBusiestTimeRange(stats)?.let { (start, end) ->
                tvBusiestTimeRange.text = "$start - $end"
            }

            StatsCalculator.calculateBusiestDay(stats)?.let { day ->
                tvBusiestDay.text = day
            }

            tvDailyAverage.text = String.format("%.1f", StatsCalculator.calculateDailyAverage(stats))
        }

        updateCharts(stats)
    }
} 