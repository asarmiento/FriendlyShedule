class ReportGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun generatePdfReport(stats: AppointmentStats, dateRange: DateRange): Uri {
        return withContext(Dispatchers.IO) {
            val document = Document()
            val fileName = "reporte_citas_${System.currentTimeMillis()}.pdf"
            val path = context.getExternalFilesDir(null)?.absolutePath + "/$fileName"
            
            PdfWriter.getInstance(document, FileOutputStream(path))
            document.open()
            
            // Título
            val title = Paragraph("Reporte de Citas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f))
            title.alignment = Element.ALIGN_CENTER
            document.add(title)
            document.add(Paragraph("\n"))
            
            // Período
            document.add(Paragraph("Período: ${dateRange.toDisplayString()}"))
            document.add(Paragraph("\n"))
            
            // Resumen
            addSummaryTable(document, stats)
            document.add(Paragraph("\n"))
            
            // Gráficos
            addCharts(document, stats)
            
            document.close()
            
            Uri.fromFile(File(path))
        }
    }

    private fun addSummaryTable(document: Document, stats: AppointmentStats) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        
        table.addCell(createCell("Total de citas", true))
        table.addCell(createCell(stats.totalAppointments.toString()))
        
        table.addCell(createCell("Citas atendidas", true))
        table.addCell(createCell("${stats.attendedAppointments} (${(stats.attendanceRate * 100).roundToInt()}%)"))
        
        table.addCell(createCell("Citas pendientes", true))
        table.addCell(createCell(stats.pendingAppointments.toString()))
        
        table.addCell(createCell("Ausencias", true))
        table.addCell(createCell("${stats.absentAppointments} (${(stats.absentRate * 100).roundToInt()}%)"))
        
        document.add(table)
    }

    private fun addCharts(document: Document, stats: AppointmentStats) {
        // Gráfico de citas por día
        val dailyChart = generateDailyChart(stats)
        document.add(Image.getInstance(dailyChart))
        document.add(Paragraph("\n"))
        
        // Gráfico de citas por hora
        val hourlyChart = generateHourlyChart(stats)
        document.add(Image.getInstance(hourlyChart))
    }

    private fun createCell(text: String, isHeader: Boolean = false): PdfPCell {
        return PdfPCell(Phrase(text)).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            if (isHeader) {
                backgroundColor = BaseColor(230, 230, 230)
            }
            padding = 8f
        }
    }

    private fun generateDailyChart(stats: AppointmentStats): ByteArray {
        val chart = LineChart(context)
        // Configurar y generar gráfico similar al de la UI
        return chart.toBitmap().toByteArray()
    }

    private fun generateHourlyChart(stats: AppointmentStats): ByteArray {
        val chart = BarChart(context)
        // Configurar y generar gráfico similar al de la UI
        return chart.toBitmap().toByteArray()
    }
}

private fun DateRange.toDisplayString(): String {
    return when (this) {
        DateRange.WEEK -> "Última semana"
        DateRange.MONTH -> "Último mes"
        DateRange.YEAR -> "Último año"
    }
} 