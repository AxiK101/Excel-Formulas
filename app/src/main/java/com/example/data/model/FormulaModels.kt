package com.example.data.model

enum class ExcelDialect(
    val displayName: String,
    val shortBadge: String,
    val separator: String,
    val decimalSeparator: String
) {
    ENGLISH(
        displayName = "English Excel",
        shortBadge = "EN (,)",
        separator = ",",
        decimalSeparator = "."
    ),
    GREEK(
        displayName = "Ελληνικό Excel",
        shortBadge = "ΕΛ (;)",
        separator = ";",
        decimalSeparator = ","
    )
}

enum class ExcelPlatform(
    val label: String,
    val subtitle: String
) {
    EXCEL_365("Excel 365", "Modern dynamic arrays (XLOOKUP, FILTER, LET)"),
    CLASSIC_EXCEL("Classic Excel", "Excel 2016–2021 (INDEX/MATCH, SUMIFS)"),
    GOOGLE_SHEETS("Google Sheets", "Google Sheets formula syntax")
}

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ChartKind {
    BAR,
    LINE
}

data class ChartDataPoint(
    val label: String,
    val value: Double,
    val isHighlighted: Boolean = false
)

data class FormulaChartSpec(
    val shouldVisualize: Boolean,
    val chartType: ChartKind = ChartKind.BAR,
    val title: String = "",
    val yAxisLabel: String = "",
    val points: List<ChartDataPoint> = emptyList()
)

data class FormulaPart(
    val token: String,
    val role: String,
    val description: String
)

data class SampleRow(
    val rowNumber: Int,
    val cells: List<String>
)

data class RelatedFormulaSuggestion(
    val title: String,
    val reason: String,
    val englishFormula: String,
    val greekFormula: String
) {
    fun formulaFor(dialect: ExcelDialect): String =
        when (dialect) {
            ExcelDialect.ENGLISH -> englishFormula
            ExcelDialect.GREEK -> greekFormula
        }
}

data class GeneratedFormulaResult(
    val id: Long = 0L,
    val userPrompt: String,
    val sheetContext: String = "",
    val platform: ExcelPlatform = ExcelPlatform.EXCEL_365,
    val category: String,
    val title: String,
    val englishFormula: String,
    val greekFormula: String,
    val purpose: String = "",
    val explanation: String,
    val usageExample: String = "",
    val greekNotes: String = "",
    val parts: List<FormulaPart> = emptyList(),
    val suggestions: List<RelatedFormulaSuggestion> = emptyList(),
    val sampleHeaders: List<String> = emptyList(),
    val sampleRows: List<SampleRow> = emptyList(),
    val sampleResultCell: String = "",
    val sampleResultValue: String = "",
    val chartSpec: FormulaChartSpec? = null,
    val isFavorite: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    fun primaryFormulaFor(dialect: ExcelDialect): String =
        when (dialect) {
            ExcelDialect.ENGLISH -> englishFormula
            ExcelDialect.GREEK -> greekFormula
        }
}

data class QuickPromptSuggestion(
    val label: String,
    val prompt: String,
    val isGreek: Boolean = false
)
