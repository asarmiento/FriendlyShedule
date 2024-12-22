class AppointmentGroupAdapter(
    private val onAttendanceChanged: (Int, Boolean) -> Unit
) : ListAdapter<AppointmentGroup, AppointmentGroupAdapter.ViewHolder>(AppointmentGroupDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentGroupBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAppointmentGroupBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(group: AppointmentGroup) {
            binding.apply {
                tvTime.text = group.time
                rvAppointments.apply {
                    if (adapter == null) {
                        layoutManager = LinearLayoutManager(context)
                        adapter = AppointmentAdapter(onAttendanceChanged)
                    }
                    (adapter as AppointmentAdapter).submitList(group.appointments)
                }
            }
        }
    }
}

data class AppointmentGroup(
    val time: String,
    val appointments: List<Appointment>
)

class AppointmentGroupDiffCallback : DiffUtil.ItemCallback<AppointmentGroup>() {
    override fun areItemsTheSame(oldItem: AppointmentGroup, newItem: AppointmentGroup): Boolean {
        return oldItem.time == newItem.time
    }

    override fun areContentsTheSame(oldItem: AppointmentGroup, newItem: AppointmentGroup): Boolean {
        return oldItem == newItem
    }
} 