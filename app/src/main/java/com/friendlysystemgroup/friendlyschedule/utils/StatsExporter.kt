@Singleton
class StatsExporter @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportToPdf(stats: AppointmentStats, dateRange: String): Uri {
        return withContext(Dispatchers.IO) {
            val document = Document()
            val fileName = "estadisticas_${System.currentTimeMillis()}.pdf"
            val path = context.getExternalFilesDir(null)?.absolutePath + "/$fileName"
            
            PdfWriter.getInstance(document, FileOutputStream(path))
            document.open()
            
            addHeader(document, dateRange)
            addSummary(document, stats)
            addCharts(document, stats)
            addDetailedStats(document, stats)
            
            document.close()
            Uri.fromFile(File(path))
        }
    }

    suspend fun exportToExcel(stats: AppointmentStats): Uri {
        return withContext(Dispatchers.IO) {
            val workbook = HSSFWorkbook()
            val sheet = workbook.createSheet("Estadísticas")
            
            createSummarySheet(sheet, stats)
            createDetailedSheet(workbook.createSheet("Detalle"), stats)
            
            val fileName = "estadisticas_${System.currentTimeMillis()}.xls"
            val path = context.getExternalFilesDir(null)?.absolutePath + "/$fileName"
            
            FileOutputStream(path).use { 
                workbook.write(it)
            }
            
            Uri.fromFile(File(path))
        }
    }

    private fun addHeader(document: Document, dateRange: String) {
        val title = Paragraph("Reporte de Estadísticas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f))
        title.alignment = Element.ALIGN_CENTER
        document.add(title)
        
        val date = Paragraph("Período: $dateRange", FontFactory.getFont(FontFactory.HELVETICA, 12f))
        date.alignment = Element.ALIGN_CENTER
        document.add(date)
        document.add(Paragraph("\n"))
    }

    private fun addSummary(document: Document, stats: AppointmentStats) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        
        addRow(table, "Total de citas", stats.totalAppointments.toString(), true)
        addRow(table, "Citas atendidas", "${stats.attendedAppointments} (${(stats.attendanceRate * 100).roundToInt()}%)")
        addRow(table, "Citas pendientes", stats.pendingAppointments.toString())
        addRow(table, "Ausencias", "${stats.absentAppointments} (${(stats.absentRate * 100).roundToInt()}%)")
        
        document.add(table)
        document.add(Paragraph("\n"))
    }

    private fun addRow(table: PdfPTable, label: String, value: String, isHeader: Boolean = false) {
        table.addCell(createCell(label, isHeader))
        table.addCell(createCell(value, isHeader))
    }

    private fun createCell(text: String, isHeader: Boolean): PdfPCell {
        return PdfPCell(Phrase(text)).apply {
            horizontalAlignment = Element.ALIGN_CENTER
            if (isHeader) {
                backgroundColor = BaseColor(230, 230, 230)
            }
            padding = 8f
        }
    }

    private fun createSummarySheet(sheet: Sheet, stats: AppointmentStats) {
        var rowNum = 0
        
        createHeaderRow(sheet, rowNum++, listOf("Métrica", "Valor"))
        createDataRow(sheet, rowNum++, "Total de citas", stats.totalAppointments.toString())
        createDataRow(sheet, rowNum++, "Citas atendidas", stats.attendedAppointments.toString())
        createDataRow(sheet, rowNum++, "Citas pendientes", stats.pendingAppointments.toString())
        createDataRow(sheet, rowNum++, "Ausencias", stats.absentAppointments.toString())
        createDataRow(sheet, rowNum++, "Tasa de asistencia", "${(stats.attendanceRate * 100).roundToInt()}%")
    }

    private fun createHeaderRow(sheet: Sheet, rowNum: Int, headers: List<String>) {
        val row = sheet.createRow(rowNum)
        headers.forEachIndexed { index, header ->
            row.createCell(index).setCellValue(header)
        }
    }

    private fun createDataRow(sheet: Sheet, rowNum: Int, label: String, value: String) {
        val row = sheet.createRow(rowNum)
        row.createCell(0).setCellValue(label)
        row.createCell(1).setCellValue(value)
    }
} 