class AppointmentAdapter(
    private val onAttendanceChanged: (Int, Boolean) -> Unit
) : ListAdapter<Appointment, AppointmentAdapter.ViewHolder>(AppointmentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemAppointmentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            binding.apply {
                tvTime.text = appointment.init_time.substring(0, 5)
                tvPatientName.text = appointment.customer?.name ?: "Sin nombre"
                tvDescription.text = appointment.description
                cbAttended.isChecked = appointment.attendance

                cbAttended.setOnCheckedChangeListener { _, isChecked ->
                    onAttendanceChanged(appointment.id, isChecked)
                }
            }
        }
    }
}

class AppointmentDiffCallback : DiffUtil.ItemCallback<Appointment>() {
    override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
        return oldItem == newItem
    }
} 