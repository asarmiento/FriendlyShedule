class NewAppointmentDialog : DialogFragment() {

    private var _binding: DialogNewAppointmentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogNewAppointmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.etIdentification.addTextChangedListener { text ->
            if (text?.length == 9) {
                viewModel.validateCustomer(text.toString())
            }
        }

        binding.etDate.setOnClickListener { showDatePicker() }
        binding.etTime.setOnClickListener { showTimePicker() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.customerValidation.collect { state ->
                when (state) {
                    is CustomerValidationState.Success -> {
                        binding.tvPatientName.apply {
                            text = state.name
                            isVisible = true
                        }
                    }
                    is CustomerValidationState.Error -> {
                        showError(state.message)
                        binding.tvPatientName.isVisible = false
                    }
                    else -> binding.tvPatientName.isVisible = false
                }
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }
                if (viewModel.isValidDate(selectedDate)) {
                    binding.etDate.setText(formatDate(selectedDate.time))
                } else {
                    showError("Solo se permiten citas de lunes a viernes")
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                val time = String.format("%02d:%02d", hour, minute)
                if (viewModel.isValidTime(time)) {
                    binding.etTime.setText(time)
                } else {
                    showError("Horario no disponible")
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error))
            .setTextColor(requireContext().getColor(R.color.text_light))
            .show()
    }

    private fun formatDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "NewAppointmentDialog"
    }
} 