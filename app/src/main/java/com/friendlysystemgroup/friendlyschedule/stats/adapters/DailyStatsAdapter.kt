class DailyStatsAdapter(
    private val stats: List<Map.Entry<String, Int>>
) : RecyclerView.Adapter<DailyStatsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDailyStatsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount() = stats.size

    class ViewHolder(
        private val binding: ItemDailyStatsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: Map.Entry<String, Int>) {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .parse(stat.key)
            
            binding.tvDate.text = date?.let {
                SimpleDateFormat("EEEE dd MMM", Locale("es")).format(it)
            } ?: stat.key
            
            binding.tvCount.text = stat.value.toString()
            binding.progressBar.progress = stat.value
        }
    }
} 