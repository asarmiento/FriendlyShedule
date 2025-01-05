class AppointmentConfirmationDialog(
    private val appointment: AppointmentData,
    private val onConfirm: () -> Unit
) : DialogFragment() {

    private var _binding: DialogAppointmentConfirmationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAppointmentConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        with(binding) {
            tvPatientName.text = "Paciente: ${appointment.patientName}"
            tvDateTime.text = "Fecha: ${appointment.formattedDateTime}"
            tvDescription.text = "Descripción: ${appointment.description}"

            btnCancel.setOnClickListener { dismiss() }
            btnConfirm.setOnClickListener {
                onConfirm()
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AppointmentConfirmationDialog"
    }
}

data class AppointmentData(
    val patientName: String,
    val formattedDateTime: String,
    val description: String?,
    val customerId: Int,
    val date: String,
    val time: String
) 