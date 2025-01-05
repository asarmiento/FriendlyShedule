package com.friendlysystemgroup.friendlyschedule.main

import AppointmentConfirmationDialog
import AppointmentData
import AppointmentGroupAdapter
import FilterDialog
import LoginActivity
import MainViewModel
import NewAppointmentDialog
import PermissionManager
import SettingsActivity
import TimeSlotsDialog
import UiState

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val appointmentGroupAdapter = AppointmentGroupAdapter(
        onAttendanceChanged = { id, attended ->
            viewModel.markAsAttended(id, attended)
        }
    )

    @Inject
    lateinit var permissionManager: PermissionManager

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (!allGranted) {
            showPermissionExplanationDialog()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermissions()
        setupUI()
        observeViewModel()
        loadTodayAppointments()
    }

    private fun checkPermissions() {
        if (!permissionManager.checkNotificationPermission() || !permissionManager.checkAlarmPermission()) {
            permissionLauncher.launch(PermissionManager.REQUIRED_PERMISSIONS)
        }
    }

    private fun showPermissionExplanationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permisos necesarios")
            .setMessage("Para recibir recordatorios de citas, necesitamos permisos para enviar notificaciones y programar alarmas.")
            .setPositiveButton("Configuración") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openAppSettings() {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            startActivity(this)
        }
    }

    private fun setupUI() {
        binding.rvAppointments.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = appointmentGroupAdapter
        }

        binding.fabAddAppointment.setOnClickListener {
            showAddAppointmentDialog()
        }

        binding.tvCurrentDate.text = getCurrentFormattedDate()

        binding.searchBar.etSearch.addTextChangedListener { text ->
            viewModel.updateSearchQuery(text?.toString() ?: "")
        }

        binding.searchBar.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.filteredAppointmentGroups.collect { groups ->
                appointmentGroupAdapter.submitList(groups)
                binding.layoutEmptyState.isVisible = groups.isEmpty()
            }
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is UiState.Loading -> showLoading(true)
                    is UiState.Success -> showLoading(false)
                    is UiState.Error -> {
                        showLoading(false)
                        showError(state.message)
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.appointmentData.collect { appointmentData ->
                appointmentData?.let { showAppointmentConfirmation(it) }
            }
        }
    }

    private fun loadTodayAppointments() {
        viewModel.loadAppointments(getCurrentDate())
    }

    private fun showAddAppointmentDialog() {
        NewAppointmentDialog().show(
            supportFragmentManager,
            NewAppointmentDialog.TAG
        )
    }

    private fun showTimeSlots(date: String) {
        viewModel.loadAvailableTimeSlots(date)
        lifecycleScope.launch {
            viewModel.availableTimeSlots.collect { slots ->
                if (slots.isNotEmpty()) {
                    TimeSlotsDialog(
                        availableSlots = slots,
                        onTimeSelected = { time ->
                            // Aquí se maneja la selección del horario
                            viewModel.selectedTime = time
                        }
                    ).show(supportFragmentManager, TimeSlotsDialog.TAG)
                } else {
                    showError("No hay horarios disponibles para esta fecha")
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(getColor(R.color.error))
            .setTextColor(getColor(R.color.text_light))
            .show()
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    private fun getCurrentFormattedDate(): String {
        return SimpleDateFormat("dd MMM, yyyy", Locale("es")).format(Date())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_calendar -> {
                showCalendarDialog()
                true
            }
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showCalendarDialog() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                viewModel.loadAppointments(formatDate(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Está seguro que desea cerrar la sesión?")
            .setPositiveButton("Sí") { _, _ ->
                viewModel.logout()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showAppointmentConfirmation(appointmentData: AppointmentData) {
        AppointmentConfirmationDialog(
            appointment = appointmentData,
            onConfirm = {
                viewModel.createAppointment(
                    customerId = appointmentData.customerId,
                    date = appointmentData.date,
                    time = appointmentData.time,
                    description = appointmentData.description
                )
            }
        ).show(supportFragmentManager, AppointmentConfirmationDialog.TAG)
    }

    private fun showFilterDialog() {
        FilterDialog(
            currentFilters = viewModel.getCurrentFilters(),
            onFiltersApplied = { filters ->
                viewModel.updateFilters(filters)
            }
        ).show(supportFragmentManager, FilterDialog.TAG)
    }
} 