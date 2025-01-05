@Singleton
class StatsExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val statsAnalyzer: StatsAnalyzer
) {
    suspend fun exportDetailedStats(stats: AppointmentStats, format: ExportFormat): Uri {
        return when (format) {
            ExportFormat.PDF -> exportToPdf(stats)
            ExportFormat.EXCEL -> exportToExcel(stats)
        }
    }

    private suspend fun exportToPdf(stats: AppointmentStats): Uri = withContext(Dispatchers.IO) {
        val document = Document()
        val fileName = "estadisticas_detalladas_${System.currentTimeMillis()}.pdf"
        val path = context.getExternalFilesDir(null)?.absolutePath + "/$fileName"
        
        PdfWriter.getInstance(document, FileOutputStream(path))
        document.open()
        
        // Título y fecha
        addHeader(document)
        
        // Resumen general
        addGeneralSummary(document, stats)
        
        // Distribución horaria
        addTimeDistribution(document, stats)
        
        // Tendencias
        addTrends(document, statsAnalyzer.analyzeStats(stats))
        
        // Gráficos
        addCharts(document, stats)
        
        document.close()
        Uri.fromFile(File(path))
    }

    private suspend fun exportToExcel(stats: AppointmentStats): Uri = withContext(Dispatchers.IO) {
        val workbook = HSSFWorkbook()
        
        // Hoja de resumen
        createSummarySheet(workbook, stats)
        
        // Hoja de distribución horaria
        createTimeDistributionSheet(workbook, stats)
        
        // Hoja de tendencias
        createTrendsSheet(workbook, statsAnalyzer.analyzeStats(stats))
        
        // Hoja de datos detallados
        createDetailedDataSheet(workbook, stats)
        
        val fileName = "estadisticas_detalladas_${System.currentTimeMillis()}.xls"
        val path = context.getExternalFilesDir(null)?.absolutePath + "/$fileName"
        
        FileOutputStream(path).use { 
            workbook.write(it)
        }
        
        Uri.fromFile(File(path))
    }

    private fun addHeader(document: Document) {
        val title = Paragraph("Estadísticas Detalladas", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18f))
        title.alignment = Element.ALIGN_CENTER
        document.add(title)
        
        val date = Paragraph(
            "Generado el ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es")).format(Date())}",
            FontFactory.getFont(FontFactory.HELVETICA, 12f)
        )
        date.alignment = Element.ALIGN_CENTER
        document.add(date)
        document.add(Paragraph("\n"))
    }

    private fun addGeneralSummary(document: Document, stats: AppointmentStats) {
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        
        addRow(table, "Total de citas", stats.totalAppointments.toString(), true)
        addRow(table, "Citas atendidas", "${stats.attendedAppointments} (${(stats.attendanceRate * 100).roundToInt()}%)")
        addRow(table, "Citas pendientes", stats.pendingAppointments.toString())
        addRow(table, "Ausencias", "${stats.absentAppointments} (${(stats.absentRate * 100).roundToInt()}%)")
        
        document.add(table)
        document.add(Paragraph("\n"))
    }

    private fun addTimeDistribution(document: Document, stats: AppointmentStats) {
        document.add(Paragraph("Distribución Horaria", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)))
        document.add(Paragraph("\n"))
        
        val table = PdfPTable(2)
        table.widthPercentage = 100f
        
        stats.appointmentsByHour.forEach { (hour, count) ->
            addRow(table, hour, count.toString())
        }
        
        document.add(table)
        document.add(Paragraph("\n"))
    }

    private fun addTrends(document: Document, analysis: StatsAnalysis) {
        document.add(Paragraph("Tendencias", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)))
        document.add(Paragraph("\n"))
        
        analysis.trends.forEach { trend ->
            val paragraph = Paragraph().apply {
                add(Chunk("• ${trend.title}: ", FontFactory.getFont(FontFactory.HELVETICA_BOLD)))
                add(trend.description)
            }
            document.add(paragraph)
        }
        
        document.add(Paragraph("\n"))
    }

    private fun addCharts(document: Document, stats: AppointmentStats) {
        document.add(Paragraph("Gráficos", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14f)))
        document.add(Paragraph("\n"))

        // Gráfico de barras para distribución horaria
        val hourlyChart = createHourlyChart(stats)
        document.add(Image.getInstance(hourlyChart.createBufferedImage(500, 300)))
        document.add(Paragraph("\n"))

        // Gráfico circular para estados de citas
        val statusChart = createStatusChart(stats)
        document.add(Image.getInstance(statusChart.createBufferedImage(500, 300)))
        document.add(Paragraph("\n"))
    }

    private fun createHourlyChart(stats: AppointmentStats): JFreeChart {
        val dataset = DefaultCategoryDataset()
        stats.appointmentsByHour.forEach { (hour, count) ->
            dataset.addValue(count.toDouble(), "Citas", hour)
        }

        return ChartFactory.createBarChart(
            "Distribución horaria de citas",
            "Hora",
            "Cantidad de citas",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        ).apply {
            categoryPlot.renderer = BarRenderer().apply {
                setSeriesPaint(0, Color(33, 150, 243)) // Color primario
                setDrawBarOutline(true)
            }
        }
    }

    private fun createStatusChart(stats: AppointmentStats): JFreeChart {
        val dataset = DefaultPieDataset<String>()
        dataset.setValue("Atendidas", stats.attendedAppointments.toDouble())
        dataset.setValue("Pendientes", stats.pendingAppointments.toDouble())
        dataset.setValue("Ausencias", stats.absentAppointments.toDouble())

        return ChartFactory.createPieChart(
            "Estado de citas",
            dataset,
            true,
            true,
            false
        ).apply {
            plot.setDrawingSupplier(DefaultDrawingSupplier(
                arrayOf(Color(76, 175, 80), Color(255, 193, 7), Color(244, 67, 54)),
                DefaultDrawingSupplier.DEFAULT_OUTLINE_PAINT_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_OUTLINE_STROKE_SEQUENCE,
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE
            ))
        }
    }

    private fun createDetailedDataSheet(workbook: HSSFWorkbook, stats: AppointmentStats) {
        val sheet = workbook.createSheet("Datos Detallados")
        var rowNum = 0

        // Estilos
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
        }

        // Cabeceras
        val headers = listOf("Fecha", "Hora", "Total", "Atendidas", "Pendientes", "Ausencias", "Tasa de Asistencia")
        createHeaderRow(sheet, rowNum++, headers, headerStyle)

        // Datos por día y hora
        stats.appointmentsByDay.forEach { (date, total) ->
            stats.appointmentsByHour.forEach { (hour, _) ->
                val row = sheet.createRow(rowNum++)
                var colNum = 0
                
                row.createCell(colNum++).setCellValue(date)
                row.createCell(colNum++).setCellValue(hour)
                row.createCell(colNum++).setCellValue(total)
                row.createCell(colNum++).setCellValue(stats.attendedAppointments)
                row.createCell(colNum++).setCellValue(stats.pendingAppointments)
                row.createCell(colNum++).setCellValue(stats.absentAppointments)
                row.createCell(colNum).setCellValue("${(stats.attendanceRate * 100).roundToInt()}%")
            }
        }

        // Autoajustar columnas
        headers.indices.forEach { sheet.autoSizeColumn(it) }
    }

    private fun createTimeDistributionSheet(workbook: HSSFWorkbook, stats: AppointmentStats) {
        val sheet = workbook.createSheet("Distribución Horaria")
        var rowNum = 0

        // Estilos
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_BLUE.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
        }

        // Cabeceras
        createHeaderRow(sheet, rowNum++, listOf("Hora", "Cantidad", "Porcentaje"), headerStyle)

        // Datos
        val total = stats.appointmentsByHour.values.sum()
        stats.appointmentsByHour.forEach { (hour, count) ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(hour)
            row.createCell(1).setCellValue(count.toDouble())
            row.createCell(2).setCellValue("${(count.toFloat() / total * 100).roundToInt()}%")
        }

        // Autoajustar columnas
        (0..2).forEach { sheet.autoSizeColumn(it) }
    }

    private fun createTrendsSheet(workbook: HSSFWorkbook, analysis: StatsAnalysis) {
        val sheet = workbook.createSheet("Tendencias")
        var rowNum = 0

        // Estilos
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.LIGHT_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
        }

        // Cabeceras
        createHeaderRow(sheet, rowNum++, listOf("Tendencia", "Descripción", "Tipo"), headerStyle)

        // Datos
        analysis.trends.forEach { trend ->
            val row = sheet.createRow(rowNum++)
            row.createCell(0).setCellValue(trend.title)
            row.createCell(1).setCellValue(trend.description)
            row.createCell(2).setCellValue(trend.type.name)
        }

        // Autoajustar columnas
        (0..2).forEach { sheet.autoSizeColumn(it) }
    }

    private fun addRow(table: PdfPTable, label: String, value: String, isHeader: Boolean = false) {
        val labelCell = PdfPCell(Phrase(label)).apply {
            backgroundColor = if (isHeader) BaseColor(230, 230, 230) else BaseColor.WHITE
            horizontalAlignment = Element.ALIGN_LEFT
            padding = 8f
        }
        val valueCell = PdfPCell(Phrase(value)).apply {
            backgroundColor = if (isHeader) BaseColor(230, 230, 230) else BaseColor.WHITE
            horizontalAlignment = Element.ALIGN_CENTER
            padding = 8f
        }
        table.addCell(labelCell)
        table.addCell(valueCell)
    }
} 