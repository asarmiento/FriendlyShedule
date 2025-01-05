class TimeSlotsDialog(
    private val availableSlots: List<String>,
    private val onTimeSelected: (String) -> Unit
) : DialogFragment() {

    private var _binding: DialogTimeSlotsBinding? = null
    private val binding get() = _binding!!
    private val adapter = TimeSlotAdapter(onTimeSelected = { time ->
        onTimeSelected(time)
        dismiss()
    })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogTimeSlotsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvTimeSlots.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = this@TimeSlotsDialog.adapter
        }
        adapter.submitList(availableSlots)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TimeSlotsDialog"
    }
} 