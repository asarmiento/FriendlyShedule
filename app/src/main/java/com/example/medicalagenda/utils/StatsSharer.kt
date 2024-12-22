@Singleton
class StatsSharer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun shareAsImage(stats: AppointmentStats): Uri {
        return withContext(Dispatchers.IO) {
            val bitmap = createStatsBitmap(stats)
            saveBitmapToFile(bitmap)
        }
    }

    private fun createStatsBitmap(stats: AppointmentStats): Bitmap {
        val width = 800
        val height = 1200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fondo
        canvas.drawColor(Color.WHITE)

        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 40f
            color = Color.BLACK
        }

        // Título
        paint.textSize = 60f
        paint.isFakeBoldText = true
        canvas.drawText("Estadísticas", 40f, 80f, paint)
        paint.isFakeBoldText = false

        // Resumen
        paint.textSize = 40f
        var y = 200f
        canvas.drawText("Total de citas: ${stats.totalAppointments}", 40f, y, paint)
        
        y += 80f
        paint.color = Color.parseColor("#4CAF50")
        canvas.drawText("Asistencia: ${(stats.attendanceRate * 100).roundToInt()}%", 40f, y, paint)
        
        y += 80f
        paint.color = Color.parseColor("#F44336")
        canvas.drawText("Ausencias: ${(stats.absentRate * 100).roundToInt()}%", 40f, y, paint)

        // Gráfico de barras
        paint.color = Color.BLACK
        y += 120f
        canvas.drawText("Citas por día", 40f, y, paint)
        drawBarChart(canvas, stats.appointmentsByDay, y + 40f)

        return bitmap
    }

    private fun drawBarChart(canvas: Canvas, data: Map<String, Int>, startY: Float) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#2196F3")
        }

        val maxValue = data.values.maxOrNull() ?: 0
        val barWidth = 60f
        val spacing = 20f
        var x = 40f

        data.forEach { (_, value) ->
            val barHeight = (value.toFloat() / maxValue) * 300
            canvas.drawRect(x, startY, x + barWidth, startY + barHeight, paint)
            x += barWidth + spacing
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri {
        val fileName = "estadisticas_${System.currentTimeMillis()}.png"
        val file = File(context.getExternalFilesDir(null), fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
} 