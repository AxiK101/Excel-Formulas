package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.ChartDataPoint
import com.example.data.model.ChartKind
import com.example.data.model.ExcelPlatform
import com.example.data.model.FormulaChartSpec
import com.example.data.model.FormulaPart
import com.example.data.model.GeneratedFormulaResult
import com.example.data.model.RelatedFormulaSuggestion
import com.example.data.model.SampleRow
import org.json.JSONArray
import org.json.JSONObject

@Entity(
    tableName = "formula_history",
    indices = [Index(value = ["category"])]
)
data class FormulaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userPrompt: String,
    val sheetContext: String,
    val platformName: String,
    val category: String,
    val title: String,
    val englishFormula: String,
    val greekFormula: String,
    val purpose: String,
    val explanation: String,
    val usageExample: String,
    val greekNotes: String,
    val partsJson: String,
    val suggestionsJson: String,
    val sampleHeadersJson: String,
    val sampleRowsJson: String,
    val sampleResultCell: String,
    val sampleResultValue: String,
    val chartSpecJson: String,
    val isFavorite: Boolean,
    val timestampMillis: Long
) {
    fun toDomain(): GeneratedFormulaResult {
        val platform = ExcelPlatform.entries.firstOrNull { it.name == platformName }
            ?: ExcelPlatform.EXCEL_365
        return GeneratedFormulaResult(
            id = id,
            userPrompt = userPrompt,
            sheetContext = sheetContext,
            platform = platform,
            category = category,
            title = title,
            englishFormula = englishFormula,
            greekFormula = greekFormula,
            purpose = purpose,
            explanation = explanation,
            usageExample = usageExample,
            greekNotes = greekNotes,
            parts = decodeParts(partsJson),
            suggestions = decodeSuggestions(suggestionsJson),
            sampleHeaders = decodeStringList(sampleHeadersJson),
            sampleRows = decodeSampleRows(sampleRowsJson),
            sampleResultCell = sampleResultCell,
            sampleResultValue = sampleResultValue,
            chartSpec = decodeChartSpec(chartSpecJson),
            isFavorite = isFavorite,
            timestampMillis = timestampMillis
        )
    }

    companion object {
        fun fromDomain(model: GeneratedFormulaResult): FormulaEntity = FormulaEntity(
            id = model.id,
            userPrompt = model.userPrompt,
            sheetContext = model.sheetContext,
            platformName = model.platform.name,
            category = model.category,
            title = model.title,
            englishFormula = model.englishFormula,
            greekFormula = model.greekFormula,
            purpose = model.purpose,
            explanation = model.explanation,
            usageExample = model.usageExample,
            greekNotes = model.greekNotes,
            partsJson = encodeParts(model.parts),
            suggestionsJson = encodeSuggestions(model.suggestions),
            sampleHeadersJson = JSONArray(model.sampleHeaders).toString(),
            sampleRowsJson = encodeSampleRows(model.sampleRows),
            sampleResultCell = model.sampleResultCell,
            sampleResultValue = model.sampleResultValue,
            chartSpecJson = encodeChartSpec(model.chartSpec),
            isFavorite = model.isFavorite,
            timestampMillis = model.timestampMillis
        )

        private fun encodeParts(parts: List<FormulaPart>): String {
            val array = JSONArray()
            parts.forEach { part ->
                array.put(
                    JSONObject().apply {
                        put("token", part.token)
                        put("role", part.role)
                        put("description", part.description)
                    }
                )
            }
            return array.toString()
        }

        private fun decodeParts(json: String): List<FormulaPart> {
            return runCatching {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    FormulaPart(
                        token = obj.optString("token"),
                        role = obj.optString("role"),
                        description = obj.optString("description")
                    )
                }
            }.getOrDefault(emptyList())
        }

        private fun encodeSuggestions(suggestions: List<RelatedFormulaSuggestion>): String {
            val array = JSONArray()
            suggestions.forEach { item ->
                array.put(
                    JSONObject().apply {
                        put("title", item.title)
                        put("reason", item.reason)
                        put("englishFormula", item.englishFormula)
                        put("greekFormula", item.greekFormula)
                    }
                )
            }
            return array.toString()
        }

        private fun decodeSuggestions(json: String): List<RelatedFormulaSuggestion> {
            return runCatching {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    RelatedFormulaSuggestion(
                        title = obj.optString("title"),
                        reason = obj.optString("reason"),
                        englishFormula = obj.optString("englishFormula"),
                        greekFormula = obj.optString("greekFormula")
                    )
                }
            }.getOrDefault(emptyList())
        }

        private fun decodeStringList(json: String): List<String> {
            return runCatching {
                val array = JSONArray(json)
                (0 until array.length()).map { array.getString(it) }
            }.getOrDefault(emptyList())
        }

        private fun encodeSampleRows(rows: List<SampleRow>): String {
            val array = JSONArray()
            rows.forEach { row ->
                array.put(
                    JSONObject().apply {
                        put("rowNumber", row.rowNumber)
                        put("cells", JSONArray(row.cells))
                    }
                )
            }
            return array.toString()
        }

        private fun decodeSampleRows(json: String): List<SampleRow> {
            return runCatching {
                val array = JSONArray(json)
                (0 until array.length()).map { i ->
                    val obj = array.getJSONObject(i)
                    val cellsArray = obj.optJSONArray("cells") ?: JSONArray()
                    SampleRow(
                        rowNumber = obj.optInt("rowNumber", i + 2),
                        cells = (0 until cellsArray.length()).map { cellsArray.getString(it) }
                    )
                }
            }.getOrDefault(emptyList())
        }

        private fun encodeChartSpec(spec: FormulaChartSpec?): String {
            if (spec == null) return ""
            return JSONObject().apply {
                put("shouldVisualize", spec.shouldVisualize)
                put("chartType", spec.chartType.name)
                put("title", spec.title)
                put("yAxisLabel", spec.yAxisLabel)
                val pts = JSONArray()
                spec.points.forEach { pt ->
                    pts.put(
                        JSONObject().apply {
                            put("label", pt.label)
                            put("value", pt.value)
                            put("isHighlighted", pt.isHighlighted)
                        }
                    )
                }
                put("points", pts)
            }.toString()
        }

        private fun decodeChartSpec(json: String): FormulaChartSpec? {
            if (json.isBlank()) return null
            return runCatching {
                val obj = JSONObject(json)
                val chartType = ChartKind.entries.firstOrNull {
                    it.name == obj.optString("chartType")
                } ?: ChartKind.BAR
                val ptsArray = obj.optJSONArray("points") ?: JSONArray()
                val pts = (0 until ptsArray.length()).map { idx ->
                    val pt = ptsArray.getJSONObject(idx)
                    ChartDataPoint(
                        label = pt.optString("label"),
                        value = pt.optDouble("value", 0.0),
                        isHighlighted = pt.optBoolean("isHighlighted", false)
                    )
                }
                FormulaChartSpec(
                    shouldVisualize = obj.optBoolean("shouldVisualize", false),
                    chartType = chartType,
                    title = obj.optString("title"),
                    yAxisLabel = obj.optString("yAxisLabel"),
                    points = pts
                )
            }.getOrNull()
        }
    }
}
