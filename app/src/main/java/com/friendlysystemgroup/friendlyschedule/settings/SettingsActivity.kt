@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.switchWeekend.setOnCheckedChangeListener { _, isChecked ->
            viewModel.toggleWeekendEnabled(isChecked)
        }

        binding.btnAddBlockedTime.setOnClickListener {
            showTimePickerDialog()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.weekendEnabled.collect { enabled ->
                binding.switchWeekend.isChecked = enabled
            }
        }

        lifecycleScope.launch {
            viewModel.blockedTimes.collect { times ->
                updateBlockedTimeChips(times)
            }
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                viewModel.addBlockedTime(time)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun updateBlockedTimeChips(times: Set<String>) {
        binding.chipGroupBlockedTimes.removeAllViews()
        times.forEach { time ->
            val chip = Chip(this).apply {
                text = time
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    viewModel.removeBlockedTime(time)
                }
            }
            binding.chipGroupBlockedTimes.addView(chip)
        }
    }
} 